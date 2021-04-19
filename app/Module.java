import com.google.inject.AbstractModule;
import models.ServiceCalendarsModel;
import models.StopTimesModel;
import models.StopsModel;
import models.TripsModel;
import models.mongodb.MongoDbServiceCalendarsModel;
import models.mongodb.MongoDbStopTimesModel;
import models.mongodb.MongoDbStopsModel;
import models.mongodb.MongoDbTripsModel;
import services.MongoDb;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        System.out.println("== initializing modules");
        bind(MongoDb.class).asEagerSingleton();
        bind(ServiceCalendarsModel.class).to(MongoDbServiceCalendarsModel.class);
        bind(StopsModel.class).to(MongoDbStopsModel.class);
        bind(StopTimesModel.class).to(MongoDbStopTimesModel.class);
        bind(TripsModel.class).to(MongoDbTripsModel.class);
    }
}
