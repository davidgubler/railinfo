package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.Stop;
import models.StopsModel;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.MongoDb;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataController extends Controller {
    @Inject
    private StopsModel stopsModel;

    @Inject
    private MongoDb mongoDb;

    public Result stops(Http.Request request, String cc) {
        GtfsConfig gtfs = mongoDb.getLatest(cc);
        List<String> stops = stopsModel.getAll(gtfs).stream().map(Stop::getName).collect(Collectors.toList());
        Collections.sort(stops);
        ArrayNode stopsArray = Json.newArray();
        stops.forEach(s -> stopsArray.add(s));
        return ok(stopsArray.toString()).as("application/json; charset=utf-8");
    }
}
