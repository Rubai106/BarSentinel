package com.barsentinel.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.Navy700
import com.barsentinel.ui.theme.Navy800

/**
 * The 3D hero moment, drawn in code. The approved mockup's shield is a
 * rendered artwork; the in-app hero is this original Canvas drawing — navy
 * glass fill, gold rim, engraved check — so the app needs no bitmap asset
 * and the shield stays crisp at any size. It is decorative: the status
 * text beside it carries the meaning.
 */
@Composable
fun ShieldHero(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.05f)
            lineTo(w * 0.83f, h * 0.17f)
            lineTo(w * 0.83f, h * 0.50f)
            cubicTo(w * 0.83f, h * 0.73f, w * 0.68f, h * 0.87f, w * 0.50f, h * 0.96f)
            cubicTo(w * 0.32f, h * 0.87f, w * 0.17f, h * 0.73f, w * 0.17f, h * 0.50f)
            lineTo(w * 0.17f, h * 0.17f)
            close()
        }
        drawPath(
            path = shield,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF2A4A7F), Navy700, Navy800),
                center = Offset(w * 0.35f, h * 0.28f),
                radius = w * 0.85f
            )
        )
        // Soft top-left highlight, like light on glass.
        drawPath(
            path = Path().apply {
                moveTo(w * 0.50f, h * 0.05f)
                lineTo(w * 0.83f, h * 0.17f)
                lineTo(w * 0.83f, h * 0.42f)
                cubicTo(w * 0.70f, h * 0.30f, w * 0.55f, h * 0.22f, w * 0.30f, h * 0.16f)
                lineTo(w * 0.17f, h * 0.17f)
                close()
            },
            color = Color.White.copy(alpha = 0.07f)
        )
        drawPath(
            path = shield,
            color = Gold,
            style = Stroke(width = w * 0.028f, join = StrokeJoin.Round)
        )
        val check = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.60f)
            lineTo(w * 0.66f, h * 0.38f)
        }
        drawPath(
            path = check,
            color = Gold,
            style = Stroke(
                width = w * 0.032f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
