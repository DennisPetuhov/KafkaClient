package com.example.kafkaclient.weather

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class WeatherDto @JsonCreator constructor(
    @JsonProperty("city") val city: String?,
    @JsonProperty("date") val date: LocalDate?,
    @JsonProperty("temperature") val temperature: Int,  // 0..35
    @JsonProperty("condition") val condition: String? // солнечно, облачно, дождь
)