package entities;

import java.util.List;
import java.util.Map;

public class Trip {
    private final String routeId;
    private final String serviceId;
    private final String tripId;
    private final String tripHeadsign;
    private final String tripShortName;
    private final String directionId;

    public Trip(Map<String, String> data) {
        this.routeId = data.get("route_id");
        this.serviceId = data.get("service_id");
        this.tripId = data.get("trip_id");
        this.tripHeadsign = data.get("trip_headsign");
        this.tripShortName = data.get("trip_short_name");
        this.directionId = data.get("direction_id");
    }

    public String routeId() {
        return routeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public String getTripShortName() {
        return tripShortName;
    }

    public String getDirectionId() {
        return directionId;
    }


    public String toString() {
        return tripId;
    }
}
