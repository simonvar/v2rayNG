package com.abuvpn.android.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.v2ray.ang.R
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.ConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.service.V2RayServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface HomeViewModel {
    val state: StateFlow<State>

    fun onSwitchClick(
        context: Context,
        launcher: ActivityResultLauncher<Intent>,
    )

    fun onVpnLauncherResult(
        context: Context,
        result: ActivityResult,
    )

    fun onConfigCreate()

    fun onConfigChange(config: String)

    @Immutable
    data class State(
        val isConnected: Boolean = false,
        val isConfigured: Boolean = false,
        val configInput: String = "",
    )
}

internal class HomeViewModelImpl :
    ViewModel(),
    HomeViewModel {
    private val _state = MutableStateFlow(HomeViewModel.State())
    override val state: StateFlow<HomeViewModel.State> = _state.asStateFlow()

    init {
        initConfig()
    }

    override fun onSwitchClick(
        context: Context,
        launcher: ActivityResultLauncher<Intent>,
    ) {
        val intent = VpnService.prepare(context)
        if (intent == null) {
            startV2Ray(context)
        } else {
            launcher.launch(intent)
        }
    }

    override fun onVpnLauncherResult(
        context: Context,
        result: ActivityResult,
    ) {
        if (result.resultCode == Activity.RESULT_OK) {
            startV2Ray(context)
        }
    }

    override fun onConfigCreate() {
        val config = _state.value.configInput
        if (config.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                val added = ConfigManager.importBatchConfig(config)
                _state.update {
                    it.copy(isConfigured = added > 0)
                }
            }
        }
    }

    override fun onConfigChange(config: String) {
        _state.update {
            it.copy(configInput = config)
        }
    }

    private fun startV2Ray(context: Context) {
        if (MmkvManager.getSelectServer().isNullOrEmpty()) {
            context.toast(R.string.title_file_chooser)
            return
        }
        V2RayServiceManager.startV2Ray(context)
    }

    private fun initConfig() =
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(isConfigured = MmkvManager.getSelectServer() != null)
            }
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModelImpl()
            }
        }
    }
}
