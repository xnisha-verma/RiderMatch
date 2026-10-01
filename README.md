# RiderMatch 🚗

A microservices-based ride-matching platform built with Spring Boot, designed to connect riders and drivers in real-time. This system efficiently handles ride requests, matches riders with nearby drivers, and manages location tracking across multiple distributed services.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Microservices](#microservices)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Key Features](#key-features)
- [Development](#development)

---

## Overview

RiderMatch is a scalable ride-sharing platform built using a microservices architecture. It enables:

- **Riders** to request rides from their current location to a destination
- **Drivers** to update their location in real-time and accept ride requests
- **Intelligent Matching** that connects riders with nearby drivers based on geospatial data
- **Event-driven Communication** between services using Apache Kafka
- **High-performance Location Storage** using Redis Geo commands

The system uses event-driven architecture with Kafka to ensure loose coupling between microservices while maintaining data consistency.

---

## Architecture

The platform follows a **microservices architecture** with the following components:

```
┌─────────────────────────────────────────────────────────────┐
│                   External Clients                           │
│            (Rider & Driver Mobile Apps)                      │
└────────┬─────────────────────────┬──────────────────────────┘
         │                         │
         ▼                         ▼
┌──────────────────┐      ┌──────────────────┐
│   Ride Service   │      │ Location Service │
│  (Port 8081)     │      │  (Port 8082)     │
└────────┬─────────┘      └────────┬─────────┘
         │                         │
         │    ┌────────────────┐   │
         ├───▶│ MySQL Database │◀──┤
         │    │  (Ride Data)   │   │
         │    └────────────────┘   │
         │                         │
         │    ┌────────────────┐   │
         │    │     Redis      │◀──┤
         │    │ (Driver Geo)   │   │
         │    └────────────────┘   │
         │                         │
         └────────┬────────────────┘
                  │
         ┌────────▼─────────┐
         │  Kafka Broker    │
         │  (Port 9092)     │
         └────────┬─────────┘
                  │
                  ▼
         ┌──────────────────┐
         │ Matching Service │
         │  (Port 8083)     │
         └──────────────────┘
```

---

## Technology Stack

### Core Framework
- **Spring Boot 4.1.1** - Application framework
- **Java 21** - Programming language
- **Maven** - Build management

### Data & Storage
- **MySQL 8.0** - Relational database for ride information
- **Redis** - In-memory data store for driver locations with geospatial indexing
- **Spring Data JPA** - ORM for database operations
- **Spring Data Redis** - Redis integration for geospatial queries

### Message Queue & Events
- **Apache Kafka 7.4.0** - Distributed event streaming
- **Zookeeper 7.4.0** - Kafka coordinator
- **Spring Kafka** - Kafka integration

### Utilities & Tools
- **Project Lombok** - Boilerplate code reduction
- **Jackson** - JSON serialization/deserialization
- **Docker & Docker Compose** - Containerization

---

## Project Structure

```
RiderMatch/
├── docker-compose.yml              # Infrastructure setup
├── ride-service/                   # Ride management microservice
│   ├── src/main/java/
│   │   └── com/rideMatcher/rideservice/
│   │       ├── controller/         # REST API endpoints
│   │       ├── service/            # Business logic
│   │       ├── model/              # JPA entities
│   │       ├── dto/                # Request/Response DTOs
│   │       ├── event/              # Kafka event classes
│   │       └── repository/         # Database access layer
│   └── pom.xml
│
├── location-service/               # Driver location microservice
│   ├── src/main/java/
│   │   └── com/rideMatcher/locationservice/
│   │       ├── controller/         # REST API endpoints
│   │       ├── service/            # Business logic
│   │       ├── dto/                # Request/Response DTOs
│   │       └── LocationServiceApplication.java
│   └── pom.xml
│
└── matching-service/               # Ride matching microservice
    ├── src/main/java/
    │   └── com/rideMatcher/matchingservice/
    │       ├── controller/         # REST API endpoints
    │       ├── service/            # Business logic
    │       └── MatchingServiceApplication.java
    └── pom.xml
```

---

## Microservices

### 1. **Ride Service** (Port 8081)
Manages ride lifecycle and operations.

**Responsibilities:**
- Create new ride requests
- Track ride status (REQUESTED → MATCHING → ACCEPTED → RIDE_STARTED → COMPLETED)
- Calculate estimated fare using Haversine distance formula
- Publish ride request events to Kafka for matching service
- Persist ride data to MySQL

**Database:** MySQL
**Key Entities:** Ride, RideStatus

**Technology:**
- Spring Boot Data JPA
- Spring Kafka (Producer)
- MySQL Connector

---

### 2. **Location Service** (Port 8082)
Handles real-time driver location tracking using geospatial queries.

**Responsibilities:**
- Store and update driver locations in Redis with geospatial index
- Find nearby drivers within a specified radius using Redis Geo commands
- Remove drivers from active pool when they go offline
- Support driver location updates every 3 seconds from mobile apps

**Database:** Redis (with Geo index)
**Key Commands:** GEOADD, GEORADIUS

**Technology:**
- Spring Data Redis
- Redis Geo commands
- Geospatial indexing

---

### 3. **Matching Service** (Port 8083)
Matches riders with nearest available drivers.

**Responsibilities:**
- Consume ride request events from Kafka
- Query location service for nearby drivers
- Assign driver with best availability to the ride
- Publish ride acceptance events
- Handle matching logic and driver selection algorithms

**Technology:**
- Spring Kafka (Consumer/Producer)
- REST client communication with other services

---

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 21 (if running locally without Docker)
- Maven 3.6+
- Git

### Installation & Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/xnisha-verma/RiderMatch.git
cd RiderMatch
```

#### 2. Start Infrastructure Services

Launch all required services using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **MySQL** on port 3306 (Database: `ride_db`)
- **Redis** on port 6380 (For geospatial indexing)
- **Zookeeper** on port 2181 (Kafka coordinator)
- **Kafka** on port 9092 (Message broker)

#### 3. Build Microservices

```bash
# Build all services
mvn clean install

# Or build individual services
cd ride-service && mvn clean install
cd ../location-service && mvn clean install
cd ../matching-service && mvn clean install
```

#### 4. Run Microservices

**Option A: Run from IDE**
- Open each service's `*Application.java` file and run as Spring Boot application

**Option B: Run from Command Line**
```bash
# Terminal 1: Ride Service
cd ride-service
mvn spring-boot:run

# Terminal 2: Location Service
cd location-service
mvn spring-boot:run

# Terminal 3: Matching Service
cd matching-service
mvn spring-boot:run
```

#### 5. Verify Services

Check service health:
```bash
curl http://localhost:8081/actuator/health  # Ride Service
curl http://localhost:8082/actuator/health  # Location Service
curl http://localhost:8083/actuator/health  # Matching Service
```

---

## Configuration

### Database Configuration (MySQL)

**Default Credentials (from docker-compose.yml):**
```
Host: localhost
Port: 3306
Database: ride_db
Username: root
Password: root
```

### Redis Configuration

**Default Configuration (from docker-compose.yml):**
```
Host: localhost
Port: 6380
Driver Locations Key: drivers:locations
```

### Kafka Configuration

**Broker Configuration:**
```
Bootstrap Server: localhost:9092
Zookeeper: localhost:2181
Auto Create Topics: Enabled
Topics: 
  - ride.requested (Ride Service → Matching Service)
```

---

## API Endpoints

### Ride Service API

#### Request a Ride
```bash
POST /api/v1/rides/request
Content-Type: application/json

{
  "riderId": "rider123",
  "pickUpLatitude": 28.6139,
  "pickupLongitude": 77.2090,
  "pickupAddress": "India Gate, New Delhi",
  "dropLatitude": 28.5244,
  "dropLongitude": 77.1855,
  "dropAddress": "Connaught Place, New Delhi"
}
```

**Response:**
```json
{
  "id": "ride_uuid",
  "riderId": "rider123",
  "driverId": null,
  "status": "MATCHING",
  "estimatedFare": 145.50,
  "actualFare": null,
  "pickupAddress": "India Gate, New Delhi",
  "dropAddress": "Connaught Place, New Delhi"
}
```

#### Get Ride by ID
```bash
GET /api/v1/rides/{rideId}
```

#### Get Rider's Ride History
```bash
GET /api/v1/rides/rider/{riderId}
```

#### Start a Ride
```bash
PUT /api/v1/rides/{rideId}/start
```

#### Complete a Ride
```bash
PUT /api/v1/rides/{rideId}/complete
```

#### Cancel a Ride
```bash
PUT /api/v1/rides/{rideId}/cancel
```

---

### Location Service API

#### Update Driver Location
```bash
POST /api/v1/locations/drivers/update
Content-Type: application/json

{
  "driverId": "driver123",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

**Response:**
```json
"Driver location updated"
```

#### Find Nearby Drivers
```bash
GET /api/v1/locations?latitude=28.6139&longitude=77.2090&radius=5.0
```

**Response:**
```json
[
  {
    "driverId": "driver123",
    "latitude": 28.6145,
    "longitude": 77.2095,
    "distanceInKm": 0.75
  },
  {
    "driverId": "driver124",
    "latitude": 28.6120,
    "longitude": 77.2085,
    "distanceInKm": 2.30
  }
]
```

#### Remove Driver (Go Offline)
```bash
DELETE /api/v1/locations/drivers/{driverId}
```

---

## Key Features

### 1. **Real-Time Location Tracking**
- Drivers update location every 3 seconds
- Redis Geo index enables efficient proximity queries
- Sub-millisecond response time for "find nearby drivers" queries

### 2. **Intelligent Ride Matching**
- Event-driven matching using Kafka
- Matches riders with 10 nearest available drivers (sorted by distance)
- Asynchronous processing prevents blocking operations

### 3. **Dynamic Fare Calculation**
- Uses **Haversine formula** to calculate great-circle distance
- Base fare: ₹50 + ₹12 per kilometer
- Estimated fare provided at ride request time

### 4. **Comprehensive Ride Lifecycle**
```
REQUESTED → MATCHING → ACCEPTED → RIDE_STARTED → COMPLETED/CANCELLED
```

### 5. **Error Handling**
- Global exception handler for validation and runtime errors
- Detailed error messages and HTTP status codes
- Request validation using Jakarta Validation API

### 6. **Logging & Monitoring**
- SLF4J logging throughout the application
- Spring Boot Actuator for health checks and metrics
- Structured logging for debugging

### 7. **Scalability**
- Microservices architecture for independent scaling
- Kafka for decoupled inter-service communication
- Redis for high-performance location queries
- MySQL for persistent ride data storage

---

## Development

### Project Technologies

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 4.1.1 |
| Database | MySQL | 8.0 |
| Cache/Geo | Redis | Latest |
| Message Queue | Kafka | 7.4.0 |
| Build Tool | Maven | 3.6+ |
| Container | Docker | Latest |

### Build Commands

```bash
# Clean build
mvn clean install

# Build specific service
mvn -f ride-service/pom.xml clean install

# Run tests
mvn test

# Package as JAR
mvn package
```

### Running Tests

```bash
mvn clean test
```

### Docker Build & Run (Optional)

```bash
# Build Docker image for Ride Service
docker build -t ridermatch/ride-service ./ride-service

# Run container
docker run -p 8081:8081 --network rideshare-network ridermatch/ride-service
```

---

## Future Enhancements

- [ ] Payment integration and transaction management
- [ ] Driver rating and review system
- [ ] Real-time push notifications for ride updates
- [ ] Advanced matching algorithm with ML/AI
- [ ] Surge pricing during peak demand
- [ ] Driver and rider authentication/authorization
- [ ] Trip analytics and reporting dashboard
- [ ] Multi-language support
- [ ] Customer support chat integration
- [ ] Refund and dispute management system

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is open source and available under the MIT License.

---

## Support & Contact

For questions or issues, please:
- Open an issue on GitHub
- Contact the maintainer: [xnisha-verma](https://github.com/xnisha-verma)

---

## Acknowledgments

- Spring Boot and Spring Data documentation
- Apache Kafka documentation
- Redis Geo commands reference
- The Java and Open Source communities

---

**Last Updated:** October 2026

Happy coding! 🚀
