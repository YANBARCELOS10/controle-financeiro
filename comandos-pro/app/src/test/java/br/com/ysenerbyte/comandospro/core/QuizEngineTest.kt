package br.com.ysenerbyte.comandospro.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizEngineTest {
    private val source = (1..16).map { index ->
        QuizQuestion(
            prompt = "Pergunta $index",
            options = listOf("Correta $index", "Distrator A", "Distrator B"),
            correctIndex = 0,
            explanation = "Explicação $index"
        )
    }

    @Test
    fun preparationSelectsTenAndPreservesCorrectAnswer() {
        val prepared = QuizEngine.prepare(source, count = 10, seed = 42)
        assertEquals(10, prepared.size)
        assertEquals(10, prepared.map { it.prompt }.distinct().size)
        prepared.forEach { question ->
            val originalNumber = question.prompt.substringAfterLast(' ')
            assertEquals("Correta $originalNumber", question.options[question.correctIndex])
        }
    }

    @Test
    fun scoringUsesSeventyPercentThreshold() {
        assertEquals(70, QuizEngine.percentage(7, 10))
        assertTrue(QuizEngine.passed(70))
        assertTrue(!QuizEngine.passed(69))
    }
}
