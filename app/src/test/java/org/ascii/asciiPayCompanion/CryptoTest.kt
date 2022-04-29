package org.ascii.asciiPayCompanion

import android.os.Bundle
import org.ascii.asciiPayCompanion.Utils.Companion.toByteArray
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class CryptoTest {

    val testId = toByteArray("C0FFE144")
    val card = Card(testId, toByteArray("5AB7B5B41110B90273EA816751E41D88"))

    @After
    fun cleanUp() {
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
    fun successfulInteractionTest() {
        val s1Response = card.interact(toByteArray("00A4000007F0000000C0FFEE"), Bundle())
        assertTrue("Card is in the wrong stage.", card.stage is Card.Phase1Stage)
        assertEquals("AID Selection failed", testId.toList(), s1Response.toList())

        // use the crypt value from our test case
        card.stage = card.Phase1Stage(toByteArray("CF62E7B53ED842CB"))
        val s2Response = card.interact(toByteArray("10"), Bundle())
        assertEquals(
            "Phase 1 failed.",
            toByteArray("005FC0F77858DAE66B").toList(),
            s2Response.toList()
        )
        assertTrue("Card is in the wrong stage.", card.stage is Card.Phase2Stage)

        val s3Response = card.interact(toByteArray("11ECC195EFFC0A5A43EE03A374B30E7764"), Bundle())
        assertEquals(
            "Phase 2 failed",
            toByteArray("0004F63553A119E9CF").toList(),
            s3Response.toList()
        )
        assertTrue("Card is in the wrong stage.", card.stage is Card.DefaultStage)

    }

    @Test
    fun malformedRequestsTest() {
        // testing the first stage
        val s1Response = card.interact(toByteArray("20A4000007F0000000C0FFEE"), Bundle())
        assertTrue("Card didn't reset, despite malformed request.", card.stage is Card.DefaultStage)
        assertEquals(
            "Card didn't gave an error code, despite a malformed request.",
            toByteArray("01").toList(),
            s1Response.toList()
        )

        card.stage = card.Phase1Stage(null)
        val s2Response = card.interact(toByteArray("00"), Bundle())
        assertTrue("Card didn't reset, despite malformed request.", card.stage is Card.DefaultStage)
        assertEquals(
            "Card didn't gave an error code, despite a malformed request.",
            toByteArray("01").toList(),
            s2Response.toList()
        )

        card.stage = card.Phase2Stage(toByteArray("CF62E7B53ED842CB"))
        val s3Response01 = card.interact(toByteArray("1000000000000000000000000000000000"), Bundle())
        assertTrue("Card didn't reset, despite malformed request.", card.stage is Card.DefaultStage)
        assertEquals(
            "Card didn't gave an error code, despite a malformed request.",
            toByteArray("01").toList(),
            s3Response01.toList()
        )

        card.stage = card.Phase2Stage(toByteArray("CF62E7B53ED842CB"))
        val s3Response02 = card.interact(toByteArray("0000000000000000000000000000000000"), Bundle())
        assertTrue("Card didn't reset, despite malformed request.", card.stage is Card.DefaultStage)
        assertEquals(
            "Card didn't gave an error code, despite a malformed request.",
            toByteArray("01").toList(),
            s3Response02.toList()
        )
    }
}