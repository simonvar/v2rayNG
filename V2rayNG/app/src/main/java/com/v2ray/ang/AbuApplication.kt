package com.v2ray.ang

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.tencent.mmkv.MMKV
import com.v2ray.ang.AppConfig.ANG_PACKAGE
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.util.Utils

class AbuApplication : Application() {
    companion object {
        lateinit var application: AbuApplication
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }

    private val workManagerConfiguration: Configuration =
        Configuration.Builder().setDefaultProcessName("${ANG_PACKAGE}:bg").build()

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        Utils.setNightMode()
        WorkManager.initialize(this, workManagerConfiguration)
        SettingsManager.initRoutingRulesets(this)
    }
}
