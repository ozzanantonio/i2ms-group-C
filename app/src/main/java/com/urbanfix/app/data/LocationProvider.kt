package com.urbanfix.app.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * Wraps Google Play Services FusedLocationProviderClient (Week 8).
 * Tries a fresh high-accuracy fix first and falls back to lastLocation before returning null.
 */
class DeviceLocationProvider(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onResult: (Pair<Double, Double>?) -> Unit) {
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    onResult(loc.latitude to loc.longitude)
                } else {
                    client.lastLocation
                        .addOnSuccessListener { last -> onResult(last?.let { it.latitude to it.longitude }) }
                        .addOnFailureListener { onResult(null) }
                }
            }
            .addOnFailureListener {
                client.lastLocation
                    .addOnSuccessListener { last -> onResult(last?.let { it.latitude to it.longitude }) }
                    .addOnFailureListener { onResult(null) }
            }
    }
}