package com.example.kafkaclient.weather

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class WeatherAnalyticsConsumer(
    private val analytics: WeatherAnalyticsService
) {

    companion object {
        private val log = LoggerFactory.getLogger(WeatherAnalyticsConsumer::class.java)
    }

    @KafkaListener(topics = ["weather-topic"], groupId = "weather-group")
    fun listen(dto: WeatherDto) {
        log.info("Received: {}", dto)
        analytics.handle(dto)
    }
}