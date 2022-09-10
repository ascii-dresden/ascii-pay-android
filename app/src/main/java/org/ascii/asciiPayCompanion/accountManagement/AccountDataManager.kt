package org.ascii.asciiPayCompanion.accountManagement

import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.work.*
import org.ascii.asciiPayCompanion.App
import org.ascii.asciiPayCompanion.Utils
import org.ascii.asciiPayCompanion.serverConnection.AccountPreparationWorker
import java.util.*
import kotlin.properties.Delegates

object AccountDataManager{
    const val cardSPName = "card"
    const val nameAttr = "fullName"
    const val idAttr = "cardId"
    const val keyAttr = "key"
    const val uuidAttr = "uuid"

    private val cardSP = App.appContext.getSharedPreferences(cardSPName, AppCompatActivity.MODE_PRIVATE)
        .apply {
            registerOnSharedPreferenceChangeListener(CardSPListener())
        }
    private val accountListenerList = ArrayList<AccountUser>()

    private var account: Account? by Delegates.observable(loadAccountData()) { _, _, newValue ->
        accountListenerList.forEach { it.onAccountChange(newValue) }
    }

    fun login(username: String, password: String): LiveData<Operation.State> {
        return WorkManager.getInstance(App.appContext).enqueueUniqueWork(
                "login",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<AccountPreparationWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInputData(workDataOf("username" to username))
                    .setInputData(workDataOf("password" to password))
                    .build(),
            ).state
    }

    fun registerAccountUser(accountUser: AccountUser) {
        accountUser.onAccountChange(account)
        accountListenerList.add(accountUser)
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
            account = loadAccountData()
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

    enum class AccountCompletionError {
        InvalidCredentials,
        Unknown,
    }
}