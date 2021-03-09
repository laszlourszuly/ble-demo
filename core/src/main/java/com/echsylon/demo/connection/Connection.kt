package com.echsylon.demo.connection

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import com.echsylon.demo.connection.model.Feature

interface Connection {
    val mtuSize: LiveData<Int>

    val deviceState: LiveData<DeviceState>

    val features: LiveData<List<Feature>>

    val value: LiveData<Pair<String, String>>

    fun hintMtuSize(hint: Int)

    fun connectGracefully(bluetoothDevice: BluetoothDevice): Boolean

    fun disconnectGracefully()
}