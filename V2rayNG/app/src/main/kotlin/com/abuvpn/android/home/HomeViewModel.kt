package com.abuvpn.android.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.handler.ConfigManager
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface HomeViewModel {
    val state: StateFlow<State>

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

    private fun initConfig() =
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(isConfigured = MmkvManager.getSelectServer() != null)
            }
        }
}
