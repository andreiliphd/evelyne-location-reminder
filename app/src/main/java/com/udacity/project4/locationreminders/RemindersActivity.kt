package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        if (locationPermissionsApproved(this)) {
            locationPermissionGranted = true
            Log.i("permissions", "Location permission already granted.")
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (locationPermissionsApproved(this))
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            RUNNING_Q_OR_LATER -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FB_RESULT_CODE
            }
            else -> REQUEST_FG_RESULT_CODE
        }
        ActivityCompat.requestPermissions(
            this@RemindersActivity,
            permissionsArray,
            resultCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        if (
            grantedResults.isEmpty() ||
            grantedResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FB_RESULT_CODE &&
                    grantedResults[BACKGROUND_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            Toast.makeText(this, "Permisssion not granted.", Toast.LENGTH_SHORT)
        } else {
            locationPermissionGranted = true
            Log.i("persmissions", "Location permission granted.")
        }
    }

    companion object {
        private const val REQUEST_FB_RESULT_CODE = 33
        private const val REQUEST_FG_RESULT_CODE = 34
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_PERMISSION_INDEX = 1
        var locationPermissionGranted: Boolean = false
        val RUNNING_Q_OR_LATER = android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.Q

        fun locationPermissionsApproved(context: Context): Boolean {
            val foregroundLocationPermissionApproved = (
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ))
            val backgroundLocationPermissionApproved =
                if (RUNNING_Q_OR_LATER) {
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                } else {
                    true
                }
            return foregroundLocationPermissionApproved && backgroundLocationPermissionApproved
        }
    }
}
