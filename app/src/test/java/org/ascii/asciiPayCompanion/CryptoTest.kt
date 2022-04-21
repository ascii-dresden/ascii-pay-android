package org.ascii.asciiPayCompanion

import android.os.Bundle
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.reflect.typeOf

class CryptoTest {

    val testId = toByteArray("C0FFE144")
    val card = Card(testId, toByteArray("5AB7B5B41110B90273EA816751E41D88"))

    @After
    fun cleanUp(){
        card.reset()
    }


    /*
    * Test Case:
    *
    * # Debug reader
    * -- key: 5A B7 B5 B4 11 10 B9 02 73 EA 81 67 51 E4 1D 88
    *
    * Call phase1
    * # Debug card
    * -- rndB: CF 62 E7 B5 3E D8 42 CB
    * ek_rndB: 5F C0 F7 78 58 DA E6 6B <- card should return this
    *
    * # Debug reader
    * -- rndA: 0F D9 E6 F7 EB 7E 1B D9
    * -- rndAshifted: D9 E6 F7 EB 7E 1B D9 0F
    * -- rndB: CF 62 E7 B5 3E D8 42 CB
    * -- rndBshifted: 62 E7 B5 3E D8 42 CB CF
    * -- rndA_rndBshifted: 0F D9 E6 F7 EB 7E 1B D9 62 E7 B5 3E D8 42 CB CF
    *
    * Call phase2 with dk_rndA_rndBshifted: EC C1 95 EF FC 0A 5A 43 EE 03 A3 74 B3 0E 77 64
    * # Debug card
    * -- rndB: CF 62 E7 B5 3E D8 42 CB
    * -- rndA: 0F D9 E6 F7 EB 7E 1B D9
    * -- rndAShifted: 0F D9 E6 F7 EB 7E 1B D9
    * ek_rndAshifted_card: 04 F6 35 53 A1 19 E9 CF <- card should return this
    *
    * # Debug reader
    * -- rndAshifted_card: D9 E6 F7 EB 7E 1B D9 0F
    *
    * SessionKey: 0F D9 E6 F7 CF 62 E7 B5 EB 7E 1B D9 3E D8 42 CB
    */
    @Test
    fun successfulInteractionTest(){
        val s1Response = card.interact(toByteArray("00A4000007F0000000C0FFEE"), Bundle())
        assertEquals("AID Selection failed", s1Response, testId)
        assertTrue("Card is in the wrong stage.", card.stage is Card.Phase1Stage)

        // use the crypt value from our test case
        card.stage = card.Phase1Stage(toByteArray("CF62E7B53ED842CB"))
        //stateField.set(card, card.Phase1Stage(toByteArray("CF62E7B53ED842CB")))
        val s2Response = card.interact(toByteArray("10"), Bundle())
        // TODO make this not failing
        assertEquals("Phase 1 failed.", s2Response.toList(), toByteArray("005FC0F77858DAE66B").toList())
        assertTrue("Card is in the wrong stage.", card.stage is Card.Phase2Stage)

        val finalResponse = card.interact(toByteArray("11ECC195EFFC0A5A43EE03A374B30E7764"), Bundle())
        assertEquals("Phase 2 failed", finalResponse.toList(), toByteArray("0004F63553A119E9CF").toList())
        assertTrue("Card is in the wrong stage.", card.stage is Card.DefaultStage)

    }
}