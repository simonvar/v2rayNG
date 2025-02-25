package com.abuvpn.android.home

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abuvpn.android.AbuString
import com.v2ray.ang.AbuApplication
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.ConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.service.V2RayServiceManager
import com.v2ray.ang.util.MessageUtil
import com.v2ray.ang.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface HomeViewModel {
    val state: StateFlow<State>

    fun onSwitchClick(launcher: ActivityResultLauncher<Intent>)

    fun onVpnLauncherResult(result: ActivityResult)

    fun onConfigCreate()

    fun onConfigChange(config: String)

    @Immutable
    data class State(
        val isConnected: Boolean = false,
        val isConfigured: Boolean = false,
        val configInput: String = "",
    )
}

internal class HomeViewModelImpl(
    application: Application,
) : AndroidViewModel(application),
    HomeViewModel {
    private val _state = MutableStateFlow(HomeViewModel.State())
    override val state: StateFlow<HomeViewModel.State> = _state.asStateFlow()

    private val serviceMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            when (intent.getIntExtra("key", 0)) {
                AppConfig.MSG_STATE_RUNNING -> {
                    _state.update { it.copy(isConnected = true) }
                }

                AppConfig.MSG_STATE_NOT_RUNNING -> {
                    _state.update { it.copy(isConnected = false) }
                }

                AppConfig.MSG_STATE_START_SUCCESS -> {
                    getApplication<AbuApplication>().toast(R.string.toast_services_success)
                    _state.update { it.copy(isConnected = true) }
                }

                AppConfig.MSG_STATE_START_FAILURE -> {
                    getApplication<AbuApplication>().toast(R.string.toast_services_failure)
                    _state.update { it.copy(isConnected = false) }
                }

                AppConfig.MSG_STATE_STOP_SUCCESS -> {
                    _state.update { it.copy(isConnected = false) }
                }
            }
        }
    }

    init {
        initConfig()
        startListenBroadcast()
    }

    override fun onSwitchClick(launcher: ActivityResultLauncher<Intent>) {
        val context = getApplication<AbuApplication>()

        if (_state.value.isConnected) {
            Utils.stopVService(context)
            return
        }

        val intent = VpnService.prepare(context)
        if (intent == null) {
            startV2Ray(context)
        } else {
            launcher.launch(intent)
        }
    }

    override fun onVpnLauncherResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            startV2Ray(getApplication())
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
            context.toast(AbuString.title_file_chooser)
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

    private fun startListenBroadcast() {
        _state.update { it.copy(isConnected = false) }
        val mFilter = IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY)
        ContextCompat.registerReceiver(
            getApplication(),
            serviceMessageReceiver,
            mFilter,
            Utils.receiverFlags(),
        )
        MessageUtil.sendMsg2Service(getApplication(), AppConfig.MSG_REGISTER_CLIENT, "")
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<AbuApplication>().unregisterReceiver(serviceMessageReceiver)
    }
}
