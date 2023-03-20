package org.ascii.asciiPayCompanion.accountManagement

class AccountSession(
    private val authenticationCard: CardData,
    private val loginFailCallback: () -> Unit,
) {

}
