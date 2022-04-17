package org.ascii.asciiPayCompanion

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray

class HostCardEmulatorService : HostApduService() {

    lateinit var card : Card

    override fun onCreate() {
        super.onCreate()
        // TODO replace this method with proper way of instantiating the card data
        val cardSP = getSharedPreferences("card", MODE_PRIVATE)
        val cardEditor = cardSP.edit()
        cardEditor.putString("id", "AFFE1337C0FFEE00")
        cardEditor.putString("key", "7665165AADE654654AACC112131415161718192021222324")
        cardEditor.apply()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val cardSP = getSharedPreferences("card", MODE_PRIVATE)
        // check that everything exists
        if (!cardSP.contains("id") || !cardSP.contains("key")) {
            Log.e(Utils.TAG, "No Card data found. Exiting...")
            stopSelf()
            // TODO decide how to handle this situation
        }
        // Converting to non nullable because existence has been asserted above
        val id = toByteArray(cardSP.getString("id", null)!!)
        val key = toByteArray(cardSP.getString("key", null)!!)
        card = Card(id, key)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            Log.e(Utils.TAG, "Apdu received by the system is null")
            return toByteArray(Utils.STATUS_FAILED)
        }
        return card.interact(commandApdu, extras)
    }

    override fun onDeactivated(reason: Int) {
        Log.d(Utils.TAG, "Deactivated: $reason")
    }
}