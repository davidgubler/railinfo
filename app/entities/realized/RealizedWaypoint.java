package entities.realized;

import entities.Stop;

import java.time.LocalDateTime;

public class RealizedWaypoint implements RealizedLocation {
    private LocalDateTime dateTime;

    private Stop stop;

    public RealizedWaypoint(Stop stop, LocalDateTime dateTime) {
        this.stop = stop;
        this.dateTime = dateTime;
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
}
