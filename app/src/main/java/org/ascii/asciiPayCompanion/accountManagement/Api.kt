package org.ascii.asciiPayCompanion.api

class Api(val token: String) {

    fun getSelf(resultHandler: ResultHandler<Account>) {
    }

    fun logout(resultHandler: ResultHandler<Unit>) {
    }

    fun createNfcCard(
        accountId: Int,
        name: String,
        cardId: ByteArray,
        data: ByteArray,
        resultHandler: ResultHandler<Unit>
    ) {
    }

    companion object {
        fun login(username: String, password: String, resultHandler: ResultHandler<String>) {
        }
    }
}

data class Account(
    val id: Int,
    val balance: Map<CoinTypeDto, Int>,
    val name: String,
    val email: String,
    val role: RoleDto,
    val enable_monthly_mail_report: Boolean,
    val enable_automatic_stamp_usage: Boolean,
)

enum class CoinTypeDto {
    Cent,
    CoffeeStamp,
    BottleStamp,
}

enum class RoleDto {
    Basic,
    Member,
    Admin,
}

interface ResultHandler<T> {
    fun onSuccess(value: T)
    fun onError(status: Int, error: String)
}