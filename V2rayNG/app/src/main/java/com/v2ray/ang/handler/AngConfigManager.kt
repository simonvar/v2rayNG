package com.v2ray.ang.handler

import android.text.TextUtils
import com.v2ray.ang.R
import com.v2ray.ang.dto.*
import com.v2ray.ang.fmt.ShadowsocksFmt
import com.v2ray.ang.fmt.SocksFmt
import com.v2ray.ang.fmt.TrojanFmt
import com.v2ray.ang.fmt.VlessFmt
import com.v2ray.ang.fmt.VmessFmt
import com.v2ray.ang.fmt.WireguardFmt
import com.v2ray.ang.util.Utils

object AngConfigManager {

    fun importBatchConfig(server: String?): Int {
        var count = parseBatchConfig(Utils.decode(server))
        if (count <= 0) {
            count = parseBatchConfig(server)
        }
        return count
    }

    private fun parseBatchConfig(servers: String?): Int {
        try {
            if (servers == null) {
                return 0
            }

            var count = 0
            servers.lines().distinct().reversed().forEach {
                val resId = parseConfig(it)
                if (resId == 0) {
                    count++
                }
            }
            return count
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun parseConfig(str: String?): Int {
        try {
            if (str == null || TextUtils.isEmpty(str)) {
                return R.string.toast_none_data
            }

            val config =
                if (str.startsWith(EConfigType.VMESS.protocolScheme)) {
                    VmessFmt.parse(str)
                } else if (str.startsWith(EConfigType.SHADOWSOCKS.protocolScheme)) {
                    ShadowsocksFmt.parse(str)
                } else if (str.startsWith(EConfigType.SOCKS.protocolScheme)) {
                    SocksFmt.parse(str)
                } else if (str.startsWith(EConfigType.TROJAN.protocolScheme)) {
                    TrojanFmt.parse(str)
                } else if (str.startsWith(EConfigType.VLESS.protocolScheme)) {
                    VlessFmt.parse(str)
                } else if (str.startsWith(EConfigType.WIREGUARD.protocolScheme)) {
                    WireguardFmt.parse(str)
                } else {
                    null
                }

            if (config == null) {
                return R.string.toast_incorrect_protocol
            }

            val guid = MmkvManager.encodeServerConfig("", config)
            MmkvManager.setSelectServer(guid)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
        return 0
    }
}
