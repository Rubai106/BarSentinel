package com.barsentinel.ui.simulation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.GoldBorder
import com.barsentinel.ui.theme.GoldGlass
import com.barsentinel.ui.theme.Navy900
import com.barsentinel.ui.theme.TextMuted
import com.barsentinel.ui.theme.TextPrimary
import com.barsentinel.ui.theme.TextSecondary
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Simulated-buddy harness shell (spec §14, Phase 1 deliverable). A marker
 * can run a full escalation alone on one device: this screen plays the buddy
 * role. Every simulated screen carries a SIMULATION banner, simulated events
 * are marked in the log, and simulation data never mixes with real envelopes.
 *
 * Phase 1 scope: the harness UI and an in-memory simulation log. The full
 * loop — short intervals, dead-phone simulation, driving real envelopes —
 * lands with the escalation engine in Phase 4.
 */
@Composable
fun SimulatedBuddyScreen(onBack: () -> Unit) {
    val log = remember { mutableStateListOf<String>() }
    var step by remember { mutableStateOf(0) }
    val time = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun record(event: String) {
        log.add("${LocalTime.now().format(time)}  SIMULATION — $event")
    }

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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GoldGlass,
                border = BorderStroke(1.dp, GoldBorder)
            ) {
                Text(
                    "SIMULATION — NOTHING HERE CONTACTS A REAL PERSON",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Simulated buddy",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "This mode plays the buddy role on this device so one person can " +
                    "rehearse the whole escalation: acceptance, a missed check-in, " +
                    "the buddy-side alert, and the stand-down. Simulated events are " +
                    "marked, and simulation data never mixes with real envelopes.",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            SimulationButton("1 · Buddy accepts envelope v1", enabled = step == 0) {
                record("Buddy accepted envelope v1. Envelope is LIVE (simulated).")
                step = 1
            }
            SimulationButton("2 · Check-in missed, grace expires", enabled = step == 1) {
                record("Check-in missed. Grace expired. Buddy-side escalation Step 2 fires (simulated).")
                step = 2
            }
            SimulationButton("3 · Escrowed location released to buddy", enabled = step == 2) {
                record("Escrowed location released to buddy (simulated). Buddy now sees a map.")
                step = 3
            }
            SimulationButton("4 · Advocate checks in late — stand down", enabled = step == 3) {
                record("Late check-in. Escalation stood down. Release stays in the timeline (simulated).")
                step = 4
            }
            SimulationButton("Reset the rehearsal", enabled = step == 4) {
                log.clear()
                step = 0
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "SIMULATION LOG",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (log.isEmpty()) {
                Text("No simulated events yet.", color = TextMuted, fontSize = 13.sp)
            } else {
                log.forEach { entry ->
                    Text(
                        entry,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "The full simulation loop — short demo intervals, simulating a " +
                    "dead advocate phone, and driving real envelopes — lands with " +
                    "the escalation engine in Phase 4.",
                color = TextMuted,
                fontSize = 12.5.sp
            )
        }
    }
}

@Composable
private fun SimulationButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Navy900,
            disabledContainerColor = Gold.copy(alpha = 0.15f),
            disabledContentColor = TextMuted
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
