package com.example.kafkaclient.weather

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import jakarta.annotation.PreDestroy

@Service
class WeatherAnalyticsService {

    companion object {
        private val log = LoggerFactory.getLogger(WeatherAnalyticsService::class.java)
        private val VALID_CONDITIONS = setOf("солнечно", "облачно", "дождь")
    }

    private val storage = ConcurrentHashMap<String, ConcurrentLinkedQueue<WeatherDto>>()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val processedCount = AtomicLong(0)
    private val errorCount = AtomicLong(0)
    
    private var lastReportTime = System.currentTimeMillis()
    private val reportIntervalMs = 60_000L
    private val maxEntriesPerCity = 1000

    fun handle(dto: WeatherDto) {
        if (!isValidWeatherData(dto)) {
            log.warn("Invalid weather data received: $dto")
            return
        }
        
        try {
            storage.computeIfAbsent(dto.city ?: "Unknown") {
                ConcurrentLinkedQueue()
            }.add(dto)
            
            serviceScope.launch {
                generateWeeklyReportIfNeeded()
            }
        } catch (e: Exception) {
            log.error("Error handling weather data: $dto", e)
        }
    }

    private fun addWeatherData(dto: WeatherDto) {
        val queue = storage.computeIfAbsent(dto.city ?: "Unknown") {
            ConcurrentLinkedQueue()
        }
        
        queue.add(dto)
        
        while (queue.size > maxEntriesPerCity) {
            queue.poll()
        }
    }

    private fun isValidWeatherData(dto: WeatherDto): Boolean {
        return dto.temperature in 0..35 && 
               dto.condition in setOf("солнечно", "облачно", "дождь") &&
               dto.date != null
    }

    private suspend fun generateWeeklyReportIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastReportTime > reportIntervalMs) {
            lastReportTime = now
            printWeeklyReport()
        }
    }

    private suspend fun printWeeklyReport() = withContext(Dispatchers.Default) {
        log.info("=== Weekly Weather Report ===")

        val allWeather = storage.values.asSequence().flatten().toList()
        
        if (allWeather.isEmpty()) {
            log.info("No weather data available")
            return@withContext
        }

        // Hottest weather
        allWeather.maxByOrNull { it.temperature }?.let { weather ->
            log.info("Hottest: ${weather.temperature}°C in ${weather.city} (${weather.date})")
        }

        // Rainy days by city
        storage.forEach { (city, weatherQueue) ->
            val rainyDays = weatherQueue.count { it.condition == "дождь" }
            log.info("$city: $rainyDays rainy days")
        }
        
        log.info("Total processed: ${processedCount.get()}, errors: ${errorCount.get()}")
    }

    suspend fun getCityStats(city: String): CityStats? = withContext(Dispatchers.Default) {
        val cityWeather = storage[city]?.toList() ?: return@withContext null

        if (cityWeather.isEmpty()) return@withContext null

        val avgTemperature = cityWeather.map { it.temperature }.average()
        val totalDays = cityWeather.size
        val rainyDays = cityWeather.count { it.condition == "дождь" }
        val sunnyDays = cityWeather.count { it.condition == "солнечно" }

        CityStats(
            city = city,
            totalDays = totalDays,
            averageTemperature = avgTemperature,
            rainyDays = rainyDays,
            sunnyDays = sunnyDays
        )
    }

    suspend fun getAllCities(): List<String> = withContext(Dispatchers.Default) {
        storage.keys.toList()
    }

    suspend fun cleanupOldData(cutoffDate: LocalDate) = withContext(Dispatchers.Default) {
        storage.values.forEach { queue ->
            queue.removeIf { weather ->
                weather.date?.isBefore(cutoffDate) == true
            }
        }
    }

    fun getMetrics(): Map<String, Long> = mapOf(
        "processed" to processedCount.get(),
        "errors" to errorCount.get(),
        "cities" to storage.size.toLong(),
        "totalEntries" to storage.values.sumOf { it.size }.toLong()
    )

    @PreDestroy
    fun cleanup() {
        serviceScope.cancel()
    }
}

data class CityStats(
    val city: String,
    val totalDays: Int,
    val averageTemperature: Double,
    val rainyDays: Int,
    val sunnyDays: Int
)
