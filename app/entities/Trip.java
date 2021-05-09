package entities;

import com.google.inject.Inject;
import entities.trans.Departure;
import models.ServiceCalendarExceptionsModel;
import models.ServiceCalendarsModel;
import models.StopTimesModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity(value = "trips", noClassnameStored = true)
public class Trip implements Comparable<Trip> {
    @Id
    private ObjectId _id;

    @Indexed(options = @IndexOptions(unique = true))
    private String tripId;

    @Indexed
    private String routeId;

    @Indexed
    private String serviceId;

    private String tripHeadsign;

    @Indexed
    private String tripShortName;

    private String directionId;

    @Transient
    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

    @Transient
    @Inject
    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    @Transient
    @Inject
    private StopTimesModel stopTimesModel;

    public Trip() {
        // dummy constructor for morphia
    }

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

    public boolean isActiveToday() {
        LocalDate now = LocalDate.now();
        List<ServiceCalendarException> serviceCalendarExceptions = serviceCalendarExceptionsModel.getByServiceId(serviceId);
        List<ServiceCalendarException> todaysExceptions = serviceCalendarExceptions.stream().filter(s -> now.equals(s.getDate())).collect(Collectors.toList());
        System.out.println("service id: " + serviceId + " has exceptions for today: " + todaysExceptions.size());
        if (todaysExceptions.size() == 1) {
            System.out.println("service is active? " + todaysExceptions.get(0).getActive());
            return todaysExceptions.get(0).getActive();
        }

        ServiceCalendar cal = serviceCalendarsModel.getByServiceId(serviceId);
        if (now.isBefore(cal.getStart()) || now.isAfter(cal.getEnd())) {
            return false;
        }

        DayOfWeek dow = now.getDayOfWeek();
        if (dow == DayOfWeek.MONDAY && cal.getMonday()) {
            return true;
        }
        if (dow == DayOfWeek.TUESDAY && cal.getTuesday()) {
            return true;
        }
        if (dow == DayOfWeek.WEDNESDAY && cal.getWednesday()) {
            return true;
        }
        if (dow == DayOfWeek.THURSDAY && cal.getThursday()) {
            return true;
        }
        if (dow == DayOfWeek.FRIDAY && cal.getFriday()) {
            return true;
        }
        if (dow == DayOfWeek.SATURDAY && cal.getSaturday()) {
            return true;
        }
        if (dow == DayOfWeek.SUNDAY && cal.getSunday()) {
            return true;
        }
        return false;
    }

    public List<StopTime> getStopTimes() {
        return stopTimesModel.getByTrip(this);
    }

    public Departure getDeparture(Collection<Stop> stops) {
        Set<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toSet());
        List<StopTime> stopTimes = getStopTimes();
        int i = 0;
        for (StopTime stopTime : stopTimes) {
            if (stopIds.contains(stopTime.getStopId())) {
                break;
            }
            i++;
        }
        return new Departure(this, stopTimes.subList(i, stopTimes.size()));
    }

    @Override
    public int compareTo(Trip trip) {
        return tripShortName.compareTo(trip.getTripShortName());
    }
}
