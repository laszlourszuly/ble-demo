package com.echsylon.demo.discovery

import androidx.lifecycle.LiveData
import com.echsylon.demo.discovery.model.Device

interface Scanner {
    val scanState: LiveData<Boolean>

    val scanResult: LiveData<Device>

    fun startScanning(): Boolean

    fun stopScanning()
}