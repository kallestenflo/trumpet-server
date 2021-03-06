package com.jayway.trumpet.server.domain.location;

import static java.util.Objects.requireNonNull;

public class Location {

    public final Double latitude;
    public final Double longitude;
    public final Integer accuracy;

    public static Location location(Double latitude, Double longitude, Integer accuracy){
        return new Location(latitude, longitude, accuracy);
    }

    private Location(Double latitude, Double longitude, Integer accuracy) {
        requireNonNull(latitude, "latitude can not be null");
        requireNonNull(longitude, "longitude can not be null");
        requireNonNull(accuracy, "accuracy can not be null");

        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }


    public Double distanceTo(Location other, DistanceUnit distanceUnit) {
        requireNonNull(latitude, "latitude can not be null");
        requireNonNull(longitude, "longitude can not be null");

        double theta = longitude - other.longitude;
        double dist = Math.sin(deg2rad(latitude)) * Math.sin(deg2rad(other.latitude)) + Math.cos(deg2rad(latitude)) * Math.cos(deg2rad(other.latitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        switch (distanceUnit){
            case METERS:
                dist = dist * 1.609344;
                dist = dist * 1000;
                break;
            case KILOMETERS:
                dist = dist * 1.609344;
                break;
            case NAUTICAL_MILES:
                dist = dist * 0.8684;
                break;
        }

        return dist;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
