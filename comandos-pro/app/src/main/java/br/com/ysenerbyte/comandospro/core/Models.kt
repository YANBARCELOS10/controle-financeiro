package br.com.ysenerbyte.comandospro.core

enum class AppScreen(val title: String, val symbol: String) {
    HOME("Início", "⌂"),
    SIMULATOR("Simulador", "⚡"),
    LAB_3D("Laboratório 3D", "◇"),
    PLC("CLP / IHM", "▦"),
    LIBRARY("Biblioteca", "▤"),
    TRAINING("Treinamento", "✓"),
    QUIZ("Avaliação", "★")
}

data class UserProgress(
    val xp: Int = 0,
    val completed: Set<String> = emptySet(),
    val studiedModules: Set<String> = emptySet(),
    val quizBest: Int = 0,
    val productionCount: Int = 0,
    val nickname: String = "Operador"
) {
    val level: Int get() = 1 + xp / 250
    val percent: Int get() = (completed.size * 8).coerceIn(0, 100)
}

data class ComponentInfo(
    val name: String,
    val tag: String,
    val category: String,
    val function: String,
    val terminals: String,
    val application: String,
    val virtualCheck: String,
    val commonFaults: String,
    val safety: String
)

data class TrainingModule(
    val id: String,
    val title: String,
    val level: String,
    val summary: String,
    val lessons: List<String>
)

data class QuizQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class FaultChallenge(
    val id: String,
    val title: String,
    val symptom: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
