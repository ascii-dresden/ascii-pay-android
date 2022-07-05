package org.ascii.asciiPayCompanion.accountManagement

data class CardData(val id: ByteArray, val key: ByteArray) {
    val credentials by lazy { Credentials(this) }
}
