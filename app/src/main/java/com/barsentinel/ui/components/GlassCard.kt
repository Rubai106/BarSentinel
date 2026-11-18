package com.barsentinel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.barsentinel.ui.theme.GlassBorder
import com.barsentinel.ui.theme.GlassSurface
import com.barsentinel.ui.theme.BarSentinelShapes

/**
 * The Moonlitt glass card: translucent surface, hairline border, 26dp radius.
 * Content is a [RowScope] by default because most cards are icon + text + chevron.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val row: @Composable () -> Unit = {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            content = content
        )
    }
    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = BarSentinelShapes.large,
            color = GlassSurface,
            border = BorderStroke(1.dp, GlassBorder),
            onClick = onClick,
            content = row
        )
    } else {
        Surface(
            modifier = modifier,
            shape = BarSentinelShapes.large,
            color = GlassSurface,
            border = BorderStroke(1.dp, GlassBorder),
            content = row
        )
    }
}

/** Column variant of [GlassCard] for stacked content. */
@Composable
fun GlassColumnCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = BarSentinelShapes.large,
        color = GlassSurface,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            content = content
        )
    }
}
