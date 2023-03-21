package org.ascii.asciiPayCompanion.accountManagement

import kotlin.random.Random

data class CardData(val id: ByteArray, val key: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CardData

        if (!id.contentEquals(other.id)) return false
        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.contentHashCode()
        result = 31 * result + key.contentHashCode()
        return result
    }

    companion object {
        fun create(): CardData {
            return CardData(Random.nextBytes(32), Random.nextBytes(32))
        }
    }
}