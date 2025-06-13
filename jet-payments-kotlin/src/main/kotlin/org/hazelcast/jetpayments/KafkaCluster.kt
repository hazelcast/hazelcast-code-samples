package org.hazelcast.jetpayments

import com.google.common.math.IntMath.pow
import com.hazelcast.jet.kafka.KafkaSources
import com.hazelcast.jet.pipeline.StreamSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/*
 * Convenience class for starting up and managing Kafka. You'll need an actual
 * instance of Kafka running somewhere, either in the cloud, or as a Docker
 * container. This class only connects to that instance, creates a topic (with
 * topicname supplied), and provides convenience methods for publishing or
 * consuming from that topic.
 */
class KafkaCluster<K, V>(
    private val topicName: String,
    private val key: K,
) : AutoCloseable {
    private val partitionCount = 1
    private val replicationFactor: Short = 1

    private val kafkaProps = mapOf(
        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to AppConfig.kafkaBootstrap,
        "security.protocol" to "PLAINTEXT",
    )

    private val producerProps = kafkaProps + mapOf(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to IntegerSerializer::class.java.canonicalName,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.canonicalName,
        ProducerConfig.ACKS_CONFIG to "all",
    )

    private val consumerProps = kafkaProps + mapOf(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to IntegerDeserializer::class.java.canonicalName,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.canonicalName,
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "true",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ISOLATION_LEVEL_CONFIG to "read_committed",
    )

    private fun consumerPropsWithCG(consumerGroup: String) =
        (consumerProps + mapOf(ConsumerConfig.GROUP_ID_CONFIG to consumerGroup)).toProperties()

    private val producer: KafkaProducer<K, V> = KafkaProducer<K, V>(producerProps)

    private fun Map<String, Any>.toProperties() = Properties().apply {
        putAll(this@toProperties)
    }

    private val topicCreateJob: Job
    private val createTopicScope =
        CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Automatically create the topic. Keep track of Job so others can wait on it.
    init {
        topicCreateJob = createTopicScope.launch {
            ensureTopic()
        }
    }

    override fun close() {
        producer.close()
        createTopicScope.cancel()
    }

    // Publish a value to the topic, waiting for topic to be created first.
    suspend fun publish(value: V) {
        topicCreateJob.join()
        producer.send(ProducerRecord(topicName, key, value))
    }

    /*
     * Create a Jet source for consuming from the topic, waiting for topic to be
     * created first.
     */
    suspend fun consumerJetSource(consumerGroup: String): StreamSource<Map.Entry<K, V>> {
        topicCreateJob.join()
        return KafkaSources.kafka(
            consumerPropsWithCG(consumerGroup), topicName
        )
    }

    /*
     * Create a Kotlin Flow for consuming from the topic, waiting for topic to be
     * created first.
     */
    suspend fun <T> consumeAsFlow(
        consumerGroup: String, numElements: Int, convert: (V) -> T
    ): Flow<T> {
        topicCreateJob.join()
        return flow {
            val consumer = KafkaConsumer<K, V>(consumerPropsWithCG(consumerGroup))
            consumer.subscribe(listOf(topicName))

            repeat(numElements) {
                val records =
                    consumer.poll(AppConfig.kafkaPollTimeout.toJavaDuration())
                for (record in records) {
                    record.value()?.let { value ->
                        emit(convert(value))
                    }
                }
            }

            consumer.close()
        }
    }

    // Create the Kafka topic. This is asynchronous, so we need to wait for it.
    private suspend fun createTopic(admin: AdminClient): Boolean {
        val topic = NewTopic(topicName, partitionCount, replicationFactor)
        try {
            admin.createTopics(listOf(topic)).all().toCompletionStage()
                .toCompletableFuture().await()
            return true
        } catch (_: TopicExistsException) {
            return false
        }
    }

    // A function for deleting the topic, in case it already exists.
    private suspend fun deleteTopic(admin: AdminClient) {
        val deleteResult = admin.deleteTopics(listOf(topicName))
        try {
            deleteResult.all().get() // still asynchronous even with this
        } catch (e: ExecutionException) {
            // Ignore error if Kafka is saying it can no longer find topic.
            if (e.cause !is UnknownTopicOrPartitionException) throw e.cause ?: e
        }

        do {
            delay(250.milliseconds)
            val remainingTopics = admin.listTopics().names().get()
        } while (topicName in remainingTopics)
    }

    // Make sure the topic gets created before we start publishing to it.
    private suspend fun ensureTopic() {
        repeat(5) { attempt ->
            AdminClient.create(kafkaProps.toProperties()).use { admin ->
                if (createTopic(admin)) return
                delay(pow(2, attempt).seconds)
                deleteTopic(admin)
            }
        }
        throw IllegalStateException("Can't create topic")
    }
}
