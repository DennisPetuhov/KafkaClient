package com.example.kafkaclient.weather

import java.time.LocalDate

data class WeatherDto(
    val city: String?,
    val date: LocalDate?,
    val temperature: Int,  // 0..35
    val condition: String? // солнечно, облачно, дождь
)