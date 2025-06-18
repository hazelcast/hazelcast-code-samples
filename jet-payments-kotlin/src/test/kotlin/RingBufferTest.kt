
import io.kotest.core.spec.style.FunSpec
import org.hazelcast.jetpayments.RingBuffer
import java.nio.BufferOverflowException
import java.nio.BufferUnderflowException

class RingBufferTest: FunSpec({

    test("Verify RingBuffer works as expected") {
        val capacity = 32
        val buf = RingBuffer<Int>(capacity)

        for (maxsize in 1..capacity) {

            repeat(maxsize) { i -> buf.add(i) }

            if (maxsize == capacity) {
                try {
                    buf.add(0) // should fail
                    assert(false) { "Buffer enqueue beyond capacity should have failed" }
                } catch (_: BufferOverflowException) {
                }
            }

            repeat(maxsize) { i -> assert(buf.remove() == i) }

            try {
                buf.remove() // should fail
                assert(false) { "Buffer dequeue beyond capacity should have failed" }
            } catch (_: BufferUnderflowException) {
            }
        }
    }
})
