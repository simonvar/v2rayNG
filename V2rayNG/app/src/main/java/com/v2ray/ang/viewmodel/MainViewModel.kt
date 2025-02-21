package com.v2ray.ang.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.AbuApplication
import com.v2ray.ang.AppConfig
import com.v2ray.ang.AppConfig.ANG_PACKAGE
import com.v2ray.ang.R
import com.v2ray.ang.dto.ServersCache
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.util.MessageUtil
import com.v2ray.ang.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private var serverList = MmkvManager.decodeServerList()

    val serversCache = mutableListOf<ServersCache>()
    val isRunning by lazy { MutableLiveData<Boolean>() }
    val updateListAction by lazy { MutableLiveData<Int>() }

    fun startListenBroadcast() {
        isRunning.value = false
        val mFilter = IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY)
        ContextCompat.registerReceiver(
            getApplication(),
            mMsgReceiver,
            mFilter,
            Utils.receiverFlags(),
        )
        MessageUtil.sendMsg2Service(getApplication(), AppConfig.MSG_REGISTER_CLIENT, "")
    }

    override fun onCleared() {
        getApplication<AbuApplication>().unregisterReceiver(mMsgReceiver)
        Log.i(ANG_PACKAGE, "Main ViewModel is cleared")
        super.onCleared()
    }

    fun reloadServerList() {
        serverList = MmkvManager.decodeServerList()
        updateCache()
        updateListAction.value = -1
    }

    fun removeServer(guid: String) {
        serverList.remove(guid)
        MmkvManager.removeServer(guid)
        val index = getPosition(guid)
        if (index >= 0) {
            serversCache.removeAt(index)
        }
    }

    @Synchronized
    fun updateCache() {
        serversCache.clear()
        for (guid in serverList) {
            var profile = MmkvManager.decodeServerConfig(guid) ?: continue
            serversCache.add(ServersCache(guid, profile))
        }
    }

    fun getPosition(guid: String): Int {
        serversCache.forEachIndexed { index, it -> if (it.guid == guid) return index }
        return -1
    }

    fun initAssets(assets: AssetManager) {
        viewModelScope.launch(Dispatchers.Default) {
            SettingsManager.initAssets(getApplication<AbuApplication>(), assets)
        }
    }

    private val mMsgReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                ctx: Context?,
                intent: Intent?,
            ) {
                when (intent?.getIntExtra("key", 0)) {
                    AppConfig.MSG_STATE_RUNNING -> {
                        isRunning.value = true
                    }

                    AppConfig.MSG_STATE_NOT_RUNNING -> {
                        isRunning.value = false
                    }

                    AppConfig.MSG_STATE_START_SUCCESS -> {
                        getApplication<AbuApplication>().toast(R.string.toast_services_success)
                        isRunning.value = true
                    }

                    AppConfig.MSG_STATE_START_FAILURE -> {
                        getApplication<AbuApplication>().toast(R.string.toast_services_failure)
                        isRunning.value = false
                    }

                    AppConfig.MSG_STATE_STOP_SUCCESS -> {
                        isRunning.value = false
                    }
                }
            }
        }
}
