package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.Stop;
import models.GtfsConfigModel;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataController extends GtfsController {
    @Inject
    private GtfsConfigModel gtfsConfigModel;

    public Result stops(Http.Request request, String cc) {
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        List<String> stops = gtfs.getStopsModel().getAll(gtfs).stream().map(Stop::getName).collect(Collectors.toList());
        Collections.sort(stops);
        ArrayNode stopsArray = Json.newArray();
        stops.forEach(s -> stopsArray.add(s));
        return ok(stopsArray.toString()).as("application/json; charset=utf-8");
    }
}
