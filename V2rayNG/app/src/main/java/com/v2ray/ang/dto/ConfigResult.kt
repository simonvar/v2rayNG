package com.v2ray.ang.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConfigResult(
    var status: Boolean,
    var guid: String? = null,
    var content: String = "",
    var domainPort: String? = null,
)

