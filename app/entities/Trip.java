package entities;

import com.google.inject.Inject;
import models.ServiceCalendarExceptionsModel;
import models.ServiceCalendarsModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
        if (todaysExceptions.size() == 1) {
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

    @Override
    public int compareTo(Trip trip) {
        return tripShortName.compareTo(trip.getTripShortName());
    }
}
