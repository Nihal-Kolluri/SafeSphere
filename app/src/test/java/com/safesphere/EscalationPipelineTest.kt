package com.safesphere

import com.safesphere.data.model.EscalationTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EscalationPipelineTest {

    @Test
    fun testTierProgressionOrder() {
        val tier1 = EscalationTier.TIER_1_FAMILY
        val tier2 = EscalationTier.TIER_2_VOLUNTEERS
        val tier3 = EscalationTier.TIER_3_AUTHORITIES

        assertEquals(1, tier1.stepNumber)
        assertEquals(2, tier2.stepNumber)
        assertEquals(3, tier3.stepNumber)

        assertTrue(tier1.defaultDurationSeconds > 0)
        assertTrue(tier2.defaultDurationSeconds > 0)
        assertEquals(0, tier3.defaultDurationSeconds) // Final tier remains indefinitely
    }

    @Test
    fun testTierTransitions() {
        fun getNextTier(current: EscalationTier): EscalationTier {
            return when (current) {
                EscalationTier.TIER_1_FAMILY -> EscalationTier.TIER_2_VOLUNTEERS
                EscalationTier.TIER_2_VOLUNTEERS -> EscalationTier.TIER_3_AUTHORITIES
                EscalationTier.TIER_3_AUTHORITIES -> EscalationTier.TIER_3_AUTHORITIES
            }
        }

        assertEquals(EscalationTier.TIER_2_VOLUNTEERS, getNextTier(EscalationTier.TIER_1_FAMILY))
        assertEquals(EscalationTier.TIER_3_AUTHORITIES, getNextTier(EscalationTier.TIER_2_VOLUNTEERS))
        assertEquals(EscalationTier.TIER_3_AUTHORITIES, getNextTier(EscalationTier.TIER_3_AUTHORITIES))
    }
}
