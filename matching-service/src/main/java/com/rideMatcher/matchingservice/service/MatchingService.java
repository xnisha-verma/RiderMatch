package com.rideMatcher.matchingservice.service;

import com.rideMatcher.matchingservice.client.LocationServiceClient;
import com.rideMatcher.matchingservice.dto.NearByDriverResponse;
import com.rideMatcher.matchingservice.event.RideMatchedEvent;
import com.rideMatcher.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {
    private  final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static  final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

//    main matching algorithm
//    called when RideRequestEvent is consumed from kafka
//    @param event
//    steps:
//     1. Ask location service for nearby drivers

    public void matchDriverForRide(RideRequestedEvent event){

        List<NearByDriverResponse> nearBYDrivers = locationServiceClient.getNearByDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM
        );
        if(nearBYDrivers.isEmpty()){
            log.warn("No drivers found near ride: {} ");
            return;
        }
//        step 2.: score each driver and pick the best one
        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearBYDrivers);
        if(bestDriver.isEmpty()){
            log.warn("could not find suitable driver for ride");
            return;
        }
        NearByDriverResponse assignedDriver = bestDriver.get();
//        step 3 : publish rideMatchedEvent to kafka
        RideMatchedEvent matchedEvent = new RideMatchedEvent(
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceInKm()
        );
        kafkaTemplate.send(RIDE_MATCHED_TOPIC, event.getRideId(), matchedEvent);
        log.info("RideMatchedEvent published");
    }

//     dirver scoring algorithm
//     distace: 70%
//     rating: 30%
//     score = (1/ distance) * distanceWeight

    private Optional<NearByDriverResponse> findBestDriver(
            List<NearByDriverResponse> drivers
    ){
        double distanceWeight = 0.7;
        double ratingWeight = 0.3;
        return drivers.stream()
                .max(Comparator.comparingDouble(driver ->{
//                    distance score: closer =higher score
//                    add 0.1 to avoid division by zero
                    double distanceScore = 1.0/ (driver.getDistanceInKm()+0.1);

//                    simulated rating between 4.0 and 5.0
//                     in production: fetch  from driver service

                    double simulatedRating  = 4.0 +Math.random();
//                    final weighted score
                    return (distanceScore*distanceWeight) + (simulatedRating* ratingWeight);
                }));
    }
}
