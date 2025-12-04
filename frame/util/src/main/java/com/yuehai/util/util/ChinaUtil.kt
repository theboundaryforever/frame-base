package com.yuehai.util.util

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import top.zibin.luban.BuildConfig

object RegionUtil {

    private const val TAG = "RegionUtil"

    fun isChina(context: Context): Boolean {
        if (BuildConfig.DEBUG) {
            return false // Debug 模式下默认返回 false
        }

        val tm =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return false
        val countryCodeValue = tm.networkCountryIso ?: ""
        val simCodeValue = tm.simCountryIso ?: ""
        Log.d("这里的地理编码是", "$countryCodeValue --- $simCodeValue")

        val operator = tm.simOperator
        Log.d(TAG, "operator: $operator")

        when (operator) {
            "46000", "46002" -> {
                Log.d(TAG, "中国移动")
                return true
            }

            "46001" -> {
                Log.d(TAG, "中国联通")
                return true
            }

            "46003" -> {
                Log.d(TAG, "中国电信")
                return true
            }
        }

        return countryCodeValue.equals("cn", ignoreCase = true)
    }
}
