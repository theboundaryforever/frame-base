package com.yuehai.util.util

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.annotation.RequiresPermission
import com.yuehai.util.AppUtil

object NetworkUtil {

    /**
     * 检查是否连接到互联网
     * @param context 应用的上下文
     * @return 如果有互联网连接则返回 true，否则返回 false
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = AppUtil.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 针对Android 10及以上，使用 NetworkCapabilities 进行网络状态检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network: Network? = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * 检查当前是否连接到 Wi-Fi 网络
     * @param context 应用的上下文
     * @return 如果连接到 Wi-Fi 返回 true，否则返回 false
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network: Network? = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    /**
     * 检查当前是否连接到移动数据网络
     * @param context 应用的上下文
     * @return 如果连接到移动数据返回 true，否则返回 false
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isMobileDataConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network: Network? = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_MOBILE
        }
    }

    /**
     * 获取当前网络类型
     * @param context 应用的上下文
     * @return 返回当前网络类型（例如 Wi-Fi、移动数据、无网络等）
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getNetworkType(context: Context): String {
        return when {
            isWifiConnected(context) -> "Wi-Fi"
            isMobileDataConnected(context) -> "Mobile Data"
            else -> "No Network"
        }
    }
}
