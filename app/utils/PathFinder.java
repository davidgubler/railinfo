package utils;

import com.google.inject.Inject;
import entities.Edge;
import entities.Path;
import entities.Stop;
import entities.realized.RealizedWaypoint;
import models.EdgesModel;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PathFinder {

    @Inject
    private EdgesModel edgesModel;

    private Path quickest(Stop from, Stop to, long timeLimit, List<Edge> travelledEdges) {
        if (from.getParentStopId().equals(to.getParentStopId()) && timeLimit >= 0) {
            return new Path();
        }
        if (timeLimit <= 0) {
            return null;
        }


        Edge quickestEdge = null;
        Path quickestPath = null;

        for (Edge tryEdge : edgesModel.getEdgesFrom(from)) {
            if (travelledEdges.contains(tryEdge)) {
                continue;
            }
            List<Edge> newTravelledEdges = new LinkedList<>(travelledEdges);
            newTravelledEdges.add(tryEdge);
            long newTimeLimit = timeLimit - tryEdge.getTypicalTime();
            Path solution = quickest(tryEdge.getDestination(from), to, newTimeLimit, newTravelledEdges);
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
        Path quickest = quickest(from, to, timeLimit, new LinkedList<>());
        quickestPaths.put(key, quickest);
        String reverseKey = to.getParentStopId() + "|" + from.getParentStopId();
        quickestPaths.put(reverseKey, quickest.getReverse());
        return quickest;
    }

    public List<RealizedWaypoint> getIntermediate(Stop from, Stop to, LocalDateTime departure, LocalDateTime arrival) {
        long scheduledSeconds = departure.until(arrival, ChronoUnit.SECONDS);

        Path quickest = quickest(from, to, scheduledSeconds * 3 / 2);

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
