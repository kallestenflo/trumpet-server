package com.jayway.trumpet.server.domain;

import com.jayway.trumpet.server.domain.location.DistanceUnit;
import com.jayway.trumpet.server.domain.location.Location;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationTest {

    @Test
    public void distance_between_locations_can_be_calculated() {

        Location location1 = Location.location(55.583985D, 12.957578D, 1);
        Location location2 = Location.location(55.584126D, 12.957406D, 1);

        int distanceInMeters = location1.distanceTo(location2, DistanceUnit.METERS).intValue();

        assertThat(distanceInMeters).isEqualTo(19);
    }

}
