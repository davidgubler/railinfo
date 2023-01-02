package biz;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import entities.mongodb.MongoDbEdge;
import models.EdgesModel;
import models.RoutesModel;
import models.TripsModel;
import utils.NotAllowedException;

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

}
