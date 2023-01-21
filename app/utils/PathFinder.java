package utils;

import com.google.inject.Inject;
import entities.Edge;
import entities.Path;
import entities.Stop;
import entities.realized.RealizedWaypoint;
import geometry.EdgeDirectionComparator;
import models.EdgesModel;
import models.StopsModel;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PathFinder {

    @Inject
    private StopsModel stopsModel;

    @Inject
    private EdgesModel edgesModel;

    private Path quickest(Stop current, Stop to, long timeLimit, Path travelledPath, Map<String, Long> timeFromStart) {
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

        List<? extends Edge> edges = edgesModel.getEdgesFrom(current);
        EdgeDirectionComparator comp = new EdgeDirectionComparator(current, to);
        edges.sort(comp);

        for (Edge tryEdge : edges) {
            Path solution = quickest(tryEdge.getDestination(current), to, timeLimit, new Path(travelledPath, tryEdge), timeFromStart);
            if (solution != null) {
                quickestPath = solution;
                timeLimit = quickestPath.getDuration() - 1; // we're only interested in quicker paths
            }
        }

        return quickestPath;
    }

    private ConcurrentHashMap<String, Path> quickestPaths = new ConcurrentHashMap<>();

    private Path quickest(Stop from, Stop to, long timeLimit) {
        String key = from.getParentStopId() + "|" + to.getParentStopId();
        if (quickestPaths.containsKey(key)) {
            return quickestPaths.get(key);
        }
        long start = System.currentTimeMillis();
        Map<String, Long> cacheMap = new HashMap<>();
        Path quickest = quickest(from, to, timeLimit, new Path(), cacheMap);

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

        quickestPaths.put(key, quickest);
        String reverseKey = to.getParentStopId() + "|" + from.getParentStopId();
        quickestPaths.put(reverseKey, quickest.getReverse());
        return quickest;
    }

    public List<RealizedWaypoint> getIntermediate(Stop from, Stop to, LocalDateTime departure, LocalDateTime arrival) {
        long scheduledSeconds = departure.until(arrival, ChronoUnit.SECONDS);

        Path quickest = quickest(from, to, scheduledSeconds * 2);

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
}
