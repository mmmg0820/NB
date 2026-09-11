package com.mtj.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun MtjSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        eyebrow?.let {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(
                    Modifier
                        .size(7.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                )
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
    }
}

@Composable
fun MtjQuietPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Shadow alone reads as a floating soft surface; pairing it with a hard border
    // doubled up as "box inside a box" (2026-09-11 UX follow-up), so the border is gone.
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MtjTokens.PanelCorner),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
    ) {
        Column(
            Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
fun MtjEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("아직 비어 있음", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
