package com.v2ray.ang.dto

import com.v2ray.ang.AppConfig
import kotlinx.serialization.Serializable

@Serializable
enum class EConfigType(
    val value: Int,
    val protocolScheme: String,
) {
    VMESS(1, AppConfig.VMESS),
    SHADOWSOCKS(3, AppConfig.SHADOWSOCKS),
    SOCKS(4, AppConfig.SOCKS),
    VLESS(5, AppConfig.VLESS),
    TROJAN(6, AppConfig.TROJAN),
    WIREGUARD(7, AppConfig.WIREGUARD),
    HYSTERIA2(9, AppConfig.HYSTERIA2),
}
