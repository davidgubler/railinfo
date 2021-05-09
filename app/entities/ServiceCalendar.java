package entities;

import org.bson.types.ObjectId;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Entity(value = "serviceCalendars")
public class ServiceCalendar {
    @Id
    private ObjectId _id;

    @Indexed
    private String serviceId;

    private Boolean monday;

    private Boolean tuesday;

    private Boolean wednesday;

    private Boolean thursday;

    private Boolean friday;

    private Boolean saturday;

    private Boolean sunday;

    private java.time.LocalDate start;

    private java.time.LocalDate end;

    public ServiceCalendar() {
        // dummy constructor for morphia
    }

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
}
