package models.mongodb;

import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertManyOptions;
import dev.morphia.aggregation.expressions.AccumulatorExpressions;
import dev.morphia.aggregation.expressions.impls.ValueExpression;
import dev.morphia.aggregation.stages.Group;
import dev.morphia.query.filters.Filters;
import entities.LocalDateRange;
import entities.ServiceCalendar;
import entities.mongodb.MongoDbServiceCalendar;
import entities.Trip;
import entities.mongodb.aggregated.MongoDbDateRange;
import models.ServiceCalendarsModel;
import dev.morphia.query.Query;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDbServiceCalendarsModel implements ServiceCalendarsModel {

    private Query<MongoDbServiceCalendar> query(GtfsConfig gtfs) {
        return gtfs.getDs().find(MongoDbServiceCalendar.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("serviceCalendars").drop();
    }

    @Override
    public ServiceCalendar create(GtfsConfig gtfs, Map<String, String> data) {
        ServiceCalendar serviceCalendar = new MongoDbServiceCalendar(data);
        gtfs.getDs().save(serviceCalendar);
        return serviceCalendar;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<ServiceCalendar> serviceCalendarExceptions = dataBatch.stream().map(data -> new MongoDbServiceCalendar(data)).collect(Collectors.toList());
        gtfs.getDs().save(serviceCalendarExceptions, new InsertManyOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public ServiceCalendar getByTrip(Trip trip) {
        return query(trip.getSourceGtfs()).filter(Filters.eq("serviceId", trip.getServiceId())).first();
    }

    @Override
    public Map<String, ServiceCalendar> getByTrips(GtfsConfig gtfs, Collection<Trip> trips) {
        Set<String> serviceIds = trips.stream().map(Trip::getServiceId).collect(Collectors.toSet());
        Map<String, ServiceCalendar> serviceCalendarByServiceId = new HashMap<>();
        for (ServiceCalendar serviceCalendar : query(gtfs).filter(Filters.in("serviceId", serviceIds)).iterator().toList() ) {
            serviceCalendarByServiceId.put(serviceCalendar.getServiceId(), serviceCalendar);
        }
        return serviceCalendarByServiceId;
    }

    @Override
    public LocalDateRange getDateRange(GtfsConfig gtfs) {
        try {
            return gtfs.getDs().aggregate(MongoDbServiceCalendar.class).group(Group.group()
                    .field("start", AccumulatorExpressions.min(new ValueExpression("$start")))
                    .field("end", AccumulatorExpressions.max(new ValueExpression("$end")))
            ).execute(MongoDbDateRange.class).next();
        } catch (NoSuchElementException e) {
            // This may happen e.g. if the collection is empty/does not exist
            return null;
        }
    }
}
