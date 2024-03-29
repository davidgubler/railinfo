package models;

import configs.GtfsConfig;
import entities.*;
import entities.mongodb.MongoDbRoute;
import entities.realized.RealizedLocation;
import entities.realized.RealizedPass;
import entities.realized.RealizedTrip;
import utils.PathFinder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class RealizerModel {

    public Set<RealizedTrip> realizeTrips(GtfsConfig gtfs, LocalDate d1, Collection<Trip> trips) {
        Set<RealizedTrip> realizedTrips = new HashSet<>();

        LocalDate d0 = d1.plusDays(-1);
        LocalDate d2 = d1.plusDays(1);

        Map<String, List<ServiceCalendarException>> serviceCalendarExceptionsByServiceId = gtfs.getServiceCalendarExceptionsModel().getByTripsAndDates(gtfs, trips, Arrays.asList(d0, d1, d2));
        Map<String, ServiceCalendar> serviceCalendarByServiceId = gtfs.getServiceCalendarsModel().getByTrips(gtfs, trips);
        for (Trip trip : trips) {
            List<ServiceCalendarException> serviceCalendarExceptions = serviceCalendarExceptionsByServiceId.get(trip.getServiceId());
            ServiceCalendar serviceCalendar = serviceCalendarByServiceId.get(trip.getServiceId());
            if (trip.isActive(d0, serviceCalendarExceptions, serviceCalendar)) {
                realizedTrips.add(new RealizedTrip(trip, d0));
            }
            if (trip.isActive(d1, serviceCalendarExceptions, serviceCalendar)) {
                realizedTrips.add(new RealizedTrip(trip, d1));
            }
            if (trip.isActive(d2, serviceCalendarExceptions, serviceCalendar)) {
                realizedTrips.add(new RealizedTrip(trip, d2));
            }
        }
        for (RealizedTrip realizedTrip : realizedTrips) {
            realizedTrip.setGtfs(gtfs);
        }
        return realizedTrips;
    }

    public List<RealizedPass> getPasses(GtfsConfig gtfs, Edge edge, LocalDateTime dateTime) {
        Set<String> routeIds = PathFinder.getRouteIdsByEdge(gtfs, edge);
        Set<MongoDbRoute> routes = new HashSet<>();
        for (String routeId : routeIds) {
            routes.add(gtfs.getRoutesModel().getByRouteId(gtfs, routeId));
        }

        Set<Trip> trips = new HashSet<>();
        for (MongoDbRoute route : routes) {
            trips.addAll(gtfs.getTripsModel().getByRoute(route));
        }

        Set<RealizedTrip> realizedTrips = realizeTrips(gtfs, dateTime.toLocalDate(), trips);

        List<RealizedPass> realizedPasses = new LinkedList<>();
        Set<String> edgeStops = Set.of(edge.getStop1().getBaseId(), edge.getStop2().getBaseId());

        for (RealizedTrip realizedTrip : realizedTrips) {
            RealizedLocation previousLocation = null;
            List<RealizedLocation> realizedLocations = realizedTrip.getRealizedStopTimesWithIntermediate();
            for (RealizedLocation realizedLocation : realizedLocations) {
                if (previousLocation != null) {
                    if (previousLocation.getDeparture().isAfter(dateTime)) {
                        if (edgeStops.contains(previousLocation.getStop().getBaseId()) && edgeStops.contains(realizedLocation.getStop().getBaseId())) {
                            realizedPasses.add(new RealizedPass(gtfs, realizedTrip, previousLocation, realizedLocation));
                        }
                    }
                }
                previousLocation = realizedLocation;
            }
        }
        Collections.sort(realizedPasses);
        return realizedPasses;
    }
}
