package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.Edge;
import entities.NearbyEdge;
import entities.Route;
import entities.Trip;
import entities.realized.RealizedLocation;
import entities.realized.RealizedPass;
import entities.realized.RealizedPassIntermediateComparator;
import entities.realized.RealizedTrip;
import geometry.Point;
import geometry.PolarCoordinates;
import models.EdgesModel;
import models.RealizerModel;
import models.RoutesModel;
import models.TripsModel;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.MongoDb;
import utils.InputUtils;
import utils.PathFinder;

import java.time.*;
import java.time.temporal.TemporalUnit;
import java.util.*;

public class ApiController extends Controller {
    @Inject
    private MongoDb mongoDb;

    @Inject
    private EdgesModel edgesModel;

    @Inject
    private PathFinder pathFinder;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private RealizerModel realizerModel;

    private class LateRealizedPass implements Comparable<LateRealizedPass> {
        private RealizedPass realizedPass;
        private long lateSeconds;

        public LateRealizedPass(RealizedPass realizedPass, long lateSeconds) {
            this.realizedPass = realizedPass;
            this.lateSeconds = lateSeconds;
        }

        public long getLateSeconds() {
            return lateSeconds;
        }

        public RealizedPass getRealizedPass() {
            return realizedPass;
        }

        @Override
        public int compareTo(LateRealizedPass delayedRealizedPass) {
            return Long.compare(Math.abs(lateSeconds), Math.abs(delayedRealizedPass.getLateSeconds()));
        }

        @Override
        public String toString() {
            String start = realizedPass.getTrip().getBegins().getName();
            String end = realizedPass.getTrip().getEnds().getName();
            String shortName = realizedPass.getTrip().getRoute().getShortName();
            String tripShortName = realizedPass.getTrip().getTripShortName();
            String late = "";
            if (getLateSeconds() >= 300 && getLateSeconds() <= 600) {
                late = " mit +" + getLateSeconds()/60;
            }
            if (getLateSeconds() > 600 && getLateSeconds() <= 3600) {
                late = " mit +" + Math.round(getLateSeconds()/300.0)*5;
            }
            if (getLateSeconds() > 3600) {
                late = " mit +" + Math.round(getLateSeconds()/600)*10;
            }
            return shortName + " " + tripShortName + " " +start + " - " + end + late;
        }
    }

    public Result guessTheTrain(Http.Request request, String lngStr, String latStr, String dateStr, String timeStr) {
        GtfsConfig gtfs = mongoDb.getLatest("ch");

        Double lng = InputUtils.toDouble(lngStr);
        Double lat = InputUtils.toDouble(latStr);
        LocalDate date = InputUtils.parseDate(dateStr);
        LocalTime time = InputUtils.parseTime(timeStr);
        if (lng == null || lat == null || date == null || time == null) {
            return badRequest();
        }

        LocalDateTime dateTime = LocalDateTime.of(date, time);
        LocalDateTime lookupStart = dateTime.minusMinutes(180);

        Point point = new Point(lat, lng);
        List<NearbyEdge> nearbyEdges = edgesModel.getByPoint(gtfs, point);

        List<LateRealizedPass> candidates = new LinkedList<>();
        for (NearbyEdge nearbyEdge : nearbyEdges) {
            if (nearbyEdge.getNearbyFactor() <= 0.1) {
                break;
            }
            List<RealizedPass> realizedPasses = realizerModel.getPasses(gtfs, nearbyEdge.getEdge(), lookupStart);
            for (RealizedPass realizedPass : realizedPasses) {
                LocalDateTime passTime = realizedPass.getIntermediate(true, nearbyEdge.getPos());
                Duration diff = Duration.between(passTime, dateTime);
                if (diff.getSeconds() > -300) {
                    candidates.add(new LateRealizedPass(realizedPass, diff.getSeconds()));
                }
            }
        }

        Collections.sort(candidates);

        // Take the first five candidates and all which are less than 5 minutes late, whichever is more
        int i = 0;
        for (LateRealizedPass p : candidates) {
            i++;
            if (i >= 5 && p.getLateSeconds() >= 300) {
                break;
            }
        }
        candidates = candidates.subList(0, i);

        for (LateRealizedPass lateRealizedPass : candidates) {
            System.out.println(lateRealizedPass);
        }

        ArrayNode candiatesArray = Json.newArray();
        candidates.forEach(s -> candiatesArray.add(s.toString()));
        return ok(candiatesArray.toString()).as("application/json; charset=utf-8");
    }
}
