package com.v2ray.ang.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.service.V2RayServiceManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        if (!MmkvManager.decodeStartOnBoot() || MmkvManager.getSelectServer().isNullOrEmpty()) {
            return
        }

        V2RayServiceManager.startV2Ray(context)
    }
}
