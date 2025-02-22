package com.abuvpn.android.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abuvpn.android.Destination

internal fun NavGraphBuilder.settings() {
    composable<Destination.Settings> { entry ->
        SettingsScreen(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { }
    }
}
