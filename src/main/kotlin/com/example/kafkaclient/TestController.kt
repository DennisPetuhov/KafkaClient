package com.example.kafkaclient

import com.example.kafkaclient.weather.WeatherDto
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/test")
class TestController {

    @PostMapping("/weather")
    fun testWeather(@RequestBody weatherDto: WeatherDto): String {
        println("Test endpoint received: $weatherDto")
        return "Received: $weatherDto"
    }

    @GetMapping("/sample")
    fun getSampleWeather(): WeatherDto {
        return WeatherDto(
            city = "Moscow",
            date = LocalDate.now(),
            temperature = 25,
            condition = "солнечно"
        )
    }
} 