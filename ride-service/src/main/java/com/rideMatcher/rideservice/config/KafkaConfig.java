package com.rideMatcher.rideservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    // topic where ride service published ride request
    // matching service subscribers to this topic

    @Bean
    public NewTopic rideRequestedTopic(){
        return TopicBuilder.name("ride.requested")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // topic where matching service publishes match results
    // ride service subscribers to this topic

    public NewTopic rideMatchedTopic(){
        return TopicBuilder.name("ride.mathced")
                .partitions(3)
                .replicas(1)
                .build();
    }


}
