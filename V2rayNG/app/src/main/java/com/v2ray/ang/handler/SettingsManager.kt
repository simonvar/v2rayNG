package com.v2ray.ang.handler

import android.content.Context
import android.content.res.AssetManager
import android.text.TextUtils
import android.util.Log
import com.v2ray.ang.AppConfig
import com.v2ray.ang.AppConfig.ANG_PACKAGE
import com.v2ray.ang.AppConfig.GEOIP_PRIVATE
import com.v2ray.ang.AppConfig.GEOSITE_PRIVATE
import com.v2ray.ang.AppConfig.TAG_DIRECT
import com.v2ray.ang.dto.RoutingType
import com.v2ray.ang.dto.RulesetItem
import com.v2ray.ang.util.JsonUtil
import com.v2ray.ang.util.Utils
import com.v2ray.ang.util.Utils.parseInt
import java.io.File
import java.io.FileOutputStream
import kotlin.Int

object SettingsManager {
    fun initRoutingRulesets(context: Context) {
        val exist = MmkvManager.decodeRoutingRulesets()
        if (exist.isNullOrEmpty()) {
            val rulesetList = getPresetRoutingRulesets(context)
            MmkvManager.encodeRoutingRulesets(rulesetList)
        }
    }

    private fun getPresetRoutingRulesets(
        context: Context,
        index: Int = 0,
    ): MutableList<RulesetItem>? {
        val fileName = RoutingType.fromIndex(index).fileName
        val assets = Utils.readTextFromAssets(context, fileName)
        if (TextUtils.isEmpty(assets)) {
            return null
        }

        return JsonUtil.fromJson(assets, Array<RulesetItem>::class.java).toMutableList()
    }

    fun routingRulesetsBypassLan(): Boolean {
        val vpnBypassLan = MmkvManager.decodeSettingsString(AppConfig.PREF_VPN_BYPASS_LAN) ?: "0"
        if (vpnBypassLan == "1") {
            return true
        } else if (vpnBypassLan == "2") {
            return false
        }

        val rulesetItems = MmkvManager.decodeRoutingRulesets()
        val exist =
            rulesetItems
                ?.filter { it.enabled && it.outboundTag == TAG_DIRECT }
                ?.any {
                    it.domain?.contains(GEOSITE_PRIVATE) == true ||
                        it.ip?.contains(GEOIP_PRIVATE) == true
                }
        return exist == true
    }

    fun getSocksPort(): Int =
        parseInt(
            MmkvManager.decodeSettingsString(AppConfig.PREF_SOCKS_PORT),
            AppConfig.PORT_SOCKS.toInt(),
        )

    fun getHttpPort(): Int = getSocksPort() + (if (Utils.isXray()) 0 else 1)

    fun initAssets(
        context: Context,
        assets: AssetManager,
    ) {
        val extFolder = Utils.userAssetPath(context)

        try {
            val geo = arrayOf("geosite.dat", "geoip.dat")
            assets
                .list("")
                ?.filter { geo.contains(it) }
                ?.filter { !File(extFolder, it).exists() }
                ?.forEach {
                    val target = File(extFolder, it)
                    assets.open(it).use { input ->
                        FileOutputStream(target).use { output -> input.copyTo(output) }
                    }
                    Log.i(ANG_PACKAGE, "Copied from apk assets folder to ${target.absolutePath}")
                }
        } catch (e: Exception) {
            Log.e(ANG_PACKAGE, "asset copy failed", e)
        }
    }
}
