package com.hoscat.mtj.dev

import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.SaverScope
import com.hoscat.core.model.*
import mtj.records.Action
import mtj.records.MigrationPlanner
import mtj.records.RecordsJsonCodec
import mtj.saju.MtjSajuEvaluation
import org.junit.Assert.*
import org.junit.Test

class SajuRecreationStateTest {
    private val saverScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = value is String || value is Int ||
            (value is List<*> && value.all { it != null && canBeSaved(it) })
    }

    // Exercise the same Saver and registry boundary used by rememberSaveable, without a device.
    private fun recreate(state: SajuRecreationState): SajuRecreationState {
        val registry = SaveableStateRegistry(null, saverScope::canBeSaved)
        registry.registerProvider("saju") { with(SajuRecreationSaver) { saverScope.save(state) } }
        val restored = SaveableStateRegistry(registry.performSave(), saverScope::canBeSaved)
        return checkNotNull(SajuRecreationSaver.restore(checkNotNull(restored.consumeRestored("saju"))))
    }

    @Test fun knownTimeResultAndRecordIdentitySurviveRepeatedRecreation() {
        val original = SajuRecreationState.from(accepted(14))
        var restored = original
        repeat(3) {
            restored = recreate(restored)
            assertEquals(original, restored)
            val before = checkNotNull(original.saveBatch)
            val after = checkNotNull(restored.saveBatch)
            before.zip(after).forEach { (a, b) ->
                assertArrayEquals(RecordsJsonCodec().encode(a), RecordsJsonCodec().encode(b))
            }
            val plan = MigrationPlanner().plan(before, after)
            assertTrue(plan.ready)
            assertTrue(plan.decisions.all { it.action == Action.SKIP })
        }
    }

    @Test fun unknownTimeNeverRestoresAFabricatedHour() {
        val original = SajuRecreationState.from(accepted(null))
        val restored = recreate(recreate(original))
        assertEquals(original, restored)
        val result = restored.evaluation as MtjSajuEvaluation.Accepted
        assertNull(result.input.birthDateTime.hour)
        assertNull(result.chart.hourPillar)
        val hour = sajuChartPillarDisplays(result.chart).single { it.role == "시주" }
        assertEquals("시간 모름", hour.stateText)
        assertEquals("오행 산출 제외", hour.elementText)
        assertEquals("", hour.stemHanjaText)
    }

    @Test fun rejectedResultPreservesItsMessageWithoutCreatingRecords() {
        val state = SajuRecreationState.from(MtjSajuEvaluation.Rejected("입력 확인"))
        assertEquals(state, recreate(state))
        assertNull(recreate(state).saveBatch)
    }

    @Test fun initialAndExplicitEditStateStayOnInputAfterRecreation() {
        assertEquals(SajuRecreationState(), recreate(SajuRecreationState()))
        var state = recreate(SajuRecreationState.from(accepted(14)))
        assertTrue(state.evaluation is MtjSajuEvaluation.Accepted)
        state = SajuRecreationState()
        assertNull(recreate(state).evaluation)
        assertNull(recreate(state).saveBatch)
    }

    @Test fun malformedStateReportsRecoveryFailureWithoutInventingAResult() {
        val saved = SajuRecreationState.from(accepted(null)).save()
        for (invalid in listOf("bad", emptyList<Any>(), listOf(2, "input"), saved.dropLast(1),
            saved.toMutableList().also { it[2] = "null" })) {
            val state = SajuRecreationState.restore(invalid)
            assertTrue(state.evaluation is MtjSajuEvaluation.Rejected)
            assertNull(state.saveBatch)
            assertEquals(state, recreate(state))
        }
    }

    @Test fun savedResultUsesOnlyBundleSafePrimitivesWithinAConservativeFixtureBudget() {
        val saved = SajuRecreationState.from(accepted(null)).save()
        assertTrue(saverScope.canBeSaved(saved))
        val utf16Bytes = saved.filterIsInstance<String>().sumOf { it.length * 2 }
        assertTrue("Fixture saved state was $utf16Bytes bytes", utf16Bytes < 64 * 1024)
    }

    private fun accepted(hour: Int?): MtjSajuEvaluation.Accepted {
        val input = BirthProfileInput("테스트", BirthDateTime(1990, 1, 23, hour, if (hour == null) 0 else 35),
            calendarType = CalendarType.Lunar, isLeapMonth = true, gender = Gender.Female)
        val chart = SajuChart(
            name = input.name,
            yearPillar = Pillar("갑", "자", "연주"), monthPillar = Pillar("병", "인", "월주"),
            dayPillar = Pillar("무", "오", "일주"), hourPillar = hour?.let { Pillar("경", "신", "시주") },
            dayMaster = "무", summary = "원래 계산 결과",
            evidence = CalculationEvidence("Asia/Seoul", "myeongri_kr_v2", "Lichun", "SolarJieTerms",
                "StandardTwoHourBlocks", "test-data", false, "fixture", previousSolarTermName = "소한",
                previousSolarTermAtKst = "1990-01-06T00:00:00+09:00"),
            analysis = SajuAnalysisSnapshot(listOf(ElementCount(FiveElement.Wood, 2)),
                listOf(TenGodRelation("연주", "갑", "편관")), listOf(HiddenStemSet("자", listOf("계")))),
        )
        return MtjSajuEvaluation.Accepted(input, chart)
    }
}
