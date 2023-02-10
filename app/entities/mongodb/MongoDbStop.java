package entities.mongodb;

import com.google.inject.Inject;
import dev.morphia.utils.IndexType;
import entities.Stop;
import models.StopTimesModel;
import models.StopsModel;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity(value = "stops", noClassnameStored = true)
@Indexes(@Index(fields = @Field(value = "name", type = IndexType.TEXT)))
public class MongoDbStop implements Stop {
    @Id
    private ObjectId _id;

    @Indexed(options = @IndexOptions(unique = true))
    private String stopId;

    @Indexed
    private String name;

    private Double lat;

    private Double lng;

    private String type;

    private Integer importance = null;

    private Boolean modified = null;

    @Indexed
    private String parentId;

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
        this.lat = lat;
        this.lng = lng;
        this.modified = true;
    }

    public MongoDbStop(Map<String, String> data) {
        this.stopId = data.get("stop_id");
        this.name = data.get("stop_name");
        this.lat = data.get("stop_lat").isBlank() ? null : Double.parseDouble(data.get("stop_lat"));
        this.lng = data.get("stop_lon").isBlank() ? null : Double.parseDouble(data.get("stop_lon"));
        this.type = data.get("location_type").isBlank() ? null : data.get("location_type");
        this.parentId = data.get("parent_station").isBlank() ? null : data.get("parent_station");
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getType() {
        return type;
    }

    public String getBaseId() {
        String baseId = stopId.split(":")[0];
        // base ID can contain stuff like "Parent" or "P"
        baseId = baseId.replaceAll("[^0-9]", "");
        return baseId;
    }

    public String getParentId() {
        return parentId;
    }

    public Integer getImportance() {
        if (importance == null) {
            Set<Stop> stops = new HashSet<>(stopsModel.getByName(this.getName()));
            importance = stopTimesModel.getByStops(stops).size();
            stopsModel.updateImportance(stops, importance);
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
