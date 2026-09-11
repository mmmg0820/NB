package mtj.saju

import android.content.Context
import com.hoscat.core.manse.AssetLunarDateDataSource
import com.hoscat.core.manse.AssetSolarTermDataSource
import com.hoscat.core.manse.DefaultSajuCalculator
import com.hoscat.core.manse.SajuCalculationException
import com.hoscat.core.model.BirthInputDraft
import com.hoscat.core.model.BirthProfileInput
import com.hoscat.core.model.KOREA_STANDARD_TIME_ZONE_ID
import com.hoscat.core.model.SajuChart
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface MtjSajuEvaluation {
    data class Accepted(val input: BirthProfileInput, val chart: SajuChart) : MtjSajuEvaluation
    data class Rejected(val message: String) : MtjSajuEvaluation
}

class MtjSajuRuntime(context: Context) {
    private val appContext = context.applicationContext
    private val access = Mutex()
    private var loaded: RuntimeData? = null

    suspend fun evaluate(draft: BirthInputDraft): MtjSajuEvaluation = withContext(Dispatchers.Default) {
        // Upstream lazy(NONE) data and indexes never escape this lock, even across dispatchers.
        access.withLock {
            try {
                currentCoroutineContext().ensureActive()
                val data = loaded ?: withContext(Dispatchers.IO) {
                    val lunar = AssetLunarDateDataSource(appContext, LUNAR_ASSET_PATH)
                    val solar = AssetSolarTermDataSource(appContext, SOLAR_TERM_ASSET_PATH)
                    lunar.version
                    solar.version
                    RuntimeData(lunar, solar)
                }.also { loaded = it }
                currentCoroutineContext().ensureActive()
                val adapter = MtjBirthInputAdapter { year, month, day, leap ->
                    data.lunar.dateByLunarDate(year, month, day, leap)?.solarDate?.let(LocalDate::parse)
                }
                when (val parsed = adapter.parse(
                    draft,
                    LocalDateTime.now(ZoneId.of(KOREA_STANDARD_TIME_ZONE_ID)),
                )) {
                    is MtjBirthInputResult.Accepted -> {
                        currentCoroutineContext().ensureActive()
                        val chart = data.calculator.calculate(parsed.input)
                        currentCoroutineContext().ensureActive()
                        MtjSajuEvaluation.Accepted(parsed.input, chart)
                    }
                    is MtjBirthInputResult.Rejected -> MtjSajuEvaluation.Rejected(parsed.message)
                    MtjBirthInputResult.LunarResolverRequired -> error("Runtime resolver invariant violated")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: SajuCalculationException) {
                currentCoroutineContext().ensureActive()
                MtjSajuEvaluation.Rejected(failure.userMessage)
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                // Never expose raw asset/JSON exceptions or birth input in diagnostics.
                MtjSajuEvaluation.Rejected("만세력 데이터를 불러오거나 계산할 수 없습니다. 잠시 후 다시 시도해주세요.")
            }
        }
    }

    private class RuntimeData(
        val lunar: AssetLunarDateDataSource,
        solar: AssetSolarTermDataSource,
    ) {
        val calculator = DefaultSajuCalculator(
            solarTermDataSource = solar,
            lunarDateDataSource = lunar,
        )
    }

    companion object {
        const val LUNAR_ASSET_PATH = "manse/lunar_dates_1900_2100.json.gz.bin"
        const val SOLAR_TERM_ASSET_PATH = "manse/solar_terms_1900_2100.json.gz.bin"
    }
}
