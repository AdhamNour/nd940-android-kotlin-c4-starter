package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    //Use Koin to get the view model of the SaveReminder
    private val TAG = SelectLocationFragment::class.java.simpleName
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var selectedLocationMarker: Marker? = null
    private var selectedPoi: PointOfInterest? = null
    private var selectedLatLng: LatLng? = null
    private lateinit var locationManager: LocationManager
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 1000f
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveButton.setOnClickListener {
            /*if (isPermissionGranted()) {
                onLocationSelected()
            } else {
                requestLocationPermissions()
            }*/
            onLocationSelected()
        }
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return binding.root
    }

    private fun onLocationSelected() {
        if (selectedLatLng != null) {
            _viewModel.reminderSelectedLocationStr.postValue(
                String.format(
                    "Lat: %1$.5f, Long: %2$.5f",
                    selectedLatLng?.latitude, selectedLatLng?.longitude
                )
            )
            _viewModel.longitude.postValue(selectedLatLng?.longitude)
            _viewModel.latitude.postValue(selectedLatLng?.latitude)
        } else if (selectedPoi != null) {
            _viewModel.reminderSelectedLocationStr.postValue(selectedPoi?.name)
            _viewModel.longitude.postValue(selectedPoi?.latLng?.longitude)
            _viewModel.latitude.postValue(selectedPoi?.latLng?.latitude)
            _viewModel.selectedPOI.postValue(selectedPoi)
        } else {
            Toast.makeText(
                requireActivity(),
                getString(R.string.select_loc_message),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        navigateBackSaveReminder()
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (isPermissionGranted()) {
            enableLocationChange()
        } else {
            requestLocationPermissions()
        }
        setMapLongListener(map)
        setPoiClick(map)
        setMapStyle(map)
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationChange() {
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            MIN_TIME,
            MIN_DISTANCE,
            this
        );
        map.isMyLocationEnabled = true
    }

    private fun setMapLongListener(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            selectedLatLng = latLng
            selectedPoi = null
            removeMarker()
            val snippet = String.format(
                "Selected location Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude, latLng.longitude
            )
            selectedLocationMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            selectedLocationMarker?.showInfoWindow()
        }

    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            selectedLatLng = null
            selectedPoi = poi
            removeMarker()
            selectedLocationMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            selectedLocationMarker?.showInfoWindow()
        }
    }

    private fun removeMarker() {
        if (selectedLocationMarker != null)
            selectedLocationMarker?.remove()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions
                    .loadRawResourceStyle(requireActivity(), R.raw.map_style)
            )

            if (!success)
                Log.e(TAG, "Style parsing failed.")
        } catch (e: Exception) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val zoomLevel = 17f
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        map.animateCamera(cameraUpdate)
        locationManager.removeUpdates(this)
    }

    private fun navigateBackSaveReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == REQUEST_LOCATION_PERMISSION
        ) {
            map.isMyLocationEnabled = true
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE,
                this
            );
        }
        else{
            binding.root.let {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation_2, Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        // Displays App settings screen.
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return checkSelfPermission(
            requireActivity(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        if (!isPermissionGranted()) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
        checkDeviceLocationSettings()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        29, null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
"AN",                        "Error geting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                !locationManager.isLocationEnabled
            } else {
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
        ) {
            checkDeviceLocationSettings()
            return false
        }
        return true
    }

    override fun onMyLocationClick(p0: Location) {
        if (!isPermissionGranted()) {
            requestLocationPermissions()
        }
    }
}
