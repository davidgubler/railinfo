package controllers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.Edge;
import entities.NearbyEdge;
import entities.api.ApiEdgePass;
import entities.api.ApiEdgeTraffic;
import entities.realized.RealizedPass;
import entities.realized.RealizedPassPos;
import geometry.Point;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.MongoDb;
import utils.InputUtils;
import utils.PathFinder;
import utils.StringUtils;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class ApiController extends Controller {
    @Inject
    private MongoDb mongoDb;

    @Inject
    private StopsModel stopsModel;

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

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


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

    public Result edgePos(Http.Request request) {
        GtfsConfig gtfs = mongoDb.getLatest("ch");

        Map<Edge, Double> edges = new HashMap<>();

        for (Map.Entry<String, String[]> param : request.queryString().entrySet()) {
            Double position;
            try {
                position = Double.parseDouble(param.getValue()[0]);
            } catch (Exception e) {
                continue;
            }
            Edge edge = edgesModel.getEdgeByString(gtfs, param.getKey());
            if (edge == null) {
                continue;
            }
            edges.put(edge, position);
        }

        List<RealizedPassPos> realizedPassesWithPos = new LinkedList<>();
        for (Edge edge : edges.keySet()) {
            List<RealizedPass> realizedPasses = realizerModel.getPasses(gtfs, edge, LocalDateTime.now(gtfs.getZoneId()).minusHours(12));
            realizedPassesWithPos.addAll(realizedPasses.stream().map(rp -> new RealizedPassPos(rp, edge, edges.get(edge))).collect(Collectors.toList()));
        }
        Collections.sort(realizedPassesWithPos);

        List<ApiEdgePass> apiEdgePasses = new LinkedList<>();
        for (RealizedPassPos rp : realizedPassesWithPos) {
            String edgeName = rp.getEdge().getDisplayName();
            boolean forward = rp.isForward();
            LocalDateTime localDateTime = rp.getIntermediate();
            long epochTime = StringUtils.formatTimeEpochSecond(rp.getIntermediate(), rp.getZoneId());

            String shortName = rp.getTrip().getRoute().getShortName();
            String tripShortName = rp.getTrip().getTripShortName();
            String tripBegins = rp.getTrip().getBegins().getName();
            String tripEnds = rp.getTrip().getEnds().getName();

            apiEdgePasses.add(new ApiEdgePass(edgeName, forward, localDateTime.toLocalTime().toString(), epochTime, shortName, tripShortName, tripBegins, tripEnds));
        }

        Map<String, Double> displayEdges = new HashMap<>();
        for (Map.Entry<Edge, Double> entry : edges.entrySet()) {
            displayEdges.put(entry.getKey().getDisplayName(), entry.getValue());
        }

        ApiEdgeTraffic edgeTraffic = new ApiEdgeTraffic(displayEdges, apiEdgePasses);
        try {
            return ok(MAPPER.writeValueAsString(edgeTraffic)).as("application/json; charset=utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
