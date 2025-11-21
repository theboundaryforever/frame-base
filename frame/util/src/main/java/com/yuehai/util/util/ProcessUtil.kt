package com.yuehai.util.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.os.Process
import android.text.TextUtils
import androidx.annotation.CheckResult
import androidx.core.graphics.TypefaceCompatUtil.closeQuietly
import com.yuehai.util.AppUtil
import java.io.BufferedReader
import java.io.FileReader

object ProcessUtil {
    @Volatile
    private var currentProcessName: String? = null

    @CheckResult
    fun getCurrentProcessName(): String? {
        if (TextUtils.isEmpty(currentProcessName)) {
            synchronized(ProcessUtil::class.java) {
                if (TextUtils.isEmpty(currentProcessName)) {
                    val pid = Process.myPid()
                    val processName: String? =
                        getProcessNameFromFile(pid)
                    currentProcessName =
                        if (TextUtils.isEmpty(processName)) getProcessNameFromActivityService(
                            pid
                        ) else processName
                }
            }
        }
        return currentProcessName
    }

    fun isUIProcess(): Boolean {
        val processName: String? = getCurrentProcessName()
        return processName != null && !processName.contains(":")
    }

    private fun getProcessNameFromActivityService(pid: Int): String? {
        var processInfos: List<*>? = null
        try {
            processInfos = AppUtil.getSystemService<ActivityManager>("activity")?.runningAppProcesses
        } catch (e: Throwable) {
        }
        if (processInfos != null) {
            val iterator = processInfos.iterator()
            while (iterator.hasNext()) {
                val info = iterator.next() as RunningAppProcessInfo
                if (info.pid == pid) {
                    return info.processName
                }
            }
        }
        return null
    }

    @SuppressLint("RestrictedApi")
    private fun getProcessNameFromFile(pid: Int): String? {
        var fileReader: FileReader? = null
        var bufferedReader: BufferedReader? = null
        try {
            fileReader = FileReader("/proc/$pid/cmdline")
            bufferedReader = BufferedReader(fileReader)
            return bufferedReader.readLine().trim { it <= ' ' }
        } catch (var7: Exception) {
        } finally {
            closeQuietly(bufferedReader)
            closeQuietly(fileReader)
        }
        return null
    }

}