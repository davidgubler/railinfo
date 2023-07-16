package configs;

import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;

import java.time.ZoneId;

public interface GtfsConfig {
    ZoneId getZoneId();

    Datastore getDs();

    MongoDatabase getDatabase();
}
