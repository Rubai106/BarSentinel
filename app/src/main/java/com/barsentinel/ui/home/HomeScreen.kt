package com.barsentinel.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barsentinel.data.demo.DemoData
import com.barsentinel.data.demo.DemoEnvelope
import com.barsentinel.ui.components.CountdownRing
import com.barsentinel.ui.components.GlassCard
import com.barsentinel.ui.components.GlassColumnCard
import com.barsentinel.ui.components.GoldButton
import com.barsentinel.ui.components.ShieldHero
import com.barsentinel.ui.components.SosHoldButton
import com.barsentinel.ui.theme.AvatarViolet
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.GoldBorder
import com.barsentinel.ui.theme.GoldGlass
import com.barsentinel.ui.theme.InfoBlue
import com.barsentinel.ui.theme.Navy700
import com.barsentinel.ui.theme.Navy800
import com.barsentinel.ui.theme.Navy900
import com.barsentinel.ui.theme.Success
import com.barsentinel.ui.theme.TextFaint
import com.barsentinel.ui.theme.TextMuted
import com.barsentinel.ui.theme.TextPrimary
import com.barsentinel.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Home screen (spec §11), built from the approved mockup: one giant status,
 * one clear action, SOS always reachable. Copy follows the spec §12 rules —
 * states facts, never promises protection.
 */
