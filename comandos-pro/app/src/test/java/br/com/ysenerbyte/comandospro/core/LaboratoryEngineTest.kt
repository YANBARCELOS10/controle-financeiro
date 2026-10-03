package br.com.ysenerbyte.comandospro.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaboratoryEngineTest {
    @Test
    fun benchAcceptsOnlyTheFunctionalOrder() {
        assertTrue(LaboratoryEngine.isBenchFlowValid(LaboratoryEngine.expectedBenchFlow))
        assertFalse(LaboratoryEngine.isBenchFlowValid(LaboratoryEngine.expectedBenchFlow.reversed()))
        assertFalse(LaboratoryEngine.isBenchFlowValid(LaboratoryEngine.expectedBenchFlow.dropLast(1)))
    }

    @Test
    fun signalLocationFollowsAvailableEvidence() {
        assertEquals("FIELD_DEVICE", LaboratoryEngine.locateInputSignal(false, false, false))
        assertEquals("FIELD_TO_CHANNEL", LaboratoryEngine.locateInputSignal(true, false, false))
        assertEquals("CHANNEL_TO_LOGIC", LaboratoryEngine.locateInputSignal(true, true, false))
        assertEquals("SIGNAL_COMPLETE", LaboratoryEngine.locateInputSignal(true, true, true))
    }
}
