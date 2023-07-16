package entities.realized;

import configs.GtfsConfig;
import entities.Stop;
import entities.StopTime;
import models.StopsModel;
import utils.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RealizedStopTime implements RealizedLocation {

    private LocalDateTime arrival;

    private LocalDateTime departure;

    private LocalDate startDate;

    private String stopId;

    private StopsModel stopsModel;

    private GtfsConfig gtfs;

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

    public RealizedStopTime(StopTime stopTime, LocalDate startDate, StopsModel stopsModel, GtfsConfig gtfs) {
        this.startDate = startDate;
        arrival = realizeGoogleTransportTime(stopTime.getArrival(), startDate);
        departure = realizeGoogleTransportTime(stopTime.getDeparture(), startDate);
        stopId = stopTime.getStopId();
        this.stopsModel = stopsModel;
        this.gtfs = gtfs;
    }

    public LocalDateTime getArrival() {
        return arrival;
    }

    public void setArrival(LocalDateTime arrival) {
        this.arrival = arrival;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public void setDeparture(LocalDateTime departure) {
        this.departure = departure;
    }

    public String getStopId() {
        return stopId;
    }

    public Stop getStop() {
        return stopsModel.getByStopId(gtfs, stopId);
    }

    @Override
    public boolean stops() {
        return true;
    }

    @Override
    public String toString() {
        return getStop() + " " + StringUtils.formatStopTime(startDate, arrival) + " -> " + StringUtils.formatStopTime(startDate, departure);
    }
}
