package com.echsylon.demo.screen.device

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.echsylon.demo.CoreApplication
import com.echsylon.demo.connection.Connection
import com.echsylon.demo.connection.DeviceState
import com.echsylon.demo.connection.model.Feature
import com.echsylon.demo.discovery.model.Device

class DeviceViewModel(app: Application) : AndroidViewModel(app) {
    private val coreApplication = app.let { it as CoreApplication }
    private val bleConnection: Connection by lazy { coreApplication.connection }
    private val _selectedDevice = MutableLiveData<Device>()

    val device: LiveData<Device>
        get() = _selectedDevice

    val deviceState: LiveData<DeviceState>
        get() = bleConnection.deviceState

    val features: LiveData<List<Feature>>
        get() = bleConnection.features

    val value: LiveData<Pair<String, String>>
        get() = bleConnection.value


    fun selectDevice(device: Device) {
        _selectedDevice.value = device
    }

    fun connectSelectedDevice() {
        _selectedDevice.value
            ?.let { it.bluetoothDevice }
            ?.let { bleConnection.connectGracefully(it) }
    }

    fun disconnectSelectedDevice() {
        bleConnection.disconnectGracefully()
    }
}