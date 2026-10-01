package com.rideMatcher.locationservice.service;

import com.rideMatcher.locationservice.dto.DriverLocationRequest;
import com.rideMatcher.locationservice.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    // redis key for all driver locations
    private  final StringRedisTemplate redisTemplate;
    private static  final String DRIVERS_GEO_KEY ="drivers:locations";

//     update driver location in redis
//    called every 3sec by driver's phone
//    map to redis GEOADD command

    public void updateDriverLocation(DriverLocationRequest driverLocationRequest) {

        log.info("Updating location for driver: {}",
                driverLocationRequest.getDriverId());

        Point driverPoint = new Point(
                driverLocationRequest.getLongitude(),
                driverLocationRequest.getLatitude()
        );

        Long result = redisTemplate.opsForGeo().add(
                DRIVERS_GEO_KEY,
                driverPoint,
                driverLocationRequest.getDriverId()
        );

        log.info("GEOADD result: {}", result);

        var connection =
                redisTemplate.getConnectionFactory().getConnection();

        log.info("Redis PING: {}", connection.ping());

//        var serverInfo = connection.serverCommands().info("server");

//        log.info("Spring Redis server info: {}", serverInfo);

        log.info("Redis DB size from Spring: {}",
                connection.serverCommands().dbSize());

        log.info("Location updated for driver: {}",
                driverLocationRequest.getDriverId());
    }

    //find nearby drivers within given radius
    // called by matching service on ride request
    // map to redis command

    public List<NearByDriverResponse> findNearByDriver(
            double latitude, double longitude, double radiusInKm
    ){
        log.info("Finding drivers near lat: {} long: {} within {}km", latitude, longitude, radiusInKm);
        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS)
        );
        GeoResults<RedisGeoCommands.GeoLocation<String>> results= redisTemplate.opsForGeo().radius(
                DRIVERS_GEO_KEY,
                searchArea,
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates()
                        .includeDistance()
                        .sortAscending()
                        .limit(10)
        );
        List<NearByDriverResponse> nearbyDrivers = new ArrayList<>();
        if(results!=null){
            results.getContent().forEach(result ->{
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                nearbyDrivers.add(new NearByDriverResponse(
                        location.getName(),
                        location.getPoint().getY(),
                        location.getPoint().getX(),
                        result.getDistance().getValue()
                ));
            });
        }
        log.info("Found {} drivers nearby", nearbyDrivers.size());
        return  nearbyDrivers;
    }
//    remove driver when the go offline
    // maps to redis ZREM command

    public void removeDriver(String driverId){
        log.info("Removing driver: {}", driverId);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverId);

    }

}
