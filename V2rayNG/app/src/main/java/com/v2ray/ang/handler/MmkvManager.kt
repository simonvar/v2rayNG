package com.v2ray.ang.handler

import com.tencent.mmkv.MMKV
import com.v2ray.ang.AppConfig.PREF_IS_BOOTED
import com.v2ray.ang.AppConfig.PREF_ROUTING_RULESET
import com.v2ray.ang.dto.AssetUrlItem
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.dto.RulesetItem
import com.v2ray.ang.util.JsonUtil
import com.v2ray.ang.util.Utils

object MmkvManager {

    // private const val ID_PROFILE_CONFIG = "PROFILE_CONFIG"
    private const val ID_MAIN = "MAIN"
    private const val ID_PROFILE_FULL_CONFIG = "PROFILE_FULL_CONFIG"
    private const val ID_SERVER_AFF = "SERVER_AFF"
    private const val ID_ASSET = "ASSET"
    private const val ID_SETTING = "SETTING"
    private const val KEY_SELECTED_SERVER = "SELECTED_SERVER"
    private const val KEY_ANG_CONFIGS = "ANG_CONFIGS"

    private val mainStorage by lazy { MMKV.mmkvWithID(ID_MAIN, MMKV.MULTI_PROCESS_MODE) }
    private val profileFullStorage by lazy {
        MMKV.mmkvWithID(ID_PROFILE_FULL_CONFIG, MMKV.MULTI_PROCESS_MODE)
    }
    private val serverAffStorage by lazy { MMKV.mmkvWithID(ID_SERVER_AFF, MMKV.MULTI_PROCESS_MODE) }
    private val assetStorage by lazy { MMKV.mmkvWithID(ID_ASSET, MMKV.MULTI_PROCESS_MODE) }
    private val settingsStorage by lazy { MMKV.mmkvWithID(ID_SETTING, MMKV.MULTI_PROCESS_MODE) }

    fun getSelectServer(): String? {
        return mainStorage.decodeString(KEY_SELECTED_SERVER)
    }

    fun setSelectServer(guid: String) {
        mainStorage.encode(KEY_SELECTED_SERVER, guid)
    }

    fun encodeServerList(serverList: MutableList<String>) {
        mainStorage.encode(KEY_ANG_CONFIGS, JsonUtil.toJson(serverList))
    }

    fun decodeServerList(): MutableList<String> {
        val json = mainStorage.decodeString(KEY_ANG_CONFIGS)
        return if (json.isNullOrBlank()) {
            mutableListOf()
        } else {
            JsonUtil.fromJson(json, Array<String>::class.java).toMutableList()
        }
    }

    fun decodeServerConfig(guid: String): ProfileItem? {
        if (guid.isBlank()) {
            return null
        }
        val json = profileFullStorage.decodeString(guid)
        if (json.isNullOrBlank()) {
            return null
        }
        return JsonUtil.fromJson(json, ProfileItem::class.java)
    }

    fun encodeServerConfig(guid: String, config: ProfileItem): String {
        val key = guid.ifBlank { Utils.getUuid() }
        profileFullStorage.encode(key, JsonUtil.toJson(config))
        val serverList = decodeServerList()
        if (!serverList.contains(key)) {
            serverList.add(0, key)
            encodeServerList(serverList)
            if (getSelectServer().isNullOrBlank()) {
                mainStorage.encode(KEY_SELECTED_SERVER, key)
            }
        }
        return key
    }

    fun removeServer(guid: String) {
        if (guid.isBlank()) {
            return
        }
        if (getSelectServer() == guid) {
            mainStorage.remove(KEY_SELECTED_SERVER)
        }
        val serverList = decodeServerList()
        serverList.remove(guid)
        encodeServerList(serverList)
        profileFullStorage.remove(guid)
        serverAffStorage.remove(guid)
    }

    fun decodeAssetUrls(): List<Pair<String, AssetUrlItem>> {
        val assetUrlItems = mutableListOf<Pair<String, AssetUrlItem>>()
        assetStorage.allKeys()?.forEach { key ->
            val json = assetStorage.decodeString(key)
            if (!json.isNullOrBlank()) {
                assetUrlItems.add(Pair(key, JsonUtil.fromJson(json, AssetUrlItem::class.java)))
            }
        }
        return assetUrlItems.sortedBy { (_, value) -> value.addedTime }
    }

    fun removeAssetUrl(assetid: String) {
        assetStorage.remove(assetid)
    }

    fun encodeAsset(assetid: String, assetItem: AssetUrlItem) {
        val key = assetid.ifBlank { Utils.getUuid() }
        assetStorage.encode(key, JsonUtil.toJson(assetItem))
    }

    fun decodeAsset(assetid: String): AssetUrlItem? {
        val json = assetStorage.decodeString(assetid) ?: return null
        return JsonUtil.fromJson(json, AssetUrlItem::class.java)
    }

    // endregion

    // region Routing

    fun decodeRoutingRulesets(): MutableList<RulesetItem>? {
        val ruleset = settingsStorage.decodeString(PREF_ROUTING_RULESET)
        if (ruleset.isNullOrEmpty()) return null
        return JsonUtil.fromJson(ruleset, Array<RulesetItem>::class.java).toMutableList()
    }

    fun encodeRoutingRulesets(rulesetList: MutableList<RulesetItem>?) {
        if (rulesetList.isNullOrEmpty()) encodeSettings(PREF_ROUTING_RULESET, "")
        else encodeSettings(PREF_ROUTING_RULESET, JsonUtil.toJson(rulesetList))
    }

    // endregion
    fun encodeSettings(key: String, value: String?): Boolean {
        return settingsStorage.encode(key, value)
    }

    fun encodeSettings(key: String, value: Boolean): Boolean {
        return settingsStorage.encode(key, value)
    }

    fun encodeSettings(key: String, value: MutableSet<String>): Boolean {
        return settingsStorage.encode(key, value)
    }

    fun decodeSettingsString(key: String): String? {
        return settingsStorage.decodeString(key)
    }

    fun decodeSettingsString(key: String, defaultValue: String?): String? {
        return settingsStorage.decodeString(key, defaultValue)
    }

    fun decodeSettingsBool(key: String): Boolean {
        return settingsStorage.decodeBool(key, false)
    }

    fun decodeSettingsBool(key: String, defaultValue: Boolean): Boolean {
        return settingsStorage.decodeBool(key, defaultValue)
    }

    fun decodeSettingsStringSet(key: String): MutableSet<String>? {
        return settingsStorage.decodeStringSet(key)
    }

    fun decodeStartOnBoot(): Boolean {
        return decodeSettingsBool(PREF_IS_BOOTED, false)
    }
}
