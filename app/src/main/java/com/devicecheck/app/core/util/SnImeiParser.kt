package com.devicecheck.app.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * SN/IMEI 解析工具类
 * 支持主流品牌出厂日期解析与库存风险评估
 */
object SnImeiParser {

    /**
     * 品牌枚举
     */
    enum class Brand {
        APPLE, SAMSUNG, XIAOMI, HUAWEI, HONOR, OPPO, ONEPLUS, VIVO, IQOO,
        MOTOROLA, ASUS_ROG, DELL, HP, UNKNOWN
    }

    /**
     * 风险等级
     */
    enum class RiskLevel {
        NEW,       // ≤30天 新机
        STOCK,     // 31-90天 库存机
        LONG_STOCK, // 91-180天 长库龄
        HIGH_RISK  // >180天 高库龄
    }

    /**
     * 解析结果
     */
    data class ParseResult(
        val brand: Brand,
        val manufactureDate: LocalDate?,
        val purchaseDate: LocalDate?,
        val daysFromManufacture: Int?,
        val riskLevel: RiskLevel?,
        val riskColor: androidx.compose.ui.graphics.Color
    )

    /**
     * IMEI Luhn 校验
     */
    fun validateImei(imei: String): Boolean {
        if (imei.length != 15 || !imei.all { it.isDigit() }) return false

        var sum = 0
        for (i in imei.indices) {
            var digit = imei[i].digitToInt()
            if (i % 2 == 1) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
        }
        return sum % 10 == 0
    }

    /**
     * 通过 TAC 前缀识别品牌
     */
    fun identifyBrandByTac(imei: String): Brand {
        if (imei.length < 8) return Brand.UNKNOWN
        val tac = imei.substring(0, 8)

        return when {
            tac.startsWith("353") || tac.startsWith("356") || tac.startsWith("359") -> Brand.APPLE
            tac.startsWith("49") -> Brand.SAMSUNG
            tac.startsWith("86") -> Brand.XIAOMI // 国产品牌需用户确认
            tac.startsWith("01") -> Brand.MOTOROLA
            else -> Brand.UNKNOWN
        }
    }

    /**
     * 解析 SN 出厂日期
     */
    fun parseSn(sn: String, brand: Brand): LocalDate? {
        if (sn.isBlank()) return null

        return when (brand) {
            Brand.XIAOMI -> parseXiaomiSn(sn)
            Brand.HUAWEI, Brand.HONOR -> parseHuaweiSn(sn)
            Brand.OPPO, Brand.ONEPLUS -> parseOppoSn(sn)
            Brand.VIVO, Brand.IQOO -> parseVivoSn(sn)
            Brand.SAMSUNG -> parseSamsungSn(sn)
            Brand.APPLE -> parseAppleSn(sn)
            Brand.ASUS_ROG -> parseAsusSn(sn)
            Brand.DELL -> parseDellSn(sn)
            Brand.HP -> parseHpSn(sn)
            else -> null
        }
    }

    // 小米: SN[6-7]年 + SN[8]月
    private fun parseXiaomiSn(sn: String): LocalDate? {
        if (sn.length < 9) return null
        try {
            val year = 2000 + sn.substring(6, 8).toInt()
            val month = sn.substring(8, 9).toInt()
            return LocalDate.of(year, month, 1)
        } catch (e: Exception) {
            return null
        }
    }

    // 华为/荣耀: SN[7]年 + SN[8]月 + SN[9-10]日
    private fun parseHuaweiSn(sn: String): LocalDate? {
        if (sn.length < 11) return null
        try {
            val yearChar = sn[7]
            val year = 2000 + (yearChar - '0')
            val monthChar = sn[8]
            val month = monthToNumber(monthChar)
            val day = sn.substring(9, 11).toInt()
            return LocalDate.of(year, month, day)
        } catch (e: Exception) {
            return null
        }
    }

    // 华为月份字母映射: 1-9=数字, A=10, B=11, C=12
    private fun monthToNumber(c: Char): Int {
        return when (c) {
            in '1'..'9' -> c - '0'
            'A' -> 10
            'B' -> 11
            'C' -> 12
            else -> 1
        }
    }

    // OPPO/一加: SN[3-4]年+周 (DG2423=2024年23周)
    private fun parseOppoSn(sn: String): LocalDate? {
        if (sn.length < 5) return null
        try {
            val year = 2000 + sn.substring(3, 5).toInt()
            val week = sn.substring(5, 7).toInt()
            return LocalDate.of(year, 1, 1).plusWeeks(week - 1)
        } catch (e: Exception) {
            return null
        }
    }

    // vivo/iQOO: SN[6-7]年 + SN[8]月 + SN[9-10]日
    private fun parseVivoSn(sn: String): LocalDate? {
        if (sn.length < 11) return null
        try {
            val year = 2000 + sn.substring(6, 8).toInt()
            val month = sn.substring(8, 10).toInt()
            val day = sn.substring(10, 12).toInt()
            return LocalDate.of(year, month, day)
        } catch (e: Exception) {
            return null
        }
    }

