package org.ascii.asciiPayCompanion

import android.content.SharedPreferences
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray
import org.ascii.asciiPayCompanion.Utils.Companion.toHex
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.random.Random


class Card(service: HostCardEmulatorService) {
    var stage: CardStage = DefaultStage()
    val sp: SharedPreferences = service.getSharedPreferences("card", HostApduService.MODE_PRIVATE)
    val cardSPListener = CardSPListener()

    val id: ByteArray
    val key: ByteArray

    companion object {
        val H10 = byteArrayOf(0x10)
        val H01 = byteArrayOf(0x01)
    }

    init {
        // check that everything exists
        if (!sp.contains("id") || !sp.contains("key")) {
            Log.e(Utils.TAG, "No Card data found. Exiting...")
            service.stopSelf()
            // TODO decide how to handle this situation
        }
        // Converting to non nullable because existence has been asserted above
        id = toByteArray(sp.getString("id", null)!!)
        key = toByteArray(sp.getString("key", null)!!)

        if (id.size != 8) {
            Log.e(Utils.TAG, "Card id is malformed: " + Utils.toHex(id))
        }
        sp.registerOnSharedPreferenceChangeListener(cardSPListener)
    }

    inner class CardSPListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sp: SharedPreferences, name: String) {
            // TODO add dynamic listener for changes of the card sp
        }
    }

    // the apdu will be forwarded to this function
    fun interact(apdu: ByteArray, extras: Bundle?): ByteArray {
        if (apdu.isEmpty()) {
            return H01
        }
        val (ret, stage) = stage.progress(apdu, extras)
        this.stage = stage
        return ret
    }

    fun reset() {
        stage = DefaultStage()
    }

    /*
    first and default stage of the card
    an iso select application is expected here
    the unique ascii card id will be returned

    Request: ISO SELECT FILE with AID F0 00 00 00 C0 FF EE
    "00 A4 00 00 07 F0 00 00 00 C0 FF EE"
    Response: card id [8 Byte]
    "00 00 00 00 00 00 00 00"
    */
    inner class DefaultStage : CardStage {
        override fun progress(apdu: ByteArray, extras: Bundle?): Pair<ByteArray, CardStage> {
            // TODO do we need more checks?
            if (apdu.toList() != toByteArray("00A4000007F0000000C0FFEE").toList())
                return H01 to this
            // return the id
            return Pair(id, Phase1Stage(null))
        }
    }

    /*
    second stage of the authentication
    Request: start of authentication [1 Byte + 0 Byte] "10"
    Response: ek_rndB [1 Byte + 8 Byte]

    "00 00 00 00 00 00 00 00 00"
    "01"
    */
    inner class Phase1Stage(private val rndB: ByteArray?) : CardStage {
        override fun progress(apdu: ByteArray, extras: Bundle?): Pair<ByteArray, CardStage> {
            // 1. case: adding a new key
            // format checking
            if (apdu[0] == 0x20.toByte()) {
                // set card key
                if (apdu.size != 17) {
                    Log.e(Utils.TAG, "Error: Init card request has the wrong size.")
                    return H01 to this
                }
                // write new key to storage
                val key = apdu.slice(1..16)
                val cardEditor = sp.edit()
                cardEditor.putString("key", toHex(key.toByteArray()))
                cardEditor.apply()
                return byteArrayOf(0x00) to DefaultStage()
            }

            // 2. case: authentication
            // check request format
            if (!apdu.contentEquals(H10))
                return H01 to DefaultStage()

            // Generate client challenge
            val rndB = rndB ?: Random.nextBytes(8)
            // Encrypt challenge with secret key
            val ek_rndB = encrypt(key, rndB)

            return Pair(byteArrayOf(0x00) + ek_rndB, Phase2Stage(rndB))
        }
    }

    /*
    third stage of the authentication
    request: dk_rndA_rndBshifted [1 Byte + 16 Byte]
    "11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
    response: ek_rndAshifted [1Byte + 8 Byte]
    "00 00 00 00 00 00 00 00 00"
    "01"
    */
    inner class Phase2Stage(private val rndB: ByteArray) : CardStage {
        override fun progress(apdu: ByteArray, extras: Bundle?): Pair<ByteArray, CardStage> {
            val result = try {
                val bytes = authPhase2(key, rndB, apdu.drop(1).toByteArray())
                byteArrayOf(0x00) + bytes
            } catch (e: Exception) {
                H01
            }
            return result to DefaultStage()
        }

        fun authPhase2(key: ByteArray, rndB: ByteArray, dk_rndA_rndBshifted: ByteArray): ByteArray {
            // Decrypt server request
            val rndA_rndBshifted = decrypt(key, dk_rndA_rndBshifted)

            // Split server request in client challenge response and server challenge
            val rndA = rndA_rndBshifted.sliceArray(0..7)
            val rndBshifted = rndA_rndBshifted.sliceArray(8..15)

            // Verify client challenge response
            if (!rndBshifted.contentEquals(rndB.leftShift(1))) {
                // TODO this doesn't get caught
                throw Error("Client challenge failed!")
            }

            // Generate server challenge response
            val rndAshifted = rndA.leftShift(1)
            val ek_rndAshifted = encrypt(key, rndAshifted)

            return ek_rndAshifted
        }
    }

    // functions needed for crypto
    // -----------------------------------------------------------

    fun tdesEncryptBlock(key: ByteArray, value: ByteArray): ByteArray {
        val secretKey1 = SecretKeySpec(key.sliceArray(0 until 8), "DES")
        val secretKey2 = SecretKeySpec(key.sliceArray(8 until 16), "DES")
        val secretKey3 = SecretKeySpec(key.sliceArray(0 until 8), "DES")

        val cipher1 = Cipher.getInstance("DES/ECB/NoPadding")
        val cipher2 = Cipher.getInstance("DES/ECB/NoPadding")
        val cipher3 = Cipher.getInstance("DES/ECB/NoPadding")

        cipher1.init(Cipher.ENCRYPT_MODE, secretKey1)
        val enc1 = cipher1.doFinal(value)
        cipher2.init(Cipher.DECRYPT_MODE, secretKey2)
        val enc2 = cipher2.doFinal(enc1)
        cipher3.init(Cipher.ENCRYPT_MODE, secretKey3)
        val enc3 = cipher3.doFinal(enc2)

        return enc3
    }

    fun encrypt(key: ByteArray, value: ByteArray): ByteArray {
        var buffer = ByteArray(8)
        var result = ByteArray(0)

        for (block in value.toList().chunked(8)) {
            val blockArray = block.toByteArray()
            val xorArray = blockArray.xor(buffer)
            val enc = tdesEncryptBlock(key, xorArray)

            buffer = enc
            result += enc
        }

        return result
    }

    fun decrypt(key: ByteArray, value: ByteArray): ByteArray {
        var buffer = ByteArray(8)
        var result = ByteArray(0)

        for (block in value.toList().chunked(8)) {
            val blockArray = block.toByteArray()
            val enc = tdesEncryptBlock(key, blockArray)
            val xorArray = enc.xor(buffer)

            buffer = blockArray
            result += xorArray
        }

        return result
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

    fun ByteArray.xor(other: ByteArray): ByteArray {
        if (size != other.size) throw Error("Array sizes!")

        return zip(other).map { (a, b) -> a.xor(b) }.toByteArray()
    }
    // -----------------------------------------------------------
}

interface CardStage {
    fun progress(apdu: ByteArray, extras: Bundle?): Pair<ByteArray, CardStage>
}

