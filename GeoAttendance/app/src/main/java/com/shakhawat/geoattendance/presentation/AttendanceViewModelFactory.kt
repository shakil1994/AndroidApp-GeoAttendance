package com.shakhawat.geoattendance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shakhawat.geoattendance.data.LocationDataSource
import com.shakhawat.geoattendance.domain.repository.AttendanceRepository

class AttendanceViewModelFactory(

    private val repository: AttendanceRepository,

    private val locationDataSource: LocationDataSource

) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return AttendanceViewModel(

            repository = repository,

            locationDataSource = locationDataSource

        ) as T
    }
}