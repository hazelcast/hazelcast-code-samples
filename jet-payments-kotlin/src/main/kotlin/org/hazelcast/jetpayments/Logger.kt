package org.hazelcast.jetpayments

import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern

/*
 * A very simple Logger class that allows for some simple formatting goodies.
 */
open class Logger(
    private val label: String, private val addTimestamp: Boolean = true
) : Serializable {

    protected open fun getFormattedTime(): String =
        LocalDateTime.now().format(ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

    fun log(vararg strings: String) {
        require(strings.isNotEmpty())

        val prefix = if (addTimestamp) {
            "${getFormattedTime()} [${label}]: "
        } else "[${label}]: "

        strings.forEach { println("$prefix$it") }
    }

    fun log(s: Collection<String>): Unit = log(*s.toTypedArray())
}

internal fun Collection<String>.log(logger: Logger) = logger.log(this)
internal fun TextBox.log(logger: Logger) = logger.log(this.toStrings())
