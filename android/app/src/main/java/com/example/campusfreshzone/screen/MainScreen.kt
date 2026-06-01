package com.example.campusfreshzone.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat

import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

import com.example.campusfreshzone.model.SensorData
import com.example.campusfreshzone.network.SensorRepository

@Composable
fun MainScreen() {

    var showDialog by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    var currentLocation by remember {
        mutableStateOf(
            LatLng(36.6285, 127.4570)
        )
    }
    var sensorList by remember {
        mutableStateOf<List<SensorData>>(emptyList())
    }

    val repository = remember {
        SensorRepository()
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation,
            15f
        )
    }

    val currentMarkerState = remember {
        MarkerState(
            position = currentLocation
        )
    }

    val sensor1State = remember {
        MarkerState(
            position = LatLng(
                36.6295,
                127.4563
            )
        )
    }

    val sensor2State = remember {
        MarkerState(
            position = LatLng(
                36.6288,
                127.4575
            )
        )
    }

    val sensor3State = remember {
        MarkerState(
            position = LatLng(
                36.6302,
                127.4552
            )
        )
    }

    fun startLocationUpdates() {

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000
            ).build()

        val locationCallback =
            object : LocationCallback() {

                override fun onLocationResult(
                    locationResult: LocationResult
                ) {

                    val location =
                        locationResult.lastLocation

                    location?.let {

                        val latLng = LatLng(
                            it.latitude,
                            it.longitude
                        )

                        currentLocation = latLng

                        currentMarkerState.position =
                            latLng

                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                latLng,
                                16f
                            )
                        )
                    }
                }
            }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                startLocationUpdates()
            }
        }

    LaunchedEffect(Unit) {

        try {
            sensorList = repository.getSensors()
            println("센서 개수: ${sensorList.size}")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        when (
            PackageManager.PERMISSION_GRANTED
        ) {

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {

                startLocationUpdates()
            }

            else -> {

                locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "센서 수 : ${sensorList.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {

                    Marker(
                        state = currentMarkerState,
                        title = "현재 위치",

                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )

                    Marker(
                        state = sensor1State,
                        title = "센서 1",
                        snippet = "미세먼지 좋음",

                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        )
                    )

                    Marker(
                        state = sensor2State,
                        title = "센서 2",
                        snippet = "미세먼지 보통",

                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_YELLOW
                        )
                    )

                    Marker(
                        state = sensor3State,
                        title = "센서 3",
                        snippet = "미세먼지 나쁨",

                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {

                Text(
                    text = "행동 가이드",
                    fontSize = 20.sp
                )
            }
        }

        if (showDialog) {

            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },

                title = {
                    Text("행동 가이드")
                },

                text = {
                    Text(
                        "현재 환경 상태는 안전합니다.\n\nFresh Zone 추천:\n중앙도서관 휴게실"
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {

                        Text("확인")
                    }
                }
            )
        }
    }
}