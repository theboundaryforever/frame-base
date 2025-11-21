package com.yuehai.util.util
import com.yuehai.util.AppUtil
import com.yuehai.util.language.MultiLanguages

object LocationUtil {
    fun isAr(): Boolean {
        val lan = MultiLanguages.getAppLanguage(AppUtil.appContext).language
        return "ar".equals(lan, ignoreCase = true)
    }

    fun isUr(): Boolean {
        val lan = MultiLanguages.getAppLanguage(AppUtil.appContext).language
        return "ur".equals(lan, ignoreCase = true)
    }
}