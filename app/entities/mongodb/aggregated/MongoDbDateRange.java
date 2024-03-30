package entities.mongodb.aggregated;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import entities.LocalDateRange;
import org.bson.types.ObjectId;

import java.time.LocalDate;

@Entity
public class MongoDbDateRange implements LocalDateRange {
    @Id
    private ObjectId _id;

    private String start;

    private String end;

    @Override
    public String toString() {
        return getStart() + " - " + getEnd();
    }

    public LocalDate getStart() {
        return LocalDate.parse(start);
    }

    public LocalDate getEnd() {
        return LocalDate.parse(end);
    }
}
