package com.example.campusfreshzone.screen
import com.google.maps.android.compose.MarkerInfoWindowContent
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

@SuppressLint("MissingPermission")
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

    val selectedSensor =
        sensorList.firstOrNull {
            it.humidity != null
        }

    val bestZone =
        sensorList.firstOrNull {
            it.isBestZone
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

    var firstMove by remember {
        mutableStateOf(true)
    }

    val currentMarkerState = remember {
        MarkerState(
            position = currentLocation
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

                        if (firstMove) {

                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLng,
                                    16f
                                )
                            )

                            firstMove = false
                        }
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
                    .height(190.dp)
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "현재 상태",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text =
                                if (selectedSensor != null)
                                    "기준 센서 : ${selectedSensor.sensor}"
                                else
                                    "기준 센서 : 정보 없음"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text =
                                if (selectedSensor != null)
                                    "온도 : ${
                                        selectedSensor.temp?.let {
                                            "%.1f℃".format(it)
                                        } ?: "-"
                                    }"
                                else
                                    "온도 : -"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text =
                                if (selectedSensor != null)
                                    "습도 : ${
                                        selectedSensor.humidity?.let {
                                            "%.1f%%".format(it)
                                        } ?: "-"
                                    }"
                                else
                                    "습도 : -"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text =
                                if (selectedSensor != null)
                                    "위험도 : ${
                                        when (selectedSensor.mainRisk) {
                                            "NORMAL" -> "정상"
                                            "WARNING" -> "주의"
                                            "DANGER" -> "위험"
                                            else -> "데이터 지연"
                                        }
                                    }"
                                else
                                    "위험도 : 정보 없음"
                        )

                        Spacer(modifier = Modifier.height(8.dp))


                    }
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
                    sensorList.forEach { sensor ->

                        if (
                            sensor.latitude != null &&
                            sensor.longitude != null
                        ) {

                            MarkerInfoWindowContent(
                                state = MarkerState(
                                    position = LatLng(
                                        sensor.latitude,
                                        sensor.longitude
                                    )
                                ),

                                icon = BitmapDescriptorFactory.defaultMarker(
                                    if (sensor.isBestZone)
                                        BitmapDescriptorFactory.HUE_GREEN
                                    else
                                        BitmapDescriptorFactory.HUE_RED
                                )
                            )

                            { marker ->

                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {

                                    Text(
                                        text = sensor.sensor,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "온도: ${
                                            sensor.temp?.let {
                                                "%.1f".format(it)
                                            } ?: "-"
                                        }℃"
                                    )

                                    Text(
                                        text = "습도: ${
                                            sensor.humidity?.let {
                                                "%.1f".format(it)
                                            } ?: "-"
                                        }%"
                                    )

                                    Text(
                                        text = "위험도: ${
                                            sensor.mainRisk.ifBlank {
                                                "데이터 지연"
                                            }
                                        }"
                                    )
                                }
                            }
                        }
                    }
                }


            }

            Spacer(modifier = Modifier.height(20.dp))

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

            if (showDialog) {

                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                    },

                    title = {
                        Text("행동 가이드")
                    },

                    text = {

                        Column {

                            Text(
                                text =
                                    if (selectedSensor != null)
                                        "기준 센서 : ${selectedSensor.sensor}"
                                    else
                                        "기준 센서 : 정보 없음",

                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text =
                                    selectedSensor?.solution
                                        ?: "데이터를 불러오는 중입니다."
                            )

                            if (
                                selectedSensor != null &&
                                selectedSensor.mainRisk != "NORMAL" &&
                                bestZone != null
                            ) {

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                Text(
                                    text = "추천 Fresh Zone: ${bestZone.sensor}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )


                        }
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
}