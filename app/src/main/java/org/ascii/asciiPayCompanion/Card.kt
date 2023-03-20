package org.ascii.asciiPayCompanion

import android.os.Bundle
import android.util.Log
import org.ascii.asciiPayCompanion.accountManagement.Account
import org.ascii.asciiPayCompanion.accountManagement.AccountDataManager
import org.ascii.asciiPayCompanion.accountManagement.AccountUser
import org.ascii.asciiPayCompanion.accountManagement.CardData
import org.ascii.asciiPayCompanion.Utils.Companion.TAG
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.random.Random


class Card(accountManager: AccountDataManager) {
    private var stage: CardStage = defaultStage()

    // if cardData is null, the Card authentication functionality is disabled
    var cardData : CardData? = null

    companion object {
        val H01 = byteArrayOf(0x01)
    }

    private fun defaultStage(): CardStage{
        return Phase1Stage(null)
    }

    init {
        accountManager.registerAccountUser(AccountListener())
    }

    inner class AccountListener : AccountUser {
        override fun onAccountChange(account: Account?) {
            // if the account is non null and has card data save it, otherwise set card data to null
            cardData = account?.cardData
        }

    }

    // the apdu will be forwarded to this function
    fun interact(apdu: ByteArray, extras: Bundle?): ByteArray {
        if (apdu.isEmpty()) {
            return H01
        }
        /*
        first and default stage of the card
        an iso select application is expected here
        the unique ascii card id will be returned

        No stage progression will happen so far

        Request: ISO SELECT FILE with AID F0 00 00 00 C0 FF EE
        "00 A4 00 00 07 F0 00 00 00 C0 FF EE"
        Response: card id [8 Byte]
        "00 00 00 00 00 00 00 00"
        */
        return cardData?.let { cardData ->
            if (apdu.toList() == toByteArray("00A4040007F0000000C0FFEE").toList()) {
                Log.e(TAG, "Got aid select instruction; sending card id: " + cardData.id)
                this.stage = defaultStage()
                // return the id
                return byteArrayOf(0x00) + cardData.id
            }

            val (ret, stage) = stage.progress(apdu, extras, cardData)
            this.stage = stage
            return ret
        } ?: let {
            Log.e(TAG, "Please login with your account first before using the card feature.")
            // TODO find an extra error code for the case, that there is no card
            return H01
        }
    }

    /*
    second stage of the authentication
    Request: start of authentication [1 Byte + 0 Byte] "10"
    Response: ek_rndB [1 Byte + 32 Byte]

    "00 00 00 00 00 00 00 00 00"
    "01"
    */
    inner class Phase1Stage(private val rndB: ByteArray?) : CardStage {
        override fun progress(
            apdu: ByteArray,
            extras: Bundle?,
            cardData: CardData
        ): Pair<ByteArray, CardStage> {
            // check request format
            if (apdu.firstOrNull() != 0x10.toByte()) {
                Log.e(TAG, "Error: Invalid request")
                return H01 to defaultStage()
            }

            Log.e(TAG, "Starting Authentication...")
            // Generate client challenge
            val rndB = rndB ?: Random.nextBytes(32)
            // Encrypt challenge with secret key
            val ekRndb = encrypt(cardData.key, rndB)

            return Pair(byteArrayOf(0x00) + ekRndb, Phase2Stage(rndB))
        }
    }

    /*
    third stage of the authentication
    request: dk_rndA_rndBshifted [1 Byte + 64 Byte]
    "11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
    response: ek_rndAshifted [1Byte + 32 Byte]
    "00 00 00 00 00 00 00 00 00"
    "01"
    */
    inner class Phase2Stage(private val rndB: ByteArray) : CardStage {
        override fun progress(
            apdu: ByteArray,
            extras: Bundle?,
            cardData: CardData
        ): Pair<ByteArray, CardStage> {
            Log.e(TAG, "Authentication Phase 2")
            val result = try {
                // TODO use apdu.drop everywhere
                val bytes = authPhase2(cardData.key, rndB, apdu.drop(1).toByteArray())
                byteArrayOf(0x00) + bytes
            } catch (e: Exception) {
                Log.e(TAG, "Error: Malformed key")
                H01
            }
            return result to defaultStage()
        }

        private fun authPhase2(key: ByteArray, rndB: ByteArray, dk_rndA_rndBshifted: ByteArray): ByteArray {
            // Decrypt server request
            val rndA_rndBshifted = decrypt(key, dk_rndA_rndBshifted)

            // Split server request in client challenge response and server challenge
            val rndA = rndA_rndBshifted.sliceArray(0..31)
            val rndBshifted = rndA_rndBshifted.sliceArray(32..63)

            // Verify client challenge response
            if (!rndBshifted.contentEquals(rndB.leftShift(1))) {
                Log.e(TAG, "Error: Client challenge failed!")
                throw Error()
            }

            // Generate server challenge response
            val rndAshifted = rndA.leftShift(1)
            val ek_rndAshifted = encrypt(key, rndAshifted)

            return ek_rndAshifted
        }
    }

    // functions needed for crypto
    // -----------------------------------------------------------

    private fun aesEncryptBlock(key: ByteArray, value: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "AES")

        val cipher = Cipher.getInstance("AES/ECB/NoPadding")

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(value)
    }

    private fun aesDecryptBlock(key: ByteArray, value: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "AES")

        val cipher = Cipher.getInstance("AES/ECB/NoPadding")

        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(value)
    }

    fun encrypt(key: ByteArray, value: ByteArray): ByteArray {
        var buffer = ByteArray(32)
        var result = ByteArray(0)

        for (block in value.toList().chunked(32)) {
            val blockArray = block.toByteArray()
            val xorArray = blockArray.xor(buffer)
            val enc = aesEncryptBlock(key, xorArray)

            buffer = enc
            result += enc
        }

        return result
    }

    fun decrypt(key: ByteArray, value: ByteArray): ByteArray {
        var buffer = ByteArray(32)
        var result = ByteArray(0)

        for (block in value.toList().chunked(32)) {
            val blockArray = block.toByteArray()
            val enc = aesDecryptBlock(key, blockArray)
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

    private fun ByteArray.xor(other: ByteArray): ByteArray {
        if (size != other.size) throw Error("Array sizes!")

        return zip(other).map { (a, b) -> a.xor(b) }.toByteArray()
    }
    // -----------------------------------------------------------
}

interface CardStage {
    fun progress(apdu: ByteArray, extras: Bundle?, cardData: CardData): Pair<ByteArray, CardStage>
}

