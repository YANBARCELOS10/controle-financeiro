package br.com.ysenerbyte.comandospro.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.ysenerbyte.comandospro.core.ComponentInfo
import br.com.ysenerbyte.comandospro.core.LaboratoryEngine
import br.com.ysenerbyte.comandospro.data.CatalogData
import br.com.ysenerbyte.comandospro.ui.SafetyBanner
import br.com.ysenerbyte.comandospro.ui.ScreenIntro
import br.com.ysenerbyte.comandospro.ui.theme.AlarmRed
import br.com.ysenerbyte.comandospro.ui.theme.ElectricBlue
import br.com.ysenerbyte.comandospro.ui.theme.SafetyAmber
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen

private enum class LabSection(val label: String) {
    COMPONENTS("Componentes"),
    BENCH("Bancada virtual"),
    DIAGNOSTICS("Diagnóstico"),
    LEARNING("Aprendizado")
}

private enum class DeviceVisual {
    BREAKER, FUSE, POWER_SUPPLY, BUTTON, CONTACTOR, RELAY, PLC, SENSOR,
    LIMIT_SWITCH, HMI, DRIVE, MOTOR, VALVE, TERMINAL, TRANSFORMER, SIGNAL, CONTROLLER
}

private data class BenchBlock(
    val id: String,
    val label: String,
    val explanation: String
)

