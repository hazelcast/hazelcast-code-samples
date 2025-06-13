package org.hazelcast.jetpayments

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/*
 * A very simple Logger class that allows for some simple formatting goodies.
 */
open class Logger(
    private val label: String, private val addTimestamp: Boolean = true
) : java.io.Serializable {
    companion object {
        private val formatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }

    protected open fun getFormattedTime() = LocalDateTime.now().format(formatter)!!

    fun log(vararg strings: String) {
        require(strings.isNotEmpty())

        val prefix = if (addTimestamp) {
            "${getFormattedTime()} [${label}]: "
        } else "[${label}]: "

        strings.forEach { println("$prefix$it") }
    }

    fun log(s: Collection<String>) = log(*s.toTypedArray())
}

internal fun Collection<String>.log(logger: Logger) = logger.log(this)
internal fun TextBox.log(logger: Logger) = logger.log(this.toStrings())
