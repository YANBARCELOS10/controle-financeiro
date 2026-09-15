package br.com.ysenerbyte.comandospro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ysenerbyte.comandospro.core.FaultChallenge
import br.com.ysenerbyte.comandospro.core.TrainingModule
import br.com.ysenerbyte.comandospro.core.UserProgress
import br.com.ysenerbyte.comandospro.data.TrainingData
import br.com.ysenerbyte.comandospro.ui.SafetyBanner
import br.com.ysenerbyte.comandospro.ui.ScreenIntro
import br.com.ysenerbyte.comandospro.ui.theme.AlarmRed
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen

@Composable
fun TrainingScreen(
    progress: UserProgress,
    onModuleComplete: (String) -> Unit,
    onAward: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedModule by remember { mutableStateOf<String?>(null) }
    var selectedFault by remember { mutableIntStateOf(-1) }
    val faultAnswers = remember { mutableStateMapOf<String, Int>() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "Trilha profissional",
                title = "Treinamento e diagnóstico",
                description = "Estude módulos curtos e resolva sintomas pelo método evidência → hipótese → validação."
            )
        }
        item { SafetyBanner() }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progresso da trilha", fontWeight = FontWeight.Bold)
                        Text("${progress.studiedModules.size}/${TrainingData.modules.size}")
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress.studiedModules.size / TrainingData.modules.size.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item { Text("Módulos", style = MaterialTheme.typography.titleLarge) }
        items(TrainingData.modules, key = { it.id }) { module ->
            ModuleCard(
                module = module,
                expanded = expandedModule == module.id,
                completed = module.id in progress.studiedModules,
                onExpand = {
                    expandedModule = if (expandedModule == module.id) null else module.id
                },
                onComplete = {
                    onModuleComplete(module.id)
                    onAward("module_${module.id}", 35)
                }
            )
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("Desafios de diagnóstico", style = MaterialTheme.typography.titleLarge)
            Text(
                "Cada desafio registra apenas a primeira resposta da tentativa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(TrainingData.faults.size) { index ->
            val fault = TrainingData.faults[index]
            FaultCard(
                fault = fault,
                expanded = selectedFault == index,
                selectedAnswer = faultAnswers[fault.id],
                onExpand = { selectedFault = if (selectedFault == index) -1 else index },
                onAnswer = { answer ->
                    if (fault.id !in faultAnswers) {
                        faultAnswers[fault.id] = answer
                        if (answer == fault.correctIndex) onAward("fault_${fault.id}", 45)
                    }
                }
            )
        }
    }
}

@Composable
private fun ModuleCard(
    module: TrainingModule,
    expanded: Boolean,
    completed: Boolean,
    onExpand: () -> Unit,
    onComplete: () -> Unit
) {
    ElevatedCard(onClick = onExpand, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(module.level.uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(if (completed) "✓ CONCLUÍDO" else if (expanded) "−" else "+", color = if (completed) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(module.title, style = MaterialTheme.typography.titleLarge)
            Text(module.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                module.lessons.forEach { lesson ->
                    Text("• $lesson", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(5.dp))
                }
                Button(
                    onClick = onComplete,
                    enabled = !completed,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (completed) "MÓDULO CONCLUÍDO" else "MARCAR COMO ESTUDADO") }
            }
        }
    }
}

@Composable
private fun FaultCard(
    fault: FaultChallenge,
    expanded: Boolean,
    selectedAnswer: Int?,
    onExpand: () -> Unit,
    onAnswer: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("⚠  ${fault.title}", fontWeight = FontWeight.Bold)
            Text(fault.symptom, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onExpand, modifier = Modifier.fillMaxWidth()) {
                Text(if (expanded) "FECHAR" else "DIAGNOSTICAR")
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Text("Qual é a primeira decisão correta?", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                fault.options.forEachIndexed { index, option ->
                    val answered = selectedAnswer != null
                    val color = when {
                        answered && index == fault.correctIndex -> SignalGreen
                        answered && index == selectedAnswer -> AlarmRed
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    OutlinedButton(
                        onClick = { onAnswer(index) },
                        enabled = !answered,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(option, color = color)
                    }
                    Spacer(Modifier.height(7.dp))
                }
                if (selectedAnswer != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedAnswer == fault.correctIndex) {
                                SignalGreen.copy(alpha = 0.12f)
                            } else {
                                AlarmRed.copy(alpha = 0.12f)
                            }
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (selectedAnswer == fault.correctIndex) "Resposta correta" else "Resposta a revisar",
                                fontWeight = FontWeight.Bold
                            )
                            Text(fault.explanation, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
