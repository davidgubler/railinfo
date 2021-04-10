package entities;

import org.joda.time.format.DateTimeFormat;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ServiceCalendar {
    private final String serviceId;
    private final boolean monday;
    private final boolean tuesday;
    private final boolean wednesday;
    private final boolean thursday;
    private final boolean friday;
    private final boolean saturday;
    private final boolean sunday;
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
}
