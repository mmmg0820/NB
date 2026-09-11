package com.hoscat.mtj.dev

import mtj.records.Envelope
import mtj.records.Kind
import mtj.records.Origin
import mtj.records.Value
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordDetailRowsTest {
    @Test fun sajuRowsExposeSavedSnapshotIdentity() {
        val record = Envelope(
            Origin("mtj-test", "saju"),
            Kind.SAJU,
            Value.Obj(mapOf(
                "chart" to Value.Obj(mapOf(
                    "yearPillar" to pillar("연주", "갑", "자"),
                    "monthPillar" to pillar("월주", "을", "축"),
                    "dayPillar" to pillar("일주", "병", "인"),
                    "hourPillar" to Value.Null,
                    "dayMaster" to Value.Str("병"),
                    "evidence" to Value.Obj(mapOf(
                        "calculationBasisLabel" to Value.Str("대한민국 표준시"),
                        "policyCode" to Value.Str("myeongri_kr_v2"),
                        "dataVersion" to Value.Str("asset-v1"),
                        "trustLevel" to Value.Str("GeneratedUnverified"),
                        "isVerified" to Value.Bool(false),
                    )),
                )),
                "input" to Value.Obj(mapOf(
                    "birthDateTime" to Value.Obj(mapOf(
                        "year" to Value.Num("2000"),
                        "month" to Value.Num("1"),
                        "day" to Value.Num("2"),
                        "hour" to Value.Null,
                        "minute" to Value.Num("0"),
                    )),
                )),
            )),
            profileRefs = listOf(Origin("mtj-test", "profile")),
        )

        val rows = record.detailRows()

        assertEquals("2000-01-02", rows.valueFor("생년월일"))
        assertEquals("시간 모름", rows.valueFor("태어난 시간"))
        assertEquals("연주 갑자 / 월주 을축 / 일주 병인", rows.valueFor("명식"))
        assertEquals("검토 필요", rows.valueFor("검증 상태"))
        assertFalse(rows.any { it.label == "정책" })
        assertFalse(rows.any { it.label == "데이터" })
    }

    @Test fun tarotRowsExposeSavedSpreadAndCardOrder() {
        val record = Envelope(
            Origin("mtj-test", "tarot"),
            Kind.TAROT,
            Value.Obj(mapOf(
                "snapshot" to Value.Obj(mapOf(
                    "spread" to Value.Obj(mapOf("title" to Value.Str("클래식 켈틱"))),
                    "reading" to Value.Obj(mapOf(
                        "question" to Value.Str("오늘의 흐름"),
                        "cards" to Value.Arr(listOf(
                            Value.Obj(mapOf(
                                "order" to Value.Num("1"),
                                "positionLabel" to Value.Str("현재"),
                                "nameKr" to Value.Str("바보"),
                                "directionLabel" to Value.Str("정방향"),
                            )),
                            Value.Obj(mapOf(
                                "order" to Value.Num("2"),
                                "positionLabel" to Value.Str("장애"),
                                "nameKr" to Value.Str("마법사"),
                                "directionLabel" to Value.Str("역방향"),
                            )),
                        )),
                    )),
                )),
            )),
        )

        val rows = record.detailRows()

        assertEquals("클래식 켈틱", rows.valueFor("스프레드"))
        assertEquals("오늘의 흐름", rows.valueFor("질문"))
        assertTrue(rows.valueFor("선택 카드").contains("1. 현재 - 바보 · 정방향"))
        assertTrue(rows.valueFor("선택 카드").contains("2. 장애 - 마법사 · 역방향"))
    }

    private fun pillar(label: String, stem: String, branch: String) = Value.Obj(mapOf(
        "label" to Value.Str(label),
        "stem" to Value.Str(stem),
        "branch" to Value.Str(branch),
    ))

    private fun List<RecordDetailRow>.valueFor(label: String): String =
        first { it.label == label }.value
}
