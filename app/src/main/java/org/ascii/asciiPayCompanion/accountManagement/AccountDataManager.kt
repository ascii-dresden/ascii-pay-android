package org.ascii.asciiPayCompanion.accountManagement

import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.ascii.asciiPayCompanion.App
import org.ascii.asciiPayCompanion.Utils
import org.ascii.asciiPayCompanion.api.AccountDto
import org.ascii.asciiPayCompanion.api.Api
import org.ascii.asciiPayCompanion.api.AuthResponseDto
import org.ascii.asciiPayCompanion.api.ResultHandler
import java.util.*
import kotlin.properties.Delegates

object AccountDataManager{
    const val cardSPName = "card"
    const val tokenAttr = "token"
    const val nameAttr = "fullName"
    const val idAttr = "cardId"
    const val keyAttr = "key"
    const val uuidAttr = "uuid"

    private val cardSP = App.appContext.getSharedPreferences(cardSPName, AppCompatActivity.MODE_PRIVATE)
        .apply {
            registerOnSharedPreferenceChangeListener(CardSPListener())
        }
    private val accountListenerList = ArrayList<AccountUser>()

    // create Account Session and make sure to update all accountListeners on change of the session
    private var accountSession: AccountSession? by Delegates.observable(loadSession()) { _, _, newSession ->
        accountListenerList.forEach { it.onAccountChange(newSession?.account) }
    }

    internal class AccountSession(
        val api: Api,
    ){
        var account: Account? by Delegates.observable(loadAccountData()) { _, _, newAccount ->
            accountListenerList.forEach { it.onAccountChange(newAccount) }
        }
    }

    fun registerAccountUser(accountUser: AccountUser) {
        accountUser.onAccountChange(accountSession?.account)
        accountListenerList.add(accountUser)
    }

    private fun loadSession(): AccountSession? {
        val token = cardSP.getString(tokenAttr, null)
        return token?.let { token ->
            AccountSession(
                Api(token),
            )
        }
    }
    private fun loadAccountData(): Account? {
        val fullName = cardSP.getString(nameAttr, null)
        val uuid = cardSP.getString(uuidAttr, null)?.let {
            UUID.fromString(it)
        }
        val cardKey = cardSP.getString(idAttr, null)
        val cardId = cardSP.getString(idAttr, null)

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
        return fullName?.let {
            uuid?.let {
                Account(fullName, uuid, cardData)
            }
        }
    }


    // reload all data from disk if it changes
    class CardSPListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sp: SharedPreferences, name: String) {
            accountSession = loadSession()
        }
    }

    private fun createDummyCard() {
        // TODO replace this method with proper way of instantiating the card data
        val cardEditor = cardSP.edit()
            .putString(idAttr, "AFFE1337C0FFEE00")
            .putString(keyAttr, "7665165AADE654654AACC112131415161718192021222324")
            .putString(nameAttr, "Peter Zwegat")
            .putString(uuidAttr, "")
        cardEditor.apply()
    }

    fun login(username: String, password: String, success: ()->Unit, error: (LoginError)->Unit) {
        Api.login(username, password, object : ResultHandler<AuthResponseDto> {
            override fun onSuccess(value: AuthResponseDto) {
                Log.e("LOGIN", "Success: ${value.token}")

                Api(value.token).getSelf(object : ResultHandler<AccountDto> {
                    override fun onSuccess(value: AccountDto) {
                        Log.e("ACCOUNT", "Success: $value")
                        success()
                    }

                    override fun onError(status: Int, error: String) {
                        Log.e("ACCOUNT", "Error $status")
                        // TODO case decision between error types
                        error(LoginError.unknown)
                    }
                })
            }

            override fun onError(status: Int, error: String) {
                Log.e("LOGIN", "Error $status")
            }
        })
    }
}