package models.mongodb;

import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertManyOptions;
import dev.morphia.aggregation.expressions.AccumulatorExpressions;
import dev.morphia.aggregation.expressions.impls.ValueExpression;
import dev.morphia.aggregation.stages.Group;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import entities.LocalDateRange;
import entities.ServiceCalendarException;
import entities.mongodb.MongoDbServiceCalendar;
import entities.mongodb.MongoDbServiceCalendarException;
import entities.Trip;
import entities.mongodb.aggregated.MongoDbDateRange;
import models.ServiceCalendarExceptionsModel;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDbServiceCalendarExceptionsModel implements ServiceCalendarExceptionsModel {

    private Query<MongoDbServiceCalendarException> query(GtfsConfig gtfs) {
        return gtfs.getDs().find(MongoDbServiceCalendarException.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("serviceCalendarExceptions").drop();
    }

    @Override
    public ServiceCalendarException create(GtfsConfig gtfs, Map<String, String> data) {
        ServiceCalendarException serviceCalendarException = new MongoDbServiceCalendarException(data);
        gtfs.getDs().save(serviceCalendarException);
        return serviceCalendarException;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<ServiceCalendarException> serviceCalendarExceptions = dataBatch.stream().map(data -> new MongoDbServiceCalendarException(data)).collect(Collectors.toList());
        gtfs.getDs().save(serviceCalendarExceptions, new InsertManyOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public List<? extends ServiceCalendarException> getByTrip(Trip trip) {
        return query(trip.getSourceGtfs()).filter(Filters.eq("serviceId", trip.getServiceId())).iterator().toList();
    }

    @Override
    public Map<String, List<ServiceCalendarException>> getByTripsAndDates(GtfsConfig gtfs, Collection<Trip> trips, Collection<LocalDate> localDates) {
        Set<String> serviceIds = trips.stream().map(Trip::getServiceId).collect(Collectors.toSet());
        Set<String> dates = localDates.stream().map(LocalDate::toString).collect(Collectors.toSet());
        Map<String, List<ServiceCalendarException>> exceptionsByServiceId = new HashMap<>();
        for (ServiceCalendarException sce : query(gtfs).filter(Filters.in("serviceId", serviceIds), Filters.in("date", dates)).iterator().toList() ) {
            if (!exceptionsByServiceId.containsKey(sce.getServiceId())) {
                exceptionsByServiceId.put(sce.getServiceId(), new LinkedList<>());
            }
            exceptionsByServiceId.get(sce.getServiceId()).add(sce);
        }
        return exceptionsByServiceId;
    }

    @Override
    public LocalDateRange getDateRange(GtfsConfig gtfs) {
        try {
            return gtfs.getDs().aggregate(MongoDbServiceCalendarException.class).group(Group.group()
                    .field("start", AccumulatorExpressions.min(new ValueExpression("$date")))
                    .field("end", AccumulatorExpressions.max(new ValueExpression("$date")))
            ).execute(MongoDbDateRange.class).next();
        } catch (NoSuchElementException e) {
            // This may happen e.g. if the collection is empty/does not exist
            return null;
        }
    }
}