    // 三星: SN[4]字母=年 + SN[5]=月
    // 年份映射: R=2021, S=2022, T=2023, U=2024, V=2025, W=2026, Z=2025
    private fun parseSamsungSn(sn: String): LocalDate? {
        if (sn.length < 6) return null
        val yearMap = mapOf(
            'R' to 2021, 'S' to 2022, 'T' to 2023, 'U' to 2024,
            'V' to 2025, 'W' to 2026, 'X' to 2027, 'Y' to 2028, 'Z' to 2025
        )
        val monthMap = mapOf(
            '1' to 1, '2' to 2, '3' to 3, '4' to 4, '5' to 5, '6' to 6,
            '7' to 7, '8' to 8, '9' to 9, 'A' to 10, 'B' to 11, 'C' to 12
        )
        try {
            val yearChar = sn[4]
            val monthChar = sn[5]
            val year = yearMap[yearChar] ?: return null
            val month = monthMap[monthChar] ?: return null
            return LocalDate.of(year, month, 1)
        } catch (e: Exception) {
            return null
        }
    }

    // Apple旧SN: SN[4]半年 + SN[5]周
    private fun parseAppleSn(sn: String): LocalDate? {
        if (sn.length < 6) return null
        // 简化实现，实际需要更复杂的映射表
        try {
            val halfYearChar = sn[4]
            val weekChar = sn[5]
            val year = when (halfYearChar) {
                'C' -> 2020; 'D' -> 2020; 'F' -> 2021; 'G' -> 2021
                'H' -> 2022; 'J' -> 2022; 'K' -> 2023; 'L' -> 2023
                'M' -> 2024; 'N' -> 2024; 'P' -> 2025; 'Q' -> 2025
                'R' -> 2026; 'S' -> 2026; else -> 2024
            }
            val week = when (weekChar) {
                in '1'..'9' -> weekChar - '0'
                'C' -> 10; 'D' -> 11; 'F' -> 12; 'G' -> 13
                'H' -> 14; 'J' -> 15; 'K' -> 16; 'L' -> 17
                'M' -> 18; 'N' -> 19; 'P' -> 20; 'Q' -> 21
                'R' -> 22; 'S' -> 23; 'T' -> 24; 'V' -> 25
                'W' -> 26; 'X' -> 27; 'Y' -> 28; else -> 1
            }
            return LocalDate.of(year, 1, 1).plusWeeks(week - 1)
        } catch (e: Exception) {
            return null
        }
    }

    // 华硕/ROG: SN[1]字母=年 + SN[2]=月
    private fun parseAsusSn(sn: String): LocalDate? {
        if (sn.length < 3) return null
        val yearMap = mapOf(
            'R' to 2026, 'S' to 2027, 'T' to 2028, 'U' to 2029,
            'V' to 2030, 'W' to 2031
        )
        try {
            val year = yearMap[sn[0]] ?: 2024
            val month = sn[1].digitToIntOrNull() ?: 1
            return LocalDate.of(year, month, 1)
        } catch (e: Exception) {
            return null
        }
    }

    // 戴尔: Service Tag [1-2]年 + [3-4]周
    private fun parseDellSn(sn: String): LocalDate? {
        if (sn.length < 5) return null
        try {
            val year = 2000 + sn.substring(0, 2).toInt()
            val week = sn.substring(2, 4).toInt()
            return LocalDate.of(year, 1, 1).plusWeeks(week - 1)
        } catch (e: Exception) {
            return null
        }
    }

    // 惠普: SN[4]年 + SN[5-6]周
    private fun parseHpSn(sn: String): LocalDate? {
        if (sn.length < 7) return null
        try {
            val year = 2000 + sn[4].digitToInt()
            val week = sn.substring(5, 7).toInt()
            return LocalDate.of(year, 1, 1).plusWeeks(week - 1)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 计算库存风险
     */
    fun calculateRiskLevel(manufactureDate: LocalDate?, purchaseDate: LocalDate?): RiskLevel? {
        if (manufactureDate == null || purchaseDate == null) return null

        val days = ChronoUnit.DAYS.between(manufactureDate, purchaseDate).toInt()
        return when {
            days <= 30 -> RiskLevel.NEW
            days <= 90 -> RiskLevel.STOCK
            days <= 180 -> RiskLevel.LONG_STOCK
            else -> RiskLevel.HIGH_RISK
        }
    }

    /**
     * 获取风险颜色
     */
    fun getRiskColor(riskLevel: RiskLevel?): androidx.compose.ui.graphics.Color {
        return when (riskLevel) {
            RiskLevel.NEW -> com.devicecheck.app.core.ui.theme.RiskGreen
            RiskLevel.STOCK -> com.devicecheck.app.core.ui.theme.RiskYellow
            RiskLevel.LONG_STOCK -> com.devicecheck.app.core.ui.theme.RiskOrange
            RiskLevel.HIGH_RISK -> com.devicecheck.app.core.ui.theme.RiskRed
            null -> androidx.compose.ui.graphics.Color.Gray
        }
    }

    /**
     * 完整解析流程
     */
    fun parse(sn: String, imei: String?, purchaseDate: LocalDate?): ParseResult {
        val brand = if (imei != null && validateImei(imei)) {
            identifyBrandByTac(imei)
        } else {
            Brand.UNKNOWN
        }

        val manufactureDate = parseSn(sn, brand)
        val daysFromManufacture = if (manufactureDate != null && purchaseDate != null) {
            ChronoUnit.DAYS.between(manufactureDate, purchaseDate).toInt()
        } else null

        val riskLevel = calculateRiskLevel(manufactureDate, purchaseDate)
        val riskColor = getRiskColor(riskLevel)

        return ParseResult(
            brand = brand,
            manufactureDate = manufactureDate,
            purchaseDate = purchaseDate,
            daysFromManufacture = daysFromManufacture,
            riskLevel = riskLevel,
            riskColor = riskColor
        )
    }
}