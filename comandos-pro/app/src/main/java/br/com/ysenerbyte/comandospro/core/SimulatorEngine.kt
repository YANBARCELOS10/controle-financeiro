package br.com.ysenerbyte.comandospro.core

enum class CircuitType(val title: String, val shortTitle: String) {
    DIRECT("Partida direta", "Direta"),
    REVERSE("Reversão intertravada", "Reversão"),
    STAR_DELTA("Estrela-triângulo", "Y-Δ"),
    VFD("Inversor de frequência", "Inversor")
}

enum class RunDirection { STOPPED, FORWARD, REVERSE }

enum class StarPhase {
    IDLE,
    STAR,
    TRANSITION,
    DELTA
}

enum class Contactor {
    MAIN,
    FORWARD,
    REVERSE,
    STAR,
    DELTA,
    DRIVE
}

data class SimulationState(
    val circuit: CircuitType = CircuitType.DIRECT,
    val direction: RunDirection = RunDirection.STOPPED,
    val starPhase: StarPhase = StarPhase.IDLE,
    val contactors: Set<Contactor> = emptySet(),
    val tripped: Boolean = false,
    val frequencyHz: Float = 30f,
    val message: String = "Circuito virtual pronto.",
    val eventCode: String = "READY"
) {
    val running: Boolean
        get() = direction != RunDirection.STOPPED && contactors.isNotEmpty()

    val estimatedRpm: Int
        get() = if (circuit == CircuitType.VFD && running) {
            (frequencyHz * 30f).toInt()
        } else if (running) {
            1_800
        } else {
            0
        }

    val hasUnsafeOverlap: Boolean
        get() = (Contactor.FORWARD in contactors && Contactor.REVERSE in contactors) ||
            (Contactor.STAR in contactors && Contactor.DELTA in contactors)
}

sealed interface SimulationAction {
    data class SelectCircuit(val circuit: CircuitType) : SimulationAction
    data object Start : SimulationAction
    data object StartForward : SimulationAction
    data object StartReverse : SimulationAction
    data object Stop : SimulationAction
    data object Trip : SimulationAction
    data object Reset : SimulationAction
    data object AdvanceStarTransition : SimulationAction
    data class SetFrequency(val hz: Float) : SimulationAction
}

object SimulatorEngine {
    fun reduce(state: SimulationState, action: SimulationAction): SimulationState = when (action) {
        is SimulationAction.SelectCircuit -> SimulationState(
            circuit = action.circuit,
            frequencyHz = state.frequencyHz,
            message = "${action.circuit.title}: circuito virtual pronto.",
            eventCode = "CIRCUIT_SELECTED"
        )

        SimulationAction.Start -> start(state)
        SimulationAction.StartForward -> startDirection(state, RunDirection.FORWARD)
        SimulationAction.StartReverse -> startDirection(state, RunDirection.REVERSE)
        SimulationAction.Stop -> state.copy(
            direction = RunDirection.STOPPED,
            starPhase = StarPhase.IDLE,
            contactors = emptySet(),
            message = "Parada normal: comando virtual desenergizado.",
            eventCode = "STOPPED"
        )

        SimulationAction.Trip -> state.copy(
            direction = RunDirection.STOPPED,
            starPhase = StarPhase.IDLE,
            contactors = emptySet(),
            tripped = true,
            message = "Sobrecarga simulada: o relé térmico abriu o comando.",
            eventCode = "THERMAL_TRIP"
        )

        SimulationAction.Reset -> if (state.running) {
            state.copy(
                message = "RESET bloqueado enquanto o circuito está em movimento.",
                eventCode = "RESET_BLOCKED"
            )
        } else {
            state.copy(
                tripped = false,
                message = "Falha reconhecida. Sistema virtual pronto.",
                eventCode = "RESET_OK"
            )
        }

        SimulationAction.AdvanceStarTransition -> advanceStar(state)
        is SimulationAction.SetFrequency -> state.copy(
            frequencyHz = action.hz.coerceIn(0f, 60f),
            message = if (state.running) {
                "Referência ajustada para ${action.hz.coerceIn(0f, 60f).toInt()} Hz."
            } else {
                "Referência definida. Pressione RUN para iniciar."
            },
            eventCode = "FREQUENCY_SET"
        )
    }

    private fun start(state: SimulationState): SimulationState {
        if (state.tripped) return blockedByTrip(state)
        return when (state.circuit) {
            CircuitType.DIRECT -> state.copy(
                direction = RunDirection.FORWARD,
                contactors = setOf(Contactor.MAIN),
                message = "K1 energizado e selo virtual confirmado.",
                eventCode = "DIRECT_RUNNING"
            )

            CircuitType.REVERSE -> startDirection(state, RunDirection.FORWARD)
            CircuitType.STAR_DELTA -> state.copy(
                direction = RunDirection.FORWARD,
                starPhase = StarPhase.STAR,
                contactors = setOf(Contactor.MAIN, Contactor.STAR),
                message = "Etapa estrela: K1 + KY ativos. Transição temporizada em andamento.",
                eventCode = "STAR_RUNNING"
            )

            CircuitType.VFD -> state.copy(
                direction = RunDirection.FORWARD,
                contactors = setOf(Contactor.DRIVE),
                message = "RUN ativo em ${state.frequencyHz.toInt()} Hz.",
                eventCode = "VFD_RUNNING"
            )
        }
    }

    private fun startDirection(
        state: SimulationState,
        requested: RunDirection
    ): SimulationState {
        if (state.tripped) return blockedByTrip(state)
        if (state.circuit != CircuitType.REVERSE) return start(state)
        if (state.running && state.direction != requested) {
            return state.copy(
                message = "Intertravamento atuou: pare o motor antes de inverter o sentido.",
                eventCode = "REVERSAL_BLOCKED"
            )
        }
        val contactor = if (requested == RunDirection.FORWARD) {
            Contactor.FORWARD
        } else {
            Contactor.REVERSE
        }
        val label = if (requested == RunDirection.FORWARD) "KM1 frente" else "KM2 reverso"
        return state.copy(
            direction = requested,
            contactors = setOf(contactor),
            message = "$label ativo; contato NF cruzado mantém o outro contator bloqueado.",
            eventCode = if (requested == RunDirection.FORWARD) "FORWARD_RUNNING" else "REVERSE_RUNNING"
        )
    }

    private fun advanceStar(state: SimulationState): SimulationState {
        if (state.circuit != CircuitType.STAR_DELTA || state.tripped) return state
        return when (state.starPhase) {
            StarPhase.STAR -> state.copy(
                starPhase = StarPhase.TRANSITION,
                contactors = setOf(Contactor.MAIN),
                message = "Transição segura: KY abriu; intervalo morto antes de KΔ.",
                eventCode = "STAR_GAP"
            )

            StarPhase.TRANSITION -> state.copy(
                starPhase = StarPhase.DELTA,
                contactors = setOf(Contactor.MAIN, Contactor.DELTA),
                message = "Etapa triângulo: K1 + KΔ ativos. KY permanece bloqueado.",
                eventCode = "DELTA_RUNNING"
            )

            else -> state
        }
    }

    private fun blockedByTrip(state: SimulationState) = state.copy(
        direction = RunDirection.STOPPED,
        contactors = emptySet(),
        message = "Partida bloqueada: reconheça a falha com RESET.",
        eventCode = "START_BLOCKED"
    )
}
