package com.barsentinel.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barsentinel.ui.components.BarSentinelBottomBar
import com.barsentinel.ui.home.HomeScreen
import com.barsentinel.ui.placeholder.PhasePlaceholderScreen
import com.barsentinel.ui.privacy.PrivacyCentreScreen
import com.barsentinel.ui.simulation.SimulatedBuddyScreen

/**
 * Single-activity navigation (spec §11). Bottom navigation serves the
 * advocate role; the buddy view is deliberately smaller and is reached
 * through envelopes, not the bottom bar.
 */
@Composable
fun BarSentinelNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BarSentinelBottomBar(
                    currentRoute = currentRoute ?: Routes.HOME,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onStartEnvelope = { navController.navigate(Routes.COMPILE) },
                        onOpenPrivacy = { navController.navigate(Routes.PRIVACY_CENTRE) },
                        onOpenScenarios = { navController.navigate(Routes.SCENARIO_TESTER) },
                        onOpenVault = { navController.navigate(Routes.VAULT) },
                        onOpenTimeline = { id -> navController.navigate(Routes.timeline(id)) },
                        onOpenSimulation = { navController.navigate(Routes.SIMULATED_BUDDY) }
                    )
                }
                composable(Routes.ENVELOPES) {
                    PhasePlaceholderScreen(
                        title = "Envelopes",
                        phase = "Phase 3",
                        description = "Your envelopes live here: proposed, live, escalating and closed — each with its version, buddy state and timeline."
                    )
                }
                composable(Routes.VAULT) {
                    PhasePlaceholderScreen(
                        title = "Evidence vault",
                        phase = "Phase 6",
                        description = "Photos and notes captured inside an envelope, each hashed at capture and kept behind your device credential."
                    )
                }
                composable(Routes.MORE) {
                    PhasePlaceholderScreen(
                        title = "More",
                        phase = "Phase 7",
                        description = "Settings, language, privacy centre and the simulated-buddy mode live here."
                    )
                }
                composable(Routes.COMPILE) {
                    PhasePlaceholderScreen(
                        title = "Compile wizard",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 2",
                        description = "A 60-second context assessment that compiles into explained precautions. The rules engine behind it is already built and tested — see the scenario suite."
                    )
                }
                composable(Routes.PRECAUTIONS) {
                    PhasePlaceholderScreen(
                        title = "Precautions review",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 2",
                        description = "Every precaution with the condition that triggered it and its rule ID, plus the what-would-change-this-plan preview."
                    )
                }
                composable(Routes.ENVELOPE_DETAIL) {
                    PhasePlaceholderScreen(
                        title = "Envelope detail",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 3",
                        description = "The live agreement: schedule, buddy state, check-in, extend, close and the timeline."
                    )
                }
                composable(Routes.INVALIDATION_DIFF) {
                    PhasePlaceholderScreen(
                        title = "What changed",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 7",
                        description = "When the context changes, this screen shows which precautions fell, which are newly required, and what needs re-acceptance."
                    )
                }
                composable(Routes.BUDDY_INCOMING) {
                    PhasePlaceholderScreen(
                        title = "Buddy — incoming",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 3",
                        description = "Accept or decline a specific envelope version, with the full duty summary."
                    )
                }
                composable(Routes.BUDDY_WATCHING) {
                    PhasePlaceholderScreen(
                        title = "Buddy — watching",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 5",
                        description = "Status only while all is well. A map appears only after a missed check-in or SOS."
                    )
                }
                composable(Routes.SOS) {
                    PhasePlaceholderScreen(
                        title = "SOS",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 4",
                        description = "Hold to fire, a cancel window, a category, and the 999 dialler. The hold control itself is already built — try it on Home."
                    )
                }
                composable(Routes.TIMELINE) {
                    PhasePlaceholderScreen(
                        title = "Timeline + verify",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 6",
                        description = "Readable events, the hash view, export, and offline re-verification. The chain builder and verifier are already built and tested."
                    )
                }
                composable(Routes.SCENARIO_TESTER) {
                    PhasePlaceholderScreen(
                        title = "Scenario tester",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 2",
                        description = "Runs the 30-case suite in the app and shows pass/fail per rule. The suite itself already runs as unit tests — 30/30 is Gate G1."
                    )
                }
                composable(Routes.PRIVACY_CENTRE) {
                    PrivacyCentreScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.SIMULATED_BUDDY) {
                    SimulatedBuddyScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.SETTINGS) {
                    PhasePlaceholderScreen(
                        title = "Settings",
                        onBack = { navController.popBackStack() },
                        phase = "Phase 7",
                        description = "Language (Bangla / English), check-in defaults, SOS window, contacts, and export or delete my data."
                    )
                }
            }
        }
    }
}
