package com.abuvpn.android.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface HomeViewModel {
    val state: StateFlow<State>

    @Immutable
    data class State(
        val isConnected: Boolean = false,
        val hasConfig: Boolean = false,
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

    private fun initConfig() =
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(hasConfig = MmkvManager.getSelectServer() != null)
            }
        }
}
