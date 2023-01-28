package controllers;

import biz.Users;
import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.User;
import models.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.InputUtils;
import utils.InputValidationException;
import utils.NotAllowedException;
import utils.NotFoundException;

import java.util.*;

public class AdminController extends Controller {
    @Inject
    private RoutesModel routesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private StopsModel stopsModel;

    @Inject
    private EdgesModel edgesModel;

    @Inject
    private UsersModel usersModel;

    @Inject
    private Users users;

    @Inject
    private Injector injector;

    public Result usersList(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        List<? extends User> users = usersModel.getAll();
        return ok(views.html.admin.users.list.render(request, users, user));
    }

    public Result usersCreate(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        return ok(views.html.admin.users.create.render(request, null, null, null, InputUtils.NOERROR, user));
    }

    public Result usersCreatePost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String email = InputUtils.trimToNull(data.get("email"));
        String name = InputUtils.trimToNull(data.get("name"));
        String password = InputUtils.trimToNull(data.get("password"));
        try {
            users.create(request, email, name, password, user);
            return redirect(controllers.routes.AdminController.usersList());
        } catch (InputValidationException e) {
            return ok(views.html.admin.users.create.render(request, email, name, password, e.getErrors(), user));
        }
    }

    public Result usersEdit(Http.Request request, String uid) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        User editUser = usersModel.get(uid);
        if (editUser == null) {
            throw new NotFoundException("User");
        }
        return ok(views.html.admin.users.edit.render(request, editUser, editUser.getEmail(), editUser.getName(), null, InputUtils.NOERROR, user));
    }

    public Result usersEditPost(Http.Request request, String uid) {
        User user = usersModel.getFromRequest(request);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        User editUser = usersModel.get(uid);
        String email = InputUtils.trimToNull(data.get("email"));
        String name = InputUtils.trimToNull(data.get("name"));
        String password = InputUtils.trimToNull(data.get("password"));
        try {
            users.update(request, editUser, email, name, password, user);
            return redirect(controllers.routes.AdminController.usersList());
        } catch (InputValidationException e) {
            return ok(views.html.admin.users.edit.render(request, editUser, email, name, password, e.getErrors(), user));
        }
    }

    public Result usersDelete(Http.Request request, String uid) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        User deleteUser = usersModel.get(uid);
        if (deleteUser == null) {
            throw new NotFoundException("User");
        }
        return ok(views.html.admin.users.delete.render(request, deleteUser, user));
    }

    public Result usersDeletePost(Http.Request request, String uid) {
        User user = usersModel.getFromRequest(request);
        User deleteUser = usersModel.get(uid);
        if (deleteUser == null) {
            throw new NotFoundException("User");
        }
        users.delete(request, deleteUser, user);
        return redirect(controllers.routes.AdminController.usersList());
    }
}
