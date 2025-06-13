package org.hazelcast.jetpayments

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.impl.connection.tcp.RoutingMode
import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import kotlinx.coroutines.*
import org.hazelcast.jetpayments.AppConfig.enableMemberLogs
import java.util.*
import java.util.logging.LogManager
import kotlin.time.Duration.Companion.seconds

/*
 * A class for simplifying the setup and management of embedded Hazelcast instances.
 */
class HzCluster(
    private val newClusterName: String,
    val originalClusterSize: Int,
) {
    private val memberAddresses = Array(originalClusterSize) { index ->
        "127.0.0.1:${5701 + index}"
    }

    /*
     * Set the logging level for whatever comes after, including members and client.
     */
    init {
        val rootLogger = LogManager.getLogManager().getLogger("")
        rootLogger.level = AppConfig.logLevel
        for (h in rootLogger.handlers) {
            h.level = AppConfig.logLevel
        }
    }

    /*
     * Member configs.
     */
    private val memberConfigs = Array(originalClusterSize) { index ->
        Config().apply {
            clusterName = newClusterName
            properties.putAll(
                mapOf(
                    "hazelcast.persistence.auto.cluster.state" to "false",
                    "hazelcast.shutdown.timeout.seconds" to "120",
                )
            )

            // Should members log, or only the client?
            if (!enableMemberLogs) {
                properties.put("hazelcast.logging.type", "none")
            }

            jetConfig.apply {
                isEnabled = true
                scaleUpDelayMillis = AppConfig.jetScaleUpDelayMillis
            }
            val tcpIpConfig = networkConfig.join.tcpIpConfig.apply {
                isEnabled = true
            }

            memberAddresses.forEach { tcpIpConfig.addMember(it) }
        }
    }

    // Now create all the members from the configs.
    private val memberArrayDeferred = CoroutineScope(Dispatchers.Default).async {
        Array<HazelcastInstance>(originalClusterSize) {
            Hazelcast.newHazelcastInstance(memberConfigs[it])
        }
    }

    inner class ClientInstance(
        private val hzClientInstance: HazelcastInstance
    ) : HazelcastInstance by hzClientInstance, AutoCloseable {
        val originalClusterSize get() = this@HzCluster.originalClusterSize
        val size: Int get() = hzClientInstance.cluster.members.size
        val membershipListener = ClientMembershipListener(originalClusterSize)

        init {
            hzClientInstance.cluster.addMembershipListener(membershipListener)
            hzClientInstance.cluster.members.mapIndexed { i, member ->
                writeUUIDIndex(member.uuid, i)
            }
        }

        override fun close() {
            hzClientInstance.shutdown()
        }

        private fun getUuidIndexMap() =
            getMap<UUID, Int>(AppConfig.uuidToMemberIndexMapName)

        private fun writeUUIDIndex(uuid: UUID, index: Int) {
            getUuidIndexMap().put(uuid, index)
        }

        private fun deleteUUIDIndex(uuid: UUID) {
            getUuidIndexMap().delete(uuid)
        }

        // Bring a member down
        suspend fun shutdownMember(memberToKill: Int) =
            withContext(Dispatchers.Default) {
                require(size > 1) { "Can't kill a member in a one-member cluster!" }
                val memberArray = memberArrayDeferred.await()

                val targetSize = size - 1

                // The UUID of the member we're shutting down will no longer exist.
                deleteUUIDIndex(memberArray[memberToKill].cluster.localMember.uuid)

                coroutineScope {
                    launch {
                        memberArray[memberToKill].shutdown()
                    }
                    while (size != targetSize) {
                        delay(1.seconds)
                    }
                }
            }

        // Restart a down member (bring it back up)
        suspend fun restartMember(memberToRestart: Int) =
            withContext(Dispatchers.Default) {
                require(size < originalClusterSize) { "Can't restart a member in fully running cluster!" }
                val memberArray = memberArrayDeferred.await()

                val targetSize = size + 1
                val newInstance = async {
                    Hazelcast.newHazelcastInstance(memberConfigs[memberToRestart])
                }
                while (size != targetSize) {
                    delay(1.seconds)
                }
                memberArray[memberToRestart] = newInstance.await()

                // The new member has a new UUID, which we'll now record.
                writeUUIDIndex(
                    memberArray[memberToRestart].cluster.localMember.uuid,
                    memberToRestart
                )
            }
    }

    /*
     * Client config. Use a factory approach to creating the client, requiring the
     * caller to suspend until the cluster is ready, and the client can be created
     * and attached to the cluster.
     */

    private val config = ClientConfig().apply {
        clusterName = newClusterName
        networkConfig.clusterRoutingConfig.routingMode = RoutingMode.ALL_MEMBERS
        memberAddresses.forEach {
            networkConfig.addAddress(it)
        }
    }

    suspend fun getClient(): ClientInstance {
        val memberArray =
            memberArrayDeferred.await() // Wait until cluster is whole.
        val hzClientInstance = HazelcastClient.newHazelcastClient(config)!!
        // Wait for all Hazelcast members to join the cluster
        val firstMember =
            memberArray.firstOrNull() ?: error("No members in cluster!")
        while (firstMember.cluster.members.size < originalClusterSize) {
            delay(1.seconds)
        }
        return ClientInstance(hzClientInstance)
    }
}
