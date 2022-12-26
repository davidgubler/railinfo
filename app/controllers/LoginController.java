package controllers;

import biz.Login;
import biz.Topology;
import com.google.inject.Inject;
import entities.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.InputUtils;
import utils.InputValidationException;

import java.util.Map;

public class LoginController extends Controller {
    @Inject
    private Login login;

    public Result login(Http.Request request) {
        return ok(views.html.login.login.render(request, null, null, InputUtils.NOERROR));
    }

    public Result loginPost(Http.Request request) {
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String email = InputUtils.trimToNull(data.get("email"));
        String password = InputUtils.trimToNull(data.get("password"));
        try {
            User user = login.login(email, password);
            return redirect(routes.HomeController.index());
        } catch (InputValidationException e) {
            return ok(views.html.login.login.render(request, email, password, e.getErrors()));
        }
    }
}
