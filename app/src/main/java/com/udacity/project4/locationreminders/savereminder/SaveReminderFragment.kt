package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var id: String
    private val GEOFENCE_RADIUS = 500
    private val GEOFENCE_DWELL_DELAY = 10 * 1000 //10 SEC
    private var title:String? = null
    private var description:String? = null
    private var location:String? = null
    private var latitude:Double? = null
    private var longitude:Double? = null
    private var reminder:ReminderDataItem? = null
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            title = _viewModel.reminderTitle.value
            description = _viewModel.reminderDescription.value
            location = _viewModel.reminderSelectedLocationStr.value
            latitude = _viewModel.latitude.value
            longitude = _viewModel.longitude.value
            id = UUID.randomUUID().toString()

            reminder = ReminderDataItem(title, description, location, latitude, longitude, id = id)

            checkPermissionAndCreateGeofence()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private var geofence:Geofence? = null
    @SuppressLint("MissingPermission")
    private fun createGeofence() {
        geofence = latitude?.let { latitude ->
            longitude?.let { longitude ->
                Geofence.Builder()
                    .setRequestId(id)
                    .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS.toFloat())
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
                    ).setLoiteringDelay(GEOFENCE_DWELL_DELAY)
                    .build()
            }
        }

        val geofenceRequest = geofence?.let { geofence ->
            GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
        }

        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            }
            else
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        geofenceRequest?.let { geofencingClient.addGeofences(it,pendingIntent).run {
            addOnSuccessListener {
                geofence?.let { it1 -> Log.e("Added", it1.requestId) }
            }
            addOnFailureListener {
                Toast.makeText(requireContext(),"Not Added",Toast.LENGTH_LONG).show()
            }
        } }

    }

    private val getBackgroundPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->

            if(result){
                if(isGpsEnabled()) {
                    if (_viewModel.validateEnteredData(reminder!!)) {
                        createGeofence()
                        _viewModel.validateAndSaveReminder(reminder!!)
                    }
                }
                else
                    turnOnGPS()
            }

        }

    private fun checkPermissionAndCreateGeofence() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
            getBackgroundPermission.launch(permission)
        }
        else {
            if (isGpsEnabled()) {
                if (_viewModel.validateEnteredData(reminder!!)) {
                    createGeofence()
                    _viewModel.validateAndSaveReminder(reminder!!)
                }
            }
            else
                turnOnGPS()
        }
    }

    private lateinit var locationManager: LocationManager

    private fun isGpsEnabled():Boolean
    {
        locationManager=context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun turnOnGPS() {
        val request = LocationRequest.create().apply {
            interval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task= client.checkLocationSettings(builder.build())
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    startIntentSenderForResult(it.resolution.intentSender, 100,null,0,0,0,null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("testt", "Error getting location settings resolution: " + sendEx.message)
                }
            }
        }.addOnSuccessListener {
            if (_viewModel.validateEnteredData(reminder!!)) {
                createGeofence()
                _viewModel.validateAndSaveReminder(reminder!!)
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            turnOnGPS()
        }
    }
}
