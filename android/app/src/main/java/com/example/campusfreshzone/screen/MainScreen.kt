package com.example.campusfreshzone.screen
import com.google.maps.android.compose.MarkerInfoWindowContent
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

    val freshZoneColor = Color(0xFF2E7D32)
    val statusColor =
        when {
            selectedSensor == null -> Color(0xFF757575)
            !selectedSensor.fresh -> Color(0xFF757575)
            selectedSensor.statusLevel >= 3 -> Color(0xFFC62828)
            selectedSensor.statusLevel == 2 -> Color(0xFFF57C00)
            else -> freshZoneColor
        }
    val statusBackground =
        when {
            selectedSensor == null -> Color(0xFFF1F3F4)
            !selectedSensor.fresh -> Color(0xFFF1F3F4)
            selectedSensor.statusLevel >= 3 -> Color(0xFFFFEBEE)
            selectedSensor.statusLevel == 2 -> Color(0xFFFFF3E0)
            else -> Color(0xFFE8F5E9)
        }
    val animatedStatusColor by animateColorAsState(
        targetValue = statusColor,
        label = "statusColor"
    )
    val riskText =
        if (selectedSensor != null)
            when (selectedSensor.mainRisk) {
                "NORMAL" -> "정상"
                "WARNING" -> "주의"
                "DANGER" -> "위험"
                else -> "데이터 지연"
            }
        else
            "정보 없음"
    val shouldShowFreshZone =
        selectedSensor != null &&
            selectedSensor.mainRisk != "NORMAL" &&
            bestZone != null

    @Composable
    fun MetricBox(
        icon: String,
        label: String,
        value: String,
        color: Color,
        modifier: Modifier = Modifier
    ) {

        Column(
            modifier = modifier
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = icon,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }
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
            .background(Color(0xFFF4F7F5))
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
                    .height(330.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = statusBackground
                )
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
                            fontWeight = FontWeight.Bold,
                            color = animatedStatusColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .background(
                                    color = animatedStatusColor,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 6.dp
                                )
                        ) {

                            Text(
                                text = riskText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 8.dp
                                )
                        ) {

                            Text(
                                text =
                                    if (selectedSensor != null)
                                        "기준 센서 : ${selectedSensor.sensor}"
                                    else
                                        "기준 센서 : 정보 없음",
                                color = Color(0xFF374151),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            MetricBox(
                                icon = "℃",
                                label = "온도",
                                value =
                                    if (selectedSensor != null)
                                        selectedSensor.temp?.let {
                                            "%.1f".format(it)
                                        } ?: "-"
                                    else
                                        "-",
                                color = Color(0xFFE65100),
                                modifier = Modifier.weight(1f)
                            )

                            MetricBox(
                                icon = "💧",
                                label = "습도",
                                value =
                                    if (selectedSensor != null)
                                        selectedSensor.humidity?.let {
                                            "%.1f".format(it)
                                        } ?: "-"
                                    else
                                        "-",
                                color = Color(0xFF1976D2),
                                modifier = Modifier.weight(1f)
                            )

                            MetricBox(
                                icon = "!",
                                label = "위험도",
                                value = riskText,
                                color = Color(0xFFC62828),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (shouldShowFreshZone) {

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(
                                        horizontal = 14.dp,
                                        vertical = 8.dp
                                    )
                            ) {

                                Text(
                                    text = "추천 Fresh Zone : ${bestZone.sensor}",
                                    color = freshZoneColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }


                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedStatusColor
                )
            ) {

                Text(
                    text = "행동 가이드",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
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

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "● 추천",
                        color = freshZoneColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "● 일반 센서",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
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
                                shouldShowFreshZone
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
