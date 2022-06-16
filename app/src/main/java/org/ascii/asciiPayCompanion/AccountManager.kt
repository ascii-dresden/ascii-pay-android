package org.ascii.asciiPayCompanion

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import kotlin.properties.Delegates

class AccountManager (mainContext: Context){
    private val cardSP = mainContext.getSharedPreferences("card", AppCompatActivity.MODE_PRIVATE)
    lateinit var session_token : String
    var accountListenerList = ArrayList<AccountUser>()
    private val account : Account? by Delegates.observable(loadAccountData()) {
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
        // TODO on change listener
        // visual representation of the card
        val fullName = cardSP.getString("full_name", null)
        val cardId = cardSP.getString("id", null)
        val cardKey = cardSP.getString("key", null)

        val cardData : CardData? = cardId?.let { id ->
            cardKey?.let { key -> CardData(Utils.toByteArray(id), Utils.toByteArray(key)) }
        }
        return fullName?.let { Account(fullName, cardData) }
    }
    fun saveAccountData(){
        TODO()
    }
    inner class CardSPListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sp: SharedPreferences, name: String) {

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