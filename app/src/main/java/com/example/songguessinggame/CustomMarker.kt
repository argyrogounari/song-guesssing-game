package com.example.songguessinggame

import com.google.android.gms.maps.model.MarkerOptions

/**
 * A custom marker.
 *
 * This class contains an object of custom marker.
 *
 * @constructor Creates a custom marker with id and Marker Options.
 */
class CustomMarker (
    val id: Int = 0,
    val marker: MarkerOptions
)