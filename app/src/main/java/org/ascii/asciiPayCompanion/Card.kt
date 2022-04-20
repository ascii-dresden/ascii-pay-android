package org.ascii.asciiPayCompanion

import android.os.Bundle
import android.util.Log
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.random.Random


class Card(private val id: ByteArray, private val secretKey: ByteArray) {
    private var stage : CardStage = DefaultStage()

    companion object {
        val H10 = byteArrayOf(0x10)
    }

    init {
        if (id.size != 8){
            //Log.e(Utils.TAG, "Card id is malformed: " + Utils.toHex(id))
        }
    }

    // the apdu will be forwarded to this function
    fun interact(apdu: ByteArray, extras: Bundle?): ByteArray {
        val (ret, stage) = stage.progress(apdu, extras)
        this.stage = stage
        return ret
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
    private inner class DefaultStage : CardStage {
        override fun progress(apdu: ByteArray, extras: Bundle?): Pair<ByteArray, CardStage> {
            // TODO check request format
            if (false) return Pair(toByteArray(Utils.STATUS_FAILED), this)
            // return the id
            return Pair(id, Phase1Stage())
        }
    }

    /*
    second stage of the authentication
    Request: start of authentication [1 Byte + 0 Byte] "10"
    Response: ek_rndB [1 Byte + 8 Byte]

    "00 00 00 00 00 00 00 00 00"
    "01"
    */
    private inner class Phase1Stage : CardStage {
        override fun progress(apdu: ByteArray, extras: Bundle?): Pair<ByteArray, CardStage> {
            // check request format
            if (!apdu.contentEquals(H10))
                return byteArrayOf(0x01) to DefaultStage()

            // Generate client challenge
            val rndB = Random.nextBytes(8)
            // Encrypt challenge with secret key
            val ek_rndB = encrypt(secretKey, rndB)

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
    private inner class Phase2Stage(private val rndB: ByteArray) : CardStage {
        override fun progress(apdu: ByteArray, extras: Bundle?): Pair<ByteArray, CardStage> {
            val result = try {
                val bytes = authPhase2(secretKey, rndB, apdu.drop(1).toByteArray())
                byteArrayOf(0x00) + bytes
            } catch (e: Exception) {
                byteArrayOf(0x01)
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

