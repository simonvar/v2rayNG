package com.v2ray.ang.util

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.v2ray.ang.AppConfig.ANG_PACKAGE
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.fmt.Hysteria2Fmt
import com.v2ray.ang.service.ProcessService
import java.io.File

object PluginUtil {
    private const val HYSTERIA2 = "libhysteria2.so"
    private const val TAG = ANG_PACKAGE
    private val procService: ProcessService by lazy { ProcessService() }

    fun runPlugin(
        context: Context,
        config: ProfileItem?,
        domainPort: String?,
    ) {
        Log.d(TAG, "runPlugin")

        if (config?.configType?.equals(EConfigType.HYSTERIA2) == true) {
            val configFile = genConfigHy2(context, config, domainPort) ?: return
            val cmd = genCmdHy2(context, configFile)

            procService.runProcess(context, cmd)
        }
    }

    fun stopPlugin() {
        stopHy2()
    }

    private fun genConfigHy2(
        context: Context,
        config: ProfileItem,
        domainPort: String?,
    ): File? {
        Log.d(TAG, "runPlugin $HYSTERIA2")

        val socksPort =
            domainPort?.split(":")?.last().let {
                if (it.isNullOrEmpty()) return null else it.toInt()
            }
        val hy2Config = Hysteria2Fmt.toNativeConfig(config, socksPort) ?: return null

        val configFile = File(context.noBackupFilesDir, "hy2_${SystemClock.elapsedRealtime()}.json")
        Log.d(TAG, "runPlugin ${configFile.absolutePath}")

        configFile.parentFile?.mkdirs()
        configFile.writeText(JsonUtil.toJson(hy2Config))
        Log.d(TAG, JsonUtil.toJson(hy2Config))

        return configFile
    }

    private fun genCmdHy2(
        context: Context,
        configFile: File,
    ): MutableList<String> =
        mutableListOf(
            File(context.applicationInfo.nativeLibraryDir, HYSTERIA2).absolutePath,
            "--disable-update-check",
            "--config",
            configFile.absolutePath,
            "--log-level",
            "warn",
            "client",
        )

    private fun stopHy2() {
        try {
            Log.d(TAG, "$HYSTERIA2 destroy")
            procService.stopProcess()
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }
}
