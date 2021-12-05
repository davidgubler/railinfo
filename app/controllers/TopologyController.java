package controllers;

import com.google.inject.Inject;
import entities.Edge;
import entities.Route;
import entities.StopTime;
import entities.Trip;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.*;

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



    Map<String, List<Edge>> edgesFromStop = new HashMap<>();

    private void addJourney(String from, String to, int seconds) {
        if (!edgesFromStop.containsKey(from)) {
            edgesFromStop.put(from, new LinkedList<>());
        }
        Edge edge = edgesFromStop.get(from).stream().filter(e -> e.getToStopId().equals(to)).findFirst().orElse(null);
        if (edge == null) {
            edge = new Edge(from, to);
            edgesFromStop.get(from).add(edge);
        }
        edge.addJourney(seconds);
    }



    private void breakDownEdge(Edge edge) {
        // allow for 10% more time
        System.out.println("trying to break down " + stopsModel.getById(edge.getFromStopId()) + " - " + stopsModel.getById(edge.getToStopId()));
        Set<Edge> ignore = new HashSet<>();
        ignore.add(edge);
        List<Edge> brokenDown = findPath(edge.getFromStopId(), edge.getToStopId(), edge.getTypicalTime() * 110 / 100, ignore);
        if (brokenDown != null) {
            System.out.println("Edge " + stopsModel.getById(edge.getFromStopId()) + " - " + stopsModel.getById(edge.getToStopId()) + " in " + edge.getTypicalTime() + " s could be broken down to:");
            for (Edge e : brokenDown) {
                System.out.println(" * " + stopsModel.getById(e.getFromStopId()) + " - " + stopsModel.getById(e.getToStopId()) + " " + e.getTypicalTime() + " s" );
            }
        }
    }

    private List<Edge> findPath(String from, String to, int secondsLeft, Set<Edge> ignore) {
        for (Edge e : edgesFromStop.get(from)) {
            if (ignore.contains(e)) {
                continue;
            }
            if (e.getTypicalTime() > secondsLeft) {
                // not enough time to take the edge
                continue;
            }
            if (e.getToStopId().equals(to)) {
                // edge leads to destination, yay!
                return Arrays.asList(e);
            }
            HashSet<Edge> nextIgnore = new HashSet<>(ignore);
            nextIgnore.add(e);
            List<Edge> nextEdges = findPath(e.getToStopId(), to, secondsLeft - e.getTypicalTime(), nextIgnore);
            if (nextEdges != null) {
                List<Edge> edges = new LinkedList<>();
                edges.add(e);
                edges.addAll(nextEdges);
                return edges;
            }
        }
        return null;
    }



    public Result topology()  {
        long start;

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
        List<Edge> allEdges = new LinkedList<>();
        for (Map.Entry<String, List<Edge>> entry : edgesFromStop.entrySet()) {
            allEdges.addAll(entry.getValue());
        }
        System.out.println(allEdges.size() + " in " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("addJourneyTime: " + addJourneyTime + " ms");
        System.out.println("getStopTimesTime: " + getStopTimesTime + " ms");

/*
        Collections.sort(allEdges);
        Collections.reverse(allEdges);

        System.out.println(allEdges.size() + " edges found");
        int i = 0;
        for (Edge edge : allEdges) {
            breakDownEdge(edge);
        }*/

        //System.out.println("took " + (System.currentTimeMillis() - start) + " ms");

        /*for (Map.Entry<String, List<Edge>> entry : edgesFromStop.entrySet()) {
            System.out.print("From " + stopsModel.getById(entry.getKey()) + ":");
            for ( Edge edge : entry.getValue()) {
                System.out.print(" " + stopsModel.getById(edge.getDestinationStopId()) + " " + edge.getTypicalTime() + "s");
            }
            System.out.println();
        }*/

        //System.out.println("took " + (System.currentTimeMillis() - start) + " ms");

        return ok();
    }
}
