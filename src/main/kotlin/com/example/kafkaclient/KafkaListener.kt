package com.example.kafkaclient

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaListener {
    // Removed listener for test_topic - handled by Kafka Streams

    @KafkaListener(
        topics = ["user_events"],
        groupId = "test_id",
        containerFactory = "kafkaListenerContainerFactory"    )
    fun listenToDynamic(message: String) {
        println("Received: $message")
    }
}