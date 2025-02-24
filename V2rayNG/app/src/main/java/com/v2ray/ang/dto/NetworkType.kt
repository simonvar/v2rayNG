package com.v2ray.ang.dto

import kotlinx.serialization.Serializable

@Serializable
enum class NetworkType(
    val type: String,
) {
    TCP("tcp"),
    KCP("kcp"),
    WS("ws"),
    HTTP_UPGRADE("httpupgrade"),
    XHTTP("xhttp"),
    HTTP("http"),
    H2("h2"),
    GRPC("grpc"),
    ;

    companion object {
        fun fromString(type: String?) = entries.find { it.type == type } ?: TCP
    }
}
