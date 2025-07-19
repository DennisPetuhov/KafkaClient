package com.example.kafkaclient

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams

@Configuration
@EnableKafkaStreams
class KafkaStreamsConfig {
    @Bean
    fun kafkaTopology(streamsBuilder: StreamsBuilder): Topology {
        // Source Processor (чтение из входного топика)
        val sourceStream: KStream<String?, String?> = streamsBuilder.stream("test_topic")

        // Stream Processor (цепочка операций обработки)
        val processedStream: KStream<String?, String?> = sourceStream
            .filter({ key, value -> value != null && !value.isBlank() }) // Фильтрация пустых сообщений
            .mapValues({ value -> value?.uppercase() }) // Преобразование в верхний регистр
            .filter({ key, value -> value?.length!! > 5 }) // Фильтрация коротких строк


        // Sink Processor (запись результата в выходной топик)
        processedStream.to("user_events")

        return streamsBuilder.build()
    }
}