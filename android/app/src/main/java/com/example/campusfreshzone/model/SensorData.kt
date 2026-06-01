package com.example.campusfreshzone.model

data class SensorData(
    val sensor: String,
    val latitude: Double?,
    val longitude: Double?,
    val temp: Double?,
    val humidity: Double?,
    val aqi: Int?,
    val co2: Int?,
    val fresh: Boolean,
    val statusLevel: Int,
    val mainRisk: String,
    val solution: String
)