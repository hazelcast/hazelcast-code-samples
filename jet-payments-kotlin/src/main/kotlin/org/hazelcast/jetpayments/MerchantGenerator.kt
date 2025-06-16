package org.hazelcast.jetpayments

import java.util.SortedMap
import kotlin.math.min
import kotlin.random.Random

/*
 * Generates random merchant names that sound somewhat plausible.
 */
class MerchantGenerator(
    numMerchants: Int,
    private val seededRandom: Random,
) {
    private val merchantPrefix = setOf(
        "Big",
        "Blaze",
        "Bold",
        "City",
        "Compu",
        "Echo",
        "Epic",
        "Ever",
        "Flash",
        "Fresh",
        "Happy",
        "Huge",
        "Hyper",
        "Live",
        "Lunar",
        "Maxi",
        "Mega",
        "Neon",
        "Next",
        "Nova",
        "Opti",
        "Peak",
        "Pixel",
        "Prime",
        "Quant",
        "Rapid",
        "Sky",
        "Smart",
        "Sport",
        "Super",
        "Swift",
        "Sync",
        "Tech",
        "Trade",
        "Urban",
        "Vibe",
        "Vivid"
    )

    private val merchantSuffix = setOf(
        "Base",
        "Bay",
        "Buy",
        "Boost",
        "Cart",
        "Core",
        "Craft",
        "Dist",
        "Drive",
        "Edge",
        "Era",
        "Flow",
        "Forge",
        "Globe",
        "Goods",
        "Grid",
        "Haven",
        "Hub",
        "Loom",
        "Mart",
        "Nest",
        "Picks",
        "Place",
        "Plane",
        "Play",
        "Pulse",
        "Rise",
        "Run",
        "Site",
        "Shop",
        "Store",
        "Space",
        "Spark",
        "Spire",
        "Stock",
        "Tide",
        "Vault",
        "Wave",
        "World"
    )

    private val merchantPairs =
        merchantPrefix.shuffled(seededRandom).take(numMerchants)
            .zip(merchantSuffix.shuffled(seededRandom).take(numMerchants))
            .map { (prefix, suffix) -> prefix to suffix }

    private fun createMerchantIdFromName(shortName: String): String {
        require(AppConfig.merchantIdWidth > shortName.length) { "merchantIdWidth too small" }
        return shortName + (1..AppConfig.merchantIdWidth - shortName.length)
            .map { ('0'..'9').random(seededRandom) }.joinToString("")
    }

    val merchantMap: SortedMap<String, Merchant> = merchantPairs.associate { (prefix, suffix) ->
        val name = prefix + suffix
        val shortName = prefix.take(1).uppercase() + suffix.take(1).uppercase()
        val id = createMerchantIdFromName(shortName)
        val shortNameBold = shortName.map { fontBold(it) }.joinToString("")
        id to Merchant(id, name, shortNameBold)
    }.toSortedMap()

    init {
        require(numMerchants > 0) { "Number of merchants must be > 0" }

        merchantPrefix.intersect(merchantSuffix).let { dups ->
            check(dups.isEmpty()) {
                "Duplicate merchant names: ${dups.joinToString(", ")}"
            }
        }

        check(numMerchants <= min(merchantPrefix.size, merchantSuffix.size)) {
            "Don't have enough merchant names for numMerchants=$numMerchants"
        }
    }
}

