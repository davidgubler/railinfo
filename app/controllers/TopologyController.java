package controllers;

import com.google.inject.Inject;
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

    private class Edge implements Comparable<Edge> {
        private String fromStopId;
        private String toStopId;
        private Map<Integer, Integer> travelTimes = new HashMap<>(); // key are seconds, value is #journeys
        private Integer typicalTime = null;

        public Edge(String fromStopId, String toStopId) {
            this.fromStopId = fromStopId;
            this.toStopId = toStopId;
        }

        public void addJourney(Integer seconds) {
            typicalTime = null;
            // we assume that a stop takes 1min 30s, thus we subtract this
            seconds -= 90;
            if (seconds < 30) {
                // the minimum assumed travel time between stops is 30s
                seconds = 30;
            }
            if (travelTimes.containsKey(seconds)) {
                travelTimes.put(seconds, travelTimes.get(seconds) + 1);
            } else {
                travelTimes.put(seconds, 1);
            }
        }

        public Integer getTypicalTime() {
            if (typicalTime == null) {
                int mostSeconds = 0, mostJourneys = 0;
                for (Map.Entry<Integer, Integer> entry : travelTimes.entrySet()) {
                    if (entry.getValue() > mostJourneys) {
                        mostSeconds = entry.getKey();
                        mostJourneys = entry.getValue();
                    }
                }
                typicalTime = mostSeconds;
            }
            return typicalTime;
        }

        public String getFromStopId() {
            return fromStopId;
        }

        public String getToStopId() {
            return toStopId;
        }

        @Override
        public int compareTo(Edge edge) {
            return getTypicalTime().compareTo(edge.getTypicalTime());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Objects.equals(fromStopId, edge.fromStopId) && Objects.equals(toStopId, edge.toStopId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromStopId, toStopId);
        }
    }

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
        List<Route> railRoutes = routesModel.getByType(100, 199);

        //long start = System.currentTimeMillis();
        for (Route route : railRoutes) {
            List<Trip> trips = tripsModel.getByRoute(route);
            for (Trip trip : trips) {
                String lastStopId = null;
                int depHour = 0, depMinute = 0, depSecond = 0;

                for (StopTime stopTime : trip.getStopTimes()) {
                    if (lastStopId != null) {
                        String[] split = stopTime.getArrival().split(":");
                        int arrHour = Integer.parseInt(split[0]);
                        int arrMinute = Integer.parseInt(split[1]);
                        int arrSecond = Integer.parseInt(split[2]);
                        int seconds = ((arrHour - depHour) * 60 + arrMinute - depMinute) * 60 + arrSecond - depSecond;
                        addJourney(lastStopId, stopTime.getParentStopId(), seconds);
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

        Collections.sort(allEdges);
        Collections.reverse(allEdges);

        System.out.println(allEdges.size() + " edges found");
        int i = 0;
        for (Edge edge : allEdges) {
            breakDownEdge(edge);
        }

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
