package entities.mongodb;

import org.bson.types.ObjectId;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Entity(value = "serviceCalendarExceptions", useDiscriminator = false)
public class MongoDbServiceCalendarException implements entities.ServiceCalendarException {
    @Id
    private ObjectId _id;

    @Indexed
    private String serviceId;

    private String date;

    private boolean active;

    public MongoDbServiceCalendarException() {
        // dummy constructor for morphia
    }

    public MongoDbServiceCalendarException(Map<String, String> data) {
        this.serviceId = data.get("service_id");
        this.date = LocalDate.parse(data.get("date"), DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ISO_DATE);
        this.active = "1".equals(data.get("exception_type")); // 1 = service is available, 2 = service is not available
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public LocalDate getDate() {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    }

    @Override
    public boolean getActive() {
        return active;
    }
}
