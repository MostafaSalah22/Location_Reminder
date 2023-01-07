package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment() {

    private lateinit var map: GoogleMap
    private var selectedLocationLatLng:LatLng? = null
    private var reminderSelectedLocationStr:String? = null
    private lateinit var fusedLocationClient:FusedLocationProviderClient

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        checkPermissionsAndZoomToUserLocation()
        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        binding.saveLocationButton.setOnClickListener {

                onLocationSelected()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun onLocationSelected() {

        _viewModel.reminderSelectedLocationStr.value = reminderSelectedLocationStr
        _viewModel.latitude.value = selectedLocationLatLng?.latitude
        _viewModel.longitude.value = selectedLocationLatLng?.longitude
        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private val getLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ result ->

            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true){
                map.setMyLocationEnabled(true)
                fusedLocationClient.lastLocation.addOnSuccessListener { currentLocation ->
                    if(currentLocation != null){
                        with(map){
                            val latLng = LatLng(currentLocation.latitude , currentLocation.longitude)
                            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))
                        }
                    }
                }
            }
            else
                Toast.makeText(requireContext(),R.string.permission_denied_explanation,Toast.LENGTH_LONG).show()

        }

    private fun checkPermissionsAndZoomToUserLocation() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        getLocationPermissions.launch(permissions)
    }


    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("map_activity", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("map_activity", "Can't find style. Error: ", e)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            )
            map.addCircle(
                CircleOptions()
                    .center(latLng)
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(70, 150, 150, 150))
                    .radius(500.0)
            )
            selectedLocationLatLng = latLng
            reminderSelectedLocationStr = "Selected Location"
            binding.saveLocationButton.visibility = View.VISIBLE
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )?.showInfoWindow()

            map.addCircle(
                CircleOptions()
                    .center(poi.latLng)
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(70, 150, 150, 150))
                    .radius(500.0)
            )

            selectedLocationLatLng = poi.latLng
            reminderSelectedLocationStr = poi.name
            binding.saveLocationButton.visibility = View.VISIBLE

        }
    }


}
