package com.v2ray.ang.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

open class MyContextWrapper(
    base: Context?,
) : ContextWrapper(base) {
    companion object {
        fun wrap(
            context: Context,
            newLocale: Locale?,
        ): ContextWrapper {
            var mContext = context
            val res: Resources = mContext.resources
            val configuration: Configuration = res.configuration
            configuration.setLocale(newLocale)
            mContext = mContext.createConfigurationContext(configuration)
            return ContextWrapper(mContext)
        }
    }
}
