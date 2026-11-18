package com.barsentinel.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barsentinel.ui.theme.Danger
import com.barsentinel.ui.theme.DangerBorder
import com.barsentinel.ui.theme.DangerGlass
import com.barsentinel.ui.theme.DangerMuted
import com.barsentinel.ui.theme.DangerText

/**
 * Deliberate SOS control (spec §10). Fires only after a continuous hold of
 * [holdDurationMs] — a tap can never fire it. Releasing early cancels, with
 * the progress resetting. The fill shows the hold progressing; haptics
 * confirm the press and the fire. The default hold is 2 seconds per the
 * spec; the mockup shows 3 seconds of copy — the setting lands in Phase 7
 * and the copy here states the actual hold.
 *
 * Accessibility: TalkBack users get the hold described, and the SOS screen
 * (Phase 4) provides the full accessible firing flow with a cancel window.
 */
@Composable
fun SosHoldButton(
    onFired: () -> Unit,
    modifier: Modifier = Modifier,
    holdDurationMs: Int = 2000
) {
    val haptics = LocalHapticFeedback.current
    val progress = remember { Animatable(0f) }
    var pressed by remember { mutableStateOf(false) }
    var fired by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (pressed) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(holdDurationMs, easing = LinearEasing)
            )
            if (!fired) {
                fired = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onFired()
            }
        } else {
            fired = false
            progress.snapTo(0f)
        }
    }

    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DangerGlass)
            .semantics {
                role = Role.Button
                contentDescription =
                    "Hold for SOS. Press and hold for ${holdDurationMs / 1000} seconds to fire. A tap does nothing."
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Progress fill, clipped to the pill.
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.value)
                .align(Alignment.CenterStart)
                .background(Danger.copy(alpha = 0.45f))
                .padding(vertical = 24.dp)
        )
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "HOLD FOR SOS",
                color = DangerText,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Press and hold ${holdDurationMs / 1000} seconds — never a tap",
                color = DangerMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
