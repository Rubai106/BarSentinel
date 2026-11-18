package com.barsentinel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.barsentinel.ui.navigation.BarSentinelNavGraph
import com.barsentinel.ui.theme.BarSentinelTheme

/**
 * Single activity (spec §11). All screens are Compose destinations.
 * Phase 1 ships the Home screen in both states, the privacy centre and the
 * simulated-buddy shell; the remaining destinations are honest placeholders
 * that name the phase that will build them.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarSentinelTheme {
                BarSentinelNavGraph()
            }
        }
    }
}
