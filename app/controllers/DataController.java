package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import entities.Stop;
import models.StopsModel;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataController extends Controller {
    @Inject
    private StopsModel stopsModel;

    public Result stops(Http.Request request) {
        List<String> stops = stopsModel.getAll().stream().map(Stop::getName).collect(Collectors.toList());
        Collections.sort(stops);
        ArrayNode stopsArray = Json.newArray();
        stops.forEach(s -> stopsArray.add(s));
        return ok(stopsArray.toString()).as("application/json; charset=utf-8");
    }
}
