package org.hazelcast.jetpayments

/*
 * A simple Kotlin data class representing a merchant.
 * [id]: unique ID of the merchant
 * [name]: name of the merchant
 * [shortName]: short name/alias of the merchant used by ReceiptWatcher for display.
 */
data class Merchant(
    val id: String,
    val name: String,
    val shortName: String,
)
