package com.echsylon.demo.screen.main

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Dialog
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.echsylon.demo.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RationaleDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.why_permissions)
            .setMessage(R.string.needs_permissions_in_order_to_function)
            .setPositiveButton(R.string.ok) { _, _ -> requestPermissions() }
            .create()
    }

    private fun requestPermissions() {
        val permissionRequestCode = resources.getInteger(R.integer.request_code_location_permission)
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(ACCESS_FINE_LOCATION), permissionRequestCode)
    }
}
