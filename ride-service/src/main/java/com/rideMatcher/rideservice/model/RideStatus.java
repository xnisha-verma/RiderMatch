package com.rideMatcher.rideservice.model;

// flow
// reqiuested -> matching -> driver arriving
//           -> ride-started -> completed
//           -> cancelled
public enum RideStatus {
    REQUESTED,
    MATCHING,
    ACCEPTED,
    DRIVER_ARRIVING,
    RIDE_STARTED,
    COMPLETED,
    CANCELLED

}
