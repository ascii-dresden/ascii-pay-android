package org.ascii.asciiPayCompanion.accountManagement

data class AccountSession(
    val token: String,
    val name: String,
    val uid: Int,
    var cardData: CardData
)