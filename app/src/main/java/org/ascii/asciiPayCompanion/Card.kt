package org.ascii.asciiPayCompanion

import android.os.Bundle
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


class Card(private val id: ByteArray, private val secretKey: ByteArray) {
    var stage: CardStage

    companion object {
        val H10 = byteArrayOf(0x10)
    }
    init {
        if(id.size != 8) Log.e(Utils.TAG,"Card id is malformed: " + Utils.toHex(id))
    }

    // the apdu will be forwarded to this function
    fun interact(apdu: ByteArray?, extras: Bundle?): ByteArray {
        val (ret, stage) = stage.progress(apdu, extras)
        this.stage = stage
        return ret
    }

    // first and default stage of the card
    // an iso select application is expected here
    // the unique ascii card id will be returned
    inner class DefaultStage : CardStage {
        override fun progress(apdu: ByteArray?, extras: Bundle?): Pair<ByteArray, CardStage> {
            // TODO check request format
            if(false) return Pair(Utils.STATUS_FAILED.toByteArray(), this)
            // return the id
            return Pair(id, Phase1Stage())
        }
    }

    inner class Phase1Stage : CardStage {
        override fun progress(apdu: ByteArray?, extras: Bundle?): Pair<ByteArray, CardStage> {
            // request format check
            if (!apdu.contentEquals(H10))
                return Utils.STATUS_FAILED.toByteArray() to DefaultStage()

            // Generate client challenge
            val rndB = Random.nextBytes(8)
            // Encrypt challenge with secret key
            val ek_rndB = encrypt(secretKey, rndB)

            // TODO return the correct challenge
            return Pair(ek_rndB, Phase2Stage())
        }
    }

    inner class Phase2Stage : CardStage {
        override fun progress(apdu: ByteArray?, extras: Bundle?): Pair<ByteArray, CardStage> {
            TODO("Not yet implemented")
        }
    }

    // functions needed for crypto
    // -----------------------------------------------------------
    fun encrypt(key: ByteArray, value: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "DESede")
        val cipher = Cipher.getInstance("DESede/CBC/NoPadding")
        val encIv = IvParameterSpec(ByteArray(8), 0, 8)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, encIv)
        return cipher.doFinal(value)
    }

    fun decrypt(key: ByteArray, value: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "DESede")
        val cipher = Cipher.getInstance("DESede/CBC/NoPadding")
        val encIv = IvParameterSpec(ByteArray(8), 0, 8)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, encIv)
        return cipher.doFinal(value)
    }

    fun ByteArray.leftShift(d: Int): ByteArray {
        val newList = this.copyOf()
        var shift = d
        if (shift > size) shift %= size
        forEachIndexed { index, value ->
            val newIndex = (index + (size - shift)) % size
            newList[newIndex] = value
        }
        return newList
    }
    // -----------------------------------------------------------
}

interface CardStage {
    fun progress(apdu: ByteArray?, extras: Bundle?): Pair<ByteArray, CardStage>
}

