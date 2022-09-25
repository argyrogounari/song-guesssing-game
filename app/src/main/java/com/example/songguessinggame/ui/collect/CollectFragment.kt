package com.example.songguessinggame.ui.collect

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.example.songguessinggame.R
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.songguessinggame.DatabaseHandler
import com.example.songguessinggame.GoogleMapHelper
import com.example.songguessinggame.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_collect_enable_location.view.*

const val PERMISSION_ID = 42
const val DEFAULT_LATITUDE = 51.6194
const val DEFAULT_LONGITUDE = -3.8793
const val NEW_LOCATION_DATA_INTERVALS = 2000.toLong()
const val NEW_LOCATION_DATA_FASTEST_INTERVALS = 1000.toLong()
const val GRAND_RESULTS_ARRAY_INDEX = 0

/**
 * Fragment that manages the google map and the collection of lyrics.
 *
 * @constructor Creates a collection fragment.
 */
class CollectFragment : Fragment() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var enableLocationMessageFragment: View
    private var currentLocation: Location = Location(LocationManager.GPS_PROVIDER).apply {
        DEFAULT_LATITUDE
        DEFAULT_LONGITUDE
    }
    private lateinit var dbHelper : DatabaseHandler
    private lateinit var gmHelper : GoogleMapHelper

    /**
     * When view is created it initializes class variables
     * and checks for location permissions.
     */
    override fun onCreateView (
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_collect, container, false)

        (activity as MainActivity?)?.supportActionBar?.hide()

        dbHelper = DatabaseHandler(context!!)
        gmHelper = GoogleMapHelper(context!!)

        checkPermissionsForLocation(root)

        return root
    }

    /**
     * Ensures that when fragment comes back from the
     * back stack the enable location message is disabled.
     */
    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            enableLocationMessageFragment.isVisible = false
        }
    }

    /**
     * Ensures that when fragment view is destroyed
     * the action bar is visible again.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity?)?.supportActionBar?.show()
    }

    /**
     * Checks the location permissions, displaying a fragment
     * with a message if location use is not permitted.
     */
    private fun checkPermissionsForLocation(root: View) {
        enableLocationMessageFragment = root.findViewById(R.id.enable_collection_screen)
        enableLocationMessageFragment.isVisible = false
        enableLocationMessageFragment.enable_location_button.setOnClickListener {
            requestPermissions()
        }

        val sharedPref = activity!!.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val firstTimeAppUsed = sharedPref!!.getBoolean(
            getString(R.string.first_time_app_used), true)

        if (checkPermissions()) {
            initializeMap()
        } else {
            if (firstTimeAppUsed) {
                requestPermissions()
            } else {
                enableLocationMessageFragment.isVisible = true
            }
        }

        with (sharedPref.edit()) {
            putBoolean(getString(R.string.first_time_app_used), false)
            commit()
        }
    }

    /**
     * Initializes the map using the map helper.
     */
    private fun initializeMap() {
        gmHelper.initializeMap(childFragmentManager)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        getLastLocation()
        requestNewLocationData()
    }

    /**
     * Gets users last location.
     */
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(activity!!) { task ->
                    if (task.result == null) {
                        requestNewLocationData()
                    } else {
                        currentLocation = task.result!!
                        gmHelper.moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude))
                    }
                }
            }
        } else {
            requestPermissions()
        }
    }

    /**
     * Request new location from the user.
     */
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = NEW_LOCATION_DATA_INTERVALS
        mLocationRequest.fastestInterval = NEW_LOCATION_DATA_FASTEST_INTERVALS

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback, Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult?) {
            currentLocation = location?.lastLocation ?: currentLocation
        }
    }

    /**
     * Checks that the required location permissions are in place.
     * @return permissions status.
     */
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    /**
     * Returns the location permissions status.
     * @return permissions status.
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = activity!!.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * Request the user to give location permissions.
     */
    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    /**
     * Called when there is an answer from the user about location permissions.
     * @param requestCode the request code.
     * @param permissions the permissions array.
     * @param grantResults the permissions status result.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() &&
                        grantResults[GRAND_RESULTS_ARRAY_INDEX] == PackageManager.PERMISSION_GRANTED)) {
                initializeMap()
            } else {
                enableLocationMessageFragment.isVisible = true
            }
        }
    }

    /**
     * Closes database when fragment is destroyed.
     */
    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
