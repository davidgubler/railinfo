package controllers;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import models.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TopologyController extends Controller {

    @Inject
    private StopsModel stopsModel;

    @Inject
    private StopTimesModel stopTimesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

    @Inject
    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private Injector injector;


    private Map<String, Edge> edges = new HashMap<>();

    private void addJourney(String from, String to, int seconds) {
        if (from.compareTo(to) > 0) {
            String tmp = to;
            to = from;
            from = tmp;
        }
        String key = from + "|" + to;
        Edge edge = edges.get(key);
        if (edge == null) {
            edge = new Edge(from, to);
            injector.injectMembers(edge);
            edges.put(key, edge);
        }
        edge.addJourney(seconds);
    }


    public Result topology()  {
        long start;/*

        start = System.currentTimeMillis();
        System.out.print("fetching rail routes... ");
        List<Route> railRoutes = routesModel.getByType(100, 199);
        System.out.println(railRoutes.size() + " in " + (System.currentTimeMillis() - start) + " ms");

        long x, addJourneyTime = 0, getStopTimesTime = 0;

        start = System.currentTimeMillis();
        System.out.print("extracting edges... ");
        for (Route route : railRoutes) {
            System.out.println(route);
            List<Trip> trips = tripsModel.getByRoute(route);
            for (Trip trip : trips) {
                String lastStopId = null;
                int depHour = 0, depMinute = 0, depSecond = 0;

                x = System.currentTimeMillis();
                List<StopTime> stopTimes = trip.getStopTimes();
                getStopTimesTime += (System.currentTimeMillis() - x);

                for (StopTime stopTime : stopTimes) {
                    if (lastStopId != null) {
                        String[] split = stopTime.getArrival().split(":");
                        int arrHour = Integer.parseInt(split[0]);
                        int arrMinute = Integer.parseInt(split[1]);
                        int arrSecond = Integer.parseInt(split[2]);
                        int seconds = ((arrHour - depHour) * 60 + arrMinute - depMinute) * 60 + arrSecond - depSecond;
                        x = System.currentTimeMillis();
                        addJourney(lastStopId, stopTime.getParentStopId(), seconds);
                        addJourneyTime += (System.currentTimeMillis() - x);
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

        System.out.println(allEdges.size() + " in " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("addJourneyTime: " + addJourneyTime + " ms");
        System.out.println("getStopTimesTime: " + getStopTimesTime + " ms");
*/
        return ok();
    }



    public Result map(Http.Request request) {
        long start = System.currentTimeMillis();
        System.out.print("fetching rail routes... ");
        List<Route> railRoutes = routesModel.getByType(100, 199);
        System.out.println(railRoutes.size() + " in " + (System.currentTimeMillis() - start) + " ms");

        System.out.print("extracting edges... ");
        for (Route route : railRoutes) {
            System.out.println(route + " " + route.getShortName());
            List<Trip> trips = tripsModel.getByRoute(route);

            if (trips.size() > 4) {
                trips = trips.subList(0, 4);
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
                            addJourney(lastStopId, stopTime.getParentStopId(), seconds);
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

        return ok(views.html.topology.map.render(requiredEdges));
    }




    private boolean possibleWithExistingEdges(Edge e, Map<Stop, Set<Edge>> existingTopology) {
        long time = e.getTypicalTime();
        List<Stop> path = findPath(time, Arrays.asList(e.getStop1()), e.getStop2(), existingTopology, false);
        if (path == null) {
            if ("Uster".equals(e.getStop2().getName())) {
                System.out.println("=== " + e.getStop1().getName() + " to " + e.getStop2() + " is not possible in " + e.getTypicalTime() + " s with existing topology:");
                findPath(time, Arrays.asList(e.getStop1()), e.getStop2(), existingTopology, true);
            }
        } else {
            //System.out.println(" == YES! " + StringUtils.join(path, ", "));
        }
        return path != null;
    }




    private List<Stop> findPath(long remainingTime, List<Stop> stops, Stop destination, Map<Stop, Set<Edge>> existingTopology, boolean debug) {
        if (debug) {
            System.out.println("To " + destination + " : " + StringUtils.join(stops, " => ") + ", remaining " + remainingTime + " s");
        }
        if (remainingTime < -60) {
            return null;
        }
        Stop lastStop = stops.get(stops.size() - 1);
        if (destination.equals(lastStop)) {
            // success!
            return stops;
        }

        if (!existingTopology.containsKey(lastStop)) {
            return null;
        }
        Set<Edge> possibleEdges = existingTopology.get(lastStop);

        // filter all the edges that lead to places where we've already been
        possibleEdges = possibleEdges.stream().filter(e -> !stops.contains(e.getDestination(lastStop))).collect(Collectors.toSet());

        if (possibleEdges.isEmpty()) {
            return null;
        }

        for (Edge edge : possibleEdges) {
            List<Stop> newStops = new LinkedList<>(stops);
            newStops.add(edge.getDestination(lastStop));
            List<Stop> path = findPath(remainingTime - edge.getTypicalTime(), newStops, destination, existingTopology, debug);
            if (path != null) {
                return path;
            }
        }
        return null;
    }
}
