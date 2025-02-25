package com.abuvpn.android.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abuvpn.android.AbuDrawable
import com.abuvpn.android.AbuString
import com.abuvpn.android.Destination
import com.abuvpn.android.theme.Green

internal fun NavGraphBuilder.home() {
    composable<Destination.Home> { entry ->
        val context = LocalContext.current
        val vm: HomeViewModel = viewModel<HomeViewModelImpl>(
            factory = HomeViewModelImpl.Factory,
        )
        val requestVpnPermission = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { vm.onVpnLauncherResult(context, it) },
        )
        val state by vm.state.collectAsStateWithLifecycle()
        HomeScreen(
            modifier = Modifier.fillMaxSize(),
            state = state,
            configure = {
                HomeConfigure(
                    modifier = Modifier.fillMaxSize(),
                    onConfigCreateClick = vm::onConfigCreate,
                    onConfigInputChange = vm::onConfigChange,
                    configInput = state.configInput,
                )
            },
            configured = {
                HomeConfigured(
                    modifier = Modifier.fillMaxSize(),
                    onConfigSwitch = { vm.onSwitchClick(context, requestVpnPermission) },
                )
            },
        )
    }
}

@Composable
private fun HomeScreen(
    state: HomeViewModel.State,
    modifier: Modifier = Modifier,
    configure: @Composable () -> Unit = {},
    configured: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            HomeTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Crossfade(targetState = state.isConfigured) {
                if (it) configured() else configure()
            }
        }
    }
}

@Composable
private fun HomeTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.size(50.dp),
            painter = painterResource(AbuDrawable.img_abu_logo),
            contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Image(
            modifier = Modifier.height(50.dp),
            painter = painterResource(AbuDrawable.img_abu_title),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.weight(1F))
        TextButton(onClick = {}) {
            Text(text = stringResource(id = AbuString.home_settings))
        }
    }
}

@Composable
private fun HomeConfigured(
    onConfigSwitch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun HomeConfigure(
    onConfigCreateClick: () -> Unit,
    onConfigInputChange: (String) -> Unit,
    configInput: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            value = configInput,
            onValueChange = onConfigInputChange,
            placeholder = { Text(text = stringResource(id = AbuString.home_input_placeholder)) },
        )
        Spacer(Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            onClick = onConfigCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = Green),
        ) {
            Text(
                color = Color.White,
                text = stringResource(id = AbuString.home_input_button),
            )
        }
    }
}
