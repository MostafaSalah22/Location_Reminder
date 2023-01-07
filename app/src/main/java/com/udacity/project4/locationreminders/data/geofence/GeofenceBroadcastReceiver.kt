package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() , KoinComponent {

    override fun onReceive(p0: Context?, p1: Intent?) {

        if(p0 != null) {
            val geofencingEvent = p1?.let { GeofencingEvent.fromIntent(it) }
            val geofencingTransition = geofencingEvent?.geofenceTransition

            if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                sendNotification(p0 , geofencingEvent.triggeringGeofences!!)
            }
        }
    }

    private fun sendNotification(context:Context , triggeringGeofences: List<Geofence>) {

        triggeringGeofences.forEach {
            val requestId = it.requestId

            val remindersRepository: ReminderDataSource by inject()
            //Interaction to the repository has to be through a coroutine scope
            runBlocking {
                //get the reminder with the request id
                val result = remindersRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        context, ReminderDataItem(
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

}