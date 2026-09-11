package com.mtj.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hoscat.mtj.dev.R

/**
 * Flow layout, not an overlay or a fixed bottom reservation.
 * Source: ../tokens.json actions/layout/type; dimensions dp, text sp.
 *
 * Host Activity: enableEdgeToEdge(), adjustResize; do not wrap this scaffold in
 * another system/IME padding owner. Target activity-compose is 1.10.1.
 * The bottom host owns navigationBars (or their union with IME), never their sum.
 * Content receives horizontal PaddingValues only: apply once to a scrollable
 * body's contentPadding or modifier. Its viewport already ends ABOVE the 18dp
 * spacer and measured actions. Do not reserve bottom chrome again.
 *
 * actions supplies unweighted, wrap-height children with no outer bottom padding.
 * For a primary Button use heightIn(min = MtjTokens.ActionMinHeight),
 * RoundedCornerShape(MtjTokens.ActionCorner), and contentPadding with token
 * horizontal/vertical padding. Do not use fixed height or single-line truncation.
 * The slot cannot override arbitrary caller-owned button sizing or transparency.
 *
 * Test tags expose independent bounds: mtj-content, mtj-content-action-gap,
 * mtj-actions, mtj-action-nav-gap, mtj-tabs, mtj-tab-0..4, mtj-bottom-host.
 * Verify 8dp actions.bottom -> tabs.top and separate 18dp content gap;
 * IME: tabs absent and action bottom 8dp above keyboard (settled inset).
 * Compilation, 1.0/1.5/2.0 font scale, IME animation/floating keyboard and
 * gesture/3-button navigation tests remain pending after module integration.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun MtjBottomActionScaffold(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    showActionDock: Boolean = true,
    showNavigation: Boolean = true,
    actions: @Composable ColumnScope.() -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    require(selectedTab in 0..4) { "selectedTab must be in 0..4" }
    val ime = WindowInsets.ime
    val imeVisible = WindowInsets.isImeVisible || ime.getBottom(LocalDensity.current) > 0
    val bottomInsets = WindowInsets.navigationBars.union(ime).only(WindowInsetsSides.Bottom)
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                ),
        ) {
            val side = when {
                maxWidth >= MtjTokens.WideBreakpoint -> MtjTokens.SideWide
                maxWidth >= MtjTokens.MediumBreakpoint -> MtjTokens.SideMedium
                else -> MtjTokens.SideCompact
            }
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                        .consumeWindowInsets(bottomInsets)
                        .testTag("mtj-content"),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(Modifier.widthIn(max = MtjTokens.ContentMaxWidth).fillMaxSize()) {
                        content(PaddingValues(horizontal = side))
                    }
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(bottomInsets)
                        .testTag("mtj-bottom-host"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (showActionDock) {
                        Spacer(
                            Modifier.fillMaxWidth().height(MtjTokens.ContentToActionGap)
                                .testTag("mtj-content-action-gap"),
                        )
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(MtjTokens.StateLine)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .testTag("mtj-bottom-state-line"),
                        )
                        Column(
                            Modifier
                                .widthIn(max = MtjTokens.ContentMaxWidth)
                                .fillMaxWidth()
                                .padding(horizontal = side, vertical = 10.dp)
                                .testTag("mtj-actions"),
                        ) {
                            ProvideTextStyle(MtjTokens.PrimaryAction) { actions() }
                        }
                        if (showNavigation) {
                            Spacer(
                                Modifier.fillMaxWidth().height(MtjTokens.ActionToNavigationGap)
                                    .testTag("mtj-action-nav-gap"),
                            )
                        }
                    } else if (showNavigation) {
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(MtjTokens.StateLine)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .testTag("mtj-bottom-state-line"),
                        )
                    }
                    if (showNavigation && !imeVisible) {
                        MtjTabs(selectedTab, onTabSelected)
                    }
                }
            }
        }
    }
}

@Composable
private fun MtjTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val labels = listOf("홈", "사주", "타로", "기록", "설정")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.DateRange,
        Icons.Default.Star,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.Settings,
    )
    Row(
        Modifier
            .widthIn(max = MtjTokens.ContentMaxWidth)
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .selectableGroup()
            .testTag("mtj-tabs"),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .heightIn(min = MtjTokens.NavigationMinHeight)
                    .clipToBounds()
                    .background(
                        MaterialTheme.colorScheme.surface,
                    )
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onTabSelected(index) },
                    )
                    .semantics {
                        contentDescription = "$label 탭"
                        stateDescription = if (selected) "선택됨" else "선택 안 됨"
                    }
                    .testTag("mtj-tab-$index"),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Spacer(
                        Modifier
                            .widthIn(min = 24.dp, max = 24.dp)
                            .height(3.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                            ),
                    )
                    Spacer(Modifier.height(5.dp))
                    if (index == 1 || index == 2) {
                        Icon(
                            painter = painterResource(
                                if (index == 1) R.drawable.ic_saju_chart else R.drawable.ic_tarot_cards,
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                        style = MtjTokens.Navigation,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        softWrap = true,
                    )
                }
            }
        }
    }
}
