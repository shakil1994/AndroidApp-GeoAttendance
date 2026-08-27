package com.shakhawat.geoattendance.domain.repository

import com.shakhawat.geoattendance.domain.model.OfficeLocation
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    val officeLocation: Flow<OfficeLocation?>

    val lastAttendanceMillis: Flow<Long?>

    suspend fun saveOfficeLocation(
        location: OfficeLocation
    )

    suspend fun saveAttendance(
        timestampMillis: Long
    )
}