@Composable
fun HomeScreen(
    onStartEnvelope: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenScenarios: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenTimeline: (String) -> Unit,
    onOpenSimulation: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSosDemoDialog by remember { mutableStateOf(false) }

    fun demoNote(message: String) {
        scope.launch { snackbarHostState.showSnackbar("$message (demo — nothing was sent)") }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Navy700, Navy800, Navy900)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 14.dp, bottom = 24.dp)
        ) {
            Header(isLive = state is HomeUiState.Live)

            when (val current = state) {
                HomeUiState.NoEnvelope -> NoEnvelopeContent(
                    onStartEnvelope = onStartEnvelope,
                    onOpenScenarios = onOpenScenarios,
                    onOpenVault = onOpenVault,
                    onOpenTimeline = { onOpenTimeline(DemoData.liveEnvelope.id) }
                )
                is HomeUiState.Live -> LiveContent(
                    envelope = current.envelope,
                    onCheckIn = { demoNote("Checked in") },
                    onExtend = { demoNote("Extended by 30 minutes") },
                    onClose = { demoNote("Envelope closed safely") },
                    onOpenTimeline = { onOpenTimeline(current.envelope.id) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            SosHoldButton(onFired = { showSosDemoDialog = true })
            Spacer(modifier = Modifier.height(16.dp))
            DemoPreviewSwitcher(
                isLive = state is HomeUiState.Live,
                onPreviewEmpty = { viewModel.previewEmpty() },
                onPreviewLive = { viewModel.previewLive() },
                onOpenSimulation = onOpenSimulation,
                onOpenPrivacy = onOpenPrivacy
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showSosDemoDialog) {
        AlertDialog(
            onDismissRequest = { showSosDemoDialog = false },
            title = { Text("SOS fired (demo)") },
            text = {
                Text(
                    "The hold control works, but this Phase 1 build sent " +
                        "nothing to anyone. The full SOS flow — cancel window, " +
                        "category, buddy notification and the 999 dialler — " +
                        "lands in Phase 4."
                )
            },
            confirmButton = {
                TextButton(onClick = { showSosDemoDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun Header(isLive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                append("Bar")
                withStyle(SpanStyle(color = Gold)) { append("Sentinel") }
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = if (isLive) Success else Gold
        ) {
            Text(
                text = if (isLive) "● LIVE" else "DEMO",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp,
                color = Navy900
            )
        }
    }
}

@Composable
private fun NoEnvelopeContent(
    onStartEnvelope: () -> Unit,
    onOpenScenarios: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenTimeline: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text("Good evening", color = TextMuted, fontSize = 14.sp)
    Text(
        "Advocate",
        color = TextPrimary,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold
    )
    Spacer(modifier = Modifier.height(14.dp))
    GlassColumnCard(modifier = Modifier.fillMaxWidth()) {
        ShieldHero(
            modifier = Modifier
                .size(148.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No live envelope",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = TextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Start one before your next engagement.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
    Spacer(modifier = Modifier.height(14.dp))
    GoldButton(text = "Start an envelope →", onClick = onStartEnvelope)
    Spacer(modifier = Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MiniCard(
            title = "Templates",
            subtitle = "Client meeting · Site visit",
            onClick = onStartEnvelope,
            modifier = Modifier.weight(1f)
        )
        MiniCard(
            title = "Scenario tester",
            subtitle = "30 cases · all passing",
            onClick = onOpenScenarios,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    InfoCard(
        icon = Icons.Filled.Lock,
        title = "Evidence vault",
        subtitle = "${DemoData.VAULT_ITEM_COUNT} items · hash chain valid",
        onClick = onOpenVault
    )
    Spacer(modifier = Modifier.height(10.dp))
    InfoCard(
        icon = Icons.Filled.History,
        title = "Last envelope",
        subtitle = DemoData.LAST_ENVELOPE_LABEL,
        onClick = onOpenTimeline
    )
}

@Composable
private fun LiveContent(
    envelope: DemoEnvelope,
    onCheckIn: () -> Unit,
    onExtend: () -> Unit,
    onClose: () -> Unit,
    onOpenTimeline: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        CountdownRing(
            minutesLeft = envelope.minutesLeft,
            fractionLeft = envelope.fractionLeft
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Envelope live",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${envelope.type} · ${envelope.area}",
                color = TextSecondary,
                fontSize = 12.5.sp
            )
            Text(
                "Check-in ${envelope.checkInCurrent} of ${envelope.checkInTotal} · ${envelope.intervalMinutes} min",
                color = TextSecondary,
                fontSize = 12.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            CheckInDots(
                total = envelope.checkInTotal,
                current = envelope.checkInCurrent
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.weight(1.35f)) {
            GoldButton(text = "Check in", onClick = onCheckIn)
        }
        GhostButton(text = "Extend", onClick = onExtend, modifier = Modifier.weight(1f))
        GhostButton(text = "Close", onClick = onClose, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(10.dp))
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    Brush.linearGradient(listOf(InfoBlue, AvatarViolet)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                envelope.buddyInitial,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${envelope.buddyName} is watching",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                buildAnnotatedString {
                    append("Accepted plan v${envelope.acceptedVersion} · ${envelope.acceptedAt} · ")
                    withStyle(SpanStyle(color = Success, fontWeight = FontWeight.Bold)) {
                        append("Sees status only — no map")
                    }
                },
                color = TextMuted,
                fontSize = 12.5.sp
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    InfoCard(
        icon = Icons.Filled.LocationOn,
        title = "Location escrow",
        subtitle = "Precise location stays on this phone until a missed check-in or SOS.",
        onClick = null
    )
    Spacer(modifier = Modifier.height(10.dp))
    InfoCard(
        icon = Icons.Filled.Shield,
        title = "Timeline · ${envelope.timelineEvents} events",
        subtitle = "Hash chain valid · last verified just now",
        onClick = onOpenTimeline
    )
}

@Composable
private fun CheckInDots(total: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(total) { index ->
            val color = when {
                index < current - 1 -> Success
                index == current - 1 -> Gold
                else -> Color.White.copy(alpha = 0.16f)
            }
            Box(
                modifier = Modifier
                    .size(width = 26.dp, height = 6.dp)
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun MiniCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassColumnCard(modifier = modifier) {
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(GoldGlass, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextMuted, fontSize = 12.5.sp)
        }
        if (onClick != null) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextFaint
            )
        }
    }
}

@Composable
private fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50)
    ) {
        Text(text, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DemoPreviewSwitcher(
    isLive: Boolean,
    onPreviewEmpty: () -> Unit,
    onPreviewLive: () -> Unit,
    onOpenSimulation: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "DEMO PREVIEW — SWITCH HOME STATE",
            color = TextFaint,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.4.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onPreviewEmpty) {
                Text(
                    "No envelope",
                    color = if (!isLive) Gold else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = onPreviewLive) {
                Text(
                    "Envelope live",
                    color = if (isLive) Gold else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpenSimulation) {
                Text("Simulated buddy", color = TextMuted, fontSize = 12.sp)
            }
            TextButton(onClick = onOpenPrivacy) {
                Text("Privacy centre", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}
