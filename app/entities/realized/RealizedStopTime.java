package entities.realized;

import entities.Stop;
import entities.StopTime;
import models.StopsModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RealizedStopTime {

    private LocalDateTime arrival;

    private LocalDateTime departure;

    private String stopId;

    private StopsModel stopsModel;

    private LocalDateTime realizeGoogleTransportTime(String googleTransportTime, LocalDate startDate) {
        String[] split = googleTransportTime.split(":");
        int hour = Integer.parseInt(split[0]);
        int minute = Integer.parseInt(split[1]);
        int second = Integer.parseInt(split[2]);
        while (hour >= 24) {
            // may produce garbage for DST changes
            hour-=24;
            startDate = startDate.plusDays(1);
        }
        return LocalDateTime.of(startDate, LocalTime.of(hour, minute, second));
    }

    public RealizedStopTime(StopTime stopTime, LocalDate startDate, StopsModel stopsModel) {
        arrival = realizeGoogleTransportTime(stopTime.getArrival(), startDate);
        departure = realizeGoogleTransportTime(stopTime.getDeparture(), startDate);
        stopId = stopTime.getStopId();
        this.stopsModel = stopsModel;
    }

    public LocalDateTime getArrival() {
        return arrival;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public String getStopId() {
        return stopId;
    }

    public Stop getStop() {
        return stopsModel.getById(stopId);
    }
}
