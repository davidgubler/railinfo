package controllers;

import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.User;
import models.GtfsConfigModel;
import models.UsersModel;
import play.mvc.*;

public class HomeController extends Controller {
    @Inject
    private UsersModel usersModel;

    @Inject
    private GtfsConfigModel gtfsConfigModel;

    public Result index(Http.Request request, String cc) {
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        if (gtfs == null) {
            return redirect("/ch");
        }
        User user = usersModel.getFromRequest(request);
        return ok(views.html.index.render(request, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }
}
