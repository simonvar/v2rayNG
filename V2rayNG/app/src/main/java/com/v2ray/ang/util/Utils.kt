package com.v2ray.ang.util

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.os.Build
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.AppConfig.ANG_PACKAGE
import com.v2ray.ang.R
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.service.V2RayServiceManager
import java.io.IOException
import java.net.ServerSocket
import java.net.URLDecoder
import java.util.UUID

object Utils {
    fun parseInt(str: String): Int = parseInt(str, 0)

    fun parseInt(
        str: String?,
        default: Int,
    ): Int = str?.toIntOrNull() ?: default

    fun getClipboard(context: Context): String =
        try {
            val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cmb.primaryClip
                ?.getItemAt(0)
                ?.text
                .toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

    /** base64 decode */
    fun decode(text: String?): String = tryDecodeBase64(text) ?: text?.trimEnd('=')?.let { tryDecodeBase64(it) }.orEmpty()

    fun tryDecodeBase64(text: String?): String? {
        try {
            return Base64.decode(text, Base64.NO_WRAP).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.i(ANG_PACKAGE, "Parse base64 standard failed $e")
        }
        try {
            return Base64.decode(text, Base64.NO_WRAP.or(Base64.URL_SAFE)).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.i(ANG_PACKAGE, "Parse base64 url safe failed $e")
        }
        return null
    }

    /** get remote dns servers from preference */
    fun getRemoteDnsServers(): List<String> {
        val remoteDns =
            MmkvManager.decodeSettingsString(AppConfig.PREF_REMOTE_DNS) ?: AppConfig.DNS_PROXY
        val ret = remoteDns.split(",").filter { isPureIpAddress(it) || isCoreDNSAddress(it) }
        if (ret.isEmpty()) {
            return listOf(AppConfig.DNS_PROXY)
        }
        return ret
    }

    fun getVpnDnsServers(): List<String> {
        val vpnDns = MmkvManager.decodeSettingsString(AppConfig.PREF_VPN_DNS) ?: AppConfig.DNS_VPN
        return vpnDns.split(",").filter { isPureIpAddress(it) }
        // allow empty, in that case dns will use system default
    }

    /** get remote dns servers from preference */
    fun getDomesticDnsServers(): List<String> {
        val domesticDns =
            MmkvManager.decodeSettingsString(AppConfig.PREF_DOMESTIC_DNS) ?: AppConfig.DNS_DIRECT
        val ret = domesticDns.split(",").filter { isPureIpAddress(it) || isCoreDNSAddress(it) }
        if (ret.isEmpty()) {
            return listOf(AppConfig.DNS_DIRECT)
        }
        return ret
    }

    /** is ip address */
    fun isIpAddress(value: String?): Boolean {
        try {
            if (value.isNullOrEmpty()) {
                return false
            }
            var addr = value
            if (addr.isEmpty() || addr.isBlank()) {
                return false
            }
            // CIDR
            if (addr.indexOf("/") > 0) {
                val arr = addr.split("/")
                if (arr.count() == 2 && Integer.parseInt(arr[1]) > -1) {
                    addr = arr[0]
                }
            }

            // "::ffff:192.168.173.22"
            // "[::ffff:192.168.173.22]:80"
            if (addr.startsWith("::ffff:") && '.' in addr) {
                addr = addr.drop(7)
            } else if (addr.startsWith("[::ffff:") && '.' in addr) {
                addr = addr.drop(8).replace("]", "")
            }

            // addr = addr.toLowerCase()
            val octets = addr.split('.').toTypedArray()
            if (octets.size == 4) {
                if (octets[3].indexOf(":") > 0) {
                    addr = addr.substring(0, addr.indexOf(":"))
                }
                return isIpv4Address(addr)
            }

            // Ipv6addr [2001:abc::123]:8080
            return isIpv6Address(addr)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun isPureIpAddress(value: String): Boolean = isIpv4Address(value) || isIpv6Address(value)

    fun isIpv4Address(value: String): Boolean {
        val regV4 =
            Regex(
                "^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$",
            )
        return regV4.matches(value)
    }

    fun isIpv6Address(value: String): Boolean {
        var addr = value
        if (addr.indexOf("[") == 0 && addr.lastIndexOf("]") > 0) {
            addr = addr.drop(1)
            addr = addr.dropLast(addr.count() - addr.lastIndexOf("]"))
        }
        val regV6 =
            Regex(
                "^((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$",
            )
        return regV6.matches(addr)
    }

    private fun isCoreDNSAddress(s: String): Boolean =
        s.startsWith("https") ||
            s.startsWith("tcp") ||
            s.startsWith("quic") ||
            s == "localhost"

    /** is valid url */
    fun isValidUrl(value: String?): Boolean {
        try {
            if (value.isNullOrEmpty()) {
                return false
            }
            if (
                Patterns.WEB_URL.matcher(value).matches() ||
                Patterns.DOMAIN_NAME.matcher(value).matches() ||
                URLUtil.isValidUrl(value)
            ) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }

    fun startVServiceFromToggle(context: Context): Boolean {
        if (MmkvManager.getSelectServer().isNullOrEmpty()) {
            context.toast(R.string.app_tile_first_use)
            return false
        }
        V2RayServiceManager.startV2Ray(context)
        return true
    }

    fun stopVService(context: Context) {
        context.toast(R.string.toast_services_stop)
        MessageUtil.sendMsg2Service(context, AppConfig.MSG_STATE_STOP, "")
    }

    fun getUuid(): String =
        try {
            UUID.randomUUID().toString().replace("-", "")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

    fun urlDecode(url: String): String =
        try {
            URLDecoder.decode(url, Charsets.UTF_8.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            url
        }

    fun readTextFromAssets(
        context: Context?,
        fileName: String,
    ): String {
        if (context == null) {
            return ""
        }
        val content =
            context.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }
        return content
    }

    fun userAssetPath(context: Context?): String {
        if (context == null) return ""
        val extDir =
            context.getExternalFilesDir(AppConfig.DIR_ASSETS)
                ?: return context.getDir(AppConfig.DIR_ASSETS, 0).absolutePath
        return extDir.absolutePath
    }

    fun getDeviceIdForXUDPBaseKey(): String {
        val androidId = Settings.Secure.ANDROID_ID.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(androidId.copyOf(32), Base64.NO_PADDING.or(Base64.URL_SAFE))
    }

    fun getDarkModeStatus(context: Context): Boolean = context.resources.configuration.uiMode and UI_MODE_NIGHT_MASK != UI_MODE_NIGHT_NO

    fun setNightMode() {
        when (MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0")) {
            "0" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "1" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "2" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun getIpv6Address(address: String?): String {
        if (address == null) {
            return ""
        }
        return if (isIpv6Address(address) && !address.contains('[') && !address.contains(']')) {
            String.format("[%s]", address)
        } else {
            address
        }
    }

    fun fixIllegalUrl(str: String): String = str.replace(" ", "%20").replace("|", "%7C")

    fun getDelayTestUrl(second: Boolean = false): String =
        if (second) {
            AppConfig.DelayTestUrl2
        } else {
            MmkvManager.decodeSettingsString(AppConfig.PREF_DELAY_TEST_URL)
                ?: AppConfig.DelayTestUrl
        }

    fun receiverFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.RECEIVER_EXPORTED
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED
        }

    fun findFreePort(ports: List<Int>): Int {
        for (port in ports) {
            try {
                return ServerSocket(port).use { it.localPort }
            } catch (_: IOException) {
                continue // try next port
            }
        }
        throw IOException("no free port found")
    }

    fun isXray(): Boolean = true // (ANG_PACKAGE.startsWith("com.v2ray.ang"))
}
