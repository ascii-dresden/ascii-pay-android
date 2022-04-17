package org.ascii.asciiPayCompanion

import android.os.Bundle
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.lang.reflect.Type
import kotlin.reflect.typeOf

class CryptoTest {

    val testId = toByteArray("C0FFE144")
    val card = Card(testId, ByteArray(24))

    val stateField = card.javaClass.getDeclaredField("stage")
    lateinit var cardStage : CardStage

    @Before
    fun setUpCard() {
        stateField.isAccessible = true
        cardStage = stateField.get(card) as CardStage
    }

    @Test
    fun aidSelectionTest(){
        val s1Response = card.interact(toByteArray("00A4000007F0000000C0FFEE"), Bundle())
        assertEquals("AID Selection failed", s1Response, testId)
        // TODO check card stage

        val s2Response = card.interact(toByteArray("10"), Bundle())
    }
}