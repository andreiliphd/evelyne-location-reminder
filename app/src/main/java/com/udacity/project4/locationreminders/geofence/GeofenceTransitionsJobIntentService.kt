package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //        TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        //TODO: get the request id of the current geofence
        if (geofenceEvent.hasError()) {
            Log.i("geofence", "Geofence error.")
            return
        }
        val triggerListOfGeofences: MutableList<Geofence> = mutableListOf()
        if (geofenceEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.i("geofence", "Geofence entered.")
            if (geofenceEvent.triggeringGeofences.isNotEmpty()) {
                triggerListOfGeofences.add(geofenceEvent.triggeringGeofences[0])
                sendNotification(triggerListOfGeofences)
            } else {
                Log.i("geofence", "No geofence trigger.")
                return
            }
        }
    }

    private fun sendNotification(triggerListOfGeofences: List<Geofence>) {
        val requestId = when {
            triggerListOfGeofences.isNotEmpty() -> {
                triggerListOfGeofences[0].requestId
            }
            else -> {
                Log.i("geofence", "No geofence found.")
                return
            }
        }
        if (requestId.isNullOrBlank())  return
        //Get the local repository instance
        val remindersLocalRepository: RemindersLocalRepository by inject()
        //        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}