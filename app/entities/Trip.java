package entities;

import configs.GtfsConfig;
import entities.mongodb.MongoDbRoute;

import java.time.LocalDate;
import java.util.List;

public interface Trip extends Comparable<Trip> {
    String getServiceId();

    String getTripId();

    String getTripHeadsign();

    String getTripShortName();

    String getDirectionId();

    String getTrainNr();

    boolean isActive(LocalDate date, List<? extends ServiceCalendarException> serviceCalendarExceptions, ServiceCalendar serviceCalendar);

    List<? extends StopTime> getStopTimes();

    MongoDbRoute getRoute();

    GtfsConfig getSourceGtfs();
}
