package entities.realized;

import entities.Stop;

import java.time.LocalDateTime;

public interface RealizedLocation {
    LocalDateTime getArrival();
    LocalDateTime getDeparture();
    Stop getStop();
    boolean stops();
}
