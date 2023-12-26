package entities.realized;

import configs.GtfsConfig;
import entities.Stop;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class RealizedWaypoint implements RealizedLocation {
    private LocalDateTime dateTime;

    private Stop stop;

    private GtfsConfig gtfs;

    public RealizedWaypoint(Stop stop, LocalDateTime dateTime, GtfsConfig gtfs) {
        this.stop = stop;
        this.dateTime = dateTime;
        this.gtfs = gtfs;
    }

    @Override
    public LocalDateTime getArrival() {
        return dateTime;
    }

    @Override
    public LocalDateTime getDeparture() {
        return dateTime;
    }

    @Override
    public Stop getStop() {
        return stop;
    }

    @Override
    public boolean stops() {
        return false;
    }

    @Override
    public ZoneId getZoneId() {
        return gtfs.getZoneId();
    }
}
