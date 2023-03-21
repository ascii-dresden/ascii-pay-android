package org.ascii.asciiPayCompanion.accountManagement

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.ascii.asciiPayCompanion.App
import org.ascii.asciiPayCompanion.Utils
import java.util.*
import kotlin.properties.Delegates

object AccountDataManager{
    const val cardSPName = "card"
    const val tokenAttr = "token"
    const val nameAttr = "fullName"
    const val cardIdAttr = "cardId"
    const val cardKeyAttr = "key"
    const val uidAttr = "uid"

    // create Account Session and make sure to update all accountListeners on change of the session
    private val accountListenerList = ArrayList<AccountUser>()
    private val cardSP = App.appContext.getSharedPreferences(cardSPName, AppCompatActivity.MODE_PRIVATE)
        .apply {
            registerOnSharedPreferenceChangeListener { _, _ ->
                accountSession = loadSession()
                accountListenerList.forEach { it.onAccountChange(accountSession) }}
        }
    private var accountSession = loadSession()


    fun registerAccountUser(accountUser: AccountUser) {
        accountUser.onAccountChange(accountSession)
        accountListenerList.add(accountUser)
    }

    private fun loadSession(): AccountSession? {
        val token = cardSP.getString(tokenAttr, null)
        val name = cardSP.getString(nameAttr, null)
        val uid = cardSP.getString(uidAttr, null)?.toInt()
        val cardKey = cardSP.getString(cardIdAttr, null)
        val cardId = cardSP.getString(cardIdAttr, null)

        // check card id format
        cardId?.let {
            if (it.length != 8) {
                Log.e(Utils.TAG, "Card id is malformed: $it")
            }
        }
        // TODO add more format checks

        val cardData: CardData? = cardId?.let { id ->
            cardKey?.let { key -> CardData(Utils.toByteArray(id), Utils.toByteArray(key)) }
        }
        return name?.let {
            uid?.let {
                cardData?.let {
                    token?.let {
                        AccountSession(
                            token,
                            name,
                            uid,
                            cardData,
                        )
                    }
                }
            }
        }
    }

    fun login(username: String, password: String, success: ()->Unit, error: (LoginError)->Unit) {
        Api.login(username, password, object : ResultHandler<AuthResponseDto> {
            override fun onSuccess(value: AuthResponseDto) {
                val authResponseDto = value
                Log.e("LOGIN", "Success: ${authResponseDto.token}")
                Api(authResponseDto.token).getSelf(object : ResultHandler<AccountDto> {
                    override fun onSuccess(value: AccountDto) {
                        val accountDto = value
                        Log.e("ACCOUNT", "Success: $accountDto")
                        val newCard = CardData.create()

                        Api(authResponseDto.token).createNfcCard(
                            accountDto.id,
                            accountDto.name,
                            // TODO how to prevent collisions?
                            newCard.id,
                            newCard.key,
                            object : ResultHandler<Unit>{
                                override fun onSuccess(value: Unit) {
                                    Log.e("NFCCardCreation", "success $newCard")
                                    cardSP.edit()
                                        .putString(tokenAttr, authResponseDto.token)
                                        .putString(uidAttr, accountDto.id.toString())
                                        .putString(nameAttr, accountDto.name)
                                        .putString(cardIdAttr, newCard.id.toString())
                                        .putString(cardKeyAttr, newCard.key.toString())
                                        .apply()
                                    success()
                                }
                                override fun onError(status: Int, error: String) {
                                    Log.e("NFCCardCreation", "Error $status")
                                }
                            }
                        )
                    }

                    override fun onError(status: Int, error: String) {
                        Log.e("ACCOUNT", "Error $status")
                        error(LoginError.unknown)
                    }
                })
            }

            override fun onError(status: Int, error: String) {
                Log.e("LOGIN", "Error $status")
                error(when (status) {
                    401 -> LoginError.wrongCredentials
                    0 -> LoginError.networkUnavailable
                    else -> LoginError.unknown
                })
            }
        })
    }

    interface AccountUser {
        fun onAccountChange(accountSession: AccountSession?)
    }
}