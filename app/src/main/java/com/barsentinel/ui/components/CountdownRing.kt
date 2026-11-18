package com.barsentinel.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.TextMuted
import com.barsentinel.ui.theme.TextPrimary

/**
 * Tide Guide-style countdown ring: one giant number, one calm arc.
 * [fractionLeft] is 1.0 at a fresh check-in window, 0.0 at the deadline.
 */
@Composable
fun CountdownRing(
    minutesLeft: Int,
    fractionLeft: Float,
    modifier: Modifier = Modifier,
    size: Dp = 116.dp
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = Gold,
                startAngle = -90f,
                sweepAngle = 360f * fractionLeft.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = minutesLeft.toString(),
                color = TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "MIN LEFT",
                color = TextMuted,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
