package com.v2ray.ang.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ActivitySubSettingBinding
import com.v2ray.ang.databinding.ItemRecyclerUserAssetBinding
import com.v2ray.ang.dto.AssetUrlItem
import com.v2ray.ang.extension.toTrafficString
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.util.Utils
import java.io.File
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserAssetActivity : BaseActivity() {
    private val binding by lazy { ActivitySubSettingBinding.inflate(layoutInflater) }

    val extDir by lazy { File(Utils.userAssetPath(this)) }
    val builtInGeoFiles = arrayOf("geosite.dat", "geoip.dat")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = getString(R.string.title_user_asset_setting)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = UserAssetAdapter()
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_asset, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Use when to streamline the option selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.add_url ->
                startActivity(Intent(this, UserAssetUrlActivity::class.java)).let { true }
            else -> super.onOptionsItemSelected(item)
        }

    private fun addBuiltInGeoItems(
        assets: List<Pair<String, AssetUrlItem>>
    ): List<Pair<String, AssetUrlItem>> {
        val list = mutableListOf<Pair<String, AssetUrlItem>>()
        builtInGeoFiles
            .filter { geoFile -> assets.none { it.second.remarks == geoFile } }
            .forEach { list.add(Utils.getUuid() to AssetUrlItem(it, AppConfig.GeoUrl + it)) }

        return list + assets
    }

    fun initAssets() {
        lifecycleScope.launch(Dispatchers.Default) {
            SettingsManager.initAssets(this@UserAssetActivity, assets)
            withContext(Dispatchers.Main) { binding.recyclerView.adapter?.notifyDataSetChanged() }
        }
    }

    inner class UserAssetAdapter : RecyclerView.Adapter<UserAssetViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAssetViewHolder {
            return UserAssetViewHolder(
                ItemRecyclerUserAssetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: UserAssetViewHolder, position: Int) {
            var assets = MmkvManager.decodeAssetUrls()
            assets = addBuiltInGeoItems(assets)
            val item = assets.getOrNull(position) ?: return
            val file = extDir.listFiles()?.find { it.name == item.second.remarks }

            holder.itemUserAssetBinding.assetName.text = item.second.remarks

            if (file != null) {
                val dateFormat =
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
                holder.itemUserAssetBinding.assetProperties.text =
                    "${file.length().toTrafficString()}  •  ${dateFormat.format(Date(file.lastModified()))}"
            } else {
                holder.itemUserAssetBinding.assetProperties.text =
                    getString(R.string.msg_file_not_found)
            }

            if (
                item.second.remarks in builtInGeoFiles &&
                    item.second.url == AppConfig.GeoUrl + item.second.remarks
            ) {
                holder.itemUserAssetBinding.layoutEdit.visibility = GONE
                // holder.itemUserAssetBinding.layoutRemove.visibility = GONE
            } else {
                holder.itemUserAssetBinding.layoutEdit.visibility =
                    item.second.url.let { if (it == "file") GONE else VISIBLE }
                // holder.itemUserAssetBinding.layoutRemove.visibility = VISIBLE
            }

            holder.itemUserAssetBinding.layoutEdit.setOnClickListener {
                val intent = Intent(this@UserAssetActivity, UserAssetUrlActivity::class.java)
                intent.putExtra("assetId", item.first)
                startActivity(intent)
            }
            holder.itemUserAssetBinding.layoutRemove.setOnClickListener {
                AlertDialog.Builder(this@UserAssetActivity)
                    .setMessage(R.string.del_config_comfirm)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        file?.delete()
                        MmkvManager.removeAssetUrl(item.first)
                        initAssets()
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        // do noting
                    }
                    .show()
            }
        }

        override fun getItemCount(): Int {
            var assets = MmkvManager.decodeAssetUrls()
            assets = addBuiltInGeoItems(assets)
            return assets.size
        }
    }

    class UserAssetViewHolder(val itemUserAssetBinding: ItemRecyclerUserAssetBinding) :
        RecyclerView.ViewHolder(itemUserAssetBinding.root)
}
