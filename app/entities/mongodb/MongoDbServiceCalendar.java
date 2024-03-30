package entities.mongodb;

import org.bson.types.ObjectId;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Entity(value = "serviceCalendars", useDiscriminator = false)
public class MongoDbServiceCalendar implements entities.ServiceCalendar {
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

    @Indexed
    private String start;

    @Indexed
    private String end;

    public MongoDbServiceCalendar() {
        // dummy constructor for morphia
    }

    public MongoDbServiceCalendar(Map<String, String> data) {
        this.serviceId = data.get("service_id");
        this.monday = "1".equals(data.get("monday"));
        this.tuesday = "1".equals(data.get("tuesday"));
        this.wednesday = "1".equals(data.get("wednesday"));
        this.thursday = "1".equals(data.get("thursday"));
        this.friday = "1".equals(data.get("friday"));
        this.saturday = "1".equals(data.get("saturday"));
        this.sunday = "1".equals(data.get("sunday"));
        this.start = LocalDate.parse(data.get("start_date"), DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ISO_DATE);
        this.end = LocalDate.parse(data.get("end_date"), DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ISO_DATE);
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public boolean getMonday() {
        return monday;
    }

    @Override
    public boolean getTuesday() {
        return tuesday;
    }

    @Override
    public boolean getWednesday() {
        return wednesday;
    }

    @Override
    public boolean getThursday() {
        return thursday;
    }

    @Override
    public boolean getFriday() {
        return friday;
    }

    @Override
    public boolean getSaturday() {
        return saturday;
    }

    @Override
    public boolean getSunday() {
        return sunday;
    }

    @Override
    public LocalDate getStart() {
        return LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
    }

    @Override
    public LocalDate getEnd() {
        return LocalDate.parse(end, DateTimeFormatter.ISO_DATE);
    }
}
