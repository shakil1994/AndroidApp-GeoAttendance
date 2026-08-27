package com.shakhawat.geoattendance.presentation

import android.location.Location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.shakhawat.geoattendance.data.LocationDataSource
import com.shakhawat.geoattendance.domain.model.OfficeLocation
import com.shakhawat.geoattendance.domain.repository.AttendanceRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.launch

class AttendanceViewModel(

    private val repository: AttendanceRepository,

    private val locationDataSource: LocationDataSource

) : ViewModel() {

    companion object {

        const val ATTENDANCE_RADIUS_METERS = 50f
    }

    private val _uiState =
        MutableStateFlow(
            AttendanceUiState()
        )

    val uiState: StateFlow<AttendanceUiState> =
        _uiState.asStateFlow()

    init {

        observeStoredData()
    }

    private fun observeStoredData() {

        viewModelScope.launch {

            combine(

                repository.officeLocation,

                repository.lastAttendanceMillis

            ) { office, lastAttendance ->

                office to lastAttendance

            }.collect { (office, lastAttendance) ->

                _uiState.update { current ->

                    current.copy(

                        officeLocation = office,

                        lastAttendanceMillis =
                            lastAttendance,

                        isWithinRadius =
                            office != null &&
                                    (current.distanceMeters
                                        ?: Float.MAX_VALUE) <=
                                    ATTENDANCE_RADIUS_METERS
                    )
                }
            }
        }
    }

    fun startTracking() {

        viewModelScope.launch {

            locationDataSource
                .locationUpdates()

                .catch { exception ->

                    _uiState.update {

                        it.copy(

                            message =
                                exception.message
                                    ?: "Unable to read GPS location."
                        )
                    }
                }

                .collect { location ->

                    updateDistance(location)
                }
        }
    }

    fun setOfficeLocation() {

        viewModelScope.launch {

            _uiState.update {

                it.copy(

                    isLoading = true,

                    message = null
                )
            }

            runCatching {

                locationDataSource
                    .getCurrentLocation()

                    ?: error(
                        "Unable to get current GPS location."
                    )

            }.onSuccess { location ->

                repository.saveOfficeLocation(

                    OfficeLocation(

                        latitude =
                            location.latitude,

                        longitude =
                            location.longitude
                    )
                )

                updateDistance(location)

                _uiState.update {

                    it.copy(

                        isLoading = false,

                        message =
                            "Office location saved successfully."
                    )
                }

            }.onFailure { exception ->

                _uiState.update {

                    it.copy(

                        isLoading = false,

                        message =
                            exception.message
                                ?: "Unable to save office location."
                    )
                }
            }
        }
    }

    fun markAttendance() {

        val state = _uiState.value

        if (!state.isWithinRadius) {

            _uiState.update {

                it.copy(

                    message =
                        "You must be within 50 meters of the office."
                )
            }

            return
        }

        viewModelScope.launch {

            repository.saveAttendance(
                System.currentTimeMillis()
            )

            _uiState.update {

                it.copy(

                    message =
                        "Attendance marked successfully."
                )
            }
        }
    }

    fun clearMessage() {

        _uiState.update {

            it.copy(
                message = null
            )
        }
    }

    private fun updateDistance(
        location: Location
    ) {

        val office =
            _uiState.value.officeLocation
                ?: return

        val result =
            FloatArray(1)

        Location.distanceBetween(

            location.latitude,

            location.longitude,

            office.latitude,

            office.longitude,

            result
        )

        val distance =
            result[0]

        _uiState.update {

            it.copy(

                distanceMeters = distance,

                isWithinRadius =
                    distance <=
                            ATTENDANCE_RADIUS_METERS
            )
        }
    }
}