package com.shakhawat.geoattendance.presentation

import com.shakhawat.geoattendance.domain.model.OfficeLocation

data class AttendanceUiState(
    val officeLocation: OfficeLocation? = null,

    val distanceMeters: Float? = null,

    val isWithinRadius: Boolean = false,

    val isLoading: Boolean = false,

    val message: String? = null,

    val lastAttendanceMillis: Long? = null
)
