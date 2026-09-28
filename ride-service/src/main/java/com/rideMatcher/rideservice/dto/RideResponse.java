package com.rideMatcher.rideservice.dto;

import com.rideMatcher.rideservice.model.RideStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideResponse {

    private String id;

    private String riderId;

    private String driverId;

    private double pickUpLatitude;

    private double pickupLongitude;

    private String pickupAddress;

    private double dropLatitude;

    private double dropLongitude;

    private String dropAddress;

    private RideStatus status;

    private double estimatedFare;
    private double actualFare;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
