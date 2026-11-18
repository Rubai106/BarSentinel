package com.barsentinel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barsentinel.ui.navigation.Routes
import com.barsentinel.ui.theme.GlassBorder
import com.barsentinel.ui.theme.GlassSurfaceStrong
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.TextMuted

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val ITEMS = listOf(
    BottomItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomItem(Routes.ENVELOPES, "Envelopes", Icons.Filled.Email),
    BottomItem(Routes.VAULT, "Vault", Icons.Filled.Lock),
    BottomItem(Routes.MORE, "More", Icons.Filled.MoreHoriz)
)

/**
 * Floating pill bottom bar, as in the approved mockup. Only the four
 * advocate destinations; the buddy view is reached through envelopes.
 */
@Composable
fun BarSentinelBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        shape = RoundedCornerShape(50),
        color = GlassSurfaceStrong,
        border = BorderStroke(1.dp, GlassBorder),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ITEMS.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label, fontSize = 10.5.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold,
                        selectedTextColor = Gold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
