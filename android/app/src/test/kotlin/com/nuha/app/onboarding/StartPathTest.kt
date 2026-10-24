package com.nuha.app.onboarding

import com.nuha.app.Pending
import com.nuha.app.child.Activity
import org.junit.Assert.assertEquals
import org.junit.Test

/** FR-OBS-006 acceptance cases, plus the stop each path begins at (FR-HOM-005). Thresholds are PLACEHOLDER OI-05. */
class StartPathTest {
    private fun answers(vararg yes: Int, rest: Answer = Answer.No) = List(Questions.size) { i -> if (i + 1 in yes) Answer.Yes else rest }

    @Test fun allNoIsMixed() = assertEquals(StartPath.Mixed, startPathFor(answers()))

    @Test fun unansweredIsMixed() = assertEquals(StartPath.Mixed, startPathFor(List(Questions.size) { null }))

    @Test fun allUnsureIsMixed() = assertEquals(StartPath.Mixed, startPathFor(answers(rest = Answer.Unsure)))

    @Test fun communicationQuestionsLeadToCommunication() = assertEquals(StartPath.Communication, startPathFor(answers(1, 2, 3, 6, 7)))

    @Test fun attentionQuestionsLeadToAttention() = assertEquals(StartPath.Attention, startPathFor(answers(4, 5, 9, 10)))

    @Test fun letterQuestionLeadsToLearning() = assertEquals(StartPath.Learning, startPathFor(answers(8)))

    @Test fun belowHalfIsMixed() {
        // Two of five communication questions: 4 of 10 points, under the 0.5 lead.
        assertEquals(StartPath.Mixed, startPathFor(answers(1, 2)))
    }

    @Test fun tooCloseIsMixed() {
        // Communication 1.0 and attention 1.0: no margin, so neither leads.
        assertEquals(StartPath.Mixed, startPathFor(answers(1, 2, 3, 6, 7, 4, 5, 9, 10)))
    }

    @Test fun areasMatchTheSrs() {
        val byArea = Questions.withIndex().groupBy({ it.value.area }, { it.index + 1 })
        assertEquals(listOf(1, 2, 3, 6, 7), byArea[Area.Communication])
        assertEquals(listOf(4, 5, 9, 10), byArea[Area.Attention])
        assertEquals(listOf(8), byArea[Area.Learning])
    }

    @Test fun eachPathStartsAtItsFirstStop() {
        assertEquals(Activity.Emotions, Pending.startStop(StartPath.Communication.id))
        assertEquals(Activity.Star, Pending.startStop(StartPath.Attention.id))
        assertEquals(Activity.Letters, Pending.startStop(StartPath.Learning.id))
        assertEquals(Activity.Emotions, Pending.startStop(StartPath.Mixed.id))
    }

    @Test fun nineActivitiesWithUniqueIds() {
        assertEquals(9, Activity.entries.size)
        assertEquals(9, Activity.entries.map { it.id }.toSet().size)
    }
}
