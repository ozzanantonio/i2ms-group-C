package com.urbanfix.app.data

import java.util.Locale

/**
 * Normalised projection for Łódź center map view.
 */
object MapProjection {
    const val NORTH = 51.800
    const val SOUTH = 51.740
    const val WEST = 19.400
    const val EAST = 19.520

    fun toMap(lat: Double, lng: Double): Pair<Float, Float> {
        val x = ((lng - WEST) / (EAST - WEST)).coerceIn(0.0, 1.0)
        val y = ((NORTH - lat) / (NORTH - SOUTH)).coerceIn(0.0, 1.0)
        return x.toFloat() to y.toFloat()
    }

    fun toLatLng(x: Float, y: Float): Pair<Double, Double> {
        val lat = NORTH - y.coerceIn(0f, 1f) * (NORTH - SOUTH)
        val lng = WEST + x.coerceIn(0f, 1f) * (EAST - WEST)
        return lat to lng
    }

    fun isInsideMap(lat: Double, lng: Double): Boolean =
        lat in SOUTH..NORTH && lng in WEST..EAST

    fun format(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.4f, %.4f", lat, lng)
}
