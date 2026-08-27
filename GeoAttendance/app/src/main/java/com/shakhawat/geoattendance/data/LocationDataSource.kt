package com.shakhawat.geoattendance.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper

import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class LocationDataSource(
    context: Context
) {

    private val locationClient =
        LocationServices.getFusedLocationProviderClient(
            context
        )

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {

        val request =
            CurrentLocationRequest.Builder()

                .setPriority(
                    Priority.PRIORITY_HIGH_ACCURACY
                )

                .setMaxUpdateAgeMillis(2_000)

                .build()

        return locationClient
            .getCurrentLocation(
                request,
                null
            )
            .await()
    }

    @SuppressLint("MissingPermission")
    fun locationUpdates(): Flow<Location> = callbackFlow {

        val request =
            LocationRequest.Builder(

                Priority.PRIORITY_HIGH_ACCURACY,

                2_000L

            )

                .setMinUpdateIntervalMillis(
                    1_000L
                )

                .setWaitForAccurateLocation(
                    true
                )

                .build()

        val callback =
            object : LocationCallback() {

                override fun onLocationResult(
                    result: LocationResult
                ) {

                    result.locations.forEach { location ->

                        trySend(location)
                    }
                }
            }

        locationClient
            .requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()
            )
            .addOnFailureListener { exception ->

                close(exception)
            }

        awaitClose {

            locationClient
                .removeLocationUpdates(callback)
        }
    }
}