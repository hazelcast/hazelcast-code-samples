package org.hazelcast.jetpayments

import java.nio.BufferOverflowException
import java.nio.BufferUnderflowException

/**
 * RingBuffer uses a fixed length array to implement a queue, where,
 * - [tail] Items are added to the tail
 * - [head] Items are removed from the head
 * - [size] Keeps track of how many items are currently in the queue
 */
class RingBuffer<T>(val capacity: Int = 10) : Iterable<T> {
    private val arrayList = mutableListOf<T>()
    private var head = 0 // read index; oldest end
    private var tail = 0 // write index of where to write next (_after_ last item)
    private var newest = 0 // points to newest item in queue (last item)
    var size = 0 // how many items in queue
        private set

    fun isEmpty() = size == 0

    /* Operates at head--oldest end */
    fun remove(): T {
        // Check if queue is empty before attempting to remove the item
        if (size == 0) throw BufferUnderflowException()

        val removed: T = arrayList[head]
        // Loop around to the start of the array if there's a need for it
        head = (head + 1) % capacity
        size--

        return removed
    }

    /* Operates at tail--newest end */
    fun add(item: T) {
        val newSize = size + 1

        /* By definition, a RingBuffer always has a limited capacity. If we've hit
         * that capacity, throw an exception. */
        if (newSize > capacity) throw BufferOverflowException()

        /* At this point, from a logical perspective, our RingBuffer _should_ have
         * enough room to add an element, so we cannot throw another exception or
         * fail. However, we might need to increase the size of our backing store.
         * There should be two phases of growth. The first is when we are growing
         * the backing store, which should be one by one until it reaches capacity.
         * Thereafter, we should never add any elements to the backing store.
         */
        if (tail >= arrayList.size) {
            // Phase I: We're expanding the backing store one by one
            check(tail == arrayList.size)
            arrayList.add(item)
        } else {
            // Phase II: The backing store is at full capacity
            check(arrayList.size == capacity)
            arrayList[tail] = item
        }

        // If we've made it this far, we've successfully added an element to
        // the RingBuffer. Now, we need to update the tail and size variables
        // to reflect this change.
        newest = tail
        tail = (tail + 1) % capacity
        size = newSize
    }

    fun addRemoving(item: T) {
        if (size == capacity) remove()
        add(item)
    }

    fun last(): T {
        if (size == 0) throw BufferUnderflowException()
        return arrayList[newest]
    }

    fun first(): T {
        if (size == 0) throw BufferUnderflowException()
        return arrayList[head]
    }

    /*
     * To satisfy the Iterable interface, we'll need to provide back an object that
     * inherits from Iterator, so create that here.
     */
    inner class RingBufferIterator : Iterator<T> {
        private var readIndex = head
        private var itemCount = size

        override fun hasNext() = itemCount > 0
        override fun next(): T {
            val toReturn = arrayList[readIndex]
            readIndex = (readIndex + 1) % capacity
            itemCount--
            return toReturn
        }
    }

    override fun iterator(): Iterator<T> = RingBufferIterator()

    /* Use the Iterable interface to implement toString so that it shows the
     * contents of the ring buffer.
     */
    override fun toString(): String = "[${joinToString(" -> ")}]"
}
