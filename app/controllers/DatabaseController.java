package controllers;

import biz.Databases;
import biz.Users;
import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
import entities.User;
import models.GtfsConfigModel;
import models.UsersModel;
import play.mvc.Http;
import play.mvc.Result;
import services.MongoDb;
import utils.InputValidationException;
import utils.NotAllowedException;
import utils.NotFoundException;

import java.util.Collections;
import java.util.List;

public class DatabaseController extends GtfsController {
    @Inject
    private UsersModel usersModel;

    @Inject
    private Users users;

    @Inject
    private GtfsConfigModel gtfsConfigModel;

    @Inject
    private MongoDb mongoDb;

    @Inject
    private Injector injector;

    @Inject
    private Databases databases;

    public Result delete(Http.Request request, String cc, String databaseName) {
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        if (gtfs == null) {
            return redirect("/ch");
        }
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        List<String> databases = mongoDb.getTimetableDatabases(gtfs.getCode());
        if (!databases.contains(databaseName)) {
            throw new NotFoundException("Database '" + databaseName + "' not found");
        }

        return ok(views.html.admin.databases.delete.render(request, databaseName, Collections.emptyMap(), user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result deletePost(Http.Request request, String cc, String databaseName) {
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        if (gtfs == null) {
            return redirect("/ch");
        }
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }

        try {
            databases.delete(request, gtfs, databaseName, user);
        } catch (InputValidationException e) {
            return ok(views.html.admin.databases.delete.render(request, databaseName, e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }

        return redirect(routes.DataImportController.listDatabases(cc));
    }
}
