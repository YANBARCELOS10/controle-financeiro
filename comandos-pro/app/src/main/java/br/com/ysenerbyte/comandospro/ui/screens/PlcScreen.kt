package br.com.ysenerbyte.comandospro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ysenerbyte.comandospro.ui.MetricCard
import br.com.ysenerbyte.comandospro.ui.SafetyBanner
import br.com.ysenerbyte.comandospro.ui.ScreenIntro
import br.com.ysenerbyte.comandospro.ui.StatusPill
import br.com.ysenerbyte.comandospro.ui.theme.AlarmRed
import br.com.ysenerbyte.comandospro.ui.theme.ElectricBlue
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen
import kotlinx.coroutines.delay

private val inputTags = listOf(
    "START", "STOP", "RESET", "Automático", "Peça presente", "Cilindro avançado",
    "Cilindro recuado", "Vácuo OK", "Caixa posicionada", "Produto OK",
    "Emergência", "Porta aberta", "Sobrecarga", "Falha do drive"
)

private val outputTags = listOf(
    "Varredor avança", "Varredor retorna", "Cilindro sobe", "Cilindro desce",
    "Dobra esquerda", "Dobra direita", "Vácuo", "Esteira", "Ciclo ativo", "Alarme"
)

private val sequence = listOf(
    setOf(7),
    setOf(0),
    setOf(2, 6),
    setOf(4, 5),
    setOf(1, 3)
)

private val stepNames = listOf(
    "Posicionar caixa", "Avançar varredor", "Elevar com vácuo", "Dobrar abas", "Retornar atuadores"
)

@Composable
fun PlcScreen(
    productionCount: Int,
    onCycleComplete: () -> Unit,
    onAward: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val inputs = remember { mutableStateListOf<Boolean>().apply { repeat(inputTags.size) { add(false) } } }
    val outputs = remember { mutableStateListOf<Boolean>().apply { repeat(outputTags.size) { add(false) } } }
    var running by remember { mutableStateOf(false) }
    var step by remember { mutableIntStateOf(0) }
    var log by remember { mutableStateOf("CPU em RUN • ciclo virtual pronto") }
    val safetyBlocked = inputs[10] || inputs[11] || inputs[12]

    fun clearOutputs() {
        outputs.indices.forEach { outputs[it] = false }
        outputs[9] = safetyBlocked
    }

    LaunchedEffect(safetyBlocked) {
        if (safetyBlocked) {
            running = false
            clearOutputs()
            log = "Sequência bloqueada por condição de segurança virtual."
        } else {
            outputs[9] = false
        }
    }

    LaunchedEffect(running, safetyBlocked) {
        if (!running || safetyBlocked) return@LaunchedEffect
        while (running && !safetyBlocked) {
            clearOutputs()
            sequence[step].forEach { outputs[it] = true }
            outputs[8] = true
            log = "Etapa ${step + 1}: ${stepNames[step]}"
            delay(1_250)
            step = (step + 1) % sequence.size
            if (step == 0) {
                onCycleComplete()
                onAward("plc_automatic", 90)
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "Ciclo de varredura",
                title = "CLP e IHM virtual",
                description = "Altere entradas, acompanhe a lógica e visualize as saídas da sequência automática."
            )
        }
        item { SafetyBanner() }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07131C))
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("IHM • LINHA DIDÁTICA", color = ElectricBlue, fontWeight = FontWeight.Bold)
                            Text(
                                if (safetyBlocked) "MODO BLOQUEADO" else if (running) "MODO AUTOMÁTICO" else "MODO PARADO",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (safetyBlocked) AlarmRed else if (running) SignalGreen else Color.White
                            )
                        }
                        StatusPill(if (safetyBlocked) "SAFETY" else "CPU RUN", !safetyBlocked)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        log,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFBCE8FF)
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            value = if (running) (step + 1).toString() else "0",
                            label = "Etapa",
                            modifier = Modifier.weight(1f),
                            accent = ElectricBlue
                        )
                        MetricCard(
                            value = productionCount.toString(),
                            label = "Ciclos",
                            modifier = Modifier.weight(1f),
                            accent = SignalGreen
                        )
                        MetricCard(
                            value = outputs.count { it }.toString(),
                            label = "Saídas ON",
                            modifier = Modifier.weight(1f),
                            accent = ElectricBlue
                        )
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!safetyBlocked) {
                            running = true
                            inputs[3] = true
                        }
                    },
                    enabled = !running && !safetyBlocked,
                    modifier = Modifier.weight(1f)
                ) { Text("INICIAR AUTO") }
                OutlinedButton(
                    onClick = {
                        running = false
                        inputs[3] = false
                        clearOutputs()
                        log = "Parada normal solicitada."
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("PARAR") }
                OutlinedButton(
                    onClick = {
                        running = false
                        step = 0
                        inputs[10] = false
                        inputs[11] = false
                        inputs[12] = false
                        clearOutputs()
                        log = "Reset concluído; permissivos virtuais restabelecidos."
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("RESET") }
            }
        }
        item {
            Text("Entradas digitais", style = MaterialTheme.typography.titleLarge)
            Text(
                "Toque em uma tag para alternar seu estado virtual.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Card {
                Column(
                    Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    inputTags.forEachIndexed { index, tag ->
                        FilterChip(
                            selected = inputs[index],
                            onClick = {
                                inputs[index] = !inputs[index]
                                log = "I${address(index)} • $tag = ${if (inputs[index]) "1" else "0"}"
                            },
                            label = {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("I${address(index)}  $tag")
                                    Text(if (inputs[index]) "ON" else "OFF")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        item {
            Text("Saídas digitais", style = MaterialTheme.typography.titleLarge)
        }
        item {
            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    outputTags.forEachIndexed { index, tag ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.foundation.layout.Box(
                                    Modifier
                                        .size(12.dp)
                                        .background(
                                            if (outputs[index]) SignalGreen else MaterialTheme.colorScheme.outline,
                                            androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                                Text("Q${address(index)}  $tag")
                            }
                            Text(
                                if (outputs[index]) "ON" else "OFF",
                                color = if (outputs[index]) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (safetyBlocked) AlarmRed.copy(alpha = 0.13f) else SignalGreen.copy(alpha = 0.09f)
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (safetyBlocked) "Intertravamento atuado" else "Permissivos de segurança OK",
                        fontWeight = FontWeight.Bold,
                        color = if (safetyBlocked) AlarmRed else SignalGreen
                    )
                    Text(
                        if (safetyBlocked) {
                            "A sequência foi interrompida. Remova a condição virtual e execute RESET."
                        } else {
                            "Emergência, porta e relé térmico virtuais estão em condição normal."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun address(index: Int): String = if (index < 8) "0.$index" else "1.${index - 8}"
