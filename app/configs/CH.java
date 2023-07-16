package configs;

import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;

import java.time.ZoneId;

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
}
