package com.nuha.app.activities

import androidx.compose.runtime.Composable
import com.nuha.app.Numerals
import com.nuha.app.child.Activity
import com.nuha.app.task.ActivityPlan
import com.nuha.app.task.TaskScope

/** The plan for [activity], at the level its completed sessions earn (FR-ACT-012), numbers written in [numerals]. */
fun planFor(activity: Activity, completedSessions: Int, numerals: Numerals): ActivityPlan = when (activity) {
    Activity.Emotions -> EmotionsPlan(completedSessions)
    Activity.Environment -> EnvironmentPlan(completedSessions)
    Activity.SocialPath -> PathPlan(completedSessions)
    Activity.Star -> StarPlan(completedSessions)
    Activity.Maze -> MazePlan(completedSessions)
    Activity.Garden -> GardenPlan(completedSessions, numerals)
    Activity.Letters -> LettersPlan(completedSessions)
    Activity.Patterns -> PatternsPlan(completedSessions)
    Activity.Balance -> BalancePlan(completedSessions, numerals)
}

@Composable
fun ActivityContent(activity: Activity, scope: TaskScope) = when (activity) {
    Activity.Emotions -> EmotionsContent(scope)
    Activity.Environment -> EnvironmentContent(scope)
    Activity.SocialPath -> SocialPathContent(scope)
    Activity.Star -> StarTrackingContent(scope)
    Activity.Maze -> MazeContent(scope)
    Activity.Garden -> GardenContent(scope)
    Activity.Letters -> LettersContent(scope)
    Activity.Patterns -> PatternsContent(scope)
    Activity.Balance -> BalanceContent(scope)
}
