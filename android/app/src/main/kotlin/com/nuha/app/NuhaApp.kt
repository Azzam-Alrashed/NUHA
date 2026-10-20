package com.nuha.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nuha.app.lab.LabHomeScreen
import com.nuha.app.lab.ink.TraceExperimentScreen
import com.nuha.app.lab.layout.LayoutExperimentScreen
import com.nuha.app.lab.offline.OfflineExperimentScreen
import com.nuha.app.lab.select.SelectionExperimentScreen

object Routes {
    const val LAB = "lab"
    const val TRACE = "lab/trace"
    const val SELECT = "lab/select"
    const val LAYOUT = "lab/layout"
    const val OFFLINE = "lab/offline"
}

@Composable
fun NuhaApp() {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }
    NavHost(navController = nav, startDestination = Routes.LAB) {
        composable(Routes.LAB) { LabHomeScreen(onOpen = { nav.navigate(it) }) }
        composable(Routes.TRACE) { TraceExperimentScreen(onBack = back) }
        composable(Routes.SELECT) { SelectionExperimentScreen(onBack = back) }
        composable(Routes.LAYOUT) { LayoutExperimentScreen(onBack = back) }
        composable(Routes.OFFLINE) { OfflineExperimentScreen(onBack = back) }
    }
}
