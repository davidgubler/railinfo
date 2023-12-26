package entities.realized;

import entities.Stop;

import java.time.LocalDateTime;
import java.time.ZoneId;

public interface RealizedLocation {

    LocalDateTime getArrival();

    LocalDateTime getDeparture();

    Stop getStop();

    boolean stops();

    ZoneId getZoneId();
}
