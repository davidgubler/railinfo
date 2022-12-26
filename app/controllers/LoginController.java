package controllers;

import biz.Login;
import com.google.inject.Inject;
import entities.Session;
import entities.User;
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
    private UsersModel usersModel;

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
            List<? extends Session> sessions = user.getSessions();
            Session session = sessions.get(sessions.size() - 1);
            Http.Cookie sessionCookie = Http.Cookie.builder("sessionId", session.getSessionId()).withMaxAge(Duration.ofDays(365)).build();
            Http.Cookie csrfTokenCookie = Http.Cookie.builder("csrfToken", Generator.generateSessionId()).withMaxAge(Duration.ofDays(365)).build();
            return redirect(routes.HomeController.index()).withCookies(sessionCookie, csrfTokenCookie);
        } catch (InputValidationException e) {
            return ok(views.html.login.login.render(request, email, password, e.getErrors()));
        }
    }

    public Result logout(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user != null) {
            usersModel.killSessions(user);
        }
        Http.Cookie sessionCookie = Http.Cookie.builder("sessionId", "").withMaxAge(Duration.ZERO).build();
        Http.Cookie csrfTokenCookie = Http.Cookie.builder("csrfToken", "").withMaxAge(Duration.ZERO).build();
        return redirect(routes.HomeController.index()).withCookies(sessionCookie, csrfTokenCookie);
    }
}
