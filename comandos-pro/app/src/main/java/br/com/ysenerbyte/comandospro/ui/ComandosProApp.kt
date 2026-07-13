package br.com.ysenerbyte.comandospro.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import br.com.ysenerbyte.comandospro.core.AppScreen
import br.com.ysenerbyte.comandospro.core.ProgressStore
import br.com.ysenerbyte.comandospro.core.UserProgress
import br.com.ysenerbyte.comandospro.ui.screens.HomeScreen
import br.com.ysenerbyte.comandospro.ui.screens.Lab3DScreen
import br.com.ysenerbyte.comandospro.ui.screens.LibraryScreen
import br.com.ysenerbyte.comandospro.ui.screens.PlcScreen
import br.com.ysenerbyte.comandospro.ui.screens.QuizScreen
import br.com.ysenerbyte.comandospro.ui.screens.SimulatorScreen
import br.com.ysenerbyte.comandospro.ui.screens.TrainingScreen
import kotlinx.coroutines.delay

private val mainDestinations = listOf(
    AppScreen.HOME,
    AppScreen.SIMULATOR,
    AppScreen.LAB_3D,
    AppScreen.PLC,
    AppScreen.LIBRARY
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComandosProApp() {
    val context = LocalContext.current
    val store = remember(context) { ProgressStore(context) }
    var progress by remember { mutableStateOf(store.load()) }
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var showSplash by rememberSaveable { mutableStateOf(true) }

    fun persist(updated: UserProgress) {
        progress = updated
        store.save(updated)
    }

    fun award(key: String, amount: Int) {
        if (key !in progress.completed) {
            persist(
                progress.copy(
                    xp = (progress.xp + amount).coerceAtMost(10_000_000),
                    completed = progress.completed + key
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(1_850)
        showSplash = false
    }

    if (showSplash) {
        SplashCover()
        return
    }

    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (currentScreen == AppScreen.HOME) "Comandos Pro 3D" else currentScreen.title,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentScreen !in mainDestinations) {
                        TextButton(onClick = { currentScreen = AppScreen.HOME }) { Text("‹ Início") }
                    }
                },
                actions = {
                    Text(
                        "${progress.xp} XP",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                mainDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentScreen == destination,
                        onClick = { currentScreen = destination },
                        icon = { Text(destination.symbol) },
                        label = { Text(destination.title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(progress, onNavigate = { currentScreen = it })
                AppScreen.SIMULATOR -> SimulatorScreen(onAward = ::award)
                AppScreen.LAB_3D -> Lab3DScreen(onAward = ::award)
                AppScreen.PLC -> PlcScreen(
                    productionCount = progress.productionCount,
                    onCycleComplete = {
                        persist(progress.copy(productionCount = progress.productionCount + 1))
                    },
                    onAward = ::award
                )
                AppScreen.LIBRARY -> LibraryScreen(
                    progress = progress,
                    onNicknameChange = { persist(progress.copy(nickname = it.trim().take(24))) },
                    onAward = ::award
                )
                AppScreen.TRAINING -> TrainingScreen(
                    progress = progress,
                    onModuleComplete = { id ->
                        persist(progress.copy(studiedModules = progress.studiedModules + id))
                    },
                    onAward = ::award
                )
                AppScreen.QUIZ -> QuizScreen(
                    bestScore = progress.quizBest,
                    onResult = { percentage, passed ->
                        val firstPass = passed && "quiz_passed" !in progress.completed
                        persist(
                            progress.copy(
                                quizBest = maxOf(progress.quizBest, percentage),
                                xp = if (firstPass) progress.xp + 180 else progress.xp,
                                completed = if (firstPass) progress.completed + "quiz_passed" else progress.completed
                            )
                        )
                    }
                )
            }
        }
    }
}
