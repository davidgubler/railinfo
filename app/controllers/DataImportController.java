package controllers;

import biz.Importer;
import com.google.inject.Inject;
import entities.User;
import models.*;
import play.mvc.*;
import services.MongoDb;
import utils.InputUtils;
import utils.InputValidationException;
import utils.NotAllowedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DataImportController extends Controller {
    @Inject
    private UsersModel usersModel;

    @Inject
    private Importer importer;

    @Inject
    private MongoDb mongoDb;

    public Result listDatabases(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        List<String> databases = mongoDb.getTimetableDatabases("ch");
        return ok(views.html.admin.databases.index.render(request, databases, user));
    }

    public Result index(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        String databaseName = "railinfo-ch-" + LocalDate.now();
        return ok(views.html.admin.dataimport.index.render(request, "https://opentransportdata.swiss/de/dataset/timetable-2023-gtfs2020/permalink", databaseName, InputUtils.NOERROR, user));
    }

    public Result importGtfsPost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String url = InputUtils.trimToNull(data.get("url"));
        String databaseName = InputUtils.trimToNull(data.get("databaseName"));
        try {
            importer.importGtfs(request, url, databaseName, user);
        } catch (InputValidationException e ) {
            return ok(views.html.admin.dataimport.index.render(request, url, databaseName, e.getErrors(), user));
        }
        return redirect(routes.DataImportController.index());
    }
}
