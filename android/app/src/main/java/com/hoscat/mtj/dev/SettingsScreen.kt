package com.hoscat.mtj.dev

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.mtj.design.MtjBottomActionScaffold
import com.mtj.design.MtjSectionHeader
import com.mtj.design.MtjThemeMode

internal data class SettingsGroup(val title: String, val rows: List<SettingsRowContract>)

internal data class SettingsRowContract(
    val label: String,
    val contentDescription: String,
    val roleName: String,
    val stateDescription: String,
)

internal val settingsGroups = listOf(
    SettingsGroup(
        "화면",
        listOf(SettingsRowContract("화면 모드", "화면 모드 설정", "RadioGroup", "시스템, 라이트, 다크")),
    ),
    SettingsGroup(
        "리딩",
        listOf(
            SettingsRowContract("움직임 줄이기", "움직임 줄이기 설정", "Switch", "꺼짐"),
            SettingsRowContract("역방향 포함", "타로 역방향 포함", "Switch", "꺼짐"),
        ),
    ),
    SettingsGroup(
        "정보",
        listOf(SettingsRowContract("앱 정보", "샤로먕 앱 정보", "Text", "0.1-dev")),
    ),
)

internal fun readThemeMode(context: Context): MtjThemeMode {
    val raw = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getString(KEY_THEME_MODE, MtjThemeMode.System.name)
    return MtjThemeMode.values().firstOrNull { it.name == raw } ?: MtjThemeMode.System
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SettingsScreen(
    themeMode: MtjThemeMode,
    onThemeModeChanged: (MtjThemeMode) -> Unit,
    onTabSelected: (Int) -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE) }
    var reduceMotion by remember { mutableStateOf(preferences.getBoolean(KEY_REDUCE_MOTION, false)) }
    var includeReversed by remember { mutableStateOf(preferences.getBoolean(KEY_INCLUDE_REVERSED, false)) }

    MtjBottomActionScaffold(4, onTabSelected, actions = {}) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item { MtjSectionHeader("설정") }
            item {
                SettingsSection("화면") {
                    Text("화면 모드", style = MaterialTheme.typography.titleSmall)
                    Text(
                        when (themeMode) {
                            MtjThemeMode.System -> "기기 설정에 맞춤"
                            MtjThemeMode.Light -> "라이트"
                            MtjThemeMode.Dark -> "다크"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        val modes = listOf(
                            MtjThemeMode.System to "시스템",
                            MtjThemeMode.Light to "라이트",
                            MtjThemeMode.Dark to "다크",
                        )
                        modes.forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = {
                                    preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
                                    onThemeModeChanged(mode)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) { Text(label) }
                        }
                    }
                }
            }
            item {
                SettingsSection("리딩") {
                    SettingsSwitchRow(
                        label = "움직임 줄이기",
                        contentDescription = "움직임 줄이기 설정",
                        checked = reduceMotion,
                        onCheckedChange = {
                            reduceMotion = it
                            preferences.edit().putBoolean(KEY_REDUCE_MOTION, it).apply()
                        },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsSwitchRow(
                        label = "역방향 포함",
                        contentDescription = "타로 역방향 포함",
                        checked = includeReversed,
                        onCheckedChange = {
                            includeReversed = it
                            preferences.edit().putBoolean(KEY_INCLUDE_REVERSED, it).apply()
                        },
                    )
                }
            }
            item {
                SettingsSection("정보") {
                    Text("샤로먕", style = MaterialTheme.typography.titleSmall)
                    Text("버전 0.1-dev", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "사주와 타로 기록은 이 기기의 샤로먕 저장소에 보관됩니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        content()
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    contentDescription: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics {
                this.contentDescription = contentDescription
                stateDescription = if (checked) "켜짐" else "꺼짐"
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(if (checked) "켜짐" else "꺼짐", style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = null, modifier = Modifier.clearAndSetSemantics { })
    }
}

private const val SETTINGS_PREFS = "mtj-settings-v1"
private const val KEY_THEME_MODE = "theme-mode"
private const val KEY_REDUCE_MOTION = "reduce-motion"
private const val KEY_INCLUDE_REVERSED = "include-reversed"
