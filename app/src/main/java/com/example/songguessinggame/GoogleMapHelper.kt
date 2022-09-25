package com.example.songguessinggame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil.computeDistanceBetween
import com.google.android.material.dialog.MaterialAlertDialogBuilder

const val CUSTOMIZED_MARKER_ICON_SIZE = 200
const val DISTANCE_FOR_MARKER_COLLECTION = 50
const val CAMERA_BEARING = 270f
const val CAMERA_ZOOM = 19f
const val CAMERA_TILT = 65f

/**
 * A Google maps API helper.
 *
 * This class handles all requests for the API.
 *
 * @param context the context for this class.
 */
class GoogleMapHelper(private val context: Context) : GoogleMap.OnMarkerClickListener,
    OnMapReadyCallback {
    private val markerList = mutableListOf<MarkerOptions>()
    private lateinit var mMap: GoogleMap
    private lateinit var dbHelper : DatabaseHandler

    /**
     * Initializes the map.
     * @param childFragmentManager the fragment manager wheres this map is used.
     */
    fun initializeMap(childFragmentManager: FragmentManager) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    /**
     * Called when the map is ready to be refactored.
     * @param googleMap the google map.
     */
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setOnMarkerClickListener(this)
        mMap.isMyLocationEnabled = true
        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isCompassEnabled = false

        dbHelper = DatabaseHandler(context)
        val markerList = dbHelper.getMarkersTable()
        for (marker in markerList) {
            val newMarker = mMap.addMarker(getCustomizedMarker(marker.marker))
            newMarker.tag = marker.id
        }
    }

    /**
     * Calls for marker creation for both current and classic categories.
     * @param currentMarkersSize the markers on the map of the current category.
     * @param classicMarkersSize the markers on the map of the classic category.
     * @return the list of all the markers.
     */
    fun populateMarkersLatLong(currentMarkersSize: Int, classicMarkersSize: Int)
            : MutableList<MarkerOptions> {
        computeMarkers(SongCategory.CURRENT, currentMarkersSize)
        computeMarkers(SongCategory.CLASSIC, classicMarkersSize)
        return markerList
    }

    /**
     * Adds markers to the map of the specified category.
     * @param markerCategory the markers category.
     * @param markersNum how many markers should be created.
     */
    private fun computeMarkers(markerCategory: SongCategory, markersNum: Int) {
        var myMarkersNum = markersNum
        var i = 0
        while (i < myMarkersNum) {
            val ptLat = Math.random() * (bayCampusNE.latitude - bayCampusSW.latitude) +
                    bayCampusSW.latitude
            val ptLng = Math.random() * (bayCampusNE.longitude - bayCampusSW.longitude) +
                    bayCampusSW.longitude
            val location = LatLng(ptLat, ptLng)
            if (PolyUtil.containsLocation(location, bayCampusPoints, true)) {
                if (markerCategory == SongCategory.CURRENT) {
                    markerList.add(MarkerOptions()
                        .position(location)
                        .title(markerCategory.categoryString))
                } else if (markerCategory == SongCategory.CLASSIC) {
                    markerList.add(MarkerOptions()
                        .position(location)
                        .title(markerCategory.categoryString))
                }
            } else {
                myMarkersNum++
            }
            i++
        }
    }

    /**
     * Customizes the marker with the relevant icon and size.
     * @param marker marker to be customized.
     */
    private fun getCustomizedMarker(marker: MarkerOptions) : MarkerOptions {
        var markerIcon = 0
        if (marker.title == SongCategory.CURRENT.categoryString) {
            markerIcon = R.drawable.ic_current_marker
        } else if (marker.title == SongCategory.CLASSIC.categoryString) {
            markerIcon = R.drawable.ic_classic_marker
        }

        val height = CUSTOMIZED_MARKER_ICON_SIZE
        val width = CUSTOMIZED_MARKER_ICON_SIZE
        val bitmapDrawMarker =
            context.resources.getDrawable(markerIcon) as BitmapDrawable
        val markerBitmap = Bitmap.createScaledBitmap(
            bitmapDrawMarker.bitmap,
            width,
            height,
            false)

        return marker.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
    }

    /**
     * Adds listener for when marker is clicked or tapped.
     *
     * Checks that the user is within 40 meters and pops up a dialog prompting them to collect
     * the lyric. Relevant calls to the database are in place depending on the users choice.
     *
     * @param marker the marker that was clicked.
     * @return if the listener has consumed the event.
     */
    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            val distance = getDistance(
                LatLng(marker.position.latitude, marker.position.longitude)
            )
            if (distance < DISTANCE_FOR_MARKER_COLLECTION) {
                setUpMarkerCollection(marker)
            }
        }
        return false
    }

    private fun setUpMarkerCollection(marker: Marker) {
        val builder =MaterialAlertDialogBuilder(
            context,
            R.style.AlertDialogTheme)
            .setTitle(marker.title + " " + context.resources.getString(R.string.collect_dialog_title))
            .setMessage(context.resources.getString(R.string.collect_dialog_message))

            .setPositiveButton(context.resources.getString(R.string.collect_dialog_collect_button))
            { _, _ ->
                val song: Song?
                if (marker.title == SongCategory.CURRENT.categoryString) {
                    song = dbHelper.getUncollectedSongsFromPlaylist(SongCategory.CURRENT)
                        .firstOrNull()
                    if (song != null) {
                        dbHelper.updateSongCollectionStatus(song.id, SongCategory.CURRENT)
                    }
                } else {
                    song = dbHelper.getUncollectedSongsFromPlaylist(SongCategory.CLASSIC)
                        .firstOrNull()
                    if (song != null) {
                        dbHelper.updateSongCollectionStatus(song.id, SongCategory.CLASSIC)
                    }
                }
                dbHelper.deleteMarker(marker.tag.toString())
                marker.remove()
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.collect_toast_message_collected),
                    Toast.LENGTH_LONG)
                    .show()
            }

            .setNegativeButton(context.resources.getString(R.string.collect_dialog_cancel_button))
            { _, _ ->
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.collect_toast_message_cancelled),
                    Toast.LENGTH_SHORT)
                    .show()
            }

        val dialog: AlertDialog = builder.create()
        dialog.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        dialog.show()
    }

    /**
     * Returns the distance the user from the given destination.
     * @param destination the location to compare against.
     * @return the distance in meters.
     */
    private fun getDistance(destination: LatLng) : Double {
        return computeDistanceBetween(
            LatLng(mMap.myLocation.latitude, mMap.myLocation.longitude),
            destination
        )
    }

    /**
     * Move maps camera to destination.
     * @param destination the location to move camera to.
     */
    fun moveCamera(destination: LatLng) {
        val cameraPosition = CameraPosition.Builder()
            .target(destination)
            .bearing(CAMERA_BEARING)
            .zoom(CAMERA_ZOOM)
            .tilt(CAMERA_TILT)
            .build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    companion object {
        val bayCampusPoints =  mutableListOf<LatLng>()
        private var bayCampusSW : LatLng
        private var bayCampusNE : LatLng

        init {
            bayCampusPoints.add(LatLng(51.620451, -3.875558))
            bayCampusPoints.add(LatLng(51.618414, -3.874742))
            bayCampusPoints.add(LatLng(51.617506, -3.877067))
            bayCampusPoints.add(LatLng(51.617257, -3.879934))
            bayCampusPoints.add(LatLng(51.617657, -3.883254))
            bayCampusPoints.add(LatLng(51.617823, -3.885250))
            bayCampusPoints.add(LatLng(51.619322, -3.885270))
            bayCampusPoints.add(LatLng(51.620451, -3.875558))

            val bayCampusBounds = getPolygonBounds(bayCampusPoints)
            bayCampusSW = bayCampusBounds.southwest
            bayCampusNE = bayCampusBounds.northeast
        }

        /**
         * Returns the bounds of the given polygon.
         * @param polygon the given polygon.
         */
        private fun getPolygonBounds(polygon: List<LatLng>): LatLngBounds {
            val builder = LatLngBounds.Builder()
            for (i in polygon.indices) {
                builder.include(polygon[i])
            }
            return builder.build()
        }
    }
}
