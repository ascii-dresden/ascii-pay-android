package org.ascii.asciiPayCompanion

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray

class HostCardEmulatorService : HostApduService() {
    init{
        // TODO init Card object
    }

    override fun onCreate() {
        super.onCreate()

        // TODO generate and save a new card id for this instance of the app

    }


    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            Log.e(Utils.TAG, "Apdu received by the system is null")
            return toByteArray(Utils.STATUS_FAILED)
        }
        return TODO("Card not implemented yet")
    }

    override fun onDeactivated(reason: Int) {
        Log.d(Utils.TAG, "Deactivated: $reason")
    }
}