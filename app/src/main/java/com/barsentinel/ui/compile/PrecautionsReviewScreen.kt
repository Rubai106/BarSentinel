package com.barsentinel.ui.compile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barsentinel.domain.compiler.Company
import com.barsentinel.domain.compiler.EngagementCompiler
import com.barsentinel.domain.compiler.Precaution
import com.barsentinel.domain.compiler.PrecautionLevel
import com.barsentinel.domain.compiler.RiskTier
import com.barsentinel.ui.components.GlassColumnCard
import com.barsentinel.ui.components.GoldButton
import com.barsentinel.ui.theme.BarSentinelShapes
import com.barsentinel.ui.theme.Danger
import com.barsentinel.ui.theme.GlassBorder
import com.barsentinel.ui.theme.GlassSurface
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.GoldBorder
import com.barsentinel.ui.theme.GoldGlass
import com.barsentinel.ui.theme.Success
import com.barsentinel.ui.theme.TextFaint
import com.barsentinel.ui.theme.TextMuted
import com.barsentinel.ui.theme.TextPrimary
import com.barsentinel.ui.theme.TextSecondary

/** One-tap dismissal reasons (spec §7). Dismissing is a recorded decision, never a silent skip. */
private val DISMISSAL_REASONS = listOf("Already handled", "Not relevant here", "I'll handle it differently")

/**
 * Precautions review (spec §11, §7): every precaution grouped by level,
 * each with the condition that triggered it and its rule ID. Required
 * precautions cannot be dismissed. Recommended and Consider items can be
 * dismissed with a one-tap reason. The what-would-change preview shows how
 * two common changes would alter the plan, before anything is saved.
 */
