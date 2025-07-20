package com.example.kafkaclient.config

import com.example.kafkaclient.weather.WeatherDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConsumerConfig(private val kafkaProperties: KafkaProperties) {

    @Bean
    fun stringConsumerFactory(): ConsumerFactory<String, String> {
        val props = kafkaProperties.buildConsumerProperties()
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), StringDeserializer())
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = stringConsumerFactory()
        return factory
    }

    @Bean
    fun weatherDtoConsumerFactory(): ConsumerFactory<String, WeatherDto> {
        val props = kafkaProperties.buildConsumerProperties()

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(KotlinModule.Builder().build())
        
        val jsonDeserializer = JsonDeserializer(WeatherDto::class.java, objectMapper)
        jsonDeserializer.addTrustedPackages("*")
        jsonDeserializer.setUseTypeHeaders(false)

        // Use ErrorHandlingDeserializer to wrap the actual deserializers
        return DefaultKafkaConsumerFactory(
            props,
            ErrorHandlingDeserializer(StringDeserializer()),
            ErrorHandlingDeserializer(jsonDeserializer)
        )
    }

    @Bean
    fun weatherDtoKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, WeatherDto> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, WeatherDto>()
        factory.consumerFactory = weatherDtoConsumerFactory()

        // Set container properties to override application.properties
        factory.containerProperties.ackMode = ContainerProperties.AckMode.RECORD

        // Create and configure error handler with explicit backoff
        val fixedBackOff = FixedBackOff(1000L, 3L)
        val errorHandler = DefaultErrorHandler({ consumerRecord, exception ->
            println("Error processing record from ${consumerRecord.topic()}-${consumerRecord.partition()}@${consumerRecord.offset()}: ${exception.message}")
            exception.printStackTrace()
        }, fixedBackOff)
        factory.setCommonErrorHandler(errorHandler)

        return factory
    }
}