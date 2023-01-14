package biz;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import entities.mongodb.MongoDbEdge;
import entities.realized.RealizedStopTime;
import entities.realized.RealizedWaypoint;
import models.EdgesModel;
import models.RoutesModel;
import models.TripsModel;
import utils.NotAllowedException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Topology {
    @Inject
    private EdgesModel edgesModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private Injector injector;

    public void edgeUpdate(Edge edge, int time, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.update(edge, time);

        // LOG
    }

    public void edgeDelete(Edge edge, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.delete(edge);

        // LOG
    }

    public void recalculate(User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        recalculate();

        // LOG
    }




    private boolean possibleWithExistingEdges(Edge e, Map<Stop, Set<Edge>> existingTopology) {
        List<Edge> edges = findEdges(e.getTypicalTime(), new LinkedList<>(), new HashSet<>(), e.getStop1(), e.getStop2(), existingTopology, false);
        return edges != null;
    }

    private void recalculate() {
        Map<String, Edge> edges = new HashMap<>();
        long start = System.currentTimeMillis();
        System.out.print("fetching rail routes... ");
        List<Route> railRoutes = routesModel.getByType(100, 199);
        System.out.println(railRoutes.size() + " in " + (System.currentTimeMillis() - start) + " ms");

        System.out.print("extracting edges... ");
        for (Route route : railRoutes) {
            System.out.println(route + " " + route.getShortName());
            List<Trip> trips = tripsModel.getByRoute(route);

            if (trips.size() > 20) {
                trips = trips.subList(0, 20);
            }

            for (Trip trip : trips) {
                String lastStopId = null;
                int depHour = 0, depMinute = 0, depSecond = 0;

                List<StopTime> stopTimes = trip.getStopTimes();
                for (StopTime stopTime : stopTimes) {
                    if (lastStopId != null) {
                        String[] split = stopTime.getArrival().split(":");
                        int arrHour = Integer.parseInt(split[0]);
                        int arrMinute = Integer.parseInt(split[1]);
                        int arrSecond = Integer.parseInt(split[2]);
                        int seconds = ((arrHour - depHour) * 60 + arrMinute - depMinute) * 60 + arrSecond - depSecond;
                        if (seconds < 3600) {
                            addJourney(edges, lastStopId, stopTime.getParentStopId(), seconds);
                        }
                    }
                    lastStopId = stopTime.getParentStopId();
                    String[] split = stopTime.getDeparture().split(":");
                    depHour = Integer.parseInt(split[0]);
                    depMinute = Integer.parseInt(split[1]);
                    depSecond = Integer.parseInt(split[2]);
                }
            }
        }


        List<Edge> allEdges = new LinkedList<>(edges.values());

        System.out.println("took " + (System.currentTimeMillis() - start) + " ms to extract all edges");
        System.out.println(allEdges.size() + " edges found");

        System.out.println("checking edges...");
        allEdges = allEdges.stream().filter(e -> e.isPrintable()).collect(Collectors.toList());
        System.out.println("done");

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

        edgesModel.drop();
        for (Edge edge: requiredEdges) {
            edgesModel.save(edge);
        }

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



    private List<Edge> quickest(Stop from, Stop to, List<Stop> atStops, Map<Stop, Integer> times) {
        /*List<Edge> edges = new LinkedList<>();
        for (Stop stop : atStops) {
            edges.addAll(edgesModel.getEdgesFrom(stop));
        }

        for(Edge edge : edges) {

        }
        */
        //for (Edge edge : edgesModel.getEdgesFrom(from))
        return new LinkedList<>();
    }

    private long edgeSum(List<Edge> edges) {
        if (edges == null) {
            return Long.MAX_VALUE;
        }
        long sum = 0l;
        for (Edge edge : edges) {
            sum+=edge.getTypicalTime();
        }
        return sum;
    }



    // public:
    // List<Edge> quickest(Stop from, Stop to, long timeLimit)

    // internal:
    // List<Edge> quickest(Stop from, Stop to, long timeLimit, Set<Edge> blacklistedEdges)


    private static class Path {
        List<Edge> edges = new LinkedList<>();
        long duration = 0l;

        public Path() {
        }

        public Path(Edge edge, Path path) {
            edges.add(edge);
            edges.addAll(path.edges);
            duration = edge.getTypicalTime() + path.duration;
        }

        public String print(Stop start) {
            String str = start.getName();
            for (Edge edge : edges) {
                start = edge.getDestination(start);
                str += " -> " + start;
            }
            return str;
        }
    }

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
            /*if (to.getName().equals("Zug")) {
                for (Edge e : travelledEdges) {
                    System.out.print("  ");
                }
                System.out.println(tryEdge.getDestination(from) + " " + (timeLimit - tryEdge.getTypicalTime()));
            }*/
            List<Edge> newTravelledEdges = new LinkedList<>(travelledEdges);
            newTravelledEdges.add(tryEdge);
            long newTimeLimit = timeLimit - tryEdge.getTypicalTime();
            Path solution = quickest(tryEdge.getDestination(from), to, newTimeLimit, newTravelledEdges);
            if (solution != null) {
                quickestPath = solution;
                quickestEdge = tryEdge;
                timeLimit = quickestPath.duration - 1; // we're only interested in quicker paths
            }
        }

        if (quickestPath == null) {
            return null;
        }

        return new Path(quickestEdge, quickestPath);
    }

    private Path quickest(Stop from, Stop to, long timeLimit) {
        return quickest(from, to, timeLimit, new LinkedList<>());
    }

    public List<RealizedWaypoint> getIntermediate(Stop from, Stop to, LocalDateTime departure, LocalDateTime arrival) {
        long scheduledSeconds = departure.until(arrival, ChronoUnit.SECONDS);

        //System.out.println("searching from " + from + "[" + from.getParentStopId() + "] to " + to + "[" + to.getParentStopId() + "] in " + scheduledSeconds + "s");
        Path quickest = quickest(from, to, scheduledSeconds * 3 / 2);
        /*System.out.println(quickest == null ? "no path found" : quickest.print(from));
        System.out.println("scheduled duration " + scheduledSeconds + "s, topology duration " + quickest.duration + "s");
        System.out.println("");*/

        if (quickest == null) {
             return new LinkedList<>();
        }

        double speedFactor = (double)(scheduledSeconds - 60) / (double)quickest.duration;
        List<RealizedWaypoint> waypoints = new LinkedList<>();
        long topologyTime = 0;
        Stop stop = from;
        for (int i = 0; i < quickest.edges.size() - 1; i++) {
            Edge edge = quickest.edges.get(i);
            topologyTime += edge.getTypicalTime();
            stop = edge.getDestination(stop);
            waypoints.add(new RealizedWaypoint(stop, departure.plusSeconds(60 + Math.round(speedFactor * topologyTime))));
        }

        return waypoints;
    }
}
