import com.google.inject.AbstractModule;
import models.*;
import models.mongodb.*;
import services.MongoDb;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MongoDb.class).asEagerSingleton();
        bind(ServiceCalendarsModel.class).to(MongoDbServiceCalendarsModel.class);
        bind(ServiceCalendarExceptionsModel.class).to(MongoDbServiceCalendarExceptionsModel.class);
        bind(StopsModel.class).to(MongoDbStopsModel.class);
        bind(StopTimesModel.class).to(MongoDbStopTimesModel.class);
        bind(TripsModel.class).to(MongoDbTripsModel.class);
        bind(RoutesModel.class).to(MongoDbRoutesModel.class);
        bind(EdgesModel.class).to(MongoDbEdgesModel.class);
    }
}
