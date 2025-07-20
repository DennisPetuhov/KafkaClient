package com.example.kafkaclient.weather

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class WeatherAnalyticsListener(
    private val analytics: WeatherAnalyticsService
) {

    companion object {
        private val log = LoggerFactory.getLogger(WeatherAnalyticsListener::class.java)
    }

    @KafkaListener(
        topics = ["weather-topic"],
        groupId = "weather-group",
        containerFactory = "weatherDtoKafkaListenerContainerFactory"
    )
    fun listen(dto: WeatherDto, @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int) {
        println("Received: $dto from partition $partition")
        log.info("Received from partition {}: {}", partition, dto)
        analytics.handle(dto)
    }
}