package com.barsentinel.ui.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.Navy900
import com.barsentinel.ui.theme.TextMuted
import com.barsentinel.ui.theme.TextPrimary
import com.barsentinel.ui.theme.TextSecondary

/**
 * Honest stand-in for a screen whose phase has not landed yet. It names the
 * phase, describes what the screen will do, and states plainly that nothing
 * here sends, stores or shares real data. Placeholders never fake a feature.
 */
@Composable
fun PhasePlaceholderScreen(
    title: String,
    phase: String,
    description: String,
    onBack: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Construction,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Surface(shape = RoundedCornerShape(50), color = Gold) {
                Text(
                    text = phase.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    color = Navy900
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                title,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                description,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Not built yet. Nothing on this screen sends, stores or shares real data.",
                color = TextMuted,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
