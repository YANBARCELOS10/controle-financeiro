package br.com.ysenerbyte.comandospro.core

import android.content.Context

class ProgressStore(context: Context) {
    private val preferences = context.getSharedPreferences("comandos_pro_progress_v3", Context.MODE_PRIVATE)

    fun load(): UserProgress = runCatching {
        UserProgress(
            xp = preferences.getInt(KEY_XP, 0).coerceIn(0, MAX_XP),
            completed = preferences.getStringSet(KEY_COMPLETED, emptySet()).orEmpty()
                .filter { it.length <= 80 }
                .toSet(),
            studiedModules = preferences.getStringSet(KEY_STUDIED, emptySet()).orEmpty()
                .filter { it.length <= 80 }
                .toSet(),
            quizBest = preferences.getInt(KEY_QUIZ_BEST, 0).coerceIn(0, 100),
            productionCount = preferences.getInt(KEY_COUNT, 0).coerceIn(0, MAX_COUNT),
            nickname = preferences.getString(KEY_NICKNAME, "Operador")
                .orEmpty()
                .trim()
                .take(24)
                .ifBlank { "Operador" }
        )
    }.getOrElse {
        preferences.edit().clear().apply()
        UserProgress()
    }

    fun save(progress: UserProgress) {
        preferences.edit()
            .putInt(KEY_XP, progress.xp.coerceIn(0, MAX_XP))
            .putStringSet(KEY_COMPLETED, progress.completed.toSet())
            .putStringSet(KEY_STUDIED, progress.studiedModules.toSet())
            .putInt(KEY_QUIZ_BEST, progress.quizBest.coerceIn(0, 100))
            .putInt(KEY_COUNT, progress.productionCount.coerceIn(0, MAX_COUNT))
            .putString(KEY_NICKNAME, progress.nickname.trim().take(24).ifBlank { "Operador" })
            .apply()
    }

    fun reset(): UserProgress {
        preferences.edit().clear().apply()
        return UserProgress()
    }

    companion object {
        private const val KEY_XP = "xp"
        private const val KEY_COMPLETED = "completed"
        private const val KEY_STUDIED = "studied"
        private const val KEY_QUIZ_BEST = "quiz_best"
        private const val KEY_COUNT = "production_count"
        private const val KEY_NICKNAME = "nickname"
        private const val MAX_XP = 10_000_000
        private const val MAX_COUNT = 10_000_000
    }
}
