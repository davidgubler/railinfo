package entities.mongodb;

import com.google.inject.Inject;
import configs.GtfsConfig;
import dev.morphia.annotations.*;
import entities.Edge;
import entities.Stop;
import geometry.Point;
import geometry.PolarCoordinates;
import models.StopsModel;
import org.bson.types.ObjectId;

import java.util.*;

@Entity(value = "edges", noClassnameStored = true)
public class MongoDbEdge implements Edge, Comparable<Edge> {
    @Id
    private ObjectId _id;

    @Indexed
    private String stop1Id;
    @Indexed
    private String stop2Id;
    private Integer typicalTime = 0;

    @Indexed
    private Double bbNorth = null;
    @Indexed
    private Double bbSouth = null;
    @Indexed
    private Double bbEast = null;
    @Indexed
    private Double bbWest = null;

    private Boolean modified = null;

    @Transient
    private GtfsConfig gtfs;

    @Transient
    private Stop stop1;
    @Transient
    private Stop stop2;

    @Transient
    private Map<Integer, Integer> travelTimes = new HashMap<>(); // key are seconds, value is #journeys

    @Transient
    @Inject
    private StopsModel stopsModel;

    public MongoDbEdge() {
        // dummy constructor for morphia
    }

    public MongoDbEdge(StopsModel stopsModel, GtfsConfig gtfs, String stop1Id, String stop2Id) {
        this.stopsModel = stopsModel;
        this.gtfs = gtfs;
        this.stop1Id = stop1Id;
        this.stop2Id = stop2Id;
        recalculateBoundingBox();
    }

    public MongoDbEdge(StopsModel stopsModel, GtfsConfig gtfs, String stop1Id, String stop2Id, Integer typicalTime, Boolean modified) {
        this.stopsModel = stopsModel;
        this.gtfs = gtfs;
        this.stop1Id = stop1Id;
        this.stop2Id = stop2Id;
        this.typicalTime = typicalTime;
        setModified(modified);
        recalculateBoundingBox();
    }

    public void setGtfs(GtfsConfig gtfs) {
        this.gtfs = gtfs;
    }

    @Override
    public String getId() {
        return _id.toString();
    }

    @Override
    public String getIdReverse() {
        return "-" + getId();
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public void addJourney(Integer seconds) {
        // we assume that a stop takes 1 min, thus we subtract this
        seconds -= 60;
        if (seconds < 30) {
            // the minimum assumed travel time between stops is 30s
            seconds = 30;
        }
        if (travelTimes.containsKey(seconds)) {
            travelTimes.put(seconds, travelTimes.get(seconds) + 1);
        } else {
            travelTimes.put(seconds, 1);
        }

        typicalTime = calculateTypicalTime();
    }

    @Override
    public Integer getTypicalTime() {
        return typicalTime;
    }

    public void setTypicalTime(int typicalTime) {
        this.typicalTime = typicalTime;
    }

    private Integer calculateTypicalTime() {
        int smallest = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> entry : travelTimes.entrySet()) {
            if (entry.getKey() < smallest) {
                smallest = entry.getKey();
            }
        }
        return smallest < 30 ? 30 : smallest;
    }

    @Override
    public String getStop1Id() {
        return stop1Id;
    }

    @Override
    public Stop getStop1() {
        if (stop1 == null) {
            stop1 = stopsModel.getByStopId(gtfs, stop1Id);
        }
        return stop1;
    }

    @Override
    public String getStop2Id() {
        return stop2Id;
    }

    @Override
    public Stop getStop2() {
        if (stop2 == null) {
            stop2 = stopsModel.getByStopId(gtfs, stop2Id);
        }
        return stop2;
    }

    @Override
    public Stop getDestination(Stop from) {
        if (getStop1Id().equals(from.getBaseId())) {
            return getStop2();
        }
        if (getStop2Id().equals(from.getBaseId())) {
            return getStop1();
        }
        return null;
    }

    @Override
    public Point getStop1Coordinates() {
        return getStop1() == null ? null : getStop1().getCoordinates();
    }

