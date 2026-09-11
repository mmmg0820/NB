package com.hoscat.mtj.dev

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SettingsScreenTest {
    @Test fun settingsGroupsMatchDesignContractOrder() {
        assertEquals(
            listOf("화면", "리딩", "정보"),
            settingsGroups.map { it.title },
        )
    }

    @Test fun settingsRowsMatchAccessibilityContractOrder() {
        assertEquals(
            listOf("화면 모드", "움직임 줄이기", "역방향 포함", "앱 정보"),
            settingsGroups.flatMap { it.rows }.map { it.label },
        )
        assertEquals(
            listOf("RadioGroup", "Switch", "Switch", "Text"),
            settingsGroups.flatMap { it.rows }.map { it.roleName },
        )
    }

    @Test fun settingsDoNotExposeRejectedCandidateSources() {
        val text = settingsGroups.flatMap { group ->
            listOf(group.title) + group.rows.flatMap { row ->
                listOf(row.label, row.contentDescription, row.stateDescription)
            }
        }.joinToString("\n")
        assertFalse(text.contains("Zeekr", ignoreCase = true))
        assertFalse(text.contains("Oracle", ignoreCase = true))
    }
}
