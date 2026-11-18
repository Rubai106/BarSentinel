package com.barsentinel.ui.privacy

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barsentinel.ui.theme.TextMuted
import com.barsentinel.ui.theme.TextPrimary
import com.barsentinel.ui.theme.TextSecondary

/**
 * Privacy centre (spec §9, §15). Shown before the first envelope and always
 * reachable. Every statement here must match the shipped system — if a
 * behaviour changes, this screen changes in the same release.
 */
@Composable
fun PrivacyCentreScreen(onBack: () -> Unit) {
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
                "Privacy centre",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "BarSentinel is built so you can check what it does. This screen " +
                    "states who sees what, when, and when it is deleted.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Section("Who sees what") {
                Body("During a healthy envelope, your buddy sees status only — for example, “Checked in 12 minutes ago, on schedule.” They see no map, no coordinates and no history.")
                Body("Your second contact sees nothing until the buddy has not acknowledged an escalation.")
                Body("Nobody else sees anything. There is no dashboard, no monitoring team and no analytics on safety events.")
            }
            Section("When your location is shared") {
                Body("Precise location is collected only while an envelope is active or SOS has fired. Only the latest fix is kept — no history is built.")
                Body("It is released to your buddy only when a check-in is missed or SOS fires, and the release is written to your timeline so you can review it afterwards.")
                Body("On safe closure, the escrowed location is deleted, and the deletion is logged.")
            }
            Section("What BarSentinel cannot do") {
                Body("It cannot guarantee an emergency-service response. SOS notifies your buddy and offers the 999 dialler — the response is theirs, not the app's.")
                Body("With no data signal and no cellular service anywhere in the chain, delivery cannot be guaranteed. The app says so on screen instead of pretending otherwise.")
                Body("The timeline proves the record has not changed since it was written. It does not prove the events themselves were true, and it does not establish court admissibility.")
                Body("No identity is verified. BarSentinel does not check Bar Council membership or any professional credential.")
            }
            Section("Permissions, when they are asked") {
                Body("Location — at first envelope activation, in context. If denied, envelopes can run without location escrow, and both you and your buddy see the reduced-protection label.")
                Body("Notifications — at first proposal. If denied, check-ins still work inside the app and the reduced reminders are stated.")
                Body("SMS — only when the escalation ladder first needs it. If denied, the ladder stops at data plus queue, and the state is shown honestly.")
                Body("Camera — at first evidence capture. Notes still work without it.")
            }
            Section("Your data") {
                Body("No client names, case numbers or matter details are required by any screen, and the app discourages entering them.")
                Body("Export and deletion of your data are available in Settings, with the consequences stated before you confirm.")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "This screen reflects the system as specified. Until a behaviour " +
                    "is built and measured, the project documents say “not yet measured” — " +
                    "see docs/measurements.md in the repository.",
                color = TextMuted,
                fontSize = 12.5.sp
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Spacer(modifier = Modifier.height(22.dp))
    Text(title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(6.dp))
    content()
}

@Composable
private fun Body(text: String) {
    Text(
        text,
        color = TextSecondary,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
