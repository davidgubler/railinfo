package utils;

import com.google.inject.Inject;
import entities.Edge;
import entities.Path;
import entities.Stop;
import entities.realized.RealizedWaypoint;
import geometry.EdgeDirectionComparator;
import models.EdgesModel;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PathFinder {

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

        if (current.getParentStopId().equals(to.getParentStopId()) && timeLimit >= 0) {
            return new Path();
        }
        if (timeLimit <= 0) {
            return null;
        }

        Edge quickestEdge = null;
        Path quickestPath = null;

        List<? extends Edge> edges = edgesModel.getEdgesFrom(current);
        EdgeDirectionComparator comp = new EdgeDirectionComparator(current, to);
        edges.sort(comp);

        for (Edge tryEdge : edges) {
            Path newTravelledPath = new Path(travelledPath, tryEdge);
            long newTimeLimit = timeLimit - tryEdge.getTypicalTime();
            Path solution = quickest(tryEdge.getDestination(current), to, newTimeLimit, newTravelledPath, timeFromStart);
            if (solution != null) {
                quickestPath = solution;
                quickestEdge = tryEdge;
                timeLimit = quickestPath.getDuration() - 1; // we're only interested in quicker paths
            }
        }

        if (quickestPath == null) {
            return null;
        }

        return new Path(quickestEdge, quickestPath);
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
        //System.out.println("path finding took " + (System.currentTimeMillis() - start) + " ms and visited " + cacheMap.size() + " stations");
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
