package com.v2ray.ang.fmt

import com.v2ray.ang.AppConfig
import com.v2ray.ang.AppConfig.WIREGUARD_LOCAL_ADDRESS_V4
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.dto.V2rayConfig.OutboundBean
import com.v2ray.ang.extension.idnHost
import com.v2ray.ang.util.Utils
import java.net.URI
import kotlin.text.orEmpty

object WireguardFmt : FmtBase() {

    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.WIREGUARD)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        config.secretKey = uri.userInfo.orEmpty()
        config.localAddress = (queryParam["address"] ?: WIREGUARD_LOCAL_ADDRESS_V4)
        config.publicKey = queryParam["publickey"].orEmpty()
        config.preSharedKey = queryParam["presharedkey"].orEmpty()
        config.mtu = Utils.parseInt(queryParam["mtu"] ?: AppConfig.WIREGUARD_LOCAL_MTU)
        config.reserved = (queryParam["reserved"] ?: "0,0,0")

        return config
    }

    fun toOutbound(profileItem: ProfileItem): OutboundBean? {
        val outboundBean = OutboundBean.create(EConfigType.WIREGUARD)

        outboundBean?.settings?.let { wireguard ->
            wireguard.secretKey = profileItem.secretKey
            wireguard.address = (profileItem.localAddress ?: WIREGUARD_LOCAL_ADDRESS_V4).split(",")
            wireguard.peers?.firstOrNull()?.let { peer ->
                peer.publicKey = profileItem.publicKey.orEmpty()
                peer.preSharedKey = profileItem.preSharedKey.orEmpty()
                peer.endpoint = Utils.getIpv6Address(profileItem.server) + ":${profileItem.serverPort}"
            }
            wireguard.mtu = profileItem.mtu
            wireguard.reserved = profileItem.reserved?.split(",")?.map { it.toInt() }
        }

        return outboundBean
    }
}
