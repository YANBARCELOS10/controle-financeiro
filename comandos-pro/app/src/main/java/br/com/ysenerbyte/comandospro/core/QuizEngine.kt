package br.com.ysenerbyte.comandospro.core

import kotlin.random.Random

object QuizEngine {
    fun prepare(
        questions: List<QuizQuestion>,
        count: Int = 10,
        seed: Int
    ): List<QuizQuestion> {
        val random = Random(seed)
        return questions
            .shuffled(random)
            .take(count.coerceIn(1, questions.size))
            .map { question ->
                val indexed = question.options.mapIndexed { index, option -> index to option }.shuffled(random)
                question.copy(
                    options = indexed.map { it.second },
                    correctIndex = indexed.indexOfFirst { it.first == question.correctIndex }
                )
            }
    }

    fun percentage(correct: Int, total: Int): Int =
        if (total <= 0) 0 else ((correct.coerceIn(0, total) * 100f) / total).toInt()

    fun passed(percentage: Int): Boolean = percentage >= 70
}
