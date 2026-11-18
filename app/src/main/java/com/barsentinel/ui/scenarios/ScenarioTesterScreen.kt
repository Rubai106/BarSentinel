package com.barsentinel.ui.scenarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barsentinel.domain.compiler.EngagementCompiler
import com.barsentinel.domain.compiler.ScenarioOutcome
import com.barsentinel.domain.compiler.ScenarioSuite
import com.barsentinel.ui.components.GlassColumnCard
import com.barsentinel.ui.theme.BarSentinelShapes
import com.barsentinel.ui.theme.Danger
import com.barsentinel.ui.theme.DangerGlass
import com.barsentinel.ui.theme.GlassSurface
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.Success
import com.barsentinel.ui.theme.TextMuted
import com.barsentinel.ui.theme.TextPrimary
import com.barsentinel.ui.theme.TextSecondary

/**
 * Scenario tester (spec §11, §17, Gate G1). Runs the same 30-case suite the
 * unit tests run — same definitions, same ruleset — and shows pass/fail per
 * case. A failing case blocks release; this screen exists so the suite can
 * be demonstrated on the device, in front of a marker, without a laptop.
 */
@Composable
fun ScenarioTesterScreen(onBack: () -> Unit) {
    var runId by remember { mutableIntStateOf(0) }
    val outcomes = remember(runId) { ScenarioSuite.runAll() }
    val passed = outcomes.count { it.passed }
    val compileOutcomes = outcomes.take(ScenarioSuite.compileCases.size)
    val invalidationOutcomes = outcomes.drop(ScenarioSuite.compileCases.size)

    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Scenario tester",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The 30-case suite, run inside the app against ruleset " +
                    "${EngagementCompiler.RULESET_VERSION}. The unit tests run these exact cases.",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            GlassColumnCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$passed / ${outcomes.size} passed",
                        color = if (passed == outcomes.size) Success else Danger,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { runId++ }) {
                        Text("Run again", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (passed == outcomes.size) {
                        "Gate G1 holds: every case produces the expected rules, tier and interval."
                    } else {
                        "Failing cases are listed below with what went wrong. A failing case blocks release."
                    },
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            CaseGroup(title = "Compile cases", outcomes = compileOutcomes)
            Spacer(modifier = Modifier.height(20.dp))
            CaseGroup(title = "Invalidation cases", outcomes = invalidationOutcomes)
        }
    }
}

@Composable
private fun CaseGroup(title: String, outcomes: List<ScenarioOutcome>) {
    Text(title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        outcomes.forEach { outcome ->
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    shape = BarSentinelShapes.small,
                    color = if (outcome.passed) GlassSurface else DangerGlass
                ) {
                    Text(
                        outcome.id,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(outcome.label, color = TextPrimary, fontSize = 14.sp)
                    if (!outcome.passed) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(outcome.detail, color = Danger, fontSize = 13.sp)
                    }
                }
                Text(
                    if (outcome.passed) "PASS" else "FAIL",
                    color = if (outcome.passed) Success else Danger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
