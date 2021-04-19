package entities;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Entity(value = "serviceCalendars", noClassnameStored = true)
public class ServiceCalendar {
    @Id
    private ObjectId _id;

    @Indexed
    private final String serviceId;

    private final Boolean monday;

    private final Boolean tuesday;

    private final Boolean wednesday;

    private final Boolean thursday;

    private final Boolean friday;

    private final Boolean saturday;

    private final Boolean sunday;

    private final java.time.LocalDate start;

    private final java.time.LocalDate end;

    public ServiceCalendar(Map<String, String> data) {
        this.serviceId = data.get("service_id");
        this.monday = "1".equals(data.get("monday"));
        this.tuesday = "1".equals(data.get("tuesday"));
        this.wednesday = "1".equals(data.get("wednesday"));
        this.thursday = "1".equals(data.get("thursday"));
        this.friday = "1".equals(data.get("friday"));
        this.saturday = "1".equals(data.get("saturday"));
        this.sunday = "1".equals(data.get("sunday"));
        this.start = LocalDate.parse(data.get("start_date"), DateTimeFormatter.BASIC_ISO_DATE);
        this.end = LocalDate.parse(data.get("end_date"), DateTimeFormatter.BASIC_ISO_DATE);
    }

    public String getServiceId() {
        return serviceId;
    }

    public boolean getMonday() {
        return monday;
    }

    public boolean getTuesday() {
        return tuesday;
    }

    public boolean getWednesday() {
        return wednesday;
    }

    public boolean getThursday() {
        return thursday;
    }

    public boolean getFriday() {
        return friday;
    }

    public boolean getSaturday() {
        return saturday;
    }

    public boolean getSunday() {
        return sunday;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean isActiveToday() {
        LocalDate now = LocalDate.now();
        if (now.isBefore(start) || now.isAfter(end)) {
            return false;
        }
        DayOfWeek dow = now.getDayOfWeek();
        if (dow == DayOfWeek.MONDAY && getMonday()) {
            return true;
        }
        if (dow == DayOfWeek.TUESDAY && getTuesday()) {
            return true;
        }
        if (dow == DayOfWeek.WEDNESDAY && getWednesday()) {
            return true;
        }
        if (dow == DayOfWeek.THURSDAY && getThursday()) {
            return true;
        }
        if (dow == DayOfWeek.FRIDAY && getFriday()) {
            return true;
        }
        if (dow == DayOfWeek.SATURDAY && getSaturday()) {
            return true;
        }
        if (dow == DayOfWeek.SUNDAY && getSunday()) {
            return true;
        }
        return false;
    }
}
