package com.abuvpn.android.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abuvpn.android.Destination

internal fun NavGraphBuilder.home() {
    composable<Destination.Home> { entry ->
        HomeScreen(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun HomeScreen(modifier: Modifier = Modifier) {
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
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {}
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
            painter = painterResource(com.v2ray.ang.R.drawable.img_abu_logo),
            contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Image(
            modifier = Modifier.height(50.dp),
            painter = painterResource(com.v2ray.ang.R.drawable.img_abu_title),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.weight(1F))
        TextButton(onClick = {}) {
            Text(text = stringResource(id = com.v2ray.ang.R.string.home_settings))
        }
    }
}
