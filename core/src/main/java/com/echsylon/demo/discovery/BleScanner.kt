package com.echsylon.demo.discovery

import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.echsylon.demo.discovery.model.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import no.nordicsemi.android.support.v18.scanner.ScanSettings.SCAN_MODE_LOW_LATENCY
import java.util.concurrent.atomic.AtomicBoolean

class BleScanner : Scanner {
    companion object {
        private const val SCAN_TIMEOUT = 10_000L
        private val serviceUuid = ParcelUuid.fromString("00000000-0000-0000-0000-000000000000")
        private val serviceMask = ParcelUuid.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")
        private val filters = listOf(ScanFilter.Builder().setServiceUuid(serviceUuid, serviceMask).build())
        private val settings = ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_LATENCY).build()
    }

    private var scanner: BluetoothLeScannerCompat? = null
    private var watchDogScope: CoroutineScope? = null
    private val scanning = AtomicBoolean(false)
    private val cache = mutableListOf<String>()
    private val _scanState = MutableLiveData<Boolean>()
    private val _scanResult = MutableLiveData<Device>()
    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
            super.onScanResult(callbackType, scanResult)
            CoroutineScope(Dispatchers.Main)
                .launch {
                    scanResult
                        .takeUnless { result -> cache.contains(result.device.address) }
                        ?.also { result -> cache.add(result.device.address) }
                        ?.also { result -> _scanResult.value = Device(result.device, result.scanRecord) }
                }
        }
    }

    override val scanState: LiveData<Boolean>
        get() = _scanState

    override val scanResult: LiveData<Device>
        get() = _scanResult

    override fun startScanning(): Boolean {
        return if (!scanning.getAndSet(true)) {
            CoroutineScope(Dispatchers.Main)
                .launch {
                    scanner = BluetoothLeScannerCompat.getScanner()
                        .also { cache.clear() }
                        .also { it.startScan(callback) }
                        //.also { it.startScan(filters, settings, callback) }
                        .also { _scanState.value = true }
                }
            CoroutineScope(Dispatchers.IO + Job())
                .also { watchDogScope = it }
                .launch {
                    delay(SCAN_TIMEOUT)
                    stopScanning()
                }
            true
        } else {
            false
        }
    }

    override fun stopScanning() {
        CoroutineScope(Dispatchers.Main)
            .launch {
                watchDogScope?.cancel("Stop scanning")
                watchDogScope = null
                scanning.set(false)
                scanner?.stopScan(callback)
                scanner = null
                _scanState.value = false
            }
    }
}