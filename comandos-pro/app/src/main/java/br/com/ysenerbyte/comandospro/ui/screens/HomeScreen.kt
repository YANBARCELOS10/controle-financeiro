package br.com.ysenerbyte.comandospro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ysenerbyte.comandospro.core.AppScreen
import br.com.ysenerbyte.comandospro.core.UserProgress
import br.com.ysenerbyte.comandospro.ui.MetricCard
import br.com.ysenerbyte.comandospro.ui.SafetyBanner
import br.com.ysenerbyte.comandospro.ui.ScreenIntro
import br.com.ysenerbyte.comandospro.ui.theme.ElectricBlue
import br.com.ysenerbyte.comandospro.ui.theme.SafetyAmber
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen

@Composable
fun HomeScreen(
    progress: UserProgress,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "Comandos Pro 3D",
                title = "Centro de treinamento",
                description = "Simulação nativa, painel tridimensional, CLP/IHM e diagnóstico em um ambiente virtual."
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Nível ${progress.level}", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${progress.xp} XP acumulados",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            "${progress.percent}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progress.percent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = ElectricBlue,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                    )
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    value = progress.quizBest.toString() + "%",
                    label = "Melhor prova",
                    modifier = Modifier.weight(1f),
                    accent = SignalGreen
                )
                MetricCard(
                    value = progress.studiedModules.size.toString() + "/6",
                    label = "Módulos",
                    modifier = Modifier.weight(1f),
                    accent = SafetyAmber
                )
                MetricCard(
                    value = progress.productionCount.toString(),
                    label = "Ciclos CLP",
                    modifier = Modifier.weight(1f),
                    accent = ElectricBlue
                )
            }
        }

        item { SafetyBanner() }

        item {
            Text("Ambientes profissionais", style = MaterialTheme.typography.titleLarge)
        }

        item {
            FeatureRow(
                left = Feature("⚡", "Simulador", "Partidas e falhas", AppScreen.SIMULATOR),
                right = Feature("◇", "Painel 3D", "Gire, amplie e monte", AppScreen.LAB_3D),
                onNavigate = onNavigate
            )
        }
        item {
            FeatureRow(
                left = Feature("▦", "CLP / IHM", "Entradas, saídas e ciclo", AppScreen.PLC),
                right = Feature("▤", "Componentes", "Catálogo técnico", AppScreen.LIBRARY),
                onNavigate = onNavigate
            )
        }
        item {
            FeatureRow(
                left = Feature("✓", "Diagnóstico", "Módulos e desafios", AppScreen.TRAINING),
                right = Feature("★", "Avaliação", "Prova com explicações", AppScreen.QUIZ),
                onNavigate = onNavigate
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Versão profissional 3.0", fontWeight = FontWeight.Bold)
                    Text(
                        "Android nativo • Kotlin • Jetpack Compose • OpenGL ES 3 • offline-first",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class Feature(
    val symbol: String,
    val title: String,
    val subtitle: String,
    val screen: AppScreen
)

@Composable
private fun FeatureRow(
    left: Feature,
    right: Feature,
    onNavigate: (AppScreen) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FeatureCard(left, onNavigate, Modifier.weight(1f))
        FeatureCard(right, onNavigate, Modifier.weight(1f))
    }
}

@Composable
private fun FeatureCard(
    feature: Feature,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { onNavigate(feature.screen) },
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(feature.symbol, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text(feature.title, fontWeight = FontWeight.Bold)
            Text(
                feature.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
