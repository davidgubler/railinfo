package entities;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Entity(value = "serviceCalendarExceptions", noClassnameStored = true)
public class ServiceCalendarException {
    @Id
    private ObjectId _id;

    @Indexed
    private String serviceId;

    private LocalDate date;

    private boolean active;

    public ServiceCalendarException() {
        // dummy constructor for morphia
    }

    public ServiceCalendarException(Map<String, String> data) {
        this.serviceId = data.get("service_id");
        this.date = LocalDate.parse(data.get("date"), DateTimeFormatter.BASIC_ISO_DATE);
        this.active = "1".equals("exception_type"); // 1 = service is available, 2 = service is not available
    }

    public String getServiceId() {
        return serviceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean getActive() {
        return active;
    }
}
