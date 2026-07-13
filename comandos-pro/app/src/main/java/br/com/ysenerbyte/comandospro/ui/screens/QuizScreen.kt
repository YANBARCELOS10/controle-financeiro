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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ysenerbyte.comandospro.core.QuizEngine
import br.com.ysenerbyte.comandospro.data.TrainingData
import br.com.ysenerbyte.comandospro.ui.ScreenIntro
import br.com.ysenerbyte.comandospro.ui.theme.AlarmRed
import br.com.ysenerbyte.comandospro.ui.theme.SafetyAmber
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen

@Composable
fun QuizScreen(
    bestScore: Int,
    onResult: (percentage: Int, passed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var attempt by remember { mutableIntStateOf(1) }
    val questions = remember(attempt) {
        QuizEngine.prepare(TrainingData.questions, count = 10, seed = 3_000 + attempt)
    }
    var current by remember(attempt) { mutableIntStateOf(0) }
    var correct by remember(attempt) { mutableIntStateOf(0) }
    var selected by remember(attempt, current) { mutableStateOf<Int?>(null) }
    var finished by remember(attempt) { mutableStateOf(false) }
    val percentage = QuizEngine.percentage(correct, questions.size)
    val passed = QuizEngine.passed(percentage)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "Avaliação dinâmica",
                title = "Certificação virtual",
                description = "Dez questões sorteadas, alternativas embaralhadas e explicação após cada resposta."
            )
        }

        if (!finished) {
            val question = questions[current]
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("QUESTÃO ${current + 1}/${questions.size}", fontWeight = FontWeight.Bold)
                            Text("$correct acertos", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (current + if (selected != null) 1 else 0) / questions.size.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(18.dp)) {
                        Text(question.prompt, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        question.options.forEachIndexed { index, option ->
                            val answerColor = when {
                                selected != null && index == question.correctIndex -> SignalGreen
                                selected == index && index != question.correctIndex -> AlarmRed
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            OutlinedButton(
                                onClick = {
                                    if (selected == null) {
                                        selected = index
                                        if (index == question.correctIndex) correct++
                                    }
                                },
                                enabled = selected == null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(option, color = answerColor)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        selected?.let {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (it == question.correctIndex) {
                                        SignalGreen.copy(alpha = 0.12f)
                                    } else {
                                        AlarmRed.copy(alpha = 0.12f)
                                    }
                                )
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(
                                        if (it == question.correctIndex) "Correto" else "Revise este conceito",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (current == questions.lastIndex) {
                                        val finalPercentage = QuizEngine.percentage(correct, questions.size)
                                        finished = true
                                        onResult(finalPercentage, QuizEngine.passed(finalPercentage))
                                    } else {
                                        current++
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (current == questions.lastIndex) "VER RESULTADO" else "PRÓXIMA QUESTÃO")
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (passed) SignalGreen.copy(alpha = 0.14f) else SafetyAmber.copy(alpha = 0.14f)
                    )
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text(if (passed) "CERTIFICADO VIRTUAL" else "RESULTADO DA TENTATIVA", fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$percentage%",
                            style = MaterialTheme.typography.displaySmall,
                            color = if (passed) SignalGreen else SafetyAmber
                        )
                        Text("$correct de ${questions.size} respostas corretas")
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (passed) {
                                "Aprovado no treinamento Comandos Pro 3D. Este certificado registra conhecimento virtual e não substitui qualificação profissional."
                            } else {
                                "A nota mínima é 70%. Revise os módulos indicados pelas explicações e tente novamente."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(14.dp))
                        Text("Melhor resultado salvo: ${maxOf(bestScore, percentage)}%")
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { attempt++ },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("NOVA PROVA") }
                    }
                }
            }
        }
    }
}
