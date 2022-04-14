package org.ascii.asciiPayCompanion

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import javax.crypto.SecretKey

class Card(id: ByteArray, secretKey: SecretKey) {
    var stage: CardStage

    // the apdu will be forwarded to this function
    fun interact(apdu: ByteArray?, extras: Bundle?): ByteArray {
        return when (stage) {
            is DefaultStage -> {
                stage = SelectedStage()
                return stage.progress()
            }
            is SelectedStage -> {

            }
            is Phase1Stage -> {

            }
            is Phase2Stage -> {

            }
            else ->
        }
    }

    inner class DefaultStage : CardStage {
        override fun progress(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
            TODO("Not yet implemented")
        }
    }

    inner class SelectedStage : CardStage {
        override fun progress(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
            TODO("Not yet implemented")
        }
    }

    inner class Phase1Stage : CardStage {
        override fun progress(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
            TODO("Not yet implemented")
        }
    }

    inner class Phase2Stage : CardStage {
        override fun progress(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
            TODO("Not yet implemented")
        }
    }
}

interface CardStage {
    fun progress(apdu: ByteArray?, extras: Bundle?): ByteArray
}


// first and default stage of the card
// an iso select application is expected here
// the unique ascii card id will be returned


//default, selected, prePhase1, prePhase2
