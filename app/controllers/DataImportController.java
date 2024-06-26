package controllers;

import biz.Importer;
import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
import entities.User;
import models.*;
import play.mvc.*;
import services.MongoDb;
import utils.InputUtils;
import utils.InputValidationException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataImportController extends GtfsController {
    @Inject
    private UsersModel usersModel;

    @Inject
    private Importer importer;

    @Inject
    private MongoDb mongoDb;

    @Inject
    private Injector injector;

    @Inject
    private GtfsConfigModel gtfsConfigModel;

    public Result listDatabases(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        checkDbOptional(user, gtfs);
        List<String> databases = mongoDb.getTimetableDatabases(gtfs.getCode());
        List<GtfsConfig> gtfsConfigs = new LinkedList<>();

        for (String database : databases) {
            GtfsConfig dbGtfs = gtfs.withDatabase(mongoDb, database, gtfsConfigModel);
            injector.injectMembers(dbGtfs);
            gtfsConfigs.add(dbGtfs);
        }
        Collections.sort(gtfsConfigs);
        Collections.reverse(gtfsConfigs);
        return ok(views.html.admin.databases.index.render(request, gtfsConfigs, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result index(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        checkDbOptional(user, gtfs);
        String databaseName = "railinfo-" + gtfs.getCode() + "-" + LocalDate.now();
        return ok(views.html.admin.dataimport.index.render(request, gtfs.getDownloadUrl(), databaseName, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result importGtfsPost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        checkDbOptional(user, gtfs);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String url = InputUtils.trimToNull(data.get("url"));
        String databaseName = InputUtils.trimToNull(data.get("databaseName"));
        try {
            importer.importGtfs(request, gtfs, url, databaseName, user);
        } catch (InputValidationException e ) {
            return ok(views.html.admin.dataimport.index.render(request, url, databaseName, e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }
        return redirect(routes.DataImportController.index(gtfs.getCode()));
    }
}
