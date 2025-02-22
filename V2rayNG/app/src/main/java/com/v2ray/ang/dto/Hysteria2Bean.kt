package com.v2ray.ang.dto

import kotlinx.serialization.Serializable

@Serializable
data class Hysteria2Bean(
    val server: String?,
    val auth: String?,
    val lazy: Boolean? = true,
    val obfs: ObfsBean? = null,
    val socks5: Socks5Bean? = null,
    val http: Socks5Bean? = null,
    val tls: TlsBean? = null,
    val transport: TransportBean? = null,
    val bandwidth: BandwidthBean? = null,
) {
    @Serializable
    data class ObfsBean(
        val type: String?,
        val salamander: SalamanderBean?,
    ) {
        @Serializable
        data class SalamanderBean(
            val password: String?,
        )
    }

    @Serializable
    data class Socks5Bean(
        val listen: String?,
    )

    @Serializable
    data class TlsBean(
        val sni: String?,
        val insecure: Boolean?,
        val pinSHA256: String?,
    )

    @Serializable
    data class TransportBean(
        val type: String?,
        val udp: TransportUdpBean?,
    ) {
        @Serializable
        data class TransportUdpBean(
            val hopInterval: String?,
        )
    }

    @Serializable
    data class BandwidthBean(
        val down: String?,
        val up: String?,
    )
}
