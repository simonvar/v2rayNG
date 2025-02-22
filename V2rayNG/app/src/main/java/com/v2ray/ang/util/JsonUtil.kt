package com.v2ray.ang.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object JsonUtil {
    private val gson = Gson()

    fun toJson(src: Any?): String = gson.toJson(src)

    fun <T> fromJson(
        src: String,
        cls: Class<T>,
    ): T = gson.fromJson(src, cls)

    fun parseString(src: String?): JsonObject? {
        if (src == null) return null
        try {
            return JsonParser.parseString(src).getAsJsonObject()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
