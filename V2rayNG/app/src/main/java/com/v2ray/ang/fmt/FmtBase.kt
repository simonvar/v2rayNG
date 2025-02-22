package com.v2ray.ang.fmt

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.NetworkType
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.util.Utils
import java.net.URI

open class FmtBase {
    fun getQueryParam(uri: URI): Map<String, String> =
        uri.rawQuery.split("&").associate {
            it.split("=").let { (k, v) -> k to Utils.urlDecode(v) }
        }

    fun getItemFormQuery(
        config: ProfileItem,
        queryParam: Map<String, String>,
        allowInsecure: Boolean,
    ) {
        config.network = queryParam["type"] ?: NetworkType.TCP.type
        config.headerType = queryParam["headerType"]
        config.host = queryParam["host"]
        config.path = queryParam["path"]

        config.seed = queryParam["seed"]
        config.quicSecurity = queryParam["quicSecurity"]
        config.quicKey = queryParam["key"]
        config.mode = queryParam["mode"]
        config.serviceName = queryParam["serviceName"]
        config.authority = queryParam["authority"]
        config.xhttpMode = queryParam["mode"]
        config.xhttpExtra = queryParam["extra"]

        config.security = queryParam["security"]
        if (config.security != AppConfig.TLS && config.security != AppConfig.REALITY) {
            config.security = null
        }
        config.insecure =
            if (queryParam["allowInsecure"].isNullOrEmpty()) {
                allowInsecure
            } else {
                queryParam["allowInsecure"].orEmpty() == "1"
            }
        config.sni = queryParam["sni"]
        config.fingerPrint = queryParam["fp"]
        config.alpn = queryParam["alpn"]
        config.publicKey = queryParam["pbk"]
        config.shortId = queryParam["sid"]
        config.spiderX = queryParam["spx"]
        config.flow = queryParam["flow"]
    }
}
