package com.rideMatcher.rideservice.service;

import com.rideMatcher.rideservice.dto.RideRequest;
import com.rideMatcher.rideservice.dto.RideResponse;
import com.rideMatcher.rideservice.event.RideRequestEvent;
import com.rideMatcher.rideservice.model.Ride;
import com.rideMatcher.rideservice.model.RideStatus;
import com.rideMatcher.rideservice.respository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {
    private final RideRepository rideRepository;
    private  final KafkaTemplate<String, RideRequestEvent> kafkaTemplate;

    private  static  final String RIDE_REQUESTED_TOPIC = "ride.requested";

    public RideResponse requestRide(RideRequest request){
        log.info("New ride request from ride: {}", request.getRiderId());

        Ride ride = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setPickUpLatitude(request.getPickUpLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setDropAddress(request.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(request));

        Ride saveRide = rideRepository.save(ride);

//      publish event to kafka
//        matching service will consume this and find nearest driver

        RideRequestEvent event = new RideRequestEvent(
                saveRide.getId(),
                saveRide.getRiderId(),
                saveRide.getPickUpLatitude(),
                saveRide.getPickupLongitude(),
                saveRide.getPickupAddress(),
                saveRide.getDropLatitude(),
                saveRide.getDropLongitude(),
                saveRide.getDropAddress()
        );
        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, saveRide.getId(), event);
        log.info("RideRequestedEvent published to kafka for ride: {}", saveRide.getId());

        saveRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(saveRide);

        return mapToresponse(saveRide);

    }

    public void updateRideWithDriver(String rideId, String driverId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()-> new RuntimeException("Ride not found"));
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    public RideResponse startRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()-> new RuntimeException("Ride not found"));

        if(ride.getStatus() != RideStatus.ACCEPTED){
            throw new RuntimeException("Ride cannot be started. Current status: "+ride.getStatus());
        }
        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);
        return mapToresponse(ride);
    }
    public  RideResponse completeRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()-> new RuntimeException("Ride not found"));
        if(ride.getStatus()!=RideStatus.RIDE_STARTED){
            throw  new RuntimeException("Ride cannot be completed. Current status: "+ride.getStatus());
        }
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);
        return mapToresponse(ride);
    }

    public RideResponse cancelRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()-> new RuntimeException("Ride not found"));
        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
        return mapToresponse(ride);

    }

    public RideResponse getRideById(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()-> new RuntimeException("Ride not found"));
        return mapToresponse(ride);
    }

    public List<RideResponse> getRideByRider(String rideId){
        return  rideRepository.findByRiderIdOrderByCreatedAtDesc(rideId)
                .stream()
                .map(this::mapToresponse)
                .collect(Collectors.toList());
    }

    private double calculateEstimateFare(RideRequest request){
        //simplified have sine distance calculation
        double lat1 = Math.toRadians(request.getPickUpLatitude());
        double lat2 = Math.toRadians(request.getDropLatitude());

        double long1 =  Math.toRadians(request.getPickupLongitude());
        double long2 = Math.toRadians(request.getDropLongitude());

        double dLat = lat2 -lat1;
        double dlon = long2- long1;
        double a = Math.pow(Math.sin(dLat/2),2)
                + Math.cos(lat1) * Math.cos(lat2)
                *Math.pow(Math.sin(dlon/2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double distanceKm = 6371 * c;

        // base fare : 50rs + 12rs.perKm
        double fare = 50 + (distanceKm *12);
        return Math.round(fare* 100.0)/100.0;

    }

    private RideResponse mapToresponse(Ride ride){
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickUpLatitude(ride.getPickUpLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setStatus(ride.getStatus());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        return response;
    }

}
