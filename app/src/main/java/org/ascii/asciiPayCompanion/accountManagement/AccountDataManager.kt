package org.ascii.asciiPayCompanion.accountManagement

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import org.ascii.asciiPayCompanion.Utils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class AccountDataManager (private val mainContext: Context, private val mainLifecycle: Lifecycle){
    private val cardSP = mainContext.getSharedPreferences("card", AppCompatActivity.MODE_PRIVATE)
        .apply {
            registerOnSharedPreferenceChangeListener(CardSPListener())
        }
    private val accountListenerList = ArrayList<AccountUser>()

    private var account : Account? by Delegates.observable(loadAccountData()) {
            _, _, newValue ->
        accountListenerList.forEach {it.onAccountChange(newValue)}
    }


    fun registerAccountUser(accountUser: AccountUser) {
        accountUser.onAccountChange(account)
        accountListenerList.add(accountUser)
    }

    private fun loadAccountData() : Account? {
        val fullName = cardSP.getString("fullName", null)
        val uuid = cardSP.getString("uuid", null)?.let {
            UUID.fromString(it)
        }
        val cardKey = cardSP.getString("key", null)
        val cardId = cardSP.getString("cardId", null)

        // check card id format
        cardId?.let {
            if (it.length != 8) {
                Log.e(Utils.TAG, "Card id is malformed: $it")
            }
        }
        // TODO add more format checks

        val cardData : CardData? = cardId?.let { id ->
            cardKey?.let { key -> CardData(Utils.toByteArray(id), Utils.toByteArray(key)) }
        }
        return fullName?.let {
            uuid?.let {
                Account(fullName, uuid, cardData)
            }
        }
    }
    private fun saveAccountData(name: String?, uuid: UUID?){
        val editor = cardSP.edit()
        editor
            .putString("uuid", uuid?.toString()?:"")
            .putString("fullName", name?:"")
            .apply()
    }


    // reload all data from disk if it changes
    inner class CardSPListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sp: SharedPreferences, name: String) {
            account = loadAccountData()
        }
    }

    private fun createDummyCard() {
        // TODO replace this method with proper way of instantiating the card data
        val cardEditor = cardSP.edit()
            .putString("cardId", "AFFE1337C0FFEE00")
            .putString("key", "7665165AADE654654AACC112131415161718192021222324")
            .putString("fullName", "Peter Zwegat")
            .putString("uuid", "")
        cardEditor.apply()
    }
}