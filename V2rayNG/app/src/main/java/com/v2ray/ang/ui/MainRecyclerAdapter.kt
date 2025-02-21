package com.v2ray.ang.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.AbuApplication.Companion.application
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ItemRecyclerFooterBinding
import com.v2ray.ang.databinding.ItemRecyclerMainBinding
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.service.V2RayServiceManager
import com.v2ray.ang.util.Utils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class MainRecyclerAdapter(
    val activity: MainActivity,
) : RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>() {
    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_FOOTER = 2
    }

    private var mActivity: MainActivity = activity
    var isRunning = false

    override fun getItemCount() = mActivity.mainViewModel.serversCache.size + 1

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
    ) {
        if (holder is MainViewHolder) {
            val guid = mActivity.mainViewModel.serversCache[position].guid
            val profile = mActivity.mainViewModel.serversCache[position].profile

            holder.itemMainBinding.tvName.text = profile.remarks
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)

            if (guid == MmkvManager.getSelectServer()) {
                holder.itemMainBinding.layoutIndicator.setBackgroundResource(R.color.colorAccent)
            } else {
                holder.itemMainBinding.layoutIndicator.setBackgroundResource(0)
            }

            holder.itemMainBinding.tvType.text = profile.configType.name

            val strState =
                "${
                    profile.server?.let {
                        if (it.contains(":")) {
                            it.split(":").take(2).joinToString(":", postfix = ":***")
                        } else {
                            it.split('.').dropLast(1).joinToString(".", postfix = ".***")
                        }
                    }
                } : ${profile.serverPort}"

            holder.itemMainBinding.tvStatistics.text = strState

            holder.itemMainBinding.layoutRemove.setOnClickListener {
                if (guid != MmkvManager.getSelectServer()) {
                    if (MmkvManager.decodeSettingsBool(AppConfig.PREF_CONFIRM_REMOVE) == true) {
                        AlertDialog
                            .Builder(mActivity)
                            .setMessage(R.string.del_config_comfirm)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                removeServer(guid, position)
                            }.setNegativeButton(android.R.string.cancel) { _, _ ->
                                // do noting
                            }.show()
                    } else {
                        removeServer(guid, position)
                    }
                } else {
                    application.toast(R.string.toast_action_not_allowed)
                }
            }

            holder.itemMainBinding.infoContainer.setOnClickListener {
                val selected = MmkvManager.getSelectServer()
                if (guid != selected) {
                    MmkvManager.setSelectServer(guid)
                    if (!TextUtils.isEmpty(selected)) {
                        notifyItemChanged(mActivity.mainViewModel.getPosition(selected.orEmpty()))
                    }
                    notifyItemChanged(mActivity.mainViewModel.getPosition(guid))
                    if (isRunning) {
                        Utils.stopVService(mActivity)
                        Observable
                            .timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { V2RayServiceManager.startV2Ray(mActivity) }
                    }
                }
            }
        }
        if (holder is FooterViewHolder) {
            if (true) {
                holder.itemFooterBinding.layoutEdit.visibility = View.INVISIBLE
            }
        }
    }

    private fun removeServer(
        guid: String,
        position: Int,
    ) {
        mActivity.mainViewModel.removeServer(guid)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mActivity.mainViewModel.serversCache.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseViewHolder =
        when (viewType) {
            VIEW_TYPE_ITEM ->
                MainViewHolder(
                    ItemRecyclerMainBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    ),
                )

            else ->
                FooterViewHolder(
                    ItemRecyclerFooterBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    ),
                )
        }

    override fun getItemViewType(position: Int): Int =
        if (position == mActivity.mainViewModel.serversCache.size) {
            VIEW_TYPE_FOOTER
        } else {
            VIEW_TYPE_ITEM
        }

    open class BaseViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    class MainViewHolder(
        val itemMainBinding: ItemRecyclerMainBinding,
    ) : BaseViewHolder(itemMainBinding.root)

    class FooterViewHolder(
        val itemFooterBinding: ItemRecyclerFooterBinding,
    ) : BaseViewHolder(itemFooterBinding.root)
}
