package com.echsylon.demo.discovery.model

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.support.v18.scanner.ScanRecord

data class Device(
    private val device: BluetoothDevice,
    private val record: ScanRecord?
) {
    val address: String
        get() = device.address

    val alias: String?
        get() = device.alias

    val name: String?
        get() = device.name

    val manufacturer: Int
        get() = record?.manufacturerSpecificData
            ?.takeIf { it.size() > 0 }
            ?.let { it.keyAt(0) }
            ?: -1

    // 0x01: Limited discovery mode
    // 0x02: General discovery mode
    val connectable: Boolean
        get() = record?.advertiseFlags
            ?.takeIf { it >= 0 }
            ?.let { it and (0x01 or 0x02) != 0 }
            ?: false

    val bluetoothDevice: BluetoothDevice
        get() = device
}
