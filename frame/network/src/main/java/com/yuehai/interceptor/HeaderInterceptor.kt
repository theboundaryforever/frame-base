package com.yuehai.interceptor

import com.yuehai.data.collection.path.Constants
import com.yuehai.util.AppUtil
import com.yuehai.util.PackageUtil
import com.yuehai.util.language.MultiLanguages
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val language =
            MultiLanguages.getAppLanguage(AppUtil.appContext).language.takeIf { !it.isNullOrEmpty() }
                ?: "en"

        val newRequest = originalRequest.newBuilder()
            .header(Constants.AppVersion, "${PackageUtil.getVersionCode(AppUtil.appContext)}")
            .header(Constants.OS, "Android")
            .header(Constants.AcceptLanguage, language)
            .build()

        return try {
            chain.proceed(newRequest)
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }
}
