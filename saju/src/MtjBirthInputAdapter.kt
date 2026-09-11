package mtj.saju

import com.hoscat.core.model.BirthInputDraft
import com.hoscat.core.model.BirthProfileInput
import com.hoscat.core.model.CalendarType
import com.hoscat.core.model.LunarSolarDateResolver
import com.hoscat.core.model.SAJU_INPUT_CONTRACT_VERSION
import com.hoscat.core.model.parseBirthProfileInput
import java.time.LocalDateTime

sealed interface MtjBirthInputResult {
    data class Accepted(val input: BirthProfileInput) : MtjBirthInputResult
    data class Rejected(val message: String) : MtjBirthInputResult
    data object LunarResolverRequired : MtjBirthInputResult
}

class MtjBirthInputAdapter(
    private val resolveLunarSolarDate: LunarSolarDateResolver? = null,
) {
    val sourceContractVersion: Int get() = SAJU_INPUT_CONTRACT_VERSION

    fun parse(draft: BirthInputDraft, currentKstDateTime: LocalDateTime): MtjBirthInputResult {
        // The upstream parser permits unresolved lunar dates; MTJ must not accept them.
        if (draft.calendarType == CalendarType.Lunar && resolveLunarSolarDate == null) {
            return MtjBirthInputResult.LunarResolverRequired
        }
        val result = parseBirthProfileInput(draft, currentKstDateTime, resolveLunarSolarDate)
        return result.input?.let(MtjBirthInputResult::Accepted)
            ?: MtjBirthInputResult.Rejected(checkNotNull(result.errorMessage))
    }
}
