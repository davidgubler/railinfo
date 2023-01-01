package controllers;

import com.google.inject.Inject;
import entities.User;
import models.UsersModel;
import play.mvc.*;

public class HomeController extends Controller {
    @Inject
    private UsersModel usersModel;

    public Result index(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        return ok(views.html.index.render(request, user));
    }

}
