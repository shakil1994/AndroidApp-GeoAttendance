package com.shakhawat.geoattendance.data

import android.content.Context

import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shakhawat.geoattendance.domain.model.OfficeLocation
import com.shakhawat.geoattendance.domain.repository.AttendanceRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.attendanceDataStore
        by preferencesDataStore(
            name = "attendance_preferences"
        )

class AttendanceRepositoryImpl(
    private val context: Context
) : AttendanceRepository {

    private object Keys {

        val officeLatitude =
            doublePreferencesKey("office_latitude")

        val officeLongitude =
            doublePreferencesKey("office_longitude")

        val lastAttendance =
            longPreferencesKey("last_attendance")
    }

    override val officeLocation: Flow<OfficeLocation?> =

        context.attendanceDataStore.data.map { preferences ->

            val latitude =
                preferences[Keys.officeLatitude]

            val longitude =
                preferences[Keys.officeLongitude]

            if (
                latitude != null &&
                longitude != null
            ) {

                OfficeLocation(
                    latitude = latitude,
                    longitude = longitude
                )

            } else {

                null
            }
        }

    override val lastAttendanceMillis: Flow<Long?> =

        context.attendanceDataStore.data.map { preferences ->

            preferences[Keys.lastAttendance]
        }

    override suspend fun saveOfficeLocation(
        location: OfficeLocation
    ) {

        context.attendanceDataStore.edit { preferences ->

            preferences[Keys.officeLatitude] =
                location.latitude

            preferences[Keys.officeLongitude] =
                location.longitude
        }
    }

    override suspend fun saveAttendance(
        timestampMillis: Long
    ) {

        context.attendanceDataStore.edit { preferences ->

            preferences[Keys.lastAttendance] =
                timestampMillis
        }
    }
}