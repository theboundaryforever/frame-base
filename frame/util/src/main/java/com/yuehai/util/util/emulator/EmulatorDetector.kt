package com.yuehai.util.util.emulator

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.NetworkInterface
import kotlin.math.sqrt

object EmulatorSimpleDetector {

    private const val TAG = "LeidianEmulatorDetector"

    private val detectionWeights = mapOf(
        "buildProp" to 15,
        "cpuArch" to 8,
        "qemuFiles" to 15,
        "sensors" to 8,
        "batteryTemp" to 5,
        "packages" to 20,
        "soFiles" to 15,
        "loadedSo" to 15,
        "glRenderer" to 8,
        "behaviorAnalysis" to 10,
        "getprop" to 10,
        "initrc" to 5,
        "network" to 5,
        "telephony" to 5,
        "hardwareProp" to 5
    )

    private const val THRESHOLD = 50

    data class DetectionResult(
        val totalScore: Int,
        val isEmulator: Boolean,
        val triggeredItems: List<String>
    )

    private fun logCheck(key: String, result: Boolean) {
        Log.d(TAG, "$key = $result")
    }

    private fun matchAny(input: String?, keywords: List<String>, ignoreCase: Boolean = true): Boolean {
        if (input.isNullOrEmpty()) return false
        return keywords.any { input.contains(it, ignoreCase) }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun isEmulator(context: Context, behaviorCheckResult: Boolean = false): Boolean {
        Log.d("EmulatorDetector", "开始检测是否为模拟器")
        val triggered = mutableListOf<String>()
        var score = 0

        fun checkAndAdd(key: String, checkResult: Boolean) {
            logCheck(key, checkResult)
            if (checkResult) {
                score += detectionWeights[key] ?: 0
                triggered.add(key)
            }
        }

        checkAndAdd("buildProp", checkBuildProps() || checkAdditionalLeidianProps())
        checkAndAdd("cpuArch", checkCpuArch())
        checkAndAdd("qemuFiles", checkQemuFiles())
        checkAndAdd("sensors", checkSensors(context))
        checkAndAdd("batteryTemp", checkBatteryTemperature(context))
        checkAndAdd("packages", checkKnownPackages(context))
        checkAndAdd("soFiles", checkSoFiles() || checkLeidianSpecificFiles())
        checkAndAdd("loadedSo", checkLoadedSoFiles())
        checkAndAdd("glRenderer", checkGLRenderer())
        checkAndAdd("behaviorAnalysis", behaviorCheckResult)

        // 新增的增强检测项
        checkAndAdd("getprop", checkGetprop())
        checkAndAdd("initrc", checkInitRcFile())
        checkAndAdd("network", checkNetworkFeatures())
        checkAndAdd("telephony", checkTelephonyFeature(context))
        checkAndAdd("hardwareProp", checkHardwareProps())

        val isEmu = score >= THRESHOLD
        if (isEmu) Log.i(TAG, "Emulator detected! Score: $score Triggered: $triggered")
        return DetectionResult(score, isEmu, triggered).isEmulator
    }

    private fun checkBuildProps(): Boolean {
        val brands = listOf("generic", "unknown", "andy", "nox", "vbox", "ttvm", "tencent", "ldplayer", "leidian", "droid4x")
        val devices = listOf("generic", "emulator", "vbox86p", "nox", "ttvm", "tencent", "ldplayer", "leidian", "droid4x")
        val models = listOf("sdk", "google_sdk", "android sdk built for x86", "emulator", "mumu", "tiantianvm", "nox", "droid4x", "ldplayer", "leidian", "leidianx", "leidian_android", "leidian2")
        val manufacturers = listOf("genymotion", "andy", "nox", "tiantianvm", "netease", "ldplayer", "leidian")

        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.lowercase().contains("vbox") ||
                Build.FINGERPRINT.lowercase().contains("test-keys") ||
                matchAny(Build.MODEL.lowercase(), models) ||
                matchAny(Build.BRAND.lowercase(), brands) ||
                matchAny(Build.DEVICE.lowercase(), devices) ||
                matchAny(Build.MANUFACTURER.lowercase(), manufacturers) ||
                Build.HOST.startsWith("buildhost") ||
                Build.PRODUCT.lowercase() in listOf("sdk", "google_sdk", "sdk_x86", "vbox86p", "emulator_x86", "emulator", "nox", "mumu", "ldplayer", "tiantianvm", "leidian")
    }

    private fun checkAdditionalLeidianProps(): Boolean {
        val additional = listOf("leidian", "ldplayer", "lxdroid", "leidianx")
        return additional.any {
            Build.PRODUCT.contains(it, true) ||
                    Build.DEVICE.contains(it, true) ||
                    Build.MODEL.contains(it, true) ||
                    Build.BRAND.contains(it, true) ||
                    Build.MANUFACTURER.contains(it, true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkCpuArch(): Boolean {
        return Build.SUPPORTED_ABIS.any {
            (it.contains("x86") || it.contains("x86_64")) && !Build.MODEL.contains("Pixel", true)
        }
    }

    private fun checkQemuFiles(): Boolean {
        val files = listOf(
            "/dev/socket/qemud", "/dev/qemu_pipe", "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace", "/system/bin/qemu-props"
        )
        return files.any { File(it).exists() }
    }

    private fun checkSensors(context: Context): Boolean {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return false
        val sensors = sm.getSensorList(Sensor.TYPE_ALL)
        val hasAccel = sensors.any { it.type == Sensor.TYPE_ACCELEROMETER }
        val hasGyro = sensors.any { it.type == Sensor.TYPE_GYROSCOPE }
        return !(hasAccel && hasGyro)
    }

    private fun checkBatteryTemperature(context: Context): Boolean {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.applicationContext.registerReceiver(null, intentFilter)
            val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            temp <= 0 || temp > 800
        } catch (e: Exception) {
            false
        }
    }

    private fun checkKnownPackages(context: Context): Boolean {
        val known = listOf(
            "com.bluestacks", "com.microvirt", "com.nox.mopen.app", "com.vphone.launcher",
            "com.mumu.launcher", "com.genymotion.superuser", "com.koplayer.android",
            "com.andy.emulator", "com.ld.player", "com.leidian"
        )
        val pm = context.packageManager
        val installed = pm.getInstalledPackages(0)
        return installed.any { pkg -> known.any { it in pkg.packageName } }
    }

    private fun checkSoFiles(): Boolean {
        val suspicious = listOf(
            "/system/lib/libqemud.so", "/system/lib/libqemu_pipe.so", "/system/lib/libnoxd.so",
            "/system/lib/libldutils.so", "/system/lib/libmumu.so", "/system/lib/libvtplayer.so",
            "/system/lib/libsmartgaga.so", "/system/lib/libldplayer.so"
        )
        return suspicious.any { File(it).exists() }
    }

    private fun checkLeidianSpecificFiles(): Boolean {
        val leidianFiles = listOf(
            "/data/data/com.leidian", "/data/data/com.ld.player",
            "/system/lib/libleidian.so", "/system/bin/leidian"
        )
        return leidianFiles.any { File(it).exists() }
    }

    private fun checkLoadedSoFiles(): Boolean {
        val mapsFile = File("/proc/self/maps")
        if (!mapsFile.exists()) return false
        val content = mapsFile.readText()
        val keywords = listOf("nox", "ldutils", "genyd", "mumu", "smartgaga", "ldplayer", "leidian")
        return keywords.any { content.contains(it, true) }
    }

    private fun checkGLRenderer(): Boolean {
        return try {
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER)?.lowercase() ?: ""
            val vendor = GLES20.glGetString(GLES20.GL_VENDOR)?.lowercase() ?: ""
            val keywords = listOf("bluestacks", "android emulator", "ldplayer", "leidian", "nox", "genymotion")
            keywords.any { renderer.contains(it) || vendor.contains(it) }
        } catch (e: Exception) {
            false
        }
    }

    private fun checkGetprop(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val all = reader.readText().lowercase()
            listOf("leidian", "ldplayer", "nox", "ttvm", "vbox").any { all.contains(it) }
        } catch (e: Exception) {
            false
        }
    }

    private fun checkInitRcFile(): Boolean {
        return try {
            val file = File("/init.rc")
            if (!file.exists()) return false
            val content = file.readText().lowercase()
            listOf("leidian", "ldplayer", "nox", "mumu").any { content.contains(it) }
        } catch (e: Exception) {
            false
        }
    }

    private fun checkNetworkFeatures(): Boolean {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            val macPrefixes = listOf("00:1a:2a", "00:16:3e", "08:00:27")
            interfaces.any {
                val mac = it.hardwareAddress?.joinToString(":") { b -> "%02x".format(b) } ?: return@any false
                macPrefixes.any { prefix -> mac.startsWith(prefix, true) }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun checkTelephonyFeature(context: Context): Boolean {
        val pm = context.packageManager
        return !pm.hasSystemFeature("android.hardware.telephony")
    }

    private fun getSystemProperty(name: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java)
            method.invoke(null, name) as String
        } catch (e: Exception) {
            ""
        }
    }

    private fun checkHardwareProps(): Boolean {
        val hardware = getSystemProperty("ro.hardware").lowercase()
        val device = getSystemProperty("ro.product.device").lowercase()
        return listOf("ttvm", "ldplayer", "leidian", "nox", "vbox").any {
            it in hardware || it in device
        }
    }

    class SlideBehaviorDetector {
        private val slideIntervals = mutableListOf<Long>()
        private var lastEventTime: Long = 0

        fun onTouchMove(eventTime: Long) {
            if (lastEventTime != 0L) {
                val interval = eventTime - lastEventTime
                slideIntervals.add(interval)
                if (slideIntervals.size > 50) slideIntervals.removeAt(0)
            }
            lastEventTime = eventTime
        }

        fun isSuspicious(): Boolean {
            if (slideIntervals.size < 10) return false
            val mean = slideIntervals.average()
            val variance = slideIntervals.map { (it - mean) * (it - mean) }.average()
            val stdDev = sqrt(variance)
            return stdDev < 10
        }

        fun reset() {
            slideIntervals.clear()
            lastEventTime = 0
        }
    }

    fun report(context: Context, result: DetectionResult) {
        val data = mapOf(
            "score" to result.totalScore,
            "triggered" to result.triggeredItems.joinToString(","),
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "android_version" to Build.VERSION.SDK_INT,
            "timestamp" to System.currentTimeMillis()
        )
        Log.i(TAG, "Emulator report: $data")
    }
}
