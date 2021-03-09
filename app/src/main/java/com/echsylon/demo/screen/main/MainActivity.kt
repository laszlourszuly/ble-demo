package com.echsylon.demo.screen.main

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.echsylon.demo.R
import com.echsylon.demo.databinding.MainActivityBinding
import com.echsylon.demo.connection.DeviceState
import com.echsylon.demo.connection.DeviceState.CONNECTING
import com.echsylon.demo.connection.DeviceState.ERROR
import com.echsylon.demo.connection.DeviceState.READY
import com.echsylon.demo.screen.device.DeviceViewModel

class MainActivity : AppCompatActivity() {
    private var _binding: MainActivityBinding? = null
    private var hasPermissions: Boolean = false
    private var hasBluetooth: Boolean = false

    private val navigation by lazy { findNavController(R.id.navigation_host) }
    private val deviceViewModel: DeviceViewModel by viewModels()
    private val permissionRequestCode by lazy { resources.getInteger(R.integer.request_code_location_permission) }
    private val bluetoothListener by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.getIntExtra(EXTRA_STATE, -1)) {
                    STATE_OFF -> onBluetoothDisabled()
                    STATE_ON -> onBluetoothEnabled()
                }
            }
        }
    }

    private val binding
        get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MainActivityBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        NavigationUI.setupActionBarWithNavController(this, navigation)

        binding.permissions.review.setOnClickListener { requestPermissions() }
        binding.bluetooth.enable.setOnClickListener { enableBluetooth() }

        deviceViewModel.deviceState.observe(this) { onDeviceStateChange(it) }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(bluetoothListener, IntentFilter(ACTION_STATE_CHANGED))
        hasBluetooth = BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false
        when (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)) {
            PERMISSION_GRANTED -> {
                hasPermissions = true
                maybeNavigateToHome()
            }
            PERMISSION_DENIED -> {
                hasPermissions = false
                requestPermissions(true)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigation.navigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        when {
            binding.permissions.root.visibility == VISIBLE -> finish()
            binding.bluetooth.root.visibility == VISIBLE -> finish()
            binding.error.root.visibility == VISIBLE -> finish()
            else -> showNavigation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permission = permissions.indexOf(ACCESS_FINE_LOCATION)
        when (grantResults[permission]) {
            PERMISSION_GRANTED -> onPermissionsGranted()
            PERMISSION_DENIED -> onPermissionsDenied()
        }
    }

    private fun onPermissionsGranted() {
        hasPermissions = true
        maybeNavigateToHome()
    }

    private fun onPermissionsDenied() {
        hasPermissions = false
        maybeNavigateToHome()
    }

    private fun onBluetoothEnabled() {
        hasBluetooth = true
        maybeNavigateToHome()
    }

    private fun onBluetoothDisabled() {
        hasBluetooth = false
        maybeNavigateToHome()
    }

    private fun onDeviceStateChange(state: DeviceState) {
        when (state) {
            CONNECTING -> showBluetoothConnecting()
            READY -> showNavigation()
            ERROR -> showError()
            else -> Unit
        }
    }

    private fun maybeNavigateToHome() {
        when {
            !hasPermissions -> showPermissionsMissing()
            !hasBluetooth -> showBluetoothDisabled()
            else -> showNavigation()
        }
    }

    private fun requestPermissions(checkForRationale: Boolean = false) {
        when (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION) && checkForRationale) {
            true -> RationaleDialog().show(supportFragmentManager, "RationaleDialog")
            else -> ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), permissionRequestCode)
        }
    }

    private fun enableBluetooth() {
        val isEnabling = BluetoothAdapter.getDefaultAdapter()?.enable() ?: false
        if (isEnabling) showBluetoothEnabling()
    }

    private fun showBluetoothDisabled() {
        binding.permissions.root.visibility = GONE
        binding.bluetooth.root.visibility = VISIBLE
        binding.error.root.visibility = GONE
        binding.progress.root.visibility = GONE
    }

    private fun showBluetoothEnabling() {
        binding.permissions.root.visibility = GONE
        binding.bluetooth.root.visibility = GONE
        binding.error.root.visibility = GONE
        binding.progress.root.visibility = VISIBLE
        binding.progress.indicator.visibility = VISIBLE
        binding.progress.message.setText(R.string.enabling_bluetooth)
    }

    private fun showBluetoothConnecting() {
        binding.permissions.root.visibility = GONE
        binding.bluetooth.root.visibility = GONE
        binding.error.root.visibility = GONE
        binding.progress.root.visibility = VISIBLE
        binding.progress.indicator.visibility = VISIBLE
        binding.progress.message.setText(R.string.connecting_to_device)
    }

    private fun showPermissionsMissing() {
        binding.permissions.root.visibility = VISIBLE
        binding.bluetooth.root.visibility = GONE
        binding.error.root.visibility = GONE
        binding.progress.root.visibility = GONE
    }

    private fun showError(description: String? = null) {
        binding.permissions.root.visibility = GONE
        binding.bluetooth.root.visibility = GONE
        binding.error.root.visibility = VISIBLE
        binding.error.message.text = description
        binding.progress.root.visibility = GONE
    }

    private fun showNavigation() {
        binding.permissions.root.visibility = GONE
        binding.bluetooth.root.visibility = GONE
        binding.error.root.visibility = GONE
        binding.progress.root.visibility = GONE
    }

}