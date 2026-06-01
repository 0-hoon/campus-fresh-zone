package com.example.campusfreshzone.network

import com.example.campusfreshzone.model.SensorData

class SensorRepository {

    suspend fun getSensors(): List<SensorData> {
        return RetrofitClient.api.getSensors().data
    }
}