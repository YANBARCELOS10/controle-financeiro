package br.com.ysenerbyte.comandospro.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatorEngineTest {
    @Test
    fun directStartStopAndTripAreDeterministic() {
        var state = SimulationState(circuit = CircuitType.DIRECT)
        state = SimulatorEngine.reduce(state, SimulationAction.Start)
        assertTrue(state.running)
        assertEquals(setOf(Contactor.MAIN), state.contactors)

        state = SimulatorEngine.reduce(state, SimulationAction.Trip)
        assertFalse(state.running)
        assertTrue(state.tripped)
        assertTrue(state.contactors.isEmpty())

        state = SimulatorEngine.reduce(state, SimulationAction.Start)
        assertFalse(state.running)
        assertEquals("START_BLOCKED", state.eventCode)

        state = SimulatorEngine.reduce(state, SimulationAction.Reset)
        assertFalse(state.tripped)
    }

    @Test
    fun reversalInterlockNeverEnergizesBothContactors() {
        var state = SimulationState(circuit = CircuitType.REVERSE)
        state = SimulatorEngine.reduce(state, SimulationAction.StartForward)
        assertEquals(setOf(Contactor.FORWARD), state.contactors)

        state = SimulatorEngine.reduce(state, SimulationAction.StartReverse)
        assertEquals("REVERSAL_BLOCKED", state.eventCode)
        assertEquals(setOf(Contactor.FORWARD), state.contactors)
        assertFalse(state.hasUnsafeOverlap)

        state = SimulatorEngine.reduce(state, SimulationAction.Stop)
        state = SimulatorEngine.reduce(state, SimulationAction.StartReverse)
        assertEquals(setOf(Contactor.REVERSE), state.contactors)
        assertFalse(state.hasUnsafeOverlap)
    }

    @Test
    fun starDeltaUsesADeadTimeAndNeverOverlaps() {
        var state = SimulationState(circuit = CircuitType.STAR_DELTA)
        state = SimulatorEngine.reduce(state, SimulationAction.Start)
        assertEquals(StarPhase.STAR, state.starPhase)
        assertEquals(setOf(Contactor.MAIN, Contactor.STAR), state.contactors)
        assertFalse(state.hasUnsafeOverlap)

        state = SimulatorEngine.reduce(state, SimulationAction.AdvanceStarTransition)
        assertEquals(StarPhase.TRANSITION, state.starPhase)
        assertEquals(setOf(Contactor.MAIN), state.contactors)
        assertFalse(state.hasUnsafeOverlap)

        state = SimulatorEngine.reduce(state, SimulationAction.AdvanceStarTransition)
        assertEquals(StarPhase.DELTA, state.starPhase)
        assertEquals(setOf(Contactor.MAIN, Contactor.DELTA), state.contactors)
        assertFalse(state.hasUnsafeOverlap)
    }

    @Test
    fun vfdFrequencyIsClampedAndRpmFollowsReference() {
        var state = SimulationState(circuit = CircuitType.VFD)
        state = SimulatorEngine.reduce(state, SimulationAction.SetFrequency(90f))
        assertEquals(60f, state.frequencyHz)
        state = SimulatorEngine.reduce(state, SimulationAction.Start)
        assertEquals(1_800, state.estimatedRpm)

        state = SimulatorEngine.reduce(state, SimulationAction.SetFrequency(-10f))
        assertEquals(0f, state.frequencyHz)
        assertEquals(0, state.estimatedRpm)
    }
}
