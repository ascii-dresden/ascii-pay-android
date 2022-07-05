package org.ascii.asciiPayCompanion.accountManagement

import java.util.*

data class Account(
    val name: String,
    val uuid: UUID,
    var cardData: CardData?
)