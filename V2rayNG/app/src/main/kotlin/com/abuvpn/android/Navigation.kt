package com.abuvpn.android

internal sealed interface Destination {
    data object Home : Destination

    data object Settings : Destination
}
