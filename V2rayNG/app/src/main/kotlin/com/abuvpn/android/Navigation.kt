package com.abuvpn.android

import kotlinx.serialization.Serializable

internal sealed interface Destination {
    @Serializable
    data object Home : Destination

    @Serializable
    data object Settings : Destination
}
