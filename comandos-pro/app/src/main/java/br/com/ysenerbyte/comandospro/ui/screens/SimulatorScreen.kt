package br.com.ysenerbyte.comandospro.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ysenerbyte.comandospro.core.CircuitType
import br.com.ysenerbyte.comandospro.core.Contactor
import br.com.ysenerbyte.comandospro.core.RunDirection
import br.com.ysenerbyte.comandospro.core.SimulationAction
import br.com.ysenerbyte.comandospro.core.SimulationState
import br.com.ysenerbyte.comandospro.core.SimulatorEngine
import br.com.ysenerbyte.comandospro.core.StarPhase
import br.com.ysenerbyte.comandospro.ui.MetricCard
import br.com.ysenerbyte.comandospro.ui.SafetyBanner
import br.com.ysenerbyte.comandospro.ui.ScreenIntro
import br.com.ysenerbyte.comandospro.ui.StatusPill
import br.com.ysenerbyte.comandospro.ui.theme.AlarmRed
import br.com.ysenerbyte.comandospro.ui.theme.ElectricBlue
import br.com.ysenerbyte.comandospro.ui.theme.SafetyAmber
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen
import kotlinx.coroutines.delay

@Composable
fun SimulatorScreen(
    onAward: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(SimulationState()) }

    fun dispatch(action: SimulationAction) {
        state = SimulatorEngine.reduce(state, action)
    }

    LaunchedEffect(state.starPhase) {
        when (state.starPhase) {
            StarPhase.STAR -> {
                delay(2_600)
                dispatch(SimulationAction.AdvanceStarTransition)
            }
            StarPhase.TRANSITION -> {
                delay(450)
                dispatch(SimulationAction.AdvanceStarTransition)
            }
            else -> Unit
        }
    }

    LaunchedEffect(state.eventCode) {
        when (state.eventCode) {
            "DIRECT_RUNNING" -> onAward("sim_direct", 45)
            "FORWARD_RUNNING", "REVERSE_RUNNING" -> onAward("sim_reverse", 55)
            "DELTA_RUNNING" -> onAward("sim_star_delta", 65)
            "VFD_RUNNING" -> onAward("sim_vfd", 55)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "Motor lógico validado",
                title = "Simulador de partidas",
                description = "Veja estados, intertravamentos, transições e telemetria em tempo real."
            )
        }
        item { SafetyBanner() }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CircuitType.entries) { circuit ->
                    FilterChip(
                        selected = state.circuit == circuit,
                        onClick = { dispatch(SimulationAction.SelectCircuit(circuit)) },
                        label = { Text(circuit.shortTitle) }
                    )
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(state.circuit.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                if (state.tripped) "PROTEÇÃO ATUADA" else if (state.running) "EM MOVIMENTO" else "PRONTO",
                                color = if (state.tripped) AlarmRed else if (state.running) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        MotorRotor(state)
                    }
                    Spacer(Modifier.height(12.dp))
                    PowerSchematic(state)
                    Spacer(Modifier.height(12.dp))
                    ContactorStates(state)
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    state.estimatedRpm.toString(),
                    "RPM estimada",
                    Modifier.weight(1f),
                    ElectricBlue
                )
                MetricCard(
                    if (state.circuit == CircuitType.VFD) "${state.frequencyHz.toInt()} Hz" else if (state.running) "60 Hz" else "0 Hz",
                    "Referência",
                    Modifier.weight(1f),
                    SafetyAmber
                )
                MetricCard(
                    phaseLabel(state),
                    "Etapa",
                    Modifier.weight(1f),
                    SignalGreen
                )
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        state.tripped -> AlarmRed.copy(alpha = 0.13f)
                        state.running -> SignalGreen.copy(alpha = 0.11f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Estado do comando", fontWeight = FontWeight.Bold)
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    if (state.hasUnsafeOverlap) {
                        Text("Falha interna detectada: estado incompatível.", color = AlarmRed)
                    }
                }
            }
        }
        if (state.circuit == CircuitType.VFD) {
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Referência de velocidade", fontWeight = FontWeight.Bold)
                        Text("${state.frequencyHz.toInt()} Hz • ${state.estimatedRpm} RPM estimada")
                        Slider(
                            value = state.frequencyHz,
                            onValueChange = { dispatch(SimulationAction.SetFrequency(it)) },
                            valueRange = 0f..60f,
                            steps = 11
                        )
                    }
                }
            }
        }
        item { SimulatorControls(state, ::dispatch) }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Lógica funcional", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        conceptualLogic(state.circuit),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorControls(
    state: SimulationState,
    dispatch: (SimulationAction) -> Unit
) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Comandos virtuais", fontWeight = FontWeight.Bold)
            when (state.circuit) {
                CircuitType.REVERSE -> Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { dispatch(SimulationAction.StartForward) },
                        modifier = Modifier.weight(1f)
                    ) { Text("FRENTE") }
                    Button(
                        onClick = { dispatch(SimulationAction.StartReverse) },
                        modifier = Modifier.weight(1f)
                    ) { Text("REVERSO") }
                }
                CircuitType.VFD -> Button(
                    onClick = { dispatch(SimulationAction.Start) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("RUN") }
                else -> Button(
                    onClick = { dispatch(SimulationAction.Start) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("START") }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { dispatch(SimulationAction.Stop) },
                    modifier = Modifier.weight(1f)
                ) { Text("STOP") }
                OutlinedButton(
                    onClick = { dispatch(SimulationAction.Trip) },
                    modifier = Modifier.weight(1f)
                ) { Text("SIMULAR FT") }
                OutlinedButton(
                    onClick = { dispatch(SimulationAction.Reset) },
                    modifier = Modifier.weight(1f)
                ) { Text("RESET") }
            }
        }
    }
}

