package com.nuha.app.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The owner's sign list against the observation questions. Signs and thresholds are placeholders for review. */
class SignsTest {
    /** Answers with Yes at the 1-based [yes] questions, Unsure at [unsure], and No elsewhere. */
    private fun answers(yes: Set<Int> = emptySet(), unsure: Set<Int> = emptySet()) =
        List(Questions.size) { i -> if (i + 1 in yes) Answer.Yes else if (i + 1 in unsure) Answer.Unsure else Answer.No }

    private fun conditions(answers: List<Answer?>) = signsFor(answers).map { it.condition }

    @Test fun allNoShowsNothing() = assertTrue(signsFor(answers()).isEmpty())

    @Test fun unansweredShowsNothing() = assertTrue(signsFor(List(Questions.size) { null }).isEmpty())

    @Test fun everySignPointsAtAQuestion() = assertTrue(Signs.all { it.question in Questions.indices && it.atLeast != Answer.No })

    @Test fun fourAutismSignsShowAutism() {
        val match = signsFor(answers(yes = setOf(1, 2, 7, 9))).single()
        assertEquals(Condition.Autism, match.condition)
        assertEquals(4, match.matched)
        assertEquals(8, match.total)
    }

    @Test fun threeAutismSignsAreNotEnough() = assertTrue(signsFor(answers(yes = setOf(1, 2, 7))).isEmpty())

    @Test fun unsureCountsOnlyForSometimesSigns() {
        // Eye contact, response, toy and touch are yes-only signs; unsure on them counts for nothing.
        assertTrue(signsFor(answers(unsure = setOf(1, 2, 7, 9))).isEmpty())
        // Language, echoing, tasting and movement count on unsure.
        assertEquals(listOf(Condition.Autism), conditions(answers(unsure = setOf(3, 5, 6, 10))))
    }

    @Test fun attentionAndMovementShowAdhd() = assertEquals(listOf(Condition.Adhd), conditions(answers(yes = setOf(4, 5))))

    @Test fun unsureMovementIsNotAnAdhdSign() = assertTrue(signsFor(answers(yes = setOf(4), unsure = setOf(5))).isEmpty())

    @Test fun lettersShowLearningDifficultiesAndSlowLearning() =
        assertEquals(listOf(Condition.Adhd, Condition.LearningDifficulty), conditions(answers(yes = setOf(4, 8))))

    @Test fun allYesShowsAllThreeInOrder() =
        assertEquals(Condition.entries.toList(), conditions(answers(yes = (1..Questions.size).toSet())))
}
