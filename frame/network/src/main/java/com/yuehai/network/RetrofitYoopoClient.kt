package com.yuehai.network

import com.google.gson.Gson
import com.yuehai.data.collection.path.Constants
import com.yuehai.factroy.converter.RltConverterFactory
import com.yuehai.interceptor.CodeCheckInterceptor
import com.yuehai.interceptor.ErrorCatchingInterceptor
import com.yuehai.interceptor.ErrorHtmlInterceptor
import com.yuehai.interceptor.FollowInterceptor
import com.yuehai.interceptor.HeaderInterceptor
import com.yuehai.interceptor.HttpCommonHeaderInterceptor
import com.yuehai.interceptor.HttpLoggingInterceptor
import com.yuehai.interceptor.LoggingTimeInterceptor
import com.yuehai.interceptor.SafeJsonInterceptor
import com.yuehai.util.AppUtil
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit


class RetrofitYoopoClient {

    //单例模式
    companion object {
        fun getInstance() = SingletonHolder.INSTANCE
        private lateinit var retrofit: Retrofit
    }

    private object SingletonHolder {
        val INSTANCE = RetrofitYoopoClient()
    }

    init {
        retrofit = Retrofit.Builder()
            .client(getOkHttpClient())
            .addConverterFactory(RltConverterFactory.create(Gson()))
            .baseUrl(Constants.yoopoUrl)
            .build()

    }


    val timeout = 45L
    private fun getOkHttpClient(): OkHttpClient {
        val cacheSize = 150L * 1024 * 1024 // 缓存大小 150MB
        val cacheDirectory = File(AppUtil.appContext.cacheDir, "http_cache")
        val cache = Cache(cacheDirectory, cacheSize)

        return OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .cache(cache)
            .retryOnConnectionFailure(true)

            .addInterceptor(ErrorHtmlInterceptor())    // 确保 HTML 错误页面被转换为 JSON
            .addInterceptor(HttpCommonHeaderInterceptor())
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(FollowInterceptor())
            .addInterceptor(SafeJsonInterceptor())    // 安全解析 JSON，放在 ErrorHtmlInterceptor 之后
            .addInterceptor(CodeCheckInterceptor())   // 检查业务 code
            .addInterceptor(ErrorCatchingInterceptor()) // 捕获异常
            .addInterceptor(LoggingTimeInterceptor())
            .addInterceptor(HttpLoggingInterceptor())
            .build()
    }

    /**
     * 创建一个 Service
     * @param service Class<T> 需要创建的 Service
     * @return T Service实例
     */
    fun <T> create(service: Class<T>): T = retrofit.create(service)


    //这个重载方法可以修改服务器地址
    fun <T> createBaseUrl(service: Class<T>, baseUrl: String): T = Retrofit.Builder()
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .build()
        .create(service)

}
