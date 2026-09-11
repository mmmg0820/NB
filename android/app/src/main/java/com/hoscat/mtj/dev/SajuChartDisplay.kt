package com.hoscat.mtj.dev

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hoscat.core.model.DataTrustLevel
import com.hoscat.core.model.GanjiGlyphKind
import com.hoscat.core.model.Pillar
import com.hoscat.core.model.SajuChart
import com.hoscat.core.model.toGanjiHanja
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class SajuPillarDisplay(
    val role: String,
    val pillar: Pillar?,
    val isDayMaster: Boolean = false,
    val isUnknownHour: Boolean = false,
) {
    val stemText: String = glyphLine(pillar?.stem, GanjiGlyphKind.HeavenlyStem)
    val branchText: String = glyphLine(pillar?.branch, GanjiGlyphKind.EarthlyBranch)
    val stemHanjaText: String = glyphHanja(pillar?.stem, GanjiGlyphKind.HeavenlyStem)
    val stemKoreanText: String? = pillar?.stem
    val branchHanjaText: String = glyphHanja(pillar?.branch, GanjiGlyphKind.EarthlyBranch)
    val branchKoreanText: String? = pillar?.branch
    val elementText: String = when {
        isUnknownHour || pillar == null -> "오행 산출 제외"
        else -> "천간 ${stemElementName(pillar.stem)} · 지지 ${branchElementName(pillar.branch)}"
    }
    val stateText: String? = if (isUnknownHour) "시간 모름" else null
}

internal fun sajuChartPillarDisplays(chart: SajuChart): List<SajuPillarDisplay> = listOf(
    SajuPillarDisplay("연주", chart.yearPillar),
    SajuPillarDisplay("월주", chart.monthPillar),
    SajuPillarDisplay("일주", chart.dayPillar, isDayMaster = true),
    SajuPillarDisplay("시주", chart.hourPillar, isUnknownHour = chart.hourPillar == null),
)

internal fun sajuPillarColumnCount(
    maxWidth: Dp,
    measuredTileWidth: Dp,
    fontScale: Float = 1f,
): Int = when {
    fontScale >= 1.3f -> 2
    maxWidth >= 320.dp -> 4
    (measuredTileWidth * 4) + (8.dp * 3) <= maxWidth -> 4
    else -> 2
}

internal enum class SajuPillarGridMode { Standard, Compact }

internal fun sajuPillarGridMode(maxWidth: Dp, fontScale: Float): SajuPillarGridMode =
    if (maxWidth < 360.dp || fontScale >= 1.3f) SajuPillarGridMode.Compact else SajuPillarGridMode.Standard

internal fun sajuPillarCellWeight(display: SajuPillarDisplay, mode: SajuPillarGridMode): Float =
    if (display.isUnknownHour && mode == SajuPillarGridMode.Standard) 1.35f else 1f

internal fun sajuPillarFooterText(display: SajuPillarDisplay, mode: SajuPillarGridMode): String? =
    display.elementText.takeIf {
        !display.isUnknownHour && (mode == SajuPillarGridMode.Standard || display.isDayMaster)
    }

internal fun sajuChartAuthorityLabel(chart: SajuChart): String = when {
    chart.evidence.isVerified && chart.evidence.trustLevel == DataTrustLevel.ExternalAuthorityVerified -> "외부 기관 검증됨"
    chart.evidence.isVerified -> "내부 검증됨"
    chart.evidence.trustLevel == DataTrustLevel.InternalStructureChecked -> "내부 구조 검토됨"
    else -> "검토 필요"
}

internal fun sajuChartBasisLabel(chart: SajuChart): String =
    chart.evidence.calculationBasisLabel

internal fun sajuCalculationDetails(chart: SajuChart): String = listOfNotNull(
    sajuChartBasisLabel(chart),
    chart.evidence.previousSolarTermName?.let { "이전 절입: $it" },
    chart.evidence.previousSolarTermAtKst?.let { raw ->
        val formatted = runCatching {
            OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm:ss", Locale.KOREAN))
        }.getOrNull()
        formatted?.let { "절입 시각: $it (한국 표준시, UTC+09:00)" }
            ?: "절입 시각을 표시할 수 없습니다."
    },
).joinToString("\n")

private fun glyphLine(value: String?, kind: GanjiGlyphKind): String =
    value?.let { "${it.toGanjiHanja(kind)} $it" } ?: "모름"

private fun glyphHanja(value: String?, kind: GanjiGlyphKind): String =
    value?.toGanjiHanja(kind) ?: ""

private fun stemElementName(stem: String): String = when (stem) {
    "갑", "을" -> "목"
    "병", "정" -> "화"
    "무", "기" -> "토"
    "경", "신" -> "금"
    "임", "계" -> "수"
    else -> "미상"
}

private fun branchElementName(branch: String): String = when (branch) {
    "인", "묘" -> "목"
    "사", "오" -> "화"
    "진", "술", "축", "미" -> "토"
    "신", "유" -> "금"
    "자", "해" -> "수"
    else -> "미상"
}

