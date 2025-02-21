package com.v2ray.ang.ui

import android.content.Context
import android.os.Bundle
import com.v2ray.ang.R
import com.v2ray.ang.service.V2RayServiceManager
import com.v2ray.ang.util.MyContextWrapper
import com.v2ray.ang.util.Utils

class ShortcutSwitchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveTaskToBack(true)

        setContentView(R.layout.activity_none)

        if (V2RayServiceManager.v2rayPoint.isRunning) {
            Utils.stopVService(this)
        } else {
            Utils.startVServiceFromToggle(this)
        }
        finish()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase ?: return, Utils.getLocale()))
    }
}
