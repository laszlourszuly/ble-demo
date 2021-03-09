package com.echsylon.demo.screen.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.echsylon.demo.R
import com.echsylon.demo.databinding.ScanFragmentBinding
import com.echsylon.demo.discovery.model.Device
import com.echsylon.demo.screen.device.DeviceViewModel

class ScanFragment : Fragment() {
    private var _binding: ScanFragmentBinding? = null
    private var wasScanning: Boolean = false
    private val scanViewModel: ScanViewModel by activityViewModels()
    private val deviceViewModel: DeviceViewModel by activityViewModels()
    private val navigation: NavController by lazy { findNavController() }
    private val adapter = DeviceAdapter()

    private val binding
        get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ScanFragmentBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .let { it.root }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setDefaultDeviceName(getString(R.string.not_available))
        adapter.setItemClickAction { onDeviceSelected(it) }
        binding.devices.adapter = adapter
        binding.scanView.setColorSchemeResources(R.color.color_primary)
        binding.scanView.setOnRefreshListener { onScanRequest() }
        binding.scanButton.setOnClickListener { onScanRequest() }
        scanViewModel.scannedDeviceStream.observe(viewLifecycleOwner) { onDeviceFound(it) }
        scanViewModel.scanState.observe(viewLifecycleOwner) { onScanStateChange(it) }
    }

    override fun onStop() {
        super.onStop()
        scanViewModel.stopScanningForDevices()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onScanRequest() {
        val didRequestedScan = scanViewModel.scanForDevices()
        if (didRequestedScan) {
            adapter.clearScannedDevices()
        }
    }

    private fun onScanStateChange(isScanning: Boolean) {
        binding.scanView.isRefreshing = isScanning
        val offset = (binding.root.bottom - binding.scanButton.top).toFloat()
        when {
            !wasScanning && isScanning -> binding.scanButton
                .animate()
                .setInterpolator(AnticipateOvershootInterpolator(4f))
                .setDuration(250L)
                .translationY(offset)
                .start()
            wasScanning && !isScanning -> binding.scanButton
                .animate()
                .setInterpolator(AnticipateOvershootInterpolator(4f))
                .setDuration(250L)
                .translationY(0f)
                .start()
        }
        wasScanning = isScanning
    }

    private fun onDeviceFound(device: Device) {
        if (scanViewModel.scanState.value == true) {
            adapter.addScannedDevices(device)
        }
    }

    private fun onDeviceSelected(device: Device) {
        deviceViewModel.selectDevice(device)
        navigation.navigate(R.id.to_device)
    }
}