package org.ascii.asciiPayCompanion

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.apollographql.apollo3.ApolloClient
import kotlinx.coroutines.Job
import kotlin.properties.Delegates

class AccountManager (private val mainContext: Context, private val mainLifecycle: Lifecycle){
    private val cardSP = mainContext.getSharedPreferences("card", AppCompatActivity.MODE_PRIVATE)
    var sessionAuthorization : String? = null
    private val accountListenerList = ArrayList<AccountUser>()
    private val gqlClient = ApolloClient.Builder()
        .serverUrl("https://pay.ascii.coffee/api/v1/graphql")
        .build()

    private var account : Account? by Delegates.observable(loadAccountData()) {
            _, _, newValue ->
        accountListenerList.forEach {it.onAccountChange(newValue)}
    }

    init {
        cardSP.registerOnSharedPreferenceChangeListener(CardSPListener())
    }

    fun initSession() {
        TODO()
    }

    fun endSession() {
        TODO()
    }



    fun registerAccountUser(accountUser: AccountUser) {
        accountUser.onAccountChange(account)
        accountListenerList.add(accountUser)
    }
    private fun loadAccountData() : Account? {
        // visual representation of the card
        val fullName = cardSP.getString("full_name", null)
        val cardId = cardSP.getString("id", null)
        val cardKey = cardSP.getString("key", null)

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
        return fullName?.let { Account(fullName, cardData) }
    }
    fun saveAccountData(){
        TODO()
    }

    fun fetchAccountData() {
        mainLifecycle.coroutineScope.launchWhenResumed {
            val res = gqlClient.query(AccountInfoQuery()).execute()
        }

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
        cardEditor.putString("id", "AFFE1337C0FFEE00")
        cardEditor.putString("key", "7665165AADE654654AACC112131415161718192021222324")
        cardEditor.putString("full_name", "Peter Zwegat")
        cardEditor.apply()
    }
}