@Composable
private fun MotorRotor(state: SimulationState) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val animation = rememberInfiniteTransition(label = "motor")
    val angle by animation.animateFloat(
        initialValue = 0f,
        targetValue = if (state.direction == RunDirection.REVERSE) -360f else 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state.circuit == CircuitType.VFD) {
                    (2_600 - state.frequencyHz * 30).toInt().coerceAtLeast(550)
                } else {
                    850
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotorAngle"
    )
    Canvas(
        Modifier
            .size(64.dp)
            .background(surfaceColor, CircleShape)
    ) {
        drawCircle(
            color = if (state.running) ElectricBlue else outlineColor,
            radius = size.minDimension * 0.39f,
            style = Stroke(width = 5.dp.toPx())
        )
        rotate(if (state.running) angle else 0f) {
            repeat(3) { index ->
                rotate(index * 120f) {
                    drawLine(
                        color = if (state.running) SignalGreen else outlineColor,
                        start = center,
                        end = Offset(center.x, size.height * 0.16f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerSchematic(state: SimulationState) {
    val activeColor = if (state.tripped) AlarmRed else ElectricBlue
    val idleColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.58f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val y = size.height * 0.50f
        val left = size.width * 0.06f
        val right = size.width * 0.94f
        drawLine(idleColor, Offset(left, y), Offset(right, y), 4.dp.toPx(), cap = StrokeCap.Round)

        val blocks = when (state.circuit) {
            CircuitType.DIRECT -> listOf(Contactor.MAIN)
            CircuitType.REVERSE -> listOf(Contactor.FORWARD, Contactor.REVERSE)
            CircuitType.STAR_DELTA -> listOf(Contactor.MAIN, Contactor.STAR, Contactor.DELTA)
            CircuitType.VFD -> listOf(Contactor.DRIVE)
        }
        blocks.forEachIndexed { index, contactor ->
            val centerX = left + (index + 1) * (right - left) / (blocks.size + 1)
            val isOn = contactor in state.contactors
            drawRoundRect(
                color = if (isOn) activeColor.copy(alpha = 0.26f) else surfaceColor,
                topLeft = Offset(centerX - 30.dp.toPx(), y - 34.dp.toPx()),
                size = Size(60.dp.toPx(), 68.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx())
            )
            drawRoundRect(
                color = if (isOn) activeColor else idleColor,
                topLeft = Offset(centerX - 30.dp.toPx(), y - 34.dp.toPx()),
                size = Size(60.dp.toPx(), 68.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = if (isOn) SignalGreen else idleColor,
                radius = 7.dp.toPx(),
                center = Offset(centerX, y)
            )
        }
        drawCircle(
            color = if (state.running) SignalGreen.copy(alpha = 0.22f) else surfaceColor,
            radius = 27.dp.toPx(),
            center = Offset(right, y),
            style = Stroke(4.dp.toPx())
        )
        drawLine(
            color = if (state.running) SignalGreen else idleColor,
            start = Offset(right - 13.dp.toPx(), y),
            end = Offset(right + 13.dp.toPx(), y),
            strokeWidth = 3.dp.toPx()
        )
    }
}

@Composable
private fun ContactorStates(state: SimulationState) {
    val contacts = when (state.circuit) {
        CircuitType.DIRECT -> listOf("K1" to Contactor.MAIN)
        CircuitType.REVERSE -> listOf("KM1" to Contactor.FORWARD, "KM2" to Contactor.REVERSE)
        CircuitType.STAR_DELTA -> listOf(
            "K1" to Contactor.MAIN,
            "KY" to Contactor.STAR,
            "KΔ" to Contactor.DELTA
        )
        CircuitType.VFD -> listOf("RUN" to Contactor.DRIVE)
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(contacts) { (label, contactor) ->
            StatusPill(label, contactor in state.contactors)
        }
    }
}

private fun phaseLabel(state: SimulationState): String = when {
    state.tripped -> "FT"
    state.circuit == CircuitType.STAR_DELTA -> when (state.starPhase) {
        StarPhase.IDLE -> "Pronto"
        StarPhase.STAR -> "Estrela"
        StarPhase.TRANSITION -> "Intervalo"
        StarPhase.DELTA -> "Triângulo"
    }
    state.direction == RunDirection.FORWARD -> "Frente"
    state.direction == RunDirection.REVERSE -> "Reverso"
    else -> "Parado"
}

private fun conceptualLogic(circuit: CircuitType): String = when (circuit) {
    CircuitType.DIRECT -> "STOP + FT + (START OU SELO K1) → K1\nK1 → MOTOR VIRTUAL"
    CircuitType.REVERSE -> "FRENTE + NÃO KM2 → KM1\nREVERSO + NÃO KM1 → KM2"
    CircuitType.STAR_DELTA -> "START → K1 + KY\nTEMPO → ABRE KY → INTERVALO → KΔ"
    CircuitType.VFD -> "PERMISSIVOS + RUN → DRIVE\nREFERÊNCIA 0–60 Hz → RPM ESTIMADA"
}
