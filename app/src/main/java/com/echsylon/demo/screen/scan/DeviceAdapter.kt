package com.echsylon.demo.screen.scan

import android.bluetooth.BluetoothAssignedNumbers.APPLE
import android.bluetooth.BluetoothAssignedNumbers.GOOGLE
import android.bluetooth.BluetoothAssignedNumbers.MICROSOFT
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.echsylon.demo.R
import com.echsylon.demo.databinding.ScanDeviceLayoutBinding
import com.echsylon.demo.databinding.ScanEmptyLayoutBinding
import com.echsylon.demo.discovery.model.Device

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
    private companion object {
        private const val TYPE_INFO = 1
        private const val TYPE_DATA = 2
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(data: Any?, clickAction: ((Device) -> Unit)?)
    }

    inner class DeviceViewHolder(private val binding: ScanDeviceLayoutBinding) : ViewHolder(binding.root) {
        override fun bind(data: Any?, clickAction: ((Device) -> Unit)?) {
            if (data is Device) {
                when (data.manufacturer) {
                    APPLE -> binding.icon.setImageResource(R.drawable.ic_custom_apple_24)
                    GOOGLE -> binding.icon.setImageResource(R.drawable.ic_custom_google_24)
                    MICROSOFT -> binding.icon.setImageResource(R.drawable.ic_custom_windows_24)
                    else -> binding.icon.setImageDrawable(null)
                }
                binding.title.text = data.alias ?: data.name ?: defaultName
                binding.description.text = data.address
                when (data.connectable) {
                    true -> {
                        binding.connectButton.visibility = VISIBLE
                        binding.root.isEnabled = true
                        binding.root.setOnClickListener { clickAction?.invoke(data) }
                    }
                    else -> {
                        binding.connectButton.visibility = INVISIBLE
                        binding.root.isEnabled = false
                        binding.root.setOnClickListener(null)
                    }
                }
            }
        }
    }

    class EmptyViewHolder(private val binding: ScanEmptyLayoutBinding) : ViewHolder(binding.root) {
        override fun bind(data: Any?, clickAction: ((Device) -> Unit)?) {
            // This is expected to be a static view.
            // Nothing to bind.
        }
    }

    data class Data(
        val type: Int,
        val data: Any? = null
    )

    private val content = mutableListOf(Data(TYPE_INFO))
    private var clickAction: ((Device) -> Unit)? = null
    private var defaultName: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_INFO -> EmptyViewHolder(ScanEmptyLayoutBinding.inflate(inflater, parent, false))
            TYPE_DATA -> DeviceViewHolder(ScanDeviceLayoutBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(content[position].data, clickAction)
    }

    override fun getItemCount(): Int {
        return content.size
    }

    override fun getItemViewType(position: Int): Int {
        return content[position].type
    }

    fun setDefaultDeviceName(name: String?) {
        defaultName = name
    }

    fun setItemClickAction(action: ((Device) -> Unit)?) {
        clickAction = action
    }

    fun clearScannedDevices() {
        content.clear()
        notifyDataSetChanged()
    }

    fun addScannedDevices(device: Device) {
        if (content.firstOrNull()?.type == TYPE_INFO) {
            content.removeAt(0)
            notifyItemRemoved(0)
        }
        content.add(Data(TYPE_DATA, device))
        notifyItemInserted(content.lastIndex)
    }
}