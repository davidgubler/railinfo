package biz;

import com.google.inject.Inject;
import entities.User;
import models.UsersModel;
import play.mvc.Http;
import utils.*;

import java.util.HashMap;
import java.util.Map;

public class Users {
    @Inject
    private UsersModel usersModel;

    public User create(Http.RequestHeader request, String email, String name, String password, User user) throws InputValidationException {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT
        Map<String, String> errors = new HashMap<>();
        if (email == null) {
            errors.put("email", ErrorMessages.PLEASE_ENTER_VALUE);
        }
        if (name == null) {
            errors.put("name", ErrorMessages.PLEASE_ENTER_VALUE);
        }
        if (password == null) {
            errors.put("password", ErrorMessages.PLEASE_ENTER_VALUE);
        }
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        // BUSINESS
        User createUser = usersModel.create(email, name, password);

        // LOG
        RailinfoLogger.info(request, user + " created " + createUser);
        return createUser;
    }

    public void update(Http.RequestHeader request, User updateUser, String email, String name, String password, User user) throws InputValidationException {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT
        Map<String, String> errors = new HashMap<>();
        if (email == null) {
            errors.put("email", ErrorMessages.PLEASE_ENTER_VALUE);
        }
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        // BUSINESS
        usersModel.update(updateUser, email, name, password);

        // LOG
        RailinfoLogger.info(request, user + " updated " + updateUser);
    }

    public void delete(Http.RequestHeader request, User deleteUser, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT
        // nothing

        // BUSINESS
        usersModel.delete(deleteUser);

        // LOG
        RailinfoLogger.info(request, user + " deleted " + deleteUser);
    }

    public void ensureAdmin(Http.RequestHeader request) {
        // ACCESS
        // nothing

        // INPUT
        // nothing

        // BUSINESS
        User createdAdmin = null;
        String pwd = null;
        if (usersModel.getAll().isEmpty()) {
            pwd = Generator.generateSessionId();
            createdAdmin = usersModel.create("admin@localhost", "Admin", pwd);
        }

        // LOG
        if (createdAdmin != null) {
            RailinfoLogger.info(request, "Created admin user " + createdAdmin + " with password '" + pwd + "'");
        }
    }
}
