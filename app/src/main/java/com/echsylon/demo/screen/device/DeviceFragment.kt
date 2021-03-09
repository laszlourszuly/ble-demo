package com.echsylon.demo.screen.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.echsylon.demo.R
import com.echsylon.demo.databinding.DeviceFragmentBinding
import com.echsylon.demo.connection.model.Feature


class DeviceFragment : Fragment() {
    private var _binding: DeviceFragmentBinding? = null
    private val viewModel: DeviceViewModel by activityViewModels()
    private val adapter = FeatureAdapter()

    private val binding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return DeviceFragmentBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .let { it.root }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.clearFeatures()
        adapter.setDefaultValue(getString(R.string.not_available))
        binding.services.adapter = adapter
        viewModel.value.observe(viewLifecycleOwner) { onValueChange(it.first, it.second) }
        viewModel.features.observe(viewLifecycleOwner) { onFeaturesChange(it) }
        viewModel.connectSelectedDevice()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.disconnectSelectedDevice()
        _binding = null
    }

    private fun onFeaturesChange(features: List<Feature>) {
        adapter.setFeatures(features)
    }

    private fun onValueChange(key: String, value: String) {
        adapter.updateFeature(key, value)
    }
}