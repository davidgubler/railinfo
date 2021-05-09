package entities;

import org.bson.types.ObjectId;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Entity(value = "serviceCalendarExceptions")
public class ServiceCalendarException {
    @Id
    private ObjectId _id;

    @Indexed
    private String serviceId;

    private String date;

    private boolean active;

    public ServiceCalendarException() {
        // dummy constructor for morphia
    }

    public ServiceCalendarException(Map<String, String> data) {
        this.serviceId = data.get("service_id");
        this.date = LocalDate.parse(data.get("date"), DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ISO_DATE);
        this.active = "1".equals(data.get("exception_type")); // 1 = service is available, 2 = service is not available
    }

    public String getServiceId() {
        return serviceId;
    }

    public LocalDate getDate() {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    public boolean getActive() {
        return active;
    }
}
