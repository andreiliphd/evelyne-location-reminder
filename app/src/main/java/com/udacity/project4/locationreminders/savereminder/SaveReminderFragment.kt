package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_save_reminder.*
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val args: SaveReminderFragmentArgs by navArgs()
    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
//        intent.action = "ACTION_GEOFENCE_EVENT"
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        if (args.coordinates != null) {
            _viewModel.latitude.value = args.coordinates.latitude
            _viewModel.longitude.value = args.coordinates.longitude
            _viewModel.reminderSelectedLocationStr.value = args.input
            Log.i("save", args.coordinates.toString())
        }

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            val geoReminder = ReminderDataItem(title, description, location, latitude, longitude)
            _viewModel.validateAndSaveReminder(geoReminder)
            addGeofence(geoReminder)
        }
    }
    @SuppressLint("MissingPermission")
    private fun addGeofence(reminder: ReminderDataItem) {
        Log.i("geofence", "Geofence lat and lon " + reminder.latitude+ " " +  reminder.longitude)
        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                reminder.latitude!!,
                reminder.longitude!!,
                400f
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
        checkLocationPermissions()
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        if (locationPermissionsApproved(requireContext())) {
                Log.i("permissions", "Permission granted. Using Geofence.")
            geofencingClient?.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    Log.i("geofence", "Geofence intent sent.")
                }
                addOnFailureListener {
                    // Failed to add geofences
                    Log.i("geofence", "Geofence intent sending failed " + it + ".")
                }
            }
            }
    }

    fun locationPermissionsApproved(context: Context): Boolean {
        val foregroundLocationPermissionApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundLocationPermissionApproved =
            if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.Q) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationPermissionApproved && backgroundLocationPermissionApproved
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
    private fun checkLocationPermissions() {
        if (locationPermissionsApproved(requireContext())) {
            locationPermissionGranted = true
            Log.i("permissions", "Location permission already granted.")
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (locationPermissionsApproved(requireContext()))
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
            requireActivity(),
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
            Toast.makeText(requireContext(), "Permisssion not granted.", Toast.LENGTH_SHORT)
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