    @Override
    public Point getStop2Coordinates() {
        return getStop2() == null ? null : getStop2().getCoordinates();
    }

    @Override
    public boolean isPrintable() {
        return getStop1Coordinates() != null && getStop2Coordinates() != null;
    }

    @Override
    public boolean isModified() {
        return modified != null && modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified != null && modified ? true : null;
    }

    public void recalculateBoundingBox() {
        List<Point> boundingBox = getBoundingBox();
        if (boundingBox.isEmpty()) {
            return;
        }
        bbEast = boundingBox.get(0).getLng();
        bbWest = boundingBox.get(0).getLng();
        bbNorth = boundingBox.get(0).getLat();
        bbSouth = boundingBox.get(0).getLat();
        for (int i = 1; i < boundingBox.size(); i++) {
            Point p = boundingBox.get(i);
            bbEast = Math.max(bbEast, p.getLng());
            bbWest = Math.min(bbWest, p.getLng());
            bbNorth = Math.max(bbNorth, p.getLat());
            bbSouth = Math.min(bbSouth, p.getLat());
        }
    }

    public List<Point> getBoundingBox() {
        Point p1 = getStop1Coordinates();
        Point p2 = getStop2Coordinates();
        if (p1 == null || p2 == null) {
            return Collections.emptyList();
        }
        List<Point> box = new LinkedList<>();
        double distance = PolarCoordinates.distanceKm(getStop1Coordinates(), getStop2Coordinates());
        double extraDistance = Math.min(distance / 3, 3);

        double maxLat = Math.max(p1.getLat(), p2.getLat());
        double minLat = Math.min(p1.getLat(), p2.getLat());
        double maxLng = Math.max(p1.getLng(), p2.getLng());
        double minLng = Math.min(p1.getLng(), p2.getLng());
        Point northEast = new Point.PointBuilder().withLat(maxLat).withLng(maxLng).build();
        box.add(PolarCoordinates.goNorth(PolarCoordinates.goEast(northEast, extraDistance), extraDistance));

        Point southEast = new Point.PointBuilder().withLat(minLat).withLng(maxLng).build();
        box.add(PolarCoordinates.goNorth(PolarCoordinates.goEast(southEast, extraDistance), -extraDistance));

        Point southWest = new Point.PointBuilder().withLat(minLat).withLng(minLng).build();
        box.add(PolarCoordinates.goNorth(PolarCoordinates.goEast(southWest, -extraDistance), -extraDistance));

        Point northWest = new Point.PointBuilder().withLat(maxLat).withLng(minLng).build();
        box.add(PolarCoordinates.goNorth(PolarCoordinates.goEast(northWest, -extraDistance), extraDistance));

        return box;
    }

    @Override
    public int compareTo(Edge edge) {
        return getTypicalTime().compareTo(edge.getTypicalTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoDbEdge edge = (MongoDbEdge) o;
        return Objects.equals(stop1Id, edge.stop1Id) && Objects.equals(stop2Id, edge.stop2Id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stop1Id, stop2Id);
    }

    @Override
    public String toString() {
        return getStop1().getName() + "[" + getStop1().getBaseId() + "] ---" + getTypicalTime() + "s--> " + getStop2().getName() + "[" + getStop2().getBaseId() + "]";
    }

    @Override
    public String toString(Stop from) {
        return from.getName() + "[" + from.getBaseId() + "] ---" + getTypicalTime() + "s--> " + getDestination(from).getName() + "[" + getDestination(from).getBaseId() + "]";
    }

    @Override
    public Double getSpread(Point point) {
        if (getStop1Coordinates() == null || getStop2Coordinates() == null || point == null) {
            return null;
        }
        double bearing1 = PolarCoordinates.bearingDegrees(point, getStop1Coordinates());
        double bearing2 = PolarCoordinates.bearingDegrees(point, getStop2Coordinates());
        return PolarCoordinates.bearingDiff(bearing1, bearing2);
    }
}