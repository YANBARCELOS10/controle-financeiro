package br.com.ysenerbyte.comandospro.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import br.com.ysenerbyte.comandospro.gl.Panel3DView
import br.com.ysenerbyte.comandospro.ui.SafetyBanner
import br.com.ysenerbyte.comandospro.ui.ScreenIntro
import br.com.ysenerbyte.comandospro.ui.theme.AlarmRed
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen

@Composable
fun Lab3DScreen(
    onAward: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val components = remember {
        listOf("QF", "PS1", "CPU", "KSR", "KM1", "KM2", "X")
    }
    val descriptions = remember {
        listOf(
            "Proteção geral",
            "Fonte 24 Vcc",
            "Controlador",
            "Segurança",
            "Contator frente",
            "Contator reverso",
            "Bornes"
        )
    }
    var exploreMode by remember { mutableStateOf(true) }
    var activeIndex by remember { mutableStateOf(4) }
    var energized by remember { mutableStateOf(false) }
    var autoRotate by remember { mutableStateOf(true) }
    var assembly by remember { mutableStateOf(emptyList<String>()) }
    var validation by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "OpenGL ES 3",
                title = "Laboratório de painel 3D",
                description = "Arraste para girar, use dois dedos para ampliar e acompanhe a armadura do contator."
            )
        }
        item { SafetyBanner() }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = exploreMode,
                    onClick = { exploreMode = true; validation = null },
                    label = { Text("Explorar painel") }
                )
                FilterChip(
                    selected = !exploreMode,
                    onClick = { exploreMode = false; energized = false; validation = null },
                    label = { Text("Montagem virtual") }
                )
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(390.dp)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AndroidView(
                            factory = { context -> Panel3DView(context) },
                            update = { view ->
                                view.setActiveComponent(activeIndex)
                                view.setEnergized(energized && exploreMode)
                                view.setVisibleComponents(if (exploreMode) 7 else assembly.size)
                                view.setAutoRotate(autoRotate)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            if (exploreMode) "3D AO VIVO" else "${assembly.size}/7 COMPONENTES",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                                    MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            if (exploreMode) "Selecionado: ${components[activeIndex]} — ${descriptions[activeIndex]}"
                            else "Trilho virtual: ${assembly.joinToString(" → ").ifBlank { "vazio" }}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "A cena usa perspectiva, profundidade, iluminação direcional e animação em tempo real.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (exploreMode) {
            item {
                Text("Selecionar componente", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(components) { index, component ->
                        FilterChip(
                            selected = activeIndex == index,
                            onClick = { activeIndex = index },
                            label = { Text(component) }
                        )
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { energized = !energized; activeIndex = 4 },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (energized) "PARAR DEMO" else "ACIONAR KM1")
                    }
                    OutlinedButton(
                        onClick = { autoRotate = !autoRotate },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (autoRotate) "PAUSAR GIRO" else "GIRO AUTO")
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Leitura 3D", fontWeight = FontWeight.Bold)
                        Text(
                            "O destaque dourado identifica o componente selecionado. No acionamento virtual, KM1 movimenta a armadura, o motor muda de estado e os condutores de demonstração acendem.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Paleta de montagem", fontWeight = FontWeight.Bold)
                        Text(
                            "Organize apenas o modelo virtual. Isso não é um roteiro de ligação real.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(components) { _, item ->
                                FilterChip(
                                    selected = item in assembly,
                                    enabled = assembly.size < components.size && item !in assembly,
                                    onClick = {
                                        assembly = assembly + item
                                        activeIndex = components.indexOf(item)
                                        validation = null
                                    },
                                    label = { Text(item) }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    assembly = assembly.dropLast(1)
                                    validation = null
                                },
                                enabled = assembly.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text("DESFAZER") }
                            OutlinedButton(
                                onClick = { assembly = emptyList(); validation = null },
                                enabled = assembly.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text("LIMPAR") }
                            Button(
                                onClick = {
                                    val correct = assembly == components
                                    validation = if (correct) {
                                        onAward("panel_3d", 120)
                                        "Painel virtual aprovado: zonas e sequência funcional conferidas."
                                    } else {
                                        "Revise a organização funcional: proteção → alimentação → controle → segurança → acionamento → bornes."
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("VALIDAR") }
                        }
                    }
                }
            }
            validation?.let { result ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (assembly == components) {
                                SignalGreen.copy(alpha = 0.13f)
                            } else {
                                AlarmRed.copy(alpha = 0.13f)
                            }
                        )
                    ) {
                        Text(result, Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
