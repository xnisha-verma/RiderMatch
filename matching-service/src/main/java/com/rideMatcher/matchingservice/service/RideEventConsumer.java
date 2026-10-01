package com.rideMatcher.matchingservice.service;

import com.rideMatcher.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer {
    private final MatchingService matchingService;

//     listens to ride.requested  kakfa topic
//     triggered every time ride service published a new ride request
//    flow:
//    Ride service -> Kafka(ride.requested) -> This consumer -> matchingService

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent event){
        try{
            matchingService.matchDriverForRide(event);
        }catch (Exception e){
            log.error("Error processing ride request: {} - {} ", event.getRideId(), e.getMessage());

//            In production: send to dead letter queue for retry
        }
    }
}
