package com.rideMatcher.rideservice.service;

import com.rideMatcher.rideservice.event.RideMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideMatchedEventConsumer {

    private final RideService rideService;

    @KafkaListener(topics = "ride.matched", groupId = "ride-service-group")
    public void consumeRideMatchedEvent(RideMatchedEvent event) {
        log.info("Ride matched: rideId={}, driverId={}", event.getRideId(), event.getDriverId());
        rideService.updateRideWithDriver(event.getRideId(), event.getDriverId());
    }
}
