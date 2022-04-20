package org.ascii.asciiPayCompanion

import android.os.Bundle
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CryptoTest {

    val testId = toByteArray("C0FFE144")
    val card = Card(testId, toByteArray("5AB7B5B41110B90273EA816751E41D88"))

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