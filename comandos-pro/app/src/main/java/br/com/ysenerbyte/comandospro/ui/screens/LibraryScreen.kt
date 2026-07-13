package br.com.ysenerbyte.comandospro.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.ysenerbyte.comandospro.core.ComponentInfo
import br.com.ysenerbyte.comandospro.core.UserProgress
import br.com.ysenerbyte.comandospro.data.CatalogData
import br.com.ysenerbyte.comandospro.data.CloudSyncService
import br.com.ysenerbyte.comandospro.data.RemoteContentRepository
import br.com.ysenerbyte.comandospro.ui.ScreenIntro
import br.com.ysenerbyte.comandospro.ui.theme.ElectricBlue
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LibraryScreen(
    progress: UserProgress,
    onNicknameChange: (String) -> Unit,
    onAward: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val remoteRepository = remember(context) { RemoteContentRepository(context) }
    var remotePack by remember { mutableStateOf(remoteRepository.loadCached()) }
    var remoteStatus by remember { mutableStateOf("Revisão ${remotePack.revision} • ${remotePack.updatedAt}") }
    var refreshing by remember { mutableStateOf(false) }
    var cloudStatus by remember {
        mutableStateOf(
            if (CloudSyncService.isConfigured) "Firebase pronto para sincronizar."
            else "Firebase preparado; falta adicionar google-services.json."
        )
    }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todos") }
    var selected by remember { mutableStateOf<ComponentInfo?>(null) }
    var nickname by remember(progress.nickname) { mutableStateOf(progress.nickname) }

    val categories = remember { listOf("Todos") + CatalogData.components.map { it.category }.distinct().sorted() }
    val filtered = remember(search, category) {
        CatalogData.components.filter { component ->
            val categoryMatches = category == "Todos" || component.category == category
            val text = listOf(
                component.name,
                component.tag,
                component.category,
                component.function,
                component.application
            ).joinToString(" ").lowercase()
            categoryMatches && text.contains(search.trim().lowercase())
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenIntro(
                eyebrow = "25 componentes",
                title = "Biblioteca técnica",
                description = "Pesquise função, terminais conceituais, aplicação, falhas comuns e cuidados."
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(remotePack.headline, fontWeight = FontWeight.Bold)
                    Text(remotePack.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    remotePack.tips.take(4).forEach { tip ->
                        Text("• $tip", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                refreshing = true
                                remoteStatus = "Buscando conteúdo no Git…"
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) { remoteRepository.refresh() }
                                    refreshing = false
                                    result.onSuccess {
                                        remotePack = it
                                        remoteStatus = "Atualizado: revisão ${it.revision} • ${it.updatedAt}"
                                    }.onFailure {
                                        remoteStatus = "Sem atualização: ${it.message ?: "falha de rede"}"
                                    }
                                }
                            },
                            enabled = !refreshing
                        ) { Text(if (refreshing) "ATUALIZANDO…" else "ATUALIZAR CONTEÚDO") }
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(remotePack.releaseUrl)))
                                }
                            }
                        ) { Text("VERSÕES") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        remoteStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Progresso na nuvem", fontWeight = FontWeight.Bold)
                    Text(
                        "A sincronização usa autenticação anônima e salva apenas apelido, XP e progresso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it.take(24) },
                        label = { Text("Apelido (não use nome completo)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onNicknameChange(nickname)
                            if (CloudSyncService.isConfigured) {
                                cloudStatus = "Sincronizando…"
                                CloudSyncService.sync(context, progress.copy(nickname = nickname)) { result ->
                                    cloudStatus = result.getOrElse { it.message ?: "Falha na sincronização." }
                                }
                            } else {
                                cloudStatus = "Firebase ainda não ativado neste APK. O progresso local está seguro."
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("SINCRONIZAR PROGRESSO") }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        cloudStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (cloudStatus.startsWith("Progresso")) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it.take(60) },
                label = { Text("Buscar componente ou função") },
                leadingIcon = { Text("⌕") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
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
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Componentes", style = MaterialTheme.typography.titleLarge)
                Text("${filtered.size} resultados", color = MaterialTheme.colorScheme.primary)
            }
        }
        items(filtered, key = { it.name }) { component ->
            ElevatedCard(
                onClick = {
                    selected = component
                    onAward("catalog_first_open", 25)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ElectricBlue.copy(alpha = 0.14f))
                    ) {
                        Text(
                            component.tag.substringBefore(" ").take(4),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                            color = ElectricBlue,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(component.name, fontWeight = FontWeight.Bold)
                        Text(
                            "${component.tag} • ${component.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            component.function,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text("Nenhum componente encontrado.", Modifier.padding(18.dp))
                }
            }
        }
    }

    selected?.let { component ->
        ComponentDialog(component = component, onDismiss = { selected = null })
    }
}

@Composable
private fun ComponentDialog(
    component: ComponentInfo,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 720.dp)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(component.category.uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(component.name, style = MaterialTheme.typography.headlineMedium)
                Text(component.tag, color = MaterialTheme.colorScheme.onSurfaceVariant)
                DetailSection("Função", component.function)
                DetailSection("Terminais conceituais", component.terminals)
                DetailSection("Aplicação", component.application)
                DetailSection("Verificação no simulador", component.virtualCheck)
                DetailSection("Falhas comuns", component.commonFaults)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Cuidado", fontWeight = FontWeight.Bold)
                        Text(component.safety, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("FECHAR") }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, body: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
