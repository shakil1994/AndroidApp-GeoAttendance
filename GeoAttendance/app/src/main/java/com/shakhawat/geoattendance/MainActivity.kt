package com.shakhawat.geoattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import android.Manifest
import android.content.pm.PackageManager

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.runtime.collectAsState

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat

import androidx.lifecycle.viewmodel.compose.viewModel

import com.shakhawat.geoattendance.data.AttendanceRepositoryImpl
import com.shakhawat.geoattendance.data.LocationDataSource

import com.shakhawat.geoattendance.presentation.AttendanceViewModel
import com.shakhawat.geoattendance.presentation.AttendanceViewModelFactory

import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = AttendanceRepositoryImpl(applicationContext)

        val locationDataSource = LocationDataSource(applicationContext)

        setContent {
            val viewModel: AttendanceViewModel =
                viewModel(factory = AttendanceViewModelFactory(repository, locationDataSource))
            AttendanceTheme {
                AttendanceScreen(viewModel)
            }
        }
    }
}

@Composable
private fun AttendanceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary =
                Color(0xFF2457F5),

            onPrimary =
                Color.White,

            primaryContainer =
                Color(0xFFE8EEFF),

            secondary =
                Color(0xFF00A878),

            background =
                Color(0xFFF7F8FC),

            surface =
                Color.White,

            onSurface =
                Color(0xFF182033),

            surfaceVariant =
                Color(0xFFF0F2F7)
        ),

        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceScreen(
    viewModel: AttendanceViewModel
) {

    val context =
        LocalContext.current

    var hasPermission by remember {

        mutableStateOf(

            ContextCompat.checkSelfPermission(

                context,

                Manifest.permission.ACCESS_FINE_LOCATION

            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(

            ActivityResultContracts
                .RequestMultiplePermissions()

        ) { result ->

            hasPermission =

                result[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||

                        result[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true
        }

    LaunchedEffect(hasPermission) {

        if (!hasPermission) {

            permissionLauncher.launch(

                arrayOf(

                    Manifest.permission.ACCESS_FINE_LOCATION,

                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        } else {

            viewModel.startTracking()
        }
    }

    val state by
    viewModel.uiState.collectAsState()

    val withinRadius =
        state.isWithinRadius &&
                state.officeLocation != null

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Column {

                        Text(

                            "Attendance",

                            fontSize = 21.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(

                            "Geo-fenced check-in",

                            fontSize = 12.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                                    .copy(
                                        alpha = .55f
                                    )
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            viewModel.startTracking()
                        }
                    ) {

                        Icon(
                            Icons.Default.Refresh,
                            contentDescription =
                                "Refresh"
                        )
                    }
                },

                colors =
                    TopAppBarDefaults
                        .topAppBarColors(

                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .background
                        )
            )
        }

    ) { padding ->

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(
                        horizontal = 20.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            Spacer(
                Modifier.height(4.dp)
            )

            LocationStatusCard(

                distanceMeters =
                    state.distanceMeters,

                officeConfigured =
                    state.officeLocation != null,

                withinRadius =
                    withinRadius
            )

            OfficeLocationCard(

                configured =
                    state.officeLocation != null,

                loading =
                    state.isLoading,

                onSetLocation =
                    viewModel::setOfficeLocation
            )

            AttendanceActionCard(

                enabled =
                    withinRadius,

                hasPermission =
                    hasPermission,

                onMark =
                    viewModel::markAttendance
            )

            state.lastAttendanceMillis?.let {

                LastAttendanceCard(it)
            }

            state.message?.let {

                Surface(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(14.dp),

                    color =
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                ) {

                    Row(

                        modifier =
                            Modifier.padding(14.dp),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(

                            Icons.Default.CheckCircle,

                            contentDescription =
                                null,

                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )

                        Spacer(
                            Modifier.width(10.dp)
                        )

                        Text(

                            it,

                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationStatusCard(
    distanceMeters: Float?,
    officeConfigured: Boolean,
    withinRadius: Boolean
) {

    val distanceText =
        distanceMeters?.let {

            if (it < 1000) {

                "${it.toInt()} m"

            } else {

                String.format(
                    "%.2f km",
                    it / 1000f
                )
            }

        } ?: "--"

    val statusText =
        when {

            !officeConfigured ->
                "Office location not set"

            withinRadius ->
                "You are inside the attendance zone"

            else ->
                "Move closer to the office"
        }

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
    ) {

        Column(

            modifier =
                Modifier.padding(22.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(

                modifier =
                    Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(
                                alpha = .16f
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    Icons.Default.LocationOn,

                    contentDescription =
                        null,

                    tint = Color.White,

                    modifier =
                        Modifier.size(34.dp)
                )
            }

            Spacer(
                Modifier.height(12.dp)
            )

            Text(

                distanceText,

                color = Color.White,

                fontSize = 36.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Text(

                "distance from office",

                color =
                    Color.White.copy(
                        alpha = .78f
                    ),

                fontSize = 12.sp
            )

            Spacer(
                Modifier.height(14.dp)
            )

            Surface(

                shape =
                    RoundedCornerShape(50),

                color =
                    Color.White.copy(
                        alpha = .15f
                    )
            ) {

                Text(

                    statusText,

                    modifier =
                        Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),

                    color = Color.White,

                    fontSize = 12.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun OfficeLocationCard(
    configured: Boolean,
    loading: Boolean,
    onSetLocation: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            )
    ) {

        Column(
            Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(

                    modifier =
                        Modifier.size(44.dp),

                    shape =
                        RoundedCornerShape(14.dp),

                    color =
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            Icons.Default.MyLocation,

                            contentDescription =
                                null,

                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )
                    }
                }

                Spacer(
                    Modifier.width(12.dp)
                )

                Column(
                    Modifier.weight(1f)
                ) {

                    Text(

                        "Office location",

                        fontWeight =
                            FontWeight.SemiBold,

                        fontSize = 16.sp
                    )

                    Text(

                        if (configured)
                            "Saved on this device"
                        else
                            "Set your current GPS location",

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                                .copy(
                                    alpha = .55f
                                ),

                        fontSize = 12.sp
                    )
                }

                if (configured) {

                    Icon(

                        Icons.Default.CheckCircle,

                        contentDescription =
                            null,

                        tint =
                            MaterialTheme
                                .colorScheme
                                .secondary
                    )
                }
            }

            Spacer(
                Modifier.height(14.dp)
            )

            OutlinedButton(

                onClick =
                    onSetLocation,

                enabled =
                    !loading,

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(14.dp)
            ) {

                if (loading) {

                    CircularProgressIndicator(

                        modifier =
                            Modifier.size(18.dp),

                        strokeWidth = 2.dp
                    )

                } else {

                    Icon(

                        Icons.Default.MyLocation,

                        contentDescription =
                            null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(

                        if (configured)
                            "Update Office Location"
                        else
                            "Set Office Location"
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceActionCard(
    enabled: Boolean,
    hasPermission: Boolean,
    onMark: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            )
    ) {

        Column(
            Modifier.padding(18.dp)
        ) {

            Text(

                "Today's attendance",

                fontSize = 16.sp,

                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                Modifier.height(5.dp)
            )

            Text(

                if (enabled)
                    "You're eligible to check in."
                else
                    "You must be within 50 meters of the office.",

                fontSize = 12.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
                        .copy(
                            alpha = .55f
                        )
            )

            Spacer(
                Modifier.height(14.dp)
            )

            Button(

                onClick =
                    onMark,

                enabled =
                    enabled &&
                            hasPermission,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),

                shape =
                    RoundedCornerShape(15.dp)
            ) {

                Icon(

                    Icons.Default.CheckCircle,

                    contentDescription =
                        null
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(

                    "Mark Attendance",

                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LastAttendanceCard(
    timestamp: Long
) {

    Surface(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(16.dp),

        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
    ) {

        Row(

            Modifier.padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.width(10.dp))

            Column {
                Text("Last attendance", fontSize = 11.sp)
                Text(
                    DateFormat.getDateTimeInstance().format(Date(timestamp)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}