@Composable
fun PrecautionsReviewScreen(
    state: CompileUiState,
    onDismiss: (String, String) -> Unit,
    onRestore: (String) -> Unit,
    onSave: () -> Unit,
    onEditAnswers: () -> Unit,
    onExit: () -> Unit
) {
    val result = state.result
    val input = state.answers.toCompilerInput()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(bottom = 24.dp)
    ) {
        if (result == null || input == null) {
            Text(
                "No compiled plan yet. Answer the questions first — the plan appears here.",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            GoldButton(text = "Back to the questions", onClick = onEditAnswers)
            return@Column
        }

        TierCard(result.tier, result.intervalMinutes)

        if (result.conflictNotes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            result.conflictNotes.forEach { note ->
                Text(note, color = TextMuted, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (result.precautions.isEmpty()) {
            GlassColumnCard {
                Text(
                    "No precautions for this plan.",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Nothing in your answers triggered a rule. The standard check-in schedule still applies.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            PrecautionGroup(
                title = "Required",
                note = "The envelope can't go live without these.",
                precautions = result.precautions.filter { it.level == PrecautionLevel.REQUIRED },
                dismissals = state.dismissals,
                dismissible = false,
                onDismiss = onDismiss,
                onRestore = onRestore
            )
            PrecautionGroup(
                title = "Recommended",
                note = "Strongly advised. You can dismiss one with a reason — the dismissal is recorded.",
                precautions = result.precautions.filter { it.level == PrecautionLevel.RECOMMENDED },
                dismissals = state.dismissals,
                dismissible = true,
                onDismiss = onDismiss,
                onRestore = onRestore
            )
            PrecautionGroup(
                title = "Consider",
                note = "Worth thinking about for this kind of engagement.",
                precautions = result.precautions.filter { it.level == PrecautionLevel.CONSIDER },
                dismissals = state.dismissals,
                dismissible = true,
                onDismiss = onDismiss,
                onRestore = onRestore
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        WhatWouldChangePreview(input = input, current = result.precautions.map { it.ruleId })

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Ruleset ${result.rulesetVersion}. Every precaution states its reason and rule ID, so the plan can be checked.",
            color = TextFaint,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
        when {
            state.savedEnvelopeId != null -> {
                GlassColumnCard {
                    Text(
                        "Plan saved as a draft.",
                        color = Success,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "It is stored on this device only. Proposing it to a buddy lands in Phase 3.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                GoldButton(text = "Back to Home", onClick = onExit)
            }
            else -> {
                GoldButton(
                    text = if (state.isSaving) "Saving…" else "Save plan",
                    enabled = !state.isSaving,
                    onClick = onSave
                )
                if (state.saveFailed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Saving failed. Nothing was stored — try again.",
                        color = Danger,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onEditAnswers, modifier = Modifier.fillMaxWidth()) {
                    Text("Change my answers", color = TextMuted, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun TierCard(tier: RiskTier, intervalMinutes: Int) {
    val (label, explanation) = when (tier) {
        RiskTier.STANDARD -> "Standard" to "The standard precautions for this kind of engagement."
        RiskTier.ELEVATED -> "Elevated" to "Some answers raised the plan above the standard level."
        RiskTier.HIGH -> "High" to "The answers call for the strongest plan: buddy acceptance required and escrow on."
    }
    GlassColumnCard {
        Text("Risk tier: $label", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(explanation, color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Check-in every $intervalMinutes minutes. The tier summarises your precautions — it is not a prediction, and it never calls anything safe.",
            color = TextMuted,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PrecautionGroup(
    title: String,
    note: String,
    precautions: List<Precaution>,
    dismissals: Map<String, String>,
    dismissible: Boolean,
    onDismiss: (String, String) -> Unit,
    onRestore: (String) -> Unit
) {
    if (precautions.isEmpty()) return
    Text(title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Text(note, color = TextMuted, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        precautions.forEach { precaution ->
            PrecautionCard(
                precaution = precaution,
                dismissalReason = dismissals[precaution.ruleId],
                dismissible = dismissible,
                onDismiss = onDismiss,
                onRestore = onRestore
            )
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun PrecautionCard(
    precaution: Precaution,
    dismissalReason: String?,
    dismissible: Boolean,
    onDismiss: (String, String) -> Unit,
    onRestore: (String) -> Unit
) {
    var choosingReason by remember { mutableStateOf(false) }
    GlassColumnCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = BarSentinelShapes.small,
                color = GoldGlass,
                border = BorderStroke(1.dp, GoldBorder)
            ) {
                Text(
                    precaution.ruleId,
                    color = Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (!dismissible) {
                Text("Required", color = TextMuted, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(precaution.action, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Because: ${precaution.triggerText}", color = TextSecondary, fontSize = 14.sp)
        when {
            dismissalReason != null -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dismissed: $dismissalReason", color = TextMuted, fontSize = 13.sp)
                TextButton(onClick = { onRestore(precaution.ruleId) }) {
                    Text("Undo dismissal", color = Gold, fontSize = 13.sp)
                }
            }
            dismissible && choosingReason -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Why are you dismissing this?", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DISMISSAL_REASONS.forEach { reason ->
                        TextButton(onClick = { onDismiss(precaution.ruleId, reason) }) {
                            Text(reason, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
                TextButton(onClick = { choosingReason = false }) {
                    Text("Keep it", color = TextMuted, fontSize = 13.sp)
                }
            }
            dismissible -> {
                TextButton(onClick = { choosingReason = true }) {
                    Text("Dismiss with a reason", color = TextMuted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun WhatWouldChangePreview(
    input: com.barsentinel.domain.compiler.CompilerInput,
    current: List<String>
) {
    var previewAfterDark by remember(input) { mutableStateOf(input.finishesAfterDark) }
    var previewAlone by remember(input) { mutableStateOf(input.company == Company.ALONE) }

    val previewInput = input.copy(
        finishesAfterDark = previewAfterDark,
        company = when {
            previewAlone -> Company.ALONE
            input.company == Company.ALONE -> Company.WITH_COLLEAGUE
            else -> input.company
        }
    )
    val changed = previewInput != input
    val previewResult = if (changed) EngagementCompiler.compile(previewInput) else null
    val added = previewResult?.precautions?.map { it.ruleId }.orEmpty() - current.toSet()
    val removed = current - previewResult?.precautions?.map { it.ruleId }.orEmpty().toSet()

    Text(
        "What would change this plan?",
        color = TextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "Try the two most common changes. This is a preview only — your answers stay as they are.",
        color = TextMuted,
        fontSize = 13.sp
    )
    Spacer(modifier = Modifier.height(10.dp))
    GlassColumnCard {
        PreviewToggle(
            label = "Finishes after dark",
            checked = previewAfterDark,
            onCheckedChange = { previewAfterDark = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        PreviewToggle(
            label = "Going alone",
            checked = previewAlone,
            onCheckedChange = { previewAlone = it }
        )
        if (changed && previewResult != null) {
            Spacer(modifier = Modifier.height(12.dp))
            if (added.isEmpty() && removed.isEmpty()) {
                Text(
                    "No precaution would change, but the tier or check-in interval might: " +
                        "tier ${previewResult.tier.name.lowercase()}, every ${previewResult.intervalMinutes} minutes.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            } else {
                if (added.isNotEmpty()) {
                    Text("Would be added:", color = TextSecondary, fontSize = 14.sp)
                    previewResult.precautions.filter { it.ruleId in added }.forEach { precaution ->
                        Text("+ ${precaution.ruleId} — ${precaution.action}", color = TextPrimary, fontSize = 14.sp)
                    }
                }
                if (removed.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Would no longer apply:", color = TextSecondary, fontSize = 14.sp)
                    removed.forEach { ruleId ->
                        Text("− $ruleId", color = TextMuted, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Tier would be ${previewResult.tier.name.lowercase()}, check-in every ${previewResult.intervalMinutes} minutes.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Matches your current answers.", color = TextFaint, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PreviewToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Gold,
                checkedTrackColor = GoldGlass,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = GlassSurface
            )
        )
    }
}
