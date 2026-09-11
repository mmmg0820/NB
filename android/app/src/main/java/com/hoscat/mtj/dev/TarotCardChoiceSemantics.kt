package com.hoscat.mtj.dev

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription

internal fun Modifier.tarotCardChoiceSemantics(
    position: Int,
    selectedIndex: Int,
    canSelect: Boolean,
): Modifier = clearAndSetSemantics {
    // Apply after clickable: retain its sole action and clear only descendant semantics.
    contentDescription = "카드 ${position + 1}"
    selected = selectedIndex >= 0
    stateDescription = when {
        selectedIndex >= 0 -> "${selectedIndex + 1}번째 선택"
        !canSelect -> "필요한 장수 선택 완료"
        else -> "선택 안 됨"
    }
    if (!canSelect) disabled()
}
