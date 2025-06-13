
import org.hazelcast.jetpayments.ElapsedTimeLogger
import org.hazelcast.jetpayments.RingBuffer
import java.nio.BufferOverflowException
import java.nio.BufferUnderflowException
import kotlin.test.Test

class RingBufferTest {
    val logger = ElapsedTimeLogger("RingBufferTest")

    @Test
    fun `Verify RingBuffer works as expected`() {
        val capacity = 32
        val buf = RingBuffer<Int>(capacity)

        for (maxsize in 1..capacity) {
            logger.log("Adding $maxsize records to RB with capacity $capacity")

            repeat(maxsize) { i -> buf.add(i) }

            if (maxsize == capacity) {
                try {
                    logger.log("Attempting to add one more record to full buffer")
                    buf.add(0) // should fail
                    assert(false) { "Buffer enqueue beyond capacity should have failed" }
                } catch (_: BufferOverflowException) {
                    logger.log("Caught expected BufferOverflowException")
                }
            }

            logger.log("Removing $maxsize records from RB with capacity $capacity")
            repeat(maxsize) { i -> assert(buf.remove() == i) }

            try {
                logger.log("Attempting to remove one more record from empty buffer")
                buf.remove() // should fail
                assert(false) { "Buffer dequeue beyond capacity should have failed" }
            } catch (_: BufferUnderflowException) {
                logger.log("Caught expected BufferUnderflowException")
            }
        }
    }
}
