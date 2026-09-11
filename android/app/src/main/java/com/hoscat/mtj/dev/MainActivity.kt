package com.hoscat.mtj.dev

import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mtj.design.MtjTheme
import com.mtj.design.MtjThemeMode
import com.mtj.design.MtjBottomActionScaffold
import com.mtj.design.MtjQuietPanel
import com.mtj.design.MtjSectionHeader
import com.hoscat.core.model.*
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch
import mtj.saju.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var themeMode by remember { mutableStateOf(readThemeMode(context)) }
            val darkTheme = when (themeMode) {
                MtjThemeMode.System -> isSystemInDarkTheme()
                MtjThemeMode.Light -> false
                MtjThemeMode.Dark -> true
            }
            SideEffect {
                val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                window.insetsController?.setSystemBarsAppearance(if (darkTheme) 0 else mask, mask)
            }
            MtjTheme(themeMode) {
                MtjApp(
                    themeMode = themeMode,
                    onThemeModeChanged = { themeMode = it },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MtjApp(
    themeMode: MtjThemeMode,
    onThemeModeChanged: (MtjThemeMode) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabStateHolder = rememberSaveableStateHolder()
    var tarotQuestion by rememberSaveable { mutableStateOf("") }
    var selectedRecordId by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var aliasError by rememberSaveable { mutableStateOf(false) }
    val aliasFocusRequester = remember { FocusRequester() }
    val aliasBringIntoViewRequester = remember { BringIntoViewRequester() }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var birthTime by rememberSaveable { mutableStateOf("") }
    var unknown by rememberSaveable { mutableStateOf(true) }
    var showBirthInputErrors by rememberSaveable { mutableStateOf(false) }
    val birthDateFocusRequester = remember { FocusRequester() }
    val birthTimeFocusRequester = remember { FocusRequester() }
    val birthDateBringIntoViewRequester = remember { BringIntoViewRequester() }
    val birthTimeBringIntoViewRequester = remember { BringIntoViewRequester() }
    var lunar by rememberSaveable { mutableStateOf(false) }
    var leap by rememberSaveable { mutableStateOf(false) }
    var showSolarDatePicker by rememberSaveable { mutableStateOf(false) }
    var gender by rememberSaveable { mutableIntStateOf(2) }
    var busy by remember { mutableStateOf(false) }
    var evaluation by remember { mutableStateOf<MtjSajuEvaluation?>(null) }
    val context = LocalContext.current
    val runtime = remember { MtjSajuRuntime(context) }
    val store = remember { RecordStore(context) }
    val saveBatch = remember(evaluation) { (evaluation as? MtjSajuEvaluation.Accepted)?.let(RecordSnapshots::saju) }
    var saveMessage by remember(evaluation) { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val chart = (evaluation as? MtjSajuEvaluation.Accepted)?.chart
    val today = remember { LocalDate.now(ZoneId.of(KOREA_STANDARD_TIME_ZONE_ID)) }
    val dateValidation = remember(birthDate, lunar, today, showBirthInputErrors) {
        if (showBirthInputErrors) {
            validateBirthDateText(birthDate, today, if (lunar) CalendarType.Lunar else CalendarType.Solar)
        } else {
            BirthDateValidation()
        }
    }
    val timeValidation = remember(birthTime, unknown, showBirthInputErrors) {
        if (showBirthInputErrors) validateBirthTimeText(birthTime, unknown) else BirthTimeValidation()
    }
    val solarDatePickerState = rememberDatePickerState(
        yearRange = 1900..today.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcMillisToSupportedSolarDate(utcTimeMillis, today) != null

            override fun isSelectableYear(year: Int): Boolean = year in 1900..today.year
        },
    )
    if (showSolarDatePicker) {
        val selectedSolarDate = solarDatePickerState.selectedDateMillis
            ?.let { utcMillisToSupportedSolarDate(it, today) }
        DatePickerDialog(
            onDismissRequest = { showSolarDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = selectedSolarDate != null,
                    onClick = {
                        selectedSolarDate?.let { selectedDate ->
                            birthDate = "%04d%02d%02d".format(
                                selectedDate.year,
                                selectedDate.monthValue,
                                selectedDate.dayOfMonth,
                            )
                        }
                        showSolarDatePicker = false
                    },
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showSolarDatePicker = false }) { Text("취소") }
            },
        ) { DatePicker(state = solarDatePickerState) }
    }
    if (tab == 0) {
        MtjHomeScreen(
            chart = chart,
            store = store,
            question = tarotQuestion,
            onQuestionChange = { tarotQuestion = it },
            onStartTarot = { tab = 2 },
            onOpenRecord = { recordId ->
                selectedRecordId = recordId
                tab = 3
            },
            onNavigate = { tab = it },
        )
        return
    }
    if (tab == 2) {
        tabStateHolder.SaveableStateProvider("tarot") {
            TarotScreen(
                store = store,
                question = tarotQuestion,
                onQuestionChange = { tarotQuestion = it },
                onTabSelected = { tab = it },
            )
        }
        return
    }
    if (tab == 3) {
        RecordsScreen(store, selectedRecordId) { nextTab ->
            if (nextTab != 3) selectedRecordId = null
            tab = nextTab
        }
        return
    }
    if (tab == 4) {
        SettingsScreen(
            themeMode = themeMode,
            onThemeModeChanged = onThemeModeChanged,
            onTabSelected = { tab = it },
        )
        return
    }
    MtjBottomActionScaffold(tab, { tab = it }, showActionDock = chart == null, actions = {
        if (tab == 1 && chart == null) Button(
            onClick = {
                if (name.isBlank()) {
                    aliasError = true
                    scope.launch {
                        aliasFocusRequester.requestFocus()
                        aliasBringIntoViewRequester.bringIntoView()
                    }
                } else {
                    aliasError = false
                    val submittedDate = validateBirthDateText(birthDate, today, if (lunar) CalendarType.Lunar else CalendarType.Solar)
                    val submittedTime = validateBirthTimeText(birthTime, unknown)
                    if (submittedDate.hasError || submittedTime.hasErrors) {
                        showBirthInputErrors = true
                        scope.launch {
                            if (submittedDate.hasError) {
                                birthDateFocusRequester.requestFocus()
                                birthDateBringIntoViewRequester.bringIntoView()
                            } else if (submittedTime.hasErrors) {
                                birthTimeFocusRequester.requestFocus()
                                birthTimeBringIntoViewRequester.bringIntoView()
                            }
                        }
                    } else {
                        showBirthInputErrors = false
                        busy = true
                        scope.launch {
                            try {
                                evaluation = runtime.evaluate(BirthInputDraft(
                                    name = name,
                                    year = submittedDate.year,
                                    month = submittedDate.month,
                                    day = submittedDate.day,
                                    hour = if (unknown) "" else submittedTime.hour,
                                    minute = if (unknown) "" else submittedTime.minute,
                                    calendarType = if (lunar) CalendarType.Lunar else CalendarType.Solar,
                                    isLeapMonth = lunar && leap,
                                    gender = listOf(Gender.Female, Gender.Male, Gender.Unknown)[gender],
                                ))
                            } finally { busy = false }
                        }
                    }
                }
            }, enabled = !busy,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) { Text(if (busy) "계산 중" else "내 명식 보기") }
    }) { padding ->
        val contentScroll = if (chart == null) {
            Modifier.verticalScroll(rememberScrollState())
        } else {
            Modifier
        }
        Column(Modifier.fillMaxSize().then(contentScroll).padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (chart == null) {
                MtjQuietPanel {
                    MtjSectionHeader(listOf("샤로먕", "나의 사주", "지금의 선택", "기록", "설정")[tab], eyebrow = "샤로먕")
                }
            }
            when (tab) {
                1 -> {
                    if (chart == null) {
                        MtjQuietPanel {
                        MtjSectionHeader("기본 정보", eyebrow = "명식 기준")
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (it.isNotBlank()) aliasError = false
                            },
                            label = { Text("별칭") },
                            supportingText = {
                                Column {
                                    Text("실명 대신 편한 별칭을 사용해도 됩니다.")
                                    if (aliasError) Text(
                                        "별칭을 입력해주세요.",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            isError = aliasError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(aliasBringIntoViewRequester)
                                .focusRequester(aliasFocusRequester),
                        )
                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { birthDate = it.filter(Char::isDigit).take(8) },
                            label = { Text("생년월일") },
                            placeholder = { Text("YYYYMMDD") },
                            supportingText = {
                                Text(birthDateDisplayError(dateValidation.error) ?: "YYYYMMDD · 예: 19900123")
                            },
                            isError = dateValidation.error != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(birthDateBringIntoViewRequester)
                                .focusRequester(birthDateFocusRequester),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = !lunar,
                                onClick = { lunar = false; leap = false },
                                label = { Text("양력") },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            )
                            FilterChip(
                                selected = lunar,
                                onClick = { lunar = true },
                                label = { Text("음력") },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            )
                        }
                        if (!lunar) TextButton(onClick = {
                            val seed = validateBirthDateText(birthDate, today, CalendarType.Solar)
                            val seedMillis = if (seed.hasError) null else typedSolarDateToSupportedUtcMillis(seed.year, seed.month, seed.day, today)
                            solarDatePickerState.selectedDateMillis = seedMillis
                            solarDatePickerState.displayedMonthMillis = seedMillis ?: today.toUtcMillis()
                            showSolarDatePicker = true
                        }, modifier = Modifier.heightIn(min = 48.dp)) { Text("양력 날짜 선택") }
                        if (lunar) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .toggleable(
                                        value = leap,
                                        role = Role.Checkbox,
                                        onValueChange = { leap = it },
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(leap, onCheckedChange = null)
                                Text("윤달")
                            }
                        }
                    }
                        MtjQuietPanel {
                        MtjSectionHeader("시간과 성별", eyebrow = "해석 옵션")
                        OutlinedTextField(
                            value = birthTime,
                            onValueChange = { birthTime = it.filter(Char::isDigit).take(4) },
                            label = { Text("태어난 시간") },
                            placeholder = { Text("HHmm") },
                            supportingText = {
                                Text(
                                    if (unknown) "시간 모름"
                                    else birthTimeDisplayError(timeValidation.primaryError) ?: "HHmm · 예: 0930",
                                )
                            },
                            isError = !unknown && timeValidation.hasErrors,
                            enabled = !unknown,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(birthTimeBringIntoViewRequester)
                                .focusRequester(birthTimeFocusRequester),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .toggleable(
                                    value = unknown,
                                    role = Role.Checkbox,
                                    onValueChange = {
                                        unknown = it
                                        showBirthInputErrors = false
                                    },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(unknown, onCheckedChange = null)
                            Text("시간 모름")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("여성", "남성", "미상").forEachIndexed { i, label ->
                                FilterChip(
                                    selected = gender == i,
                                    onClick = { gender = i },
                                    label = { Text(label) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                )
                            }
                        }
                        Text("대한민국 표준시 기준", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                        saveMessage?.let { Text(it) }
                        (evaluation as? MtjSajuEvaluation.Rejected)?.let {
                            Text(it.message, color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        MtjQuietPanel {
                            SajuChartDisplay(chart)
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                onClick = {
                                    evaluation = null
                                    saveMessage = null
                                },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            ) { Text("정보 수정") }
                            TextButton(
                                onClick = {
                                    val batch = saveBatch ?: return@TextButton
                                    saving = true
                                    scope.launch {
                                        try {
                                            store.insert(batch)
                                            saveMessage = "기록에 저장했습니다."
                                        } catch (e: kotlinx.coroutines.CancellationException) {
                                            throw e
                                        } catch (_: Exception) {
                                            saveMessage = "저장하지 못했습니다. 다시 시도해주세요."
                                        } finally {
                                            saving = false
                                        }
                                    }
                                },
                                enabled = !saving,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            ) { Text(if (saving) "저장 중" else "명식 저장") }
                        }
                        saveMessage?.let { Text(it) }
                    }
                }
                2 -> Text("타로 리딩을 준비하고 있습니다.")
                3 -> Text("아직 저장된 기록이 없습니다.")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
