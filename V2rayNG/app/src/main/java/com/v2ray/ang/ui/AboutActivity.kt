package com.v2ray.ang.ui

import android.os.Bundle
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ActivityAboutBinding
import com.v2ray.ang.util.Utils

class AboutActivity : BaseActivity() {
    private val binding by lazy { ActivityAboutBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        title = getString(R.string.title_about)

        binding.layoutPrivacyPolicy.setOnClickListener {
            Utils.openUri(this, AppConfig.v2rayNGPrivacyPolicy)
        }

        "v${BuildConfig.VERSION_NAME}".also { binding.tvVersion.text = it }
    }
}
