package com.example.kafkaclient

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class KafkaListener {
    // Removed listener for test_topic - handled by Kafka Streams

    @KafkaListener(topics = ["user_events"], groupId = "test_id")
    fun listenToDynamic(message: String) {
        println("Received: $message")
    }
}