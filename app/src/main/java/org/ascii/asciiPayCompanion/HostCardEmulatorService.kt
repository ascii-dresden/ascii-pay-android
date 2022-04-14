package org.ascii.asciiPayCompanion

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class HostCardEmulatorService : HostApduService() {
    companion object {
        val TAG = "ascii Card Emulator"
        val STATUS_SUCCESS = "9000"
        val STATUS_FAILED = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val AID = "C0FFEE"
        val SELECT_INS = "A4"
        val DEFAULT_CLA = "00"
        val MIN_APDU_LENGTH = 12
    }

    override fun onCreate() {
        super.onCreate()

        // TODO generate and save a new card id for this instance of the app

    }

    override fun onStartCommand(){
        // TODO init card
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            Log.e(TAG, "Command APDU gotten from Android is null!")
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }


        val hexCommandApdu = Utils.toHex(commandApdu)
        Log.e(TAG, "apdu command:\n" + hexCommandApdu)

        return Utils.hexStringToByteArray("4242133700")

        if (hexCommandApdu.length < MIN_APDU_LENGTH) {
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        if (hexCommandApdu.substring(0, 2) != DEFAULT_CLA) {
            return Utils.hexStringToByteArray(CLA_NOT_SUPPORTED)
        }

    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: " + reason)
    }
}