package utils;

import com.google.inject.Inject;
import com.google.inject.Injector;
import controllers.routes;
import entities.*;
import entities.mongodb.MongoDbEdge;
import entities.realized.RealizedWaypoint;
import geometry.EdgeDirectionComparator;
import models.EdgesModel;
import models.RoutesModel;
import models.StopsModel;
import models.TripsModel;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PathFinder {

    @Inject
    private StopsModel stopsModel;

    @Inject
    private EdgesModel edgesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private Injector injector;

    private Path quickest(Stop current, Stop to, long timeLimit, Path travelledPath, Map<String, Long> timeFromStart, Function<Stop, List<? extends Edge>> f) {
        String currentId = current.getParentStopId();
        if (timeFromStart.containsKey(currentId) && timeFromStart.get(currentId) <= travelledPath.getDuration()) {
            // the current stop has already been reached via a quicker path, thus we abort
            return null;
        }

        // we've reached the current stop quicker than before
        timeFromStart.put(currentId, travelledPath.getDuration());

        if (current.getParentStopId().equals(to.getParentStopId()) && travelledPath.getDuration() <= timeLimit) {
            return travelledPath;
        }
        if (travelledPath.getDuration() > timeLimit) {
            return null;
        }

        Path quickestPath = null;

        List<? extends Edge> edges = f.apply(current); //edgesModel.getEdgesFrom(current);
        EdgeDirectionComparator comp = new EdgeDirectionComparator(current, to);
        edges.sort(comp);

        for (Edge tryEdge : edges) {
            Path solution = quickest(tryEdge.getDestination(current), to, timeLimit, new Path(travelledPath, tryEdge), timeFromStart, f);
            if (solution != null) {
                quickestPath = solution;
                timeLimit = quickestPath.getDuration() - 1; // we're only interested in quicker paths
            }
        }

        return quickestPath;
    }

    private ConcurrentHashMap<String, Path> quickestPaths = new ConcurrentHashMap<>();

    private Path quickest(Stop from, Stop to, long timeLimit, Function<Stop, List<? extends Edge>> f) {
        String key = from.getParentStopId() + "|" + to.getParentStopId();
        if (quickestPaths.containsKey(key)) {
            return quickestPaths.get(key);
        }
        long start = System.currentTimeMillis();
        Map<String, Long> cacheMap = new HashMap<>();
        Path quickest = quickest(from, to, timeLimit, new Path(), cacheMap, f);

        /*
        System.out.println("path finding from " + from + " to " + to + " in " + StringUtils.formatSeconds((int)timeLimit) + " took " + (System.currentTimeMillis() - start) + " ms and visited " + cacheMap.size() + " stations");
        Map<Long, List<Stop>> inverted = new HashMap<>();
        for (Map.Entry<String, Long> entry : cacheMap.entrySet()) {
            if (!inverted.containsKey(entry.getValue())) {
                inverted.put(entry.getValue(), new LinkedList<>());
            }
            inverted.get(entry.getValue()).add(stopsModel.getById(entry.getKey()));
        }
        List<Long> travelTimes = new LinkedList<>(inverted.keySet());
        Collections.sort(travelTimes);
        for (Long time : travelTimes) {
            System.out.println(StringUtils.formatSeconds(time.intValue()) + ": "  + inverted.get(time));
        }*/

        if (quickest == null) {
            System.out.println("XXX no path found from " + from + " to " + to + " in " + timeLimit + " seconds");
            return null;
        }
        quickestPaths.put(key, quickest);
        String reverseKey = to.getParentStopId() + "|" + from.getParentStopId();
        quickestPaths.put(reverseKey, quickest.getReverse());
        return quickest;
    }


    public List<RealizedWaypoint> getIntermediate(Stop from, Stop to, LocalDateTime departure, LocalDateTime arrival) {
        long scheduledSeconds = departure.until(arrival, ChronoUnit.SECONDS);

        Path quickest = quickest(from, to, scheduledSeconds * 2, stop -> edgesModel.getEdgesFrom(stop));

        if (quickest == null) {
            return new LinkedList<>();
        }

        double speedFactor = (double)(scheduledSeconds - 60) / (double)quickest.getDuration();
        List<RealizedWaypoint> waypoints = new LinkedList<>();
        long topologyTime = 0;
        Stop stop = from;
        for (int i = 0; i < quickest.getEdges().size() - 1; i++) {
            Edge edge = quickest.getEdges().get(i);
            topologyTime += edge.getTypicalTime();
            stop = edge.getDestination(stop);
            waypoints.add(new RealizedWaypoint(stop, departure.plusSeconds(60 + Math.round(speedFactor * topologyTime))));
        }

        return waypoints;
    }

    private static int googleTransportTimeDiff(String time1, String time2) {
        String[] split1 = time1.split(":");
        int hour1 = Integer.parseInt(split1[0]);
        int minute1 = Integer.parseInt(split1[1]);
        int second1 = Integer.parseInt(split1[2]);
        String[] split2 = time2.split(":");
        int hour2 = Integer.parseInt(split2[0]);
        int minute2 = Integer.parseInt(split2[1]);
        int second2 = Integer.parseInt(split2[2]);
        return ((hour2 - hour1) * 60 + (minute2 - minute1)) * 60 + (second2 - second1);
    }





    private boolean possibleWithExistingEdges(Edge e, Map<Stop, Set<Edge>> existingTopology) {
        Path quickest = quickest(e.getStop1(), e.getStop2(), e.getTypicalTime(), stop -> { Set<Edge> edges = existingTopology.get(stop); return edges != null ? new LinkedList<>(edges) : Collections.emptyList(); });
        return quickest != null;
    }

    public void recalculateEdges() {
        Map<String, Edge> edges = new HashMap<>();
        long start = System.currentTimeMillis();
        System.out.print("fetching rail routes... ");
        List<Route> railRoutes = routesModel.getByType(100, 199);
        System.out.println(railRoutes.size() + " in " + (System.currentTimeMillis() - start) + " ms");

        System.out.print("extracting edges... ");
        for (Route route : railRoutes) {
            System.out.println(route + " " + route.getShortName());
            List<Trip> trips = tripsModel.getByRoute(route);

            for (Trip trip : trips) {
                String depTime = null;
                String lastStopId = null;
                List<StopTime> stopTimes = trip.getStopTimes();
                for (StopTime stopTime : stopTimes) {
                    if (lastStopId != null) {
                        int seconds = googleTransportTimeDiff(depTime, stopTime.getArrival());
                        addJourney(edges, lastStopId, stopTime.getParentStopId(), seconds);
                    }
                    lastStopId = stopTime.getParentStopId();
                    depTime = stopTime.getDeparture();
                }
            }
        }

        List<Edge> allEdges = new LinkedList<>(edges.values());

        System.out.println("took " + (System.currentTimeMillis() - start) + " ms to extract all edges");
        System.out.println(allEdges.size() + " edges found");

        System.out.println("checking edges...");
        allEdges = allEdges.stream().filter(e -> e.isPrintable()).collect(Collectors.toList());
        System.out.println(allEdges.size() + " edges after checking");

        Collections.sort(allEdges);

        List<Edge> requiredEdges = new LinkedList<>();
        Map<Stop, Set<Edge>> topology = new HashMap<>();
        for (Edge edge : allEdges) {
            if (!possibleWithExistingEdges(edge, topology)) {
                if (!topology.containsKey(edge.getStop1())) {
                    topology.put(edge.getStop1(), new HashSet<>());
                }
                if (!topology.containsKey(edge.getStop2())) {
                    topology.put(edge.getStop2(), new HashSet<>());
                }
                topology.get(edge.getStop1()).add(edge);
                topology.get(edge.getStop2()).add(edge);
                requiredEdges.add(edge);
            }
        }

        System.out.println("saving edges...");
        edgesModel.drop();
        for (Edge edge: requiredEdges) {
            edgesModel.save(edge);
        }
        System.out.println("edges saved");
    }

    private List<Edge> findEdges(long remainingTime, List<Edge> edges, Set<Stop> beenTo, Stop thisStop, Stop destination, Map<Stop, Set<Edge>> existingTopology, boolean debug) {
        if (debug) {
            //System.out.println("To " + destination + " : " + StringUtils.join(stops, " => ") + ", remaining " + remainingTime + " s");
        }
        if (remainingTime < -60) {
            return null;
        }
        if (destination.equals(thisStop)) {
            // success!
            return edges;
        }

        if (!existingTopology.containsKey(thisStop)) {
            return null;
        }
        Set<Edge> possibleEdges = existingTopology.get(thisStop);

        // filter all the edges that lead to places where we've already been
        possibleEdges = possibleEdges.stream().filter(e -> !beenTo.contains(e.getDestination(thisStop))).collect(Collectors.toSet());

        if (possibleEdges.isEmpty()) {
            return null;
        }

        for (Edge edge : possibleEdges) {
            Stop edgeDestination = edge.getDestination(thisStop);
            Set<Stop> newBeenTo = new HashSet<>(beenTo);
            newBeenTo.add(thisStop);
            LinkedList<Edge> newEdges = new LinkedList<>(edges);
            newEdges.add(edge);
            List<Edge> successEdges = findEdges(remainingTime - edge.getTypicalTime(), newEdges, newBeenTo, edgeDestination, destination, existingTopology, debug);
            if (successEdges != null) {
                return successEdges;
            }
        }
        return null;
    }

    private void addJourney(Map<String, Edge> edges, String from, String to, int seconds) {
        if (from.compareTo(to) > 0) {
            String tmp = to;
            to = from;
            from = tmp;
        }
        String key = from + "|" + to;
        Edge edge = edges.get(key);
        if (edge == null) {
            edge = new MongoDbEdge(from, to);
            injector.injectMembers(edge);
            edges.put(key, edge);
        }
        edge.addJourney(seconds);
    }


    private volatile Map<Edge, Set<String>> routeIdsByEdge = null;

    public Set<String> getRouteIdsByEdge(Edge edge) {
        if (routeIdsByEdge == null) {
            routeIdsByEdge = new HashMap<>();
            recalculatePaths();
        }
        return routeIdsByEdge.get(edge);
    }

    public void recalculatePaths() {
        Map<Edge, Set<String>> routeIdsByEdge = new HashMap<>();
        List<Route> railRoutes = routesModel.getByType(100, 199);

        Map<Stop, List<Edge>> edgesLookupTable = new HashMap<>();
        for (Edge edge : edgesModel.getAll()) {
            if (!edgesLookupTable.containsKey(edge.getStop1())) {
                edgesLookupTable.put(edge.getStop1(), new LinkedList<>());
            }
            if (!edgesLookupTable.containsKey(edge.getStop2())) {
                edgesLookupTable.put(edge.getStop2(), new LinkedList<>());
            }
            edgesLookupTable.get(edge.getStop1()).add(edge);
            edgesLookupTable.get(edge.getStop2()).add(edge);
        }

        int done = 0;
        for (Route route : railRoutes) {
            System.out.println((100 * done / railRoutes.size()) + "% " + route + " " + route.getShortName());
            done++;
            List<Trip> trips = tripsModel.getByRoute(route);
            for (Trip trip : trips) {
                List<StopTime> stopTimes = trip.getStopTimes();
                for (int i = 1; i < stopTimes.size(); i++) {
                    Stop from = stopTimes.get(i-1).getStop();
                    Stop to = stopTimes.get(i).getStop();
                    long time = googleTransportTimeDiff(stopTimes.get(i-1).getDeparture(), stopTimes.get(i).getArrival());
                    Path path = quickest(from, to, time * 3 / 2 + 60, stop -> edgesLookupTable.get(stop));
                    for (Edge edge : path.getEdges()) {
                        if (!routeIdsByEdge.containsKey(edge)) {
                            routeIdsByEdge.put(edge, new HashSet<>());
                        }
                        routeIdsByEdge.get(edge).add(route.getRouteId());
                    }
                }
            }
        }
        this.routeIdsByEdge = routeIdsByEdge;
    }
}
