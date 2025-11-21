package com.yuehai.util.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object StringUtils {

    /**
     * 安全地将字符串转换为 Int
     * @param str 输入的字符串
     * @param defaultValue 转换失败时返回的默认值（默认0）
     */
    @JvmStatic
    fun safeParseInt(str: String?, defaultValue: Int = 0): Int {
        return str?.trim()?.toIntOrNull() ?: defaultValue
    }

    /**
     * 安全地将字符串转换为 Long
     */
    @JvmStatic
    fun safeParseLong(str: String?, defaultValue: Long = 0L): Long {
        return str?.trim()?.toLongOrNull() ?: defaultValue
    }

    /**
     * 安全地将字符串转换为 Double
     */
    @JvmStatic
    fun safeParseDouble(str: String?, defaultValue: Double = 0.0): Double {
        return str?.trim()?.toDoubleOrNull() ?: defaultValue
    }

    /**
     * 安全地将字符串转换为 Boolean
     */
    @JvmStatic
    fun safeParseBoolean(str: String?, defaultValue: Boolean = false): Boolean {
        return when (str?.trim()?.lowercase()) {
            "true", "1", "yes" -> true
            "false", "0", "no" -> false
            else -> defaultValue
        }
    }

    fun formatDouble(value: Double): String {
        val df = DecimalFormat("#.##") // 保留所有有效数字，去除科学计数法
        return df.format(value)
    }


    /**
     * 金额计算工具方法
     *
     * @param source 原始值（如账户余额）
     * @param amount 操作金额（如增加或扣除的数量）
     * @param isAdd 是否为加法操作，true 加，false 减
     * @return 返回计算后的结果，保留2位小数，避免精度问题
     */
    fun calculateAmount(
        source: Double,
        amount: Double,
        isAdd: Boolean = false
    ): Double {
        val bdSource = BigDecimal(source.toString())
        val bdAmount = BigDecimal(amount.toString())
        val result = if (isAdd) {
            bdSource.add(bdAmount)
        } else {
            bdSource.subtract(bdAmount)
        }
        return result.setScale(2, RoundingMode.HALF_UP).toDouble()
    }

}

/**
 * 将 BigDecimal 转换为格式化的字符串，保留两位小数，避免科学计数法
*/

/**
 * 给所有数字类型添加格式化扩展，保留两位小数，避免科学记数法
 */
fun Number.toFormattedString(scale: Int = 2): String {
    val bd = BigDecimal(this.toString()).setScale(scale, RoundingMode.HALF_UP)
    return if (bd.stripTrailingZeros().scale() <= 0) {
        // 没有小数部分，直接返回整数
        bd.toBigInteger().toString()
    } else {
        bd.stripTrailingZeros().toPlainString()
    }
}
