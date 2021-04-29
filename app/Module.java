import com.google.inject.AbstractModule;
import models.*;
import models.mongodb.*;
import services.MongoDb;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        System.out.println("== initializing modules");
        bind(MongoDb.class).asEagerSingleton();
        bind(ServiceCalendarsModel.class).to(MongoDbServiceCalendarsModel.class);
        bind(ServiceCalendarExceptionsModel.class).to(MongoDbServiceCalendarExceptionsModel.class);
        bind(StopsModel.class).to(MongoDbStopsModel.class);
        bind(StopTimesModel.class).to(MongoDbStopTimesModel.class);
        bind(TripsModel.class).to(MongoDbTripsModel.class);
    }
}
