package com.jayway.trumpet.server.domain;

import static java.util.Objects.requireNonNull;

public class Location {

    public final Double latitude;
    public final Double longitude;

    public static Location create(Double latitude, Double longitude){
        return new Location(latitude, longitude);
    }

    private Location(Double latitude, Double longitude) {
        requireNonNull(latitude, "latitude can not be null");
        requireNonNull(longitude, "longitude can not be null");

        this.latitude = latitude;
        this.longitude = longitude;
    }


    public Double distanceTo(Location other, DistanceUnit distanceUnit) {
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
