package com.hoscat.core.model

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

const val KOREA_STANDARD_TIME_ZONE_ID = "Asia/Seoul"
const val KOREA_STANDARD_TIME_LABEL = "대한민국 표준시"

data class BirthRegion(
    val id: String,
    val displayName: String,
    val provinceName: String?,
    val countryCode: String = "KR",
    val timeZoneId: String = KOREA_STANDARD_TIME_ZONE_ID,
    val longitudeEast: Double,
    val latitudeNorth: Double? = null,
)

object KoreanBirthRegions {
    const val defaultRegionId = "seoul"
    private const val standardMeridianEast = 135.0

    val regions: List<BirthRegion> = listOf(
        BirthRegion("seoul", "서울", "서울특별시", longitudeEast = 126.98, latitudeNorth = 37.57),
        BirthRegion("sejong", "세종", "세종특별자치시", longitudeEast = 127.29, latitudeNorth = 36.48),
        BirthRegion("busan", "부산", "부산광역시", longitudeEast = 129.08, latitudeNorth = 35.18),
        BirthRegion("incheon", "인천", "인천광역시", longitudeEast = 126.71, latitudeNorth = 37.46),
        BirthRegion("daegu", "대구", "대구광역시", longitudeEast = 128.60, latitudeNorth = 35.87),
        BirthRegion("daejeon", "대전", "대전광역시", longitudeEast = 127.38, latitudeNorth = 36.35),
        BirthRegion("gwangju", "광주", "광주광역시", longitudeEast = 126.85, latitudeNorth = 35.16),
        BirthRegion("ulsan", "울산", "울산광역시", longitudeEast = 129.31, latitudeNorth = 35.54),
        BirthRegion("suwon", "수원", "경기도", longitudeEast = 127.03),
        BirthRegion("seongnam", "성남", "경기도", longitudeEast = 127.13),
        BirthRegion("goyang", "고양", "경기도", longitudeEast = 126.83),
        BirthRegion("yongin", "용인", "경기도", longitudeEast = 127.18),
        BirthRegion("bucheon", "부천", "경기도", longitudeEast = 126.77),
        BirthRegion("ansan", "안산", "경기도", longitudeEast = 126.83),
        BirthRegion("anyang", "안양", "경기도", longitudeEast = 126.96),
        BirthRegion("namyangju", "남양주", "경기도", longitudeEast = 127.22),
        BirthRegion("hwaseong", "화성", "경기도", longitudeEast = 126.83),
        BirthRegion("pyeongtaek", "평택", "경기도", longitudeEast = 127.11),
        BirthRegion("uijeongbu", "의정부", "경기도", longitudeEast = 127.05),
        BirthRegion("siheung", "시흥", "경기도", longitudeEast = 126.80),
        BirthRegion("paju", "파주", "경기도", longitudeEast = 126.78),
        BirthRegion("gimpo", "김포", "경기도", longitudeEast = 126.72),
        BirthRegion("gwangmyeong", "광명", "경기도", longitudeEast = 126.87),
        BirthRegion("gwangju_gyeonggi", "광주", "경기도", longitudeEast = 127.26),
        BirthRegion("gunpo", "군포", "경기도", longitudeEast = 126.94),
        BirthRegion("icheon", "이천", "경기도", longitudeEast = 127.44),
        BirthRegion("osan", "오산", "경기도", longitudeEast = 127.08),
        BirthRegion("hanam", "하남", "경기도", longitudeEast = 127.21),
        BirthRegion("yangju", "양주", "경기도", longitudeEast = 127.05),
        BirthRegion("guri", "구리", "경기도", longitudeEast = 127.13),
        BirthRegion("anseong", "안성", "경기도", longitudeEast = 127.28),
        BirthRegion("pocheon", "포천", "경기도", longitudeEast = 127.20),
        BirthRegion("uiwang", "의왕", "경기도", longitudeEast = 126.97),
        BirthRegion("yeoju", "여주", "경기도", longitudeEast = 127.64),
        BirthRegion("dongducheon", "동두천", "경기도", longitudeEast = 127.06),
        BirthRegion("gwacheon", "과천", "경기도", longitudeEast = 126.99),
        BirthRegion("chuncheon", "춘천", "강원특별자치도", longitudeEast = 127.73),
        BirthRegion("wonju", "원주", "강원특별자치도", longitudeEast = 127.92),
        BirthRegion("gangneung", "강릉", "강원특별자치도", longitudeEast = 128.90),
        BirthRegion("donghae", "동해", "강원특별자치도", longitudeEast = 129.11),
        BirthRegion("taebaek", "태백", "강원특별자치도", longitudeEast = 128.99),
        BirthRegion("sokcho", "속초", "강원특별자치도", longitudeEast = 128.59),
        BirthRegion("samcheok", "삼척", "강원특별자치도", longitudeEast = 129.17),
        BirthRegion("cheongju", "청주", "충청북도", longitudeEast = 127.49),
        BirthRegion("chungju", "충주", "충청북도", longitudeEast = 127.93),
        BirthRegion("jecheon", "제천", "충청북도", longitudeEast = 128.19),
        BirthRegion("cheonan", "천안", "충청남도", longitudeEast = 127.15),
        BirthRegion("gongju", "공주", "충청남도", longitudeEast = 127.12),
        BirthRegion("boryeong", "보령", "충청남도", longitudeEast = 126.61),
        BirthRegion("asan", "아산", "충청남도", longitudeEast = 127.00),
        BirthRegion("seosan", "서산", "충청남도", longitudeEast = 126.45),
        BirthRegion("nonsan", "논산", "충청남도", longitudeEast = 127.10),
        BirthRegion("gyeryong", "계룡", "충청남도", longitudeEast = 127.25),
        BirthRegion("dangjin", "당진", "충청남도", longitudeEast = 126.65),
        BirthRegion("jeonju", "전주", "전북특별자치도", longitudeEast = 127.15),
        BirthRegion("gunsan", "군산", "전북특별자치도", longitudeEast = 126.74),
        BirthRegion("iksan", "익산", "전북특별자치도", longitudeEast = 126.96),
        BirthRegion("jeongeup", "정읍", "전북특별자치도", longitudeEast = 126.86),
        BirthRegion("namwon", "남원", "전북특별자치도", longitudeEast = 127.39),
        BirthRegion("gimje", "김제", "전북특별자치도", longitudeEast = 126.88),
        BirthRegion("mokpo", "목포", "전라남도", longitudeEast = 126.39),
        BirthRegion("yeosu", "여수", "전라남도", longitudeEast = 127.66),
        BirthRegion("suncheon", "순천", "전라남도", longitudeEast = 127.49),
        BirthRegion("naju", "나주", "전라남도", longitudeEast = 126.71),
        BirthRegion("gwangyang", "광양", "전라남도", longitudeEast = 127.70),
        BirthRegion("pohang", "포항", "경상북도", longitudeEast = 129.37),
        BirthRegion("gyeongju", "경주", "경상북도", longitudeEast = 129.22),
        BirthRegion("gimcheon", "김천", "경상북도", longitudeEast = 128.12),
        BirthRegion("andong", "안동", "경상북도", longitudeEast = 128.73),
        BirthRegion("gumi", "구미", "경상북도", longitudeEast = 128.34),
        BirthRegion("yeongju", "영주", "경상북도", longitudeEast = 128.62),
        BirthRegion("yeongcheon", "영천", "경상북도", longitudeEast = 128.94),
        BirthRegion("sangju", "상주", "경상북도", longitudeEast = 128.16),
        BirthRegion("mungyeong", "문경", "경상북도", longitudeEast = 128.19),
        BirthRegion("gyeongsan", "경산", "경상북도", longitudeEast = 128.74),
        BirthRegion("changwon", "창원", "경상남도", longitudeEast = 128.68),
        BirthRegion("jinju", "진주", "경상남도", longitudeEast = 128.11),
        BirthRegion("tongyeong", "통영", "경상남도", longitudeEast = 128.43),
        BirthRegion("sacheon", "사천", "경상남도", longitudeEast = 128.08),
        BirthRegion("gimhae", "김해", "경상남도", longitudeEast = 128.88),
        BirthRegion("miryang", "밀양", "경상남도", longitudeEast = 128.75),
        BirthRegion("geoje", "거제", "경상남도", longitudeEast = 128.62),
        BirthRegion("yangsan", "양산", "경상남도", longitudeEast = 129.04),
        BirthRegion("jeju", "제주", "제주특별자치도", longitudeEast = 126.53),
        BirthRegion("seogwipo", "서귀포", "제주특별자치도", longitudeEast = 126.56),
        BirthRegion("ulleung", "울릉", "특수 지역", longitudeEast = 130.90),
        BirthRegion("dokdo", "독도", "특수 지역", longitudeEast = 131.87),
    )

