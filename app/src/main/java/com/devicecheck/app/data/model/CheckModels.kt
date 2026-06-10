package com.devicecheck.app.data.model

import java.time.LocalDate

/**
 * 验机步骤数据模型
 */
data class CheckStep(
    val id: Int,
    val title: String,
    val description: String,
    val checklist: List<CheckItem>,
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false
)

data class CheckItem(
    val id: Int,
    val text: String,
    val isChecked: Boolean = false
)

/**
 * 验机六步检查法
 */
object CheckStepsData {
    val steps = listOf(
        CheckStep(
            id = 1,
            title = "拆箱检查",
            description = "检查外包装塑封膜是否完整...",
            checklist = listOf(
                CheckItem(1, "塑封膜无二次封装痕迹"),
                CheckItem(2, "防伪标签完整可验证"),
                CheckItem(3, "配件数量与官方清单一致"),
                CheckItem(4, "机身无指纹/划痕/螺丝拧痕")
            )
        ),
        CheckStep(
            id = 2,
            title = "SN/IMEI查询",
            description = "查询设备出厂日期与库存风险",
            checklist = listOf(
                CheckItem(1, "IMEI Luhn校验通过"),
                CheckItem(2, "SN出厂日期已查询"),
                CheckItem(3, "库存风险评估正常")
            )
        ),
        CheckStep(
            id = 3,
            title = "屏幕检测",
            description = "检查屏幕显示质量",
            checklist = listOf(
                CheckItem(1, "无坏点/亮点"),
                CheckItem(2, "无色差/阴阳屏"),
                CheckItem(3, "触控响应正常")
            )
        ),
        CheckStep(
            id = 4,
            title = "电池检测",
            description = "检查电池健康度与循环次数",
            checklist = listOf(
                CheckItem(1, "电池健康度≥90%"),
                CheckItem(2, "循环次数≤50次"),
                CheckItem(3, "充电功能正常")
            )
        ),
        CheckStep(
            id = 5,
            title = "功能测试",
            description = "测试各项功能是否正常",
            checklist = listOf(
                CheckItem(1, "相机拍照正常"),
                CheckItem(2, "扬声器/麦克风正常"),
                CheckItem(3, "WiFi/蓝牙连接正常"),
                CheckItem(4, "SIM卡识别正常")
            )
        ),
        CheckStep(
            id = 6,
            title = "系统验证",
            description = "验证系统版本与激活状态",
            checklist = listOf(
                CheckItem(1, "系统版本为官方版本"),
                CheckItem(2, "激活日期与购买日期一致"),
                CheckItem(3, "无异常预装应用")
            )
        )
    )
}

/**
 * SN解析结果
 */
data class SnParseResult(
    val brand: String,
    val manufactureDate: LocalDate?,
    val purchaseDate: LocalDate?,
    val daysFromManufacture: Int?,
    val riskLevel: String,
    val riskColorHex: String
)

/**
 * 品牌SN规则
 */
data class BrandSnRule(
    val brand: String,
    val ruleDescription: String,
    val example: String
)

object BrandSnRulesData {
    val rules = listOf(
        BrandSnRule("小米", "SN[6-7]年 + SN[8]月", "2503xxxx → 2025年3月"),
        BrandSnRule("华为/荣耀", "SN[7]年 + SN[8]月 + SN[9-10]日", "xxxxx25C15xx → 2025年12月15日"),
        BrandSnRule("OPPO/一加", "SN[3-4]年+周 (DG2423=2024年23周)", "DG2423 → 2024年6月"),
        BrandSnRule("vivo/iQOO", "SN[6-7]年 + SN[8]月 + SN[9-10]日", "xxxxx250315xx → 2025年3月15日"),
        BrandSnRule("三星", "SN[4]字母=年 + SN[5]=月", "R = 2021...Z = 2025"),
        BrandSnRule("Apple(旧SN)", "SN[4]半年 + SN[5]周", "C = 2020上半年..."),
        BrandSnRule("华硕/ROG", "SN[1]字母=年 + SN[2]=月", "R = 2026"),
        BrandSnRule("戴尔", "Service Tag [1-2]年 + [3-4]周", "2512xxxx → 2025年第12周"),
        BrandSnRule("惠普", "SN[4]年 + SN[5-6]周", "xxx5x12xx → 2025年第12周")
    )
}