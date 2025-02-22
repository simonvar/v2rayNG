package com.v2ray.ang.dto

import kotlinx.serialization.Serializable

@Serializable
data class RulesetItem(
    var remarks: String? = "",
    var ip: List<String>? = null,
    var domain: List<String>? = null,
    var outboundTag: String = "",
    var port: String? = null,
    var network: String? = null,
    var protocol: List<String>? = null,
    var enabled: Boolean = true,
    var locked: Boolean? = false,
)