    fun byId(id: String?): BirthRegion? = regions.firstOrNull { it.id == id }

    fun defaultRegion(): BirthRegion = requireNotNull(byId(defaultRegionId))

    fun search(query: String, limit: Int = 24): List<BirthRegion> {
        val normalizedQuery = query.replace(" ", "")
        if (normalizedQuery.isBlank()) return regions.take(limit)
        return regions
            .filter { region -> region.matchesQuery(normalizedQuery) }
            .take(limit)
    }

    fun correctionMinutes(longitudeEast: Double): Double =
        (longitudeEast - standardMeridianEast) * 4.0

    fun formattedCorrection(minutes: Double): String {
        val rounded = minutes.roundToInt()
        if (rounded == 0) return "0분"
        val direction = if (rounded < 0) "늦게" else "빠르게"
        return "약 ${abs(rounded)}분 $direction"
    }

    fun basisLine(input: BirthProfileInput): String =
        if (input.useBirthRegionSolarCorrection) {
            "출생지역 시간 보정: ${input.placeName} · ${input.solarCorrectionMinutes.roundToInt()}분"
        } else {
            "출생지역 시간 보정: 꺼짐"
        }

    private fun BirthRegion.matchesQuery(normalizedQuery: String): Boolean {
        val name = displayName.replace(" ", "")
        val province = provinceName.orEmpty().replace(" ", "")
        val provinceShort = province
            .removeSuffix("특별자치도")
            .removeSuffix("특별자치시")
            .removeSuffix("특별시")
            .removeSuffix("광역시")
            .removeSuffix("도")
        return name.startsWith(normalizedQuery) ||
            name.contains(normalizedQuery) ||
            province.contains(normalizedQuery) ||
            provinceShort.contains(normalizedQuery) ||
            "$province$name".contains(normalizedQuery) ||
            "$provinceShort$name".contains(normalizedQuery)
    }
}

fun BirthProfileInput.calculationBirthDateTime(baseSolarDate: LocalDate? = null): LocalDateTime {
    val birth = birthDateTime
    val date = baseSolarDate ?: LocalDate.of(birth.year, birth.month, birth.day)
    val time = LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, birth.hour ?: 12, birth.minute)
    if (!useBirthRegionSolarCorrection || birth.hour == null) return time
    return time.plusMinutes(solarCorrectionMinutes.roundToInt().toLong())
}
