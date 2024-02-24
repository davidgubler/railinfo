import com.google.inject.AbstractModule;
import models.*;
import models.mongodb.*;
import services.MongoDb;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MongoDb.class).asEagerSingleton();
        bind(ServiceCalendarsModel.class).to(MongoDbServiceCalendarsModel.class).asEagerSingleton();
        bind(ServiceCalendarExceptionsModel.class).to(MongoDbServiceCalendarExceptionsModel.class).asEagerSingleton();
        bind(StopsModel.class).to(MongoDbStopsModel.class).asEagerSingleton();
        bind(StopTimesModel.class).to(MongoDbStopTimesModel.class).asEagerSingleton();
        bind(TripsModel.class).to(MongoDbTripsModel.class).asEagerSingleton();
        bind(RoutesModel.class).to(MongoDbRoutesModel.class).asEagerSingleton();
        bind(EdgesModel.class).to(MongoDbEdgesModel.class).asEagerSingleton();
        bind(UsersModel.class).to(MongoDbUsersModel.class).asEagerSingleton();
    }
}
