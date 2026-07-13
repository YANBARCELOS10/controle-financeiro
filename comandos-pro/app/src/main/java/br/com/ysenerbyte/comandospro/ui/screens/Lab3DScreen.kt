package br.com.ysenerbyte.comandospro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    var activeIndex by remember { mutableIntStateOf(4) }
    var energized by remember { mutableStateOf(false) }
    var autoRotate by remember { mutableStateOf(true) }
    var assembly by remember { mutableStateOf(emptyList<String>()) }
    var validation by remember { mutableStateOf<String?>(null) }
    var rendererError by remember { mutableStateOf<String?>(null) }
    var panelView by remember { mutableStateOf<Panel3DView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            panelView?.onPause()
            panelView = null
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "OpenGL 3D compatível",
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
                        if (rendererError == null) {
                            AndroidView(
                                factory = { context ->
                                    Panel3DView(context).also { view ->
                                        panelView = view
                                        view.onRendererError = { message ->
                                            view.onPause()
                                            panelView = null
                                            rendererError = message
                                        }
                                    }
                                },
                                update = { view ->
                                    view.setActiveComponent(activeIndex)
                                    view.setEnergized(energized && exploreMode)
                                    view.setVisibleComponents(if (exploreMode) 7 else assembly.size)
                                    view.setAutoRotate(autoRotate)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CompatiblePanel(
                                activeIndex = activeIndex,
                                energized = energized && exploreMode,
                                visibleComponents = if (exploreMode) 7 else assembly.size,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            when {
                                rendererError != null -> "MODO COMPATÍVEL"
                                exploreMode -> "3D AO VIVO"
                                else -> "${assembly.size}/7 COMPONENTES"
                            },
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
                            if (rendererError == null) {
                                "A cena usa perspectiva, profundidade, iluminação direcional e animação em tempo real."
                            } else {
                                "O modo compatível foi ativado automaticamente para manter o laboratório funcionando neste aparelho."
                            },
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

@Composable
private fun CompatiblePanel(
    activeIndex: Int,
    energized: Boolean,
    visibleComponents: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.background(Color(0xFF06111E))) {
        val panelLeft = size.width * 0.06f
        val panelTop = size.height * 0.07f
        val panelWidth = size.width * 0.88f
        val panelHeight = size.height * 0.84f
        drawRect(
            color = Color(0xFF102A3C),
            topLeft = Offset(panelLeft, panelTop),
            size = Size(panelWidth, panelHeight)
        )
        drawRect(
            color = Color(0xFF4C6978),
            topLeft = Offset(panelLeft, panelTop),
            size = Size(panelWidth, panelHeight),
            style = Stroke(width = 3f)
        )

        listOf(0.27f, 0.52f, 0.75f).forEach { fraction ->
            val y = panelTop + panelHeight * fraction
            drawRect(
                color = Color(0xFF80939B),
                topLeft = Offset(panelLeft + panelWidth * 0.05f, y),
                size = Size(panelWidth * 0.90f, 6f)
            )
        }

        val centers = listOf(
            Offset(0.16f, 0.20f),
            Offset(0.34f, 0.20f),
            Offset(0.55f, 0.20f),
            Offset(0.79f, 0.20f),
            Offset(0.29f, 0.45f),
            Offset(0.53f, 0.45f),
            Offset(0.78f, 0.45f)
        )
        val componentColors = listOf(
            Color(0xFFD8DEE1),
            Color(0xFF5D676D),
            Color(0xFF28658A),
            Color(0xFFE8A719),
            Color(0xFF525D62),
            Color(0xFF525D62),
            Color(0xFF375D70)
        )

        repeat(visibleComponents.coerceIn(0, centers.size)) { index ->
            val center = centers[index]
            val width = panelWidth * if (index == 2) 0.22f else 0.13f
            val height = panelHeight * 0.16f
            val x = panelLeft + panelWidth * center.x - width / 2f
            val y = panelTop + panelHeight * center.y - height / 2f
            val activeColor = when {
                energized && index == 4 -> Color(0xFFFF6B2C)
                else -> componentColors[index]
            }
            drawRect(activeColor, Offset(x, y), Size(width, height))
            drawRect(
                color = if (index == activeIndex) Color(0xFFFFC857) else Color(0xFF17232A),
                topLeft = Offset(x, y),
                size = Size(width, height),
                style = Stroke(width = if (index == activeIndex) 7f else 2f)
            )
            repeat(3) { terminal ->
                val terminalX = x + width * (0.25f + terminal * 0.25f)
                drawCircle(Color(0xFFD8B65C), 5f, Offset(terminalX, y + 7f))
                drawCircle(Color(0xFFD8B65C), 5f, Offset(terminalX, y + height - 7f))
            }
        }

        if (visibleComponents >= 5) {
            val motorColor = if (energized) Color(0xFF1FA6D1) else Color(0xFF284F61)
            val motorWidth = panelWidth * 0.28f
            val motorHeight = panelHeight * 0.11f
            val motorX = panelLeft + (panelWidth - motorWidth) / 2f
            val motorY = panelTop + panelHeight * 0.79f
            drawRect(motorColor, Offset(motorX, motorY), Size(motorWidth, motorHeight))
            drawLine(
                color = if (energized) Color(0xFFFF6B2C) else Color(0xFF247395),
                start = Offset(panelLeft + panelWidth * 0.29f, panelTop + panelHeight * 0.53f),
                end = Offset(size.width / 2f, motorY),
                strokeWidth = 7f
            )
        }
    }
}
