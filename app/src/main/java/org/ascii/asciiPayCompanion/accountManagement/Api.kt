package org.ascii.asciiPayCompanion.accountManagement

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

const val BASE_URL = "https://ascii.lars-westermann.de/api/v1"
private val client = OkHttpClient()

class Api(private val token: String) {

    fun getSelf(resultHandler: ResultHandler<AccountDto>) {
        val request = Request.Builder()
            .url("$BASE_URL/auth/account")
            .header("AUTHORIZATION", "Bearer $token")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                resultHandler.onError(0, e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        resultHandler.onError(response.code, response.body?.string() ?: "")
                        return
                    }

                    val value = gson.fromJson(
                        response.body?.string() ?: "",
                        AccountDto::class.java
                    )
                    resultHandler.onSuccess(value)
                }
            }
        })
    }

    fun logout(resultHandler: ResultHandler<Unit>) {
        val request = Request.Builder()
            .url("$BASE_URL/auth")
            .header("AUTHORIZATION", "Bearer $token")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                resultHandler.onError(0, e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        resultHandler.onError(response.code, response.body?.string() ?: "")
                        return
                    }

                    resultHandler.onSuccess(Unit)
                }
            }
        })
    }

    fun createNfcCard(
        accountId: Int,
        name: String,
        cardId: ByteArray,
        data: ByteArray,
        resultHandler: ResultHandler<Unit>
    ) {
        val request = Request.Builder()
            .url("$BASE_URL/account/$accountId/nfc-authentication")
            .header("AUTHORIZATION", "Bearer $token")
            .post(
                gson.toJson(
                    CreateAuthNfcDto(
                        name,
                        Base64.encodeToString(cardId, Base64.NO_WRAP),
                        CardTypeDto.HostCardEmulation,
                        Base64.encodeToString(data, Base64.NO_WRAP),
                        true
                    )
                )
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                resultHandler.onError(0, e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        resultHandler.onError(response.code, response.body?.string() ?: "")
                        return
                    }

                    resultHandler.onSuccess(Unit)
                }
            }
        })
    }

    companion object {
        private val gson = Gson()
        fun login(
            username: String,
            password: String,
            resultHandler: ResultHandler<AuthResponseDto>
        ) {
            val request = Request.Builder()
                .url("$BASE_URL/auth/password?long_lived=true")
                .post(
                    gson.toJson(AuthPasswordDto(username, password))
                        .toRequestBody("application/json; charset=utf-8".toMediaType())
                )
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    resultHandler.onError(0, e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            resultHandler.onError(response.code, response.body?.string() ?: "")
                            return
                        }

                        val value = gson.fromJson(
                            response.body?.string() ?: "",
                            AuthResponseDto::class.java
                        )
                        resultHandler.onSuccess(value)
                    }
                }
            })
        }
    }
}

class AuthPasswordDto(val username: String, val password: String)
class AuthResponseDto(val token: String)

data class AccountDto(
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

enum class CardTypeDto {
    GenericNfc,
    AsciiMifare,
    HostCardEmulation,
}

data class CreateAuthNfcDto(
    val name: String,
    val card_id: String,
    val card_type: CardTypeDto,
    val data: String?,
    val depends_on_session: Boolean?,
)

interface ResultHandler<T> {
    fun onSuccess(value: T)
    fun onError(status: Int, error: String)
}