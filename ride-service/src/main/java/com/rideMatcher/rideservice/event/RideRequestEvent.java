package com.rideMatcher.rideservice.event;

// event published to kafka when a ride is requested
// matching service is consumes this event
// topic: ride.request

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class RideRequestEvent {
    private String rideId;
    private String riderId;
    //pickup
    private double pickUpLatitude;
    private double pickupLongitude;
    private String pickupAddress;

    //drop
    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;
}
