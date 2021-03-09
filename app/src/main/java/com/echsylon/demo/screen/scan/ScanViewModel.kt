package com.echsylon.demo.screen.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.echsylon.demo.CoreApplication
import com.echsylon.demo.discovery.model.Device
import com.echsylon.demo.discovery.Scanner

class ScanViewModel(app: Application) : AndroidViewModel(app) {
    private val coreApplication = app.let { it as CoreApplication }
    private val bleScanner: Scanner by lazy { coreApplication.scanner }

    val scannedDeviceStream: LiveData<Device>
        get() = bleScanner.scanResult

    val scanState: LiveData<Boolean>
        get() = bleScanner.scanState

    fun scanForDevices(): Boolean {
        return bleScanner.startScanning()
    }

    fun stopScanningForDevices() {
        bleScanner.stopScanning()
    }
}