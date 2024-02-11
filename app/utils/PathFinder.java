package utils;

import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
import entities.*;
import entities.mongodb.MongoDbEdge;
import entities.realized.RealizedWaypoint;
import models.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
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
    private StopTimesModel stopTimesModel;

    @Inject
    private Injector injector;

    private Path quickest(Stop from, Stop to, long timeLimit, Path travelledPath, Map<String, Long> timeFromStart, Function<Stop, List<? extends Edge>> f) {
        if (from.getBaseId().equals(to.getBaseId())) {
            return new Path();
            //throw new IllegalArgumentException("from and to cannot be the same");
        }

        Set<String> visited = new HashSet<>();
        List<Path> paths = new LinkedList<>();

        visited.add(from.getBaseId());
        for (Edge edge : f.apply(from)) {
            if (!visited.contains(edge.getDestination(from).getBaseId())) {
                paths.add(new Path(edge));
            }
        }

        while (!paths.isEmpty()) {
            Path path = null;
            long nextTime = Long.MAX_VALUE;
            for (Path p : paths) {
                long d = p.getDuration();
                if (d < nextTime) {
                    path = p;
                    nextTime = d;
                }
            }

            if (nextTime > timeLimit) {
                return null;
            }

            paths.remove(path);

            Stop stop = path.getDestination(from);
            String stopId = stop.getBaseId();
            if (visited.contains(stopId)) {
                continue;
            }

            if (stopId.equals(to.getBaseId())) {
                return path;
            }

            visited.add(stopId);
            for (Edge edge : f.apply(stop)) {
                if (!visited.contains(edge.getDestination(stop).getBaseId())) {
                    Path addPath = new Path(path, edge);
                    paths.add(addPath);
                }
            }
        }

        return null;
    }

    private ConcurrentHashMap<GtfsConfig, ConcurrentHashMap<String, Path>> globalPathsCache = new ConcurrentHashMap<>(1);

    private Map<String, Path> getGlobalPathsCache(GtfsConfig gtfs) {
        if (!globalPathsCache.containsKey(gtfs)) {
            globalPathsCache.put(gtfs, new ConcurrentHashMap<>(1));
        }
        return globalPathsCache.get(gtfs);
    }

    private Path quickest(Stop from, Stop to, long timeLimit, Function<Stop, List<? extends Edge>> f, Map<String, Path> cache) {
        String key = from.getBaseId() + "|" + to.getBaseId();
        if (cache.containsKey(key)) {
            Path path = cache.get(key);
            return path.getDuration() <= timeLimit ? path : null;
        }
        Map<String, Long> cacheMap = new HashMap<>();
        Path quickest = quickest(from, to, timeLimit, new Path(), cacheMap, f);
        if (quickest == null) {
            return null;
        }
        cache.put(key, quickest);
        String reverseKey = to.getBaseId() + "|" + from.getBaseId();
        cache.put(reverseKey, quickest.getReverse());
        return quickest;
    }


    public List<RealizedWaypoint> getIntermediate(GtfsConfig gtfs, Stop from, Stop to, LocalDateTime departure, LocalDateTime arrival) {
        long scheduledSeconds = departure.until(arrival, ChronoUnit.SECONDS);

        Path quickest = quickest(from, to, scheduledSeconds * 2, stop -> edgesModel.getEdgesFrom(gtfs, stop), getGlobalPathsCache(gtfs));

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
            waypoints.add(new RealizedWaypoint(stop, departure.plusSeconds(60 + Math.round(speedFactor * topologyTime)), gtfs));
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

    private boolean possibleWithExistingEdges(Edge e, Map<Stop, Set<Edge>> existingTopology, Map<String, Path> cache) {
        Path quickest = quickest(e.getStop1(), e.getStop2(), e.getTypicalTime(), stop -> { Set<Edge> edges = existingTopology.get(stop); return edges != null ? new LinkedList<>(edges) : Collections.emptyList(); }, cache);
        return quickest != null;
    }

    public void recalculateEdges(GtfsConfig gtfs) {
        Map<String, Edge> edges = new HashMap<>();
        Map<String, Path> pathsCache = new HashMap<>();
        long start = System.currentTimeMillis();
        System.out.print("fetching rail routes... ");
        List<? extends Route> railRoutes = gtfs.getRailRoutes(routesModel);
        System.out.println(railRoutes.size() + " in " + (System.currentTimeMillis() - start) + " ms");

        System.out.println("extracting edges... ");
        for (Route route : railRoutes) {
            System.out.println(route + " " + route.getShortName());
            List<? extends Trip> trips = gtfs.getRailTripsByRoute(tripsModel, route);
            for (Trip trip : trips) {
                String depTime = null;
                String lastStopId = null;
                List<? extends StopTime> stopTimes = trip.getStopTimes();
                for (StopTime stopTime : stopTimes) {
                    if (lastStopId != null) {
                        int seconds = googleTransportTimeDiff(depTime, stopTime.getArrival());
                        addJourney(gtfs, edges, lastStopId, stopTime.getStopBaseId(), seconds);
                    }
                    lastStopId = stopTime.getStopBaseId();
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
        // we want to keep all edges that have been manually modified
        for (Edge edge : edgesModel.getModified(gtfs)) {
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
        for (Edge edge : allEdges) {
            if (!possibleWithExistingEdges(edge, topology, pathsCache)) {
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

        System.out.println("saving " + requiredEdges.size() + " edges...");
        edgesModel.drop(gtfs);
        for (Edge edge: requiredEdges) {
            edgesModel.save(gtfs, edge);
        }
        System.out.println("edges saved");
        clearCache(gtfs);
        System.out.println("caches cleared");
    }


    private void addJourney(GtfsConfig gtfs, Map<String, Edge> edges, String from, String to, int seconds) {
        if (from.compareTo(to) > 0) {
            String tmp = to;
            to = from;
            from = tmp;
        }
        String key = from + "|" + to;
        Edge edge = edges.get(key);
        if (edge == null) {
            MongoDbEdge mongoDbEdge = new MongoDbEdge(stopsModel, gtfs, from, to);
            mongoDbEdge.setGtfs(gtfs);
            edge = mongoDbEdge;
            edges.put(key, edge);
        }
        edge.addJourney(gtfs.subtractStopTime(seconds));
    }

    private ConcurrentHashMap<GtfsConfig, Map<Edge, Set<String>>> routeIdsByEdge = new ConcurrentHashMap(1);

    public Set<String> getRouteIdsByEdge(GtfsConfig gtfs, Edge edge) {
        if (routeIdsByEdge.get(gtfs) == null) {
            recalculatePaths(gtfs);
        }
        Set<String> routes = routeIdsByEdge.get(gtfs).get(edge);
        return routes == null ? Collections.emptySet() : routes;
    }

    public void recalculatePaths(GtfsConfig gtfs) {
        List<? extends Route> railRoutes = gtfs.getRailRoutes(routesModel);
        Map<String, List<Edge>> edgesLookupTable = new HashMap<>();
        for (Edge edge : edgesModel.getAll(gtfs, false)) {
            if (!edgesLookupTable.containsKey(edge.getStop1Id())) {
                edgesLookupTable.put(edge.getStop1Id(), new LinkedList<>());
            }
            if (!edgesLookupTable.containsKey(edge.getStop2Id())) {
                edgesLookupTable.put(edge.getStop2Id(), new LinkedList<>());
            }
            edgesLookupTable.get(edge.getStop1Id()).add(edge);
            edgesLookupTable.get(edge.getStop2Id()).add(edge);
        }

        long total = System.currentTimeMillis();
        long totalDb = 0;
        long totalQuickest = 0;

        int cpus = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(cpus, cpus, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
        List<Map<Edge, Set<String>>> parallelResults = new LinkedList<>();

        for (Route route : railRoutes) {
            Map<Edge, Set<String>> routeIdsByEdge = new HashMap<>();
            parallelResults.add(routeIdsByEdge);
            executor.execute(() -> {
                List<? extends Trip> trips = gtfs.getRailTripsByRoute(tripsModel, route);
                Map<Trip, List<StopTime>> stopTimesForAllTrips = stopTimesModel.getByTrips(gtfs, trips);
                for (Trip trip : trips) {
                    List<StopTime> stopTimes = stopTimesForAllTrips.get(trip);
                    for (int i = 1; i < stopTimes.size(); i++) {
                        Stop from = stopTimes.get(i-1).getStop();
                        Stop to = stopTimes.get(i).getStop();
                        long time = googleTransportTimeDiff(stopTimes.get(i-1).getDeparture(), stopTimes.get(i).getArrival());

                        Path path;
                        long quickestStart = System.currentTimeMillis();
                        path = quickest(from, to, time * 3 / 2 + 60, stop -> edgesLookupTable.get(stop.getBaseId()), getGlobalPathsCache(gtfs));
                        if (System.currentTimeMillis() - quickestStart > 50) {
                            System.out.println("==> pathfinder slow for " + from.getName() + " - " + to.getName());
                        }
                        for (Edge edge : path.getEdges()) {
                            if (!routeIdsByEdge.containsKey(edge)) {
                                routeIdsByEdge.put(edge, new HashSet<>());
                            }
                            routeIdsByEdge.get(edge).add(route.getRouteId());
                        }
                    }
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                // nothing
            }
            int pct = (100 * (railRoutes.size() - executor.getQueue().size())) / railRoutes.size();
            System.out.println("Recalculating paths " + pct + "%");
        }

        // consolidate the many result maps into a single result map
        Map<Edge, Set<String>> routeIdsByEdge = new HashMap<>();
        for (Map<Edge, Set<String>> routeIdsByEdgeByRoute : parallelResults) {
            for (Map.Entry<Edge, Set<String>> entry : routeIdsByEdgeByRoute.entrySet()) {
                if (!routeIdsByEdge.containsKey(entry.getKey())) {
                    routeIdsByEdge.put(entry.getKey(), entry.getValue());
                } else {
                    routeIdsByEdge.get(entry.getKey()).addAll(entry.getValue());
                }
            }
        }

        total = System.currentTimeMillis() - total;
        System.out.println("total " + total + " ms, db " + totalDb + " ms, pathfinder " + totalQuickest + " ms, unaccounted " + (total - totalDb - totalQuickest) + " ms");
        this.routeIdsByEdge.put(gtfs, routeIdsByEdge);
    }

    public void clearCache(GtfsConfig gtfs) {
        globalPathsCache.put(gtfs, new ConcurrentHashMap<>(1));
        routeIdsByEdge.remove(gtfs);
    }
}
