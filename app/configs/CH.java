package configs;

import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;

import java.time.ZoneId;
import java.util.Objects;

public class CH implements GtfsConfig {
    @Override
    public ZoneId getZoneId() {
        return ZoneId.of("Europe/Zurich");
    }

    @Override
    public Datastore getDs() {
        return ds;
    }

    @Override
    public MongoDatabase getDatabase() {
        return db;
    }

    private MongoDatabase db;
    private Datastore ds;

    public CH(MongoDatabase db, Datastore ds) {
        this.db = db;
        this.ds = ds;
    }

    @Override
    public String toString() {
        return db.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CH ch = (CH) o;
        return Objects.equals(db.getName(), ch.db.getName());
    }

    @Override
    public int hashCode() {
        return db.getName().hashCode();
    }
}
