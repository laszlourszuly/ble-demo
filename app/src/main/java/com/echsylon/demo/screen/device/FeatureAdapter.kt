package com.echsylon.demo.screen.device

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.echsylon.demo.databinding.DeviceCharacteristicLayoutBinding
import com.echsylon.demo.databinding.DeviceDescriptorLayoutBinding
import com.echsylon.demo.databinding.DeviceServiceLayoutBinding
import com.echsylon.demo.connection.model.Characteristic
import com.echsylon.demo.connection.model.Descriptor
import com.echsylon.demo.connection.model.Feature
import com.echsylon.demo.connection.model.Service

class FeatureAdapter : RecyclerView.Adapter<FeatureAdapter.ViewHolder>() {
    private companion object {
        private const val TYPE_SERVICE = 1
        private const val TYPE_DESCRIPTOR = 2
        private const val TYPE_CHARACTERISTIC = 3
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(data: Feature, defaultValue: String?)
    }

    class ServiceViewHolder(private val binding: DeviceServiceLayoutBinding) : ViewHolder(binding.root) {
        override fun bind(data: Feature, defaultValue: String?) {
            if (data is Service) {
                binding.name.text = data.label
            }
        }
    }

    class DescriptorViewHolder(private val binding: DeviceDescriptorLayoutBinding) : ViewHolder(binding.root) {
        override fun bind(data: Feature, defaultValue: String?) {
            if (data is Descriptor) {
                binding.name.text = data.label
                binding.value.text = data.value ?: defaultValue
            }
        }
    }

    class CharacteristicViewHolder(private val binding: DeviceCharacteristicLayoutBinding) : ViewHolder(binding.root) {
        override fun bind(data: Feature, defaultValue: String?) {
            if (data is Characteristic) {
                binding.name.text = data.label
                binding.value.text = data.value ?: defaultValue
            }
        }
    }

    private class Data(
        val type: Int,
        val data: Feature
    )

    private val content = mutableListOf<Data>()
    private var defaultValue: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SERVICE -> ServiceViewHolder(DeviceServiceLayoutBinding.inflate(inflater, parent, false))
            TYPE_DESCRIPTOR -> DescriptorViewHolder(DeviceDescriptorLayoutBinding.inflate(inflater, parent, false))
            TYPE_CHARACTERISTIC -> CharacteristicViewHolder(
                DeviceCharacteristicLayoutBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(content[position].data, defaultValue)
    }

    override fun getItemCount(): Int {
        return content.size
    }

    override fun getItemViewType(position: Int): Int {
        return content[position].type
    }

    fun clearFeatures() {
        content.clear()
        notifyDataSetChanged()
    }

    fun setDefaultValue(value: String?) {
        defaultValue = value
    }

    fun setFeatures(features: List<Feature>) {
        content.clear()
        features.forEach {
            when (it) {
                is Service -> content.add(Data(TYPE_SERVICE, it))
                is Descriptor -> content.add(Data(TYPE_DESCRIPTOR, it))
                is Characteristic -> content.add(Data(TYPE_CHARACTERISTIC, it))
            }
        }
        notifyDataSetChanged()
    }

    fun updateFeature(key: String, value: String) {
        val index = content.indexOfFirst { it.data.uuid == key }
        if (index == -1) return

        when (val data = content[index].data) {
            is Descriptor -> {
                data.value = value
                notifyItemChanged(index)
            }
            is Characteristic -> {
                data.value = value
                notifyItemChanged(index)
            }
        }
    }
}