@Composable
internal fun SajuChartDisplay(chart: SajuChart, modifier: Modifier = Modifier) {
    val displays = sajuChartPillarDisplays(chart)
    var showCalculationDetails by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier.fillMaxWidth().testTag("saju-chart-display"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("명식", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${chart.name}님",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                sajuChartAuthorityLabel(chart),
                modifier = Modifier.testTag("saju-authority-state"),
                style = MaterialTheme.typography.labelSmall,
                color = if (chart.evidence.isVerified) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
            )
        }
        Column(
            modifier = Modifier.testTag("saju-day-master"),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("일간", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${chart.dayPillar.stem.toGanjiHanja(GanjiGlyphKind.HeavenlyStem)} ${chart.dayPillar.stem}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "${stemElementName(chart.dayPillar.stem)} 일간",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PillarGrid(displays)
        Text(
            sajuChartBasisLabel(chart),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("saju-calculation-basis"),
        )
        TextButton(
            onClick = { showCalculationDetails = true },
            modifier = Modifier.heightIn(min = 48.dp).testTag("saju-calculation-details"),
        ) { Text("계산 기준") }
    }
    if (showCalculationDetails) {
        AlertDialog(
            onDismissRequest = { showCalculationDetails = false },
            title = { Text("계산 기준") },
            text = { Text(sajuCalculationDetails(chart)) },
            confirmButton = {
                TextButton(onClick = { showCalculationDetails = false }) { Text("닫기") }
            },
        )
    }
}

@Composable
private fun PillarGrid(displays: List<SajuPillarDisplay>) {
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("saju-pillar-grid")) {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        val hanjaStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        val koreanStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        val unknownStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        val supportingStyle = MaterialTheme.typography.labelSmall
        val mode = sajuPillarGridMode(maxWidth, density.fontScale)
        val measuredTileWidth = remember(displays, density, mode, labelStyle, hanjaStyle, koreanStyle, unknownStyle, supportingStyle) {
            val measuredPx = displays.flatMap { display ->
                val base = listOf(TextProbe(display.role, labelStyle)) +
                    if (mode == SajuPillarGridMode.Standard || display.isUnknownHour) {
                        listOf(TextProbe(display.elementText, supportingStyle))
                    } else {
                        emptyList()
                    }
                base + if (display.isUnknownHour) {
                    listOf(TextProbe("시간 모름", unknownStyle))
                } else {
                    listOfNotNull(
                        display.stemHanjaText.takeIf { it.isNotBlank() }?.let { TextProbe(it, hanjaStyle) },
                        display.stemKoreanText?.let { TextProbe(it, koreanStyle) },
                        display.branchHanjaText.takeIf { it.isNotBlank() }?.let { TextProbe(it, hanjaStyle) },
                        display.branchKoreanText?.let { TextProbe(it, koreanStyle) },
                    )
                }
            }.maxOf { probe -> textMeasurer.measure(probe.text, style = probe.style).size.width }
            with(density) { measuredPx.toDp() } + 24.dp
        }
        val columns = sajuPillarColumnCount(maxWidth, measuredTileWidth, density.fontScale)
        val tileMinHeight = if (mode == SajuPillarGridMode.Compact) 104.dp else 112.dp

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            displays.chunked(columns).forEach { rowDisplays ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowDisplays.forEach { display ->
                        SajuPillarTile(
                            display,
                            mode,
                            Modifier
                                .weight(sajuPillarCellWeight(display, mode))
                                .heightIn(min = tileMinHeight),
                        )
                    }
                }
            }
        }
    }
}

private data class TextProbe(val text: String, val style: TextStyle)

@Composable
private fun SajuPillarTile(
    display: SajuPillarDisplay,
    mode: SajuPillarGridMode,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .testTag("saju-pillar-${display.role}")
            .semantics {
                contentDescription = if (display.isUnknownHour) {
                    "${display.role}, 시간 모름, ${display.elementText}"
                } else {
                    "${display.role}, ${display.stemText}, ${display.branchText}, ${display.elementText}"
                }
            },
        shape = RoundedCornerShape(8.dp),
        color = when {
            display.isDayMaster -> MaterialTheme.colorScheme.secondaryContainer
            display.isUnknownHour -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = if (mode == SajuPillarGridMode.Compact) 8.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (mode == SajuPillarGridMode.Compact) 3.dp else 5.dp),
        ) {
            Text(display.role, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            if (display.isUnknownHour) {
                UnavailablePillarState(display, mode)
            } else {
                PillarGlyph(display.stemHanjaText, display.stemKoreanText.orEmpty(), mode)
                PillarGlyph(display.branchHanjaText, display.branchKoreanText.orEmpty(), mode)
            }
            sajuPillarFooterText(display, mode)?.let { footerText ->
                Text(
                    footerText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun UnavailablePillarState(display: SajuPillarDisplay, mode: SajuPillarGridMode) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            "시간 모름",
            style = (if (mode == SajuPillarGridMode.Compact) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.titleLarge).copy(lineBreak = LineBreak.Heading),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            display.elementText,
            style = MaterialTheme.typography.labelSmall.copy(lineBreak = LineBreak.Heading),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PillarGlyph(hanja: String, korean: String, mode: SajuPillarGridMode) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            hanja,
            style = if (mode == SajuPillarGridMode.Compact) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
        Text(
            korean,
            style = if (mode == SajuPillarGridMode.Compact) MaterialTheme.typography.labelMedium
            else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}
