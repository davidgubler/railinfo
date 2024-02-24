package controllers;

import biz.Login;
import biz.Users;
import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.Session;
import entities.User;
import models.GtfsConfigModel;
import models.UsersModel;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Generator;
import utils.InputUtils;
import utils.InputValidationException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class LoginController extends Controller {

    @Inject
    private Users users;

    @Inject
    private UsersModel usersModel;

    @Inject
    private Login login;

    @Inject
    private GtfsConfigModel gtfsConfigModel;

    public Result login(Http.Request request, String cc) {
        users.ensureAdmin(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        if (gtfs == null) {
            return redirect("/ch");
        }
        return ok(views.html.login.login.render(request, null, null, InputUtils.NOERROR, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result loginPost(Http.Request request, String cc) {
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        if (gtfs == null) {
            return redirect("/ch");
        }
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String email = InputUtils.trimToNull(data.get("email"));
        String password = InputUtils.trimToNull(data.get("password"));
        try {
            User user = login.login(request, email, password);
            List<? extends Session> sessions = user.getSessions();
            Session session = sessions.get(sessions.size() - 1);
            Http.Cookie sessionCookie = Http.Cookie.builder("sessionId", session.getSessionId()).withMaxAge(Duration.ofDays(365)).build();
            Http.Cookie csrfTokenCookie = Http.Cookie.builder("csrfToken", Generator.generateSessionId()).withMaxAge(Duration.ofDays(365)).build();
            return redirect(routes.HomeController.index(gtfs.getCode())).withCookies(sessionCookie, csrfTokenCookie);
        } catch (InputValidationException e) {
            return ok(views.html.login.login.render(request, email, password, e.getErrors(), gtfsConfigModel.getSelectorChoices(), gtfs));
        }
    }

    public Result logout(Http.Request request, String cc) {
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        if (gtfs == null) {
            return redirect("/ch");
        }
        User user = usersModel.getFromRequest(request);
        if (user != null) {
            login.logout(request, user);
        }
        Http.Cookie sessionCookie = Http.Cookie.builder("sessionId", "").withMaxAge(Duration.ZERO).build();
        Http.Cookie csrfTokenCookie = Http.Cookie.builder("csrfToken", "").withMaxAge(Duration.ZERO).build();
        return redirect(routes.HomeController.index(gtfs.getCode())).withCookies(sessionCookie, csrfTokenCookie);
    }
}
