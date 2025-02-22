package com.abuvpn.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.abuvpn.android.home.home
import com.abuvpn.android.settings.settings
import com.abuvpn.android.theme.AbuvpnTheme

internal class AbuActivity : ComponentActivity() {
    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        callback = {},
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AbuvpnTheme {
                AbuContent(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun AbuContent(modifier: Modifier = Modifier) {
    val controller = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = controller,
        startDestination = Destination.Home,
    ) {
        this.home()
        this.settings()
    }
}
