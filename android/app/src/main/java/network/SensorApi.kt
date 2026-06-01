package com.example.campusfreshzone.network

import com.example.campusfreshzone.model.SensorResponse
import retrofit2.http.GET

interface SensorApi {

    @GET("api/sensors")
    suspend fun getSensors(): SensorResponse
}