private data class DiagnosticScenario(
    val id: String,
    val title: String,
    val symptom: String,
    val observations: List<String>,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

private data class LearningTopic(
    val id: String,
    val title: String,
    val summary: String,
    val points: List<String>
)

@Composable
fun LaboratoryScreen(
    onAward: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val components = remember { CatalogData.components }
    val categories = remember(components) { listOf("Todos") + components.map { it.category }.distinct() }
    var section by remember { mutableStateOf(LabSection.COMPONENTS) }
    var category by remember { mutableStateOf("Todos") }
    var selectedName by remember { mutableStateOf("Contator de potência") }
    var benchAssembly by remember { mutableStateOf(emptyList<String>()) }
    var benchResult by remember { mutableStateOf<String?>(null) }
    var diagnosticAnswers by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var openDiagnostic by remember { mutableStateOf<String?>(null) }
    var openTopic by remember { mutableStateOf<String?>(null) }

    val filteredComponents = remember(components, category) {
        if (category == "Todos") components else components.filter { it.category == category }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "Laboratório didático 2D",
                title = "Conheça, organize e diagnostique",
                description = "Ilustrações técnicas, fichas completas, bancada funcional e exercícios guiados em uma tela estável e leve."
            )
        }
        item { SafetyBanner() }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Como estudar nesta tela", fontWeight = FontWeight.Bold)
                    Text(
                        "1. Reconheça o dispositivo pela imagem e tag.  2. Leia função e terminais.  3. Monte o fluxo virtual.  4. Resolva falhas por evidências.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Todo conteúdo é conceitual e virtual. Não execute ligações nem medições em circuitos energizados.",
                        color = SafetyAmber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LabSection.entries) { item ->
                    FilterChip(
                        selected = section == item,
                        onClick = { section = item },
                        label = { Text(item.label) }
                    )
                }
            }
        }

        when (section) {
            LabSection.COMPONENTS -> {
                item {
                    Text("Catálogo ilustrado", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${components.size} dispositivos organizados por proteção, comando, automação, sensores e acionamento.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { item ->
                            FilterChip(
                                selected = category == item,
                                onClick = { category = item },
                                label = { Text(item) }
                            )
                        }
                    }
                }
                items(filteredComponents, key = { it.name }) { component ->
                    ComponentStudyCard(
                        component = component,
                        expanded = selectedName == component.name,
                        onToggle = {
                            selectedName = if (selectedName == component.name) "" else component.name
                        },
                        onStudied = { onAward("lab_component_${component.tag}_${component.name}", 8) }
                    )
                }
            }

            LabSection.BENCH -> {
                item {
                    BenchSection(
                        assembly = benchAssembly,
                        result = benchResult,
                        onAdd = { id ->
                            if (id !in benchAssembly) {
                                benchAssembly = benchAssembly + id
                                benchResult = null
                            }
                        },
                        onUndo = {
                            benchAssembly = benchAssembly.dropLast(1)
                            benchResult = null
                        },
                        onClear = {
                            benchAssembly = emptyList()
                            benchResult = null
                        },
                        onValidate = {
                            val correct = LaboratoryEngine.isBenchFlowValid(benchAssembly)
                            benchResult = if (correct) {
                                onAward("lab_bench_flow", 140)
                                "Fluxo aprovado. Você organizou proteção, alimentação, comando, segurança, lógica, interface, acionamento e carga."
                            } else {
                                "Ainda não está na ordem funcional. Use as dicas dos blocos e pense no caminho da energia e do comando virtual."
                            }
                        }
                    )
                }
            }

            LabSection.DIAGNOSTICS -> {
                item {
                    Text("Diagnóstico por evidências", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Comece pelo sintoma, acompanhe a cadeia funcional e escolha a primeira análise coerente.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(DIAGNOSTICS, key = { it.id }) { scenario ->
                    DiagnosticCard(
                        scenario = scenario,
                        expanded = openDiagnostic == scenario.id,
                        selectedAnswer = diagnosticAnswers[scenario.id],
                        onToggle = {
                            openDiagnostic = if (openDiagnostic == scenario.id) null else scenario.id
                        },
                        onAnswer = { answer ->
                            diagnosticAnswers = diagnosticAnswers + (scenario.id to answer)
                            if (answer == scenario.correctIndex) {
                                onAward("lab_diagnostic_${scenario.id}", 35)
                            }
                        }
                    )
                }
            }

            LabSection.LEARNING -> {
                item {
                    Text("Trilha de aprendizado", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Oito aulas rápidas para ligar o nome do componente ao funcionamento do sistema.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(LEARNING_TOPICS, key = { it.id }) { topic ->
                    LearningCard(
                        topic = topic,
                        expanded = openTopic == topic.id,
                        onToggle = { openTopic = if (openTopic == topic.id) null else topic.id },
                        onStudied = { onAward("lab_lesson_${topic.id}", 25) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentStudyCard(
    component: ComponentInfo,
    expanded: Boolean,
    onToggle: () -> Unit,
    onStudied: () -> Unit
) {
    ElevatedCard(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceIllustration(component, Modifier.size(106.dp))
                Column(Modifier.weight(1f)) {
                    Text(component.tag, color = ElectricBlue, fontWeight = FontWeight.Black)
                    Text(component.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(component.category, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (expanded) "Toque para fechar" else "Toque para abrir a ficha completa",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.height(14.dp))
                InfoBlock("Como reconhecer", recognitionText(component))
                InfoBlock("Função", component.function)
                InfoBlock("Terminais e referências", component.terminals)
                InfoBlock("Onde é aplicado", component.application)
                InfoBlock("Observação no laboratório", component.virtualCheck)
                InfoBlock("Falhas comuns", component.commonFaults, AlarmRed)
                InfoBlock("Cuidado", component.safety, SafetyAmber)
                Button(onClick = onStudied, modifier = Modifier.fillMaxWidth()) {
                    Text("MARCAR COMO ESTUDADO")
                }
            }
        }
    }
}

@Composable
private fun InfoBlock(title: String, text: String, accent: Color = ElectricBlue) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title.uppercase(), color = accent, style = MaterialTheme.typography.labelLarge)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BenchSection(
    assembly: List<String>,
    result: String?,
    onAdd: (String) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onValidate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Bancada funcional virtual", style = MaterialTheme.typography.titleLarge)
        Text(
            "Organize os blocos de uma partida supervisionada. A bancada ensina fluxo funcional; não mostra ligação física nem tensão de rede.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Seu fluxo", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (assembly.isEmpty()) {
                    Text("Nenhum bloco adicionado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    assembly.forEachIndexed { index, id ->
                        val block = BENCH_FLOW.first { it.id == id }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                            Text("  ${block.label}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Text("Blocos disponíveis", fontWeight = FontWeight.Bold)
        BENCH_CHOICES.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { id ->
                    val block = BENCH_FLOW.first { it.id == id }
                    OutlinedButton(
                        onClick = { onAdd(id) },
                        enabled = id !in assembly,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(id, color = ElectricBlue, fontWeight = FontWeight.Black)
                            Text(block.label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(14.dp)) {
                Text("Dicas dos blocos", fontWeight = FontWeight.Bold)
                BENCH_FLOW.forEach { block ->
                    Text("• ${block.id}: ${block.explanation}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onUndo, enabled = assembly.isNotEmpty(), modifier = Modifier.weight(1f)) {
                Text("DESFAZER")
            }
            OutlinedButton(onClick = onClear, enabled = assembly.isNotEmpty(), modifier = Modifier.weight(1f)) {
                Text("LIMPAR")
            }
            Button(onClick = onValidate, enabled = assembly.size == BENCH_FLOW.size, modifier = Modifier.weight(1f)) {
                Text("VALIDAR")
            }
        }
        result?.let { message ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (LaboratoryEngine.isBenchFlowValid(assembly)) {
                        SignalGreen.copy(alpha = 0.15f)
                    } else {
                        AlarmRed.copy(alpha = 0.15f)
                    }
                )
            ) {
                Text(message, Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DiagnosticCard(
    scenario: DiagnosticScenario,
    expanded: Boolean,
    selectedAnswer: Int?,
    onToggle: () -> Unit,
    onAnswer: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("⚠ ${scenario.title}", fontWeight = FontWeight.Bold)
            Text(scenario.symptom, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
                Text(if (expanded) "FECHAR ANÁLISE" else "ABRIR ANÁLISE")
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Text("Evidências disponíveis", color = ElectricBlue, fontWeight = FontWeight.Bold)
                scenario.observations.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(10.dp))
                Text("Qual é a primeira análise coerente?", fontWeight = FontWeight.Bold)
                scenario.options.forEachIndexed { index, option ->
                    OutlinedButton(
                        onClick = { onAnswer(index) },
                        enabled = selectedAnswer == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp)
                    ) {
                        Text(option)
                    }
                }
                selectedAnswer?.let { answer ->
                    Card(
                        modifier = Modifier.padding(top = 10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (answer == scenario.correctIndex) {
                                SignalGreen.copy(alpha = 0.16f)
                            } else {
                                AlarmRed.copy(alpha = 0.16f)
                            }
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (answer == scenario.correctIndex) "Resposta correta" else "Resposta a revisar",
                                fontWeight = FontWeight.Bold
                            )
                            Text(scenario.explanation, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningCard(
    topic: LearningTopic,
    expanded: Boolean,
    onToggle: () -> Unit,
    onStudied: () -> Unit
) {
    ElevatedCard(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(topic.title, fontWeight = FontWeight.Bold)
            Text(topic.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                topic.points.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(10.dp))
                Button(onClick = onStudied, modifier = Modifier.fillMaxWidth()) {
                    Text("CONCLUIR AULA")
                }
            } else {
                Text("Toque para estudar", color = ElectricBlue, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DeviceIllustration(component: ComponentInfo, modifier: Modifier = Modifier) {
    val visual = remember(component.name) { visualFor(component) }
    Box(
        modifier = modifier.background(Color(0xFF071521), MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .padding(9.dp)
        ) {
            drawDevice(visual)
        }
        Text(
            component.tag.substringBefore(" /"),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp)
                .background(Color(0xD9112430), MaterialTheme.shapes.extraSmall)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black
        )
    }
}

private fun visualFor(component: ComponentInfo): DeviceVisual {
    val name = component.name.lowercase()
    return when {
        "disjuntor" in name -> DeviceVisual.BREAKER
        "fusível" in name -> DeviceVisual.FUSE
        "fonte" in name -> DeviceVisual.POWER_SUPPLY
        "botão" in name -> DeviceVisual.BUTTON
        "contator" in name -> DeviceVisual.CONTACTOR
        "relé" in name || "temporizador" in name -> DeviceVisual.RELAY
        "clp" in name || "módulo" in name -> DeviceVisual.PLC
        "sensor" in name -> DeviceVisual.SENSOR
        "fim de curso" in name -> DeviceVisual.LIMIT_SWITCH
        "ihm" in name -> DeviceVisual.HMI
        "inversor" in name || "soft-starter" in name || "servo" in name -> DeviceVisual.DRIVE
        "motor trifásico" in name -> DeviceVisual.MOTOR
        "válvula" in name -> DeviceVisual.VALVE
        "borne" in name -> DeviceVisual.TERMINAL
        "transformador" in name -> DeviceVisual.TRANSFORMER
        "sinaleiro" in name -> DeviceVisual.SIGNAL
        else -> DeviceVisual.CONTROLLER
    }
}

private fun DrawScope.drawDevice(type: DeviceVisual) {
    val w = size.width
    val h = size.height
    val body = Color(0xFF60717A)
    val dark = Color(0xFF17252D)
    val metal = Color(0xFFC6B16A)
    val screen = Color(0xFF35B9FF)

    drawRoundRect(
        color = Color(0xFF0F2B3D),
        topLeft = Offset(w * 0.04f, h * 0.04f),
        size = Size(w * 0.92f, h * 0.84f),
        cornerRadius = CornerRadius(w * 0.10f),
        style = Stroke(width = w * 0.025f)
    )

    when (type) {
        DeviceVisual.BREAKER -> {
            drawRoundRect(Color(0xFFE4E7E8), Offset(w * .25f, h * .12f), Size(w * .5f, h * .66f), CornerRadius(w * .05f))
            drawRoundRect(dark, Offset(w * .40f, h * .28f), Size(w * .20f, h * .28f), CornerRadius(w * .03f))
            drawLine(SignalGreen, Offset(w * .44f, h * .34f), Offset(w * .56f, h * .34f), w * .035f)
            terminalDots(w, h, metal)
        }
        DeviceVisual.FUSE -> {
            drawRoundRect(body, Offset(w * .20f, h * .18f), Size(w * .60f, h * .50f), CornerRadius(w * .06f))
            drawRect(Color(0xFFE9ECEE), Offset(w * .34f, h * .22f), Size(w * .32f, h * .42f))
            drawCircle(AlarmRed, w * .06f, Offset(w * .5f, h * .43f))
            terminalDots(w, h, metal)
        }
        DeviceVisual.POWER_SUPPLY -> {
            drawRoundRect(Color(0xFF535E64), Offset(w * .16f, h * .12f), Size(w * .68f, h * .64f), CornerRadius(w * .05f))
            drawRoundRect(dark, Offset(w * .27f, h * .24f), Size(w * .46f, h * .22f), CornerRadius(w * .02f))
            drawCircle(SignalGreen, w * .04f, Offset(w * .34f, h * .35f))
            repeat(4) { i -> drawLine(Color(0xFF82939B), Offset(w * (.25f + i * .17f), h * .55f), Offset(w * (.25f + i * .17f), h * .68f), w * .025f) }
        }
        DeviceVisual.BUTTON -> {
            drawCircle(body, w * .30f, Offset(w * .5f, h * .43f))
            drawCircle(SignalGreen, w * .19f, Offset(w * .5f, h * .40f))
            drawCircle(Color.White.copy(alpha = .5f), w * .05f, Offset(w * .44f, h * .34f))
        }
        DeviceVisual.CONTACTOR -> {
            drawRoundRect(Color(0xFF535E64), Offset(w * .18f, h * .10f), Size(w * .64f, h * .69f), CornerRadius(w * .05f))
            drawRoundRect(dark, Offset(w * .30f, h * .27f), Size(w * .40f, h * .26f), CornerRadius(w * .03f))
            drawRect(Color(0xFFFF6B2C), Offset(w * .39f, h * .33f), Size(w * .22f, h * .12f))
            terminalDots(w, h, metal)
        }
        DeviceVisual.RELAY -> {
            drawRoundRect(Color(0xFFE8A719), Offset(w * .27f, h * .10f), Size(w * .46f, h * .68f), CornerRadius(w * .04f))
            drawRoundRect(dark, Offset(w * .34f, h * .25f), Size(w * .32f, h * .22f), CornerRadius(w * .02f))
            drawCircle(SignalGreen, w * .035f, Offset(w * .42f, h * .36f))
            terminalDots(w, h, metal)
        }
        DeviceVisual.PLC -> {
            drawRoundRect(Color(0xFF2B668A), Offset(w * .10f, h * .18f), Size(w * .80f, h * .48f), CornerRadius(w * .04f))
            drawRect(dark, Offset(w * .17f, h * .25f), Size(w * .23f, h * .30f))
            repeat(6) { i -> drawCircle(if (i < 3) SignalGreen else Color(0xFF34464F), w * .026f, Offset(w * (.51f + i * .06f), h * .31f)) }
            repeat(6) { i -> drawCircle(Color(0xFFB99E57), w * .025f, Offset(w * (.51f + i * .06f), h * .52f)) }
        }
        DeviceVisual.SENSOR -> {
            drawRoundRect(Color(0xFF35515F), Offset(w * .16f, h * .30f), Size(w * .60f, h * .25f), CornerRadius(h * .12f))
            drawCircle(Color(0xFFBFC9CE), h * .12f, Offset(w * .70f, h * .425f))
            drawCircle(SignalGreen, w * .035f, Offset(w * .32f, h * .425f))
            drawLine(Color(0xFF247395), Offset(w * .16f, h * .425f), Offset(w * .05f, h * .66f), w * .04f, StrokeCap.Round)
        }
        DeviceVisual.LIMIT_SWITCH -> {
            drawRoundRect(body, Offset(w * .25f, h * .34f), Size(w * .50f, h * .35f), CornerRadius(w * .04f))
            drawLine(Color(0xFFCAD1D4), Offset(w * .45f, h * .34f), Offset(w * .62f, h * .12f), w * .055f, StrokeCap.Round)
            drawCircle(Color(0xFFE4E7E8), w * .075f, Offset(w * .65f, h * .10f))
        }
        DeviceVisual.HMI -> {
            drawRoundRect(Color(0xFF26343B), Offset(w * .10f, h * .18f), Size(w * .80f, h * .52f), CornerRadius(w * .05f))
            drawRoundRect(screen, Offset(w * .18f, h * .25f), Size(w * .64f, h * .36f), CornerRadius(w * .025f))
            drawLine(Color.White.copy(alpha = .8f), Offset(w * .25f, h * .35f), Offset(w * .68f, h * .35f), w * .02f)
            drawLine(SignalGreen, Offset(w * .25f, h * .48f), Offset(w * .56f, h * .48f), w * .025f)
        }
        DeviceVisual.DRIVE -> {
            drawRoundRect(Color(0xFF434E54), Offset(w * .20f, h * .08f), Size(w * .60f, h * .70f), CornerRadius(w * .05f))
            drawRoundRect(dark, Offset(w * .31f, h * .20f), Size(w * .38f, h * .22f), CornerRadius(w * .025f))
            drawRect(screen, Offset(w * .37f, h * .25f), Size(w * .26f, h * .10f))
            repeat(3) { i -> drawCircle(Color(0xFFB6C0C4), w * .045f, Offset(w * (.37f + i * .13f), h * .55f)) }
        }
        DeviceVisual.MOTOR -> {
            drawRoundRect(Color(0xFF2C718E), Offset(w * .20f, h * .28f), Size(w * .58f, h * .32f), CornerRadius(h * .12f))
            repeat(5) { i -> drawLine(Color(0xFF1C536A), Offset(w * (.28f + i * .09f), h * .30f), Offset(w * (.28f + i * .09f), h * .58f), w * .018f) }
            drawRect(Color(0xFFADB8BD), Offset(w * .78f, h * .39f), Size(w * .15f, h * .10f))
            drawRect(Color(0xFF45565E), Offset(w * .12f, h * .34f), Size(w * .10f, h * .20f))
        }
        DeviceVisual.VALVE -> {
            drawRoundRect(Color(0xFF334851), Offset(w * .18f, h * .30f), Size(w * .64f, h * .30f), CornerRadius(w * .04f))
            drawRect(Color(0xFF276A88), Offset(w * .24f, h * .20f), Size(w * .22f, h * .18f))
            drawLine(SignalGreen, Offset(w * .50f, h * .36f), Offset(w * .68f, h * .53f), w * .035f)
            drawLine(SignalGreen, Offset(w * .68f, h * .36f), Offset(w * .50f, h * .53f), w * .035f)
        }
        DeviceVisual.TERMINAL -> {
            repeat(5) { i ->
                val x = w * (.13f + i * .16f)
                drawRoundRect(if (i == 4) SignalGreen else Color(0xFFD7D2B9), Offset(x, h * .27f), Size(w * .13f, h * .34f), CornerRadius(w * .02f))
                drawCircle(Color(0xFF75683D), w * .028f, Offset(x + w * .065f, h * .37f))
            }
        }
        DeviceVisual.TRANSFORMER -> {
            drawRect(Color(0xFF56636A), Offset(w * .18f, h * .22f), Size(w * .64f, h * .43f))
            repeat(4) { i ->
                drawCircle(Color(0xFFB06E32), w * .08f, Offset(w * (.34f + (i % 2) * .32f), h * (.34f + (i / 2) * .18f)), style = Stroke(w * .025f))
            }
        }
        DeviceVisual.SIGNAL -> {
            drawRoundRect(body, Offset(w * .37f, h * .55f), Size(w * .26f, h * .16f), CornerRadius(w * .04f))
            drawCircle(AlarmRed, w * .20f, Offset(w * .5f, h * .38f))
            drawCircle(Color.White.copy(alpha = .5f), w * .055f, Offset(w * .44f, h * .31f))
        }
        DeviceVisual.CONTROLLER -> {
            drawRoundRect(Color(0xFF344A55), Offset(w * .20f, h * .12f), Size(w * .60f, h * .64f), CornerRadius(w * .05f))
            drawRoundRect(dark, Offset(w * .29f, h * .22f), Size(w * .42f, h * .23f), CornerRadius(w * .02f))
            drawRect(screen, Offset(w * .35f, h * .28f), Size(w * .30f, h * .10f))
            repeat(4) { i -> drawCircle(Color(0xFFCBD3D6), w * .035f, Offset(w * (.35f + i * .10f), h * .57f)) }
        }
    }
}

private fun DrawScope.terminalDots(w: Float, h: Float, color: Color) {
    repeat(3) { i ->
        val x = w * (.34f + i * .16f)
        drawCircle(color, w * .035f, Offset(x, h * .14f))
        drawCircle(color, w * .035f, Offset(x, h * .73f))
    }
}

private fun recognitionText(component: ComponentInfo): String {
    val name = component.name.lowercase()
    return when {
        "contator" in name -> "Corpo robusto, três polos principais, bobina A1–A2 e contatos auxiliares. A etiqueta informa tensão da bobina e categoria de utilização."
        "relé" in name -> "Módulo menor que o contator, normalmente encaixado em base, com LED/indicador e contatos identificados no próprio corpo."
        "clp" in name || "módulo" in name -> "Módulo eletrônico com LEDs de alimentação, estado e canais. A etiqueta diferencia CPU, entradas e saídas."
        "sensor" in name -> "Corpo compacto com face de detecção, LED de estado e cabo ou conector. O princípio e a lógica aparecem na etiqueta técnica."
        "inversor" in name || "soft-starter" in name -> "Equipamento eletrônico com display, teclado, bornes de potência e comandos. A placa informa corrente e alimentação."
        "motor" in name -> "Carcaça com aletas, eixo, caixa de ligação e placa de dados. A placa é a referência para potência, tensão e corrente."
        "fonte" in name -> "Módulo ventilado para trilho DIN, com entrada, PE, saídas +24 V/0 V e indicador DC-OK."
        "disjuntor" in name -> "Dispositivo modular com alavanca, número de polos e curva/corrente nominal visíveis na frente."
        "ihm" in name -> "Tela frontal de operação com alimentação e rede na parte traseira; exibe estados, alarmes e comandos autorizados."
        else -> "Observe a tag, a etiqueta, os símbolos dos terminais, os indicadores e a posição do dispositivo no fluxo funcional."
    }
}

private val BENCH_FLOW = listOf(
    BenchBlock("QF", "Proteção", "Inicia o fluxo e representa a proteção do circuito."),
    BenchBlock("PS1", "Fonte 24 Vcc", "Disponibiliza alimentação de controle virtual."),
    BenchBlock("S", "Comando", "Representa START, STOP e seleção de modo."),
    BenchBlock("KSR", "Segurança", "Libera a sequência somente com condições seguras."),
    BenchBlock("CPU", "Lógica do CLP", "Processa entradas, permissivos e intertravamentos."),
    BenchBlock("RL", "Relé de interface", "Separa a saída lógica do estágio de acionamento."),
    BenchBlock("KM", "Contator", "Executa o acionamento virtual da carga."),
    BenchBlock("M", "Motor", "É a carga final observada no fluxo funcional.")
)

private val BENCH_CHOICES = listOf("CPU", "QF", "M", "S", "RL", "PS1", "KM", "KSR")

private val DIAGNOSTICS = listOf(
    DiagnosticScenario(
        "sensor_input", "Sensor aceso, entrada apagada",
        "O LED do sensor virtual está ligado, mas o bit de entrada do CLP permanece em 0.",
        listOf("Fonte 24 V virtual está normal", "LED do sensor responde ao alvo", "LED do canal de entrada não acende"),
        listOf("Analisar ramal, referência e canal entre sensor e entrada", "Trocar o motor", "Alterar a lógica sem observar o canal"),
        0, "A evidência mostra que o sinal se perde depois do sensor e antes do bit. O caminho deve ser analisado por etapas."
    ),
    DiagnosticScenario(
        "contactor", "Contator não aciona",
        "A saída virtual do CLP está em 1, mas KM1 não muda de estado.",
        listOf("Permissivos estão liberados", "LED da saída está ativo", "Relé de interface não indica atuação"),
        listOf("Analisar a interface entre saída e contator", "Forçar todas as saídas", "Ignorar a condição e reiniciar"),
        0, "Com a saída lógica ativa, a área provável fica no estágio de interface, alimentação de comando ou acionamento virtual."
    ),
    DiagnosticScenario(
        "safety", "Relé de segurança não rearma",
        "A emergência virtual foi liberada, mas a autorização segura continua desligada.",
        listOf("Um canal ainda aparece aberto", "EDM não confirmou retorno", "Reset foi solicitado"),
        listOf("Revisar canais, retorno e condição de reset", "Contornar o canal aberto", "Ocultar o alarme"),
        0, "Uma função de segurança só rearma quando todos os canais e retornos estão coerentes. Nunca se deve contorná-la."
    ),
    DiagnosticScenario(
        "vfd", "Inversor com RUN e 0 Hz",
        "O comando de marcha virtual está presente, porém a velocidade continua zerada.",
        listOf("Drive pronto", "RUN ativo", "Referência de frequência em 0 Hz"),
        listOf("Analisar a origem da referência", "Trocar o motor imediatamente", "Remover alarmes do histórico"),
        0, "RUN e referência são sinais diferentes. A evidência indica ausência de referência, não falha automática do motor."
    ),
    DiagnosticScenario(
        "thermal", "Relé térmico atuado",
        "A partida virtual foi bloqueada após uma condição de sobrecarga.",
        listOf("Contato de falha está aberto", "Alarme de sobrecarga registrado", "Comando START não sela"),
        listOf("Identificar a causa virtual antes de reconhecer/resetar", "Aumentar o ajuste sem análise", "Eliminar a proteção"),
        0, "O reset não corrige a causa. Primeiro é preciso compreender a sobrecarga simulada e confirmar o estado seguro."
    ),
    DiagnosticScenario(
        "supply", "24 V virtual instável",
        "Vários sensores e relés alternam de estado ao mesmo tempo.",
        listOf("Falhas surgem em vários ramais", "CPU registra queda de alimentação", "Um único sensor não explica todos os sintomas"),
        listOf("Analisar alimentação comum e carga virtual", "Trocar todos os sensores", "Apagar o programa"),
        0, "Quando vários circuitos falham juntos, uma causa comum como alimentação ou referência se torna hipótese prioritária."
    ),
    DiagnosticScenario(
        "reversal", "Reversão bloqueada",
        "O motor virtual está em frente e o comando de reverso foi solicitado.",
        listOf("KM frente está ativo", "KM reverso está bloqueado", "Intertravamento registrou comando incompatível"),
        listOf("Parar, confirmar o estado e depois solicitar o outro sentido", "Ligar os dois contatores", "Remover o intertravamento"),
        0, "Os sentidos são incompatíveis. A sequência segura exige parada e confirmação antes da nova solicitação."
    ),
    DiagnosticScenario(
        "output", "Saída acesa, válvula parada",
        "A saída digital virtual está ativa, mas o cilindro não avança.",
        listOf("Bit e LED da saída estão ativos", "Relé de interface indica comando", "Atuador não confirma posição"),
        listOf("Analisar a etapa virtual entre interface, válvula e atuador", "Alterar o programa sem evidência", "Forçar movimentos simultâneos"),
        0, "As evidências deslocam o foco para o caminho após a saída: interface, comando da válvula e condição do atuador."
    )
)

private val LEARNING_TOPICS = listOf(
    LearningTopic(
        "tags", "1. Tags e identificação",
        "Aprenda por que QF, KM, S, B, X e M aparecem nos diagramas.",
        listOf("A tag liga o componente físico ao diagrama.", "O número diferencia dispositivos da mesma família.", "A identificação deve ser consistente no painel, CLP e documentação.")
    ),
    LearningTopic(
        "contacts", "2. Contatos NA e NF",
        "Entenda estado normal, acionamento e falha segura.",
        listOf("NA fecha quando o dispositivo atua.", "NF abre quando o dispositivo atua.", "O estado deve ser interpretado com o circuito em condição normal definida no projeto.")
    ),
    LearningTopic(
        "zones", "3. Potência, comando e controle",
        "Separe mentalmente as zonas antes de diagnosticar.",
        listOf("Potência entrega energia à carga.", "Comando decide quando o acionamento ocorre.", "Controle processa sinais, lógica, permissivos e alarmes.")
    ),
    LearningTopic(
        "inputs", "4. Caminho de uma entrada digital",
        "Siga a informação do campo até o programa.",
        listOf("Dispositivo de campo gera o estado.", "Ramal e referência levam o sinal ao canal.", "O LED do canal e o bit ajudam a localizar onde o sinal foi perdido.")
    ),
    LearningTopic(
        "outputs", "5. Caminho de uma saída digital",
        "Siga a ordem lógica até o atuador virtual.",
        listOf("A lógica solicita a saída.", "O módulo indica o canal.", "Relé de interface, acionamento e atuador completam a cadeia funcional.")
    ),
    LearningTopic(
        "interlocks", "6. Permissivos e intertravamentos",
        "Descubra por que um comando válido pode permanecer bloqueado.",
        listOf("Permissivos precisam estar verdadeiros.", "Intertravamentos impedem estados incompatíveis.", "Segurança tem prioridade sobre a sequência e a produção.")
    ),
    LearningTopic(
        "diagnosis", "7. Diagnóstico estruturado",
        "Troque tentativa por evidência.",
        listOf("Registre o sintoma antes de alterar qualquer coisa.", "Divida a cadeia em etapas observáveis.", "Confirme a hipótese e documente causa, correção e validação.")
    ),
    LearningTopic(
        "documents", "8. Diagrama, CLP e IHM",
        "Relacione três visões da mesma máquina.",
        listOf("O diagrama mostra ligações e referências.", "O CLP mostra estados lógicos.", "A IHM resume operação e alarmes, mas não substitui a segurança.")
    )
)
