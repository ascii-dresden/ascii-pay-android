package org.ascii.asciiPayCompanion

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray
import org.ascii.asciiPayCompanion.Utils.Companion.toHex

class HostCardEmulatorService : HostApduService() {

    private var card : Card? = null
    private val accountManager = AccountManager(this)
    override fun onCreate() {
        card = Card(accountManager)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            Log.e(Utils.TAG, "Apdu received by the system is null")
            return toByteArray(Utils.STATUS_FAILED)
        }
        Log.e(Utils.TAG, "APDU will be processed: " + toHex(commandApdu))
        val ret = card?.interact(commandApdu, extras) ?: byteArrayOf()
        Log.e(Utils.TAG, "Returning payload: " + toHex(ret))
        return ret
    }

    override fun onDeactivated(reason: Int) {
        Log.d(Utils.TAG, "Deactivated: $reason")
    }
}