package entities.mongodb;

import com.google.inject.Inject;
import configs.GtfsConfig;
import dev.morphia.utils.IndexType;
import entities.Stop;
import geometry.Point;
import models.StopTimesModel;
import models.StopsModel;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;
import utils.StringUtils;

import java.util.*;

@Entity(value = "stops", useDiscriminator = false)
@Indexes(@Index(fields = @Field(value = "name", type = IndexType.TEXT)))
public class MongoDbStop implements Stop {
    @Id
    private ObjectId _id;

    @Indexed(options = @IndexOptions(unique = true))
    private String stopId;

    @Indexed
    private String name;

    @Indexed
    private String normalizedName;

    private Double lat;

    private Double lng;

    private String type;

    private Integer importance = null;

    private Boolean modified = null;

    @Indexed
    private String parentId;

    @Transient
    private GtfsConfig gtfs;

    @Transient
    @Inject
    private StopsModel stopsModel;

    @Transient
    @Inject
    private StopTimesModel stopTimesModel;

    public MongoDbStop() {
        // dummy constructor for Morphia
    }

    public MongoDbStop(String stopId, String name, Double lat, Double lng) {
        this.stopId = stopId;
        this.name = name;
        this.normalizedName = StringUtils.normalizeName(name);
        this.lat = lat;
        this.lng = lng;
        this.modified = true;
    }

    public MongoDbStop(Map<String, String> data) {
        this.stopId = data.get("stop_id");
        this.name = data.get("stop_name");
        this.normalizedName = StringUtils.normalizeName(this.name);
        this.lat = data.get("stop_lat").isBlank() ? null : Double.parseDouble(data.get("stop_lat"));
        this.lng = data.get("stop_lon").isBlank() ? null : Double.parseDouble(data.get("stop_lon"));
        this.type = data.get("location_type").isBlank() ? null : data.get("location_type");
        this.parentId = data.get("parent_station").isBlank() ? null : data.get("parent_station");
    }

    public void setGtfs(GtfsConfig gtfs) {
        this.gtfs = gtfs;
    }

    public String getId() {
        return _id.toString();
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public String getStopId() {
        return stopId;
    }

    @Override
    public String getNormalizedName() {
        return normalizedName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.normalizedName = StringUtils.normalizeName(name);
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Point getCoordinates() {
        if (lat == null || lng == null) {
            return null;
        }
        return new Point.PointBuilder().withLat(lat).withLng(lng).build();
    }

    public String getType() {
        return type;
    }

    public String getBaseId() {
        return gtfs.extractBaseId(this.getStopId());
    }

    public String getParentId() {
        return parentId;
    }

    public Integer getImportance() {
        if (importance == null) {
            Set<Stop> stops = new HashSet<>(stopsModel.getByName(gtfs, getName()));
            importance = stopTimesModel.getByStops(gtfs, stops).size();
            stopsModel.updateImportance(gtfs, stops, importance);
        }
        return importance;
    }

    public String toString() {
        return "stop:" + name;
    }

    @Override
    public boolean isModified() {
        return modified != null && modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified != null && modified ? true : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoDbStop stop = (MongoDbStop) o;
        return Objects.equals(stopId, stop.stopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopId);
    }

    @Override
    public int compareTo(Stop stop) {
        return name.compareTo(stop.getName());
    }
}
