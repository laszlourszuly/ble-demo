package com.echsylon.demo.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.echsylon.demo.connection.DeviceState.CONNECTED
import com.echsylon.demo.connection.DeviceState.CONNECTING
import com.echsylon.demo.connection.DeviceState.DISCONNECTED
import com.echsylon.demo.connection.DeviceState.DISCONNECTING
import com.echsylon.demo.connection.DeviceState.ERROR
import com.echsylon.demo.connection.DeviceState.READY
import com.echsylon.demo.connection.model.Characteristic
import com.echsylon.demo.connection.model.Descriptor
import com.echsylon.demo.connection.model.Feature
import com.echsylon.demo.connection.model.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min


class BleConnection(context: Context) : BleManager(context), Connection {
    private companion object {
        private const val MTU_SIZE_MAXIMUM = 517
        private const val MTU_SIZE_MINIMUM = 23
        private const val MTU_SIZE_DEFAULT = 64

        private val knownNames: Map<Int, String> = mapOf(
            0x1800 to "Generic Access",
            0x1801 to "Generic Attribute",
            0x180A to "Device Information",
            0x2902 to "Client Characteristic Configuration",
            0x2A00 to "Device Name",
            0x2A01 to "Appearance",
            0x2A05 to "Service Changed",
            0x2A24 to "Model Number String",
            0x2A25 to "Serial Number String",
            0x2A26 to "Firmware Revision String",
            0x2A27 to "Hardware Revision String",
            0x2A28 to "Software Revision String",
            0x2A29 to "Manufacturer Name String"
        )
    }

    private var mtuSizeHint: Int = MTU_SIZE_DEFAULT
    private val _mtuSize = MutableLiveData<Int>()
    private val _value = MutableLiveData<Pair<String, String>>()
    private val _features = MutableLiveData<List<Feature>>()
    private val detectedFeatures = arrayListOf<Feature>()
    private val readableDescriptors = arrayListOf<BluetoothGattDescriptor>()
    private val readableCharacteristics = arrayListOf<BluetoothGattCharacteristic>()
    private val connectionStateListener = ConnectionStateListener()


    override val mtuSize: LiveData<Int>
        get() = _mtuSize

    override val deviceState: LiveData<DeviceState>
        get() = connectionStateListener.liveData

    override val features: LiveData<List<Feature>>
        get() = _features

    override val value: LiveData<Pair<String, String>>
        get() = _value


    init {
        setConnectionObserver(connectionStateListener)
        hintMtuSize(MTU_SIZE_DEFAULT)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return BleCallback()
    }

    override fun log(priority: Int, message: String) {
        Timber.log(priority, message)
    }

    override fun log(priority: Int, messageRes: Int, vararg params: Any?) {
        Timber.log(priority, context.getString(messageRes, params))
    }

    override fun hintMtuSize(hint: Int) {
        mtuSizeHint = min(MTU_SIZE_MAXIMUM, max(MTU_SIZE_MINIMUM, hint))
        negotiateSuggestedMtuSize()
        _mtuSize.value = mtuSizeHint
    }

    override fun connectGracefully(bluetoothDevice: BluetoothDevice): Boolean {
        val connectedDevice = getBluetoothDevice()
        if (connectedDevice == null || connectedDevice.address != bluetoothDevice.address) {
            disconnect().enqueue()
            connect(bluetoothDevice).useAutoConnect(false).enqueue()
            return true
        }
        return false
    }

    override fun disconnectGracefully() {
        disconnect().enqueue()
    }

    private fun negotiateSuggestedMtuSize() {
        if (isConnected) {
            requestMtu(mtuSizeHint)
                .with { _, mtu -> _mtuSize.value = mtu }
                .fail { _, status -> Timber.d("Failed to set MTU=%i: 0x%02x", mtuSizeHint, 0xFF and status) }
                .enqueue()
        }
    }

    private inner class BleCallback : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            detectedFeatures.clear()
            for (service in gatt.services) {
                var key: Int = ((service.uuid.mostSignificantBits shr 32) and 0xFFFF).toInt()
                var name = Companion.knownNames[key]
                detectedFeatures.add(Service(service.uuid.toString(), name))
                for (characteristic in service.characteristics) {
                    key = ((characteristic.uuid.mostSignificantBits shr 32) and 0xFFFF).toInt()
                    name = Companion.knownNames[key]
                    detectedFeatures.add(Characteristic(characteristic.uuid.toString(), name))
                    if (characteristic.properties and PROPERTY_READ != 0) {
                        readableCharacteristics.add(characteristic)
                    }
                    for (descriptor in characteristic.descriptors) {
                        key = ((descriptor.uuid.mostSignificantBits shr 32) and 0xFFFF).toInt()
                        name = Companion.knownNames[key]
                        detectedFeatures.add(Descriptor(descriptor.uuid.toString(), name))
                        readableDescriptors.add(descriptor)
                    }
                }
            }
            return true
        }

        override fun initialize() {
            negotiateSuggestedMtuSize()
            _features.postValue(detectedFeatures)
            readableCharacteristics
                .forEach { characteristic ->
                    readCharacteristic(characteristic)
                        .with(ValueListener(characteristic.uuid.toString()))
                        .enqueue()
                }
            readableDescriptors
                .forEach { descriptor ->
                    readDescriptor(descriptor)
                        .with(ValueListener(descriptor.uuid.toString()))
                        .enqueue()
                }
        }

        override fun onDeviceDisconnected() {
            readableCharacteristics.clear()
            readableDescriptors.clear()
        }
    }

    private inner class ValueListener(private val key: String) : DataReceivedCallback {
        override fun onDataReceived(device: BluetoothDevice, data: Data) {
            CoroutineScope(Dispatchers.Main)
                .launch {
                    var value = data.getStringValue(0)?.trim()?.trim(0.toChar()) ?: ""
                    if (value.isBlank()) {
                        value = data.value
                            ?.joinToString("") { String.format("%02x", it) }
                            ?.let { "0x$it" }
                            ?: ""
                    }
                    _value.value = key to value
                }
        }
    }

    private class ConnectionStateListener : ConnectionObserver {
        val liveData = MutableLiveData<DeviceState>()

        override fun onDeviceConnecting(device: BluetoothDevice) {
            liveData.value = CONNECTING
        }

        override fun onDeviceConnected(device: BluetoothDevice) {
            liveData.value = CONNECTED
        }

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
            Timber.d("Failed connecting to device '${device.name ?: device.address}': $reason")
            liveData.value = ERROR
        }

        override fun onDeviceReady(device: BluetoothDevice) {
            liveData.value = READY
        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {
            liveData.value = DISCONNECTING
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
            liveData.value = DISCONNECTED
        }
    }
}