package biz;

import com.google.inject.Inject;
import entities.User;
import models.UsersModel;
import play.mvc.Http;
import utils.ErrorMessages;
import utils.InputValidationException;
import utils.RailinfoLogger;

import java.util.HashMap;
import java.util.Map;

public class Login {
    @Inject
    private UsersModel usersModel;

    public User login(Http.RequestHeader request, String email, String password) throws InputValidationException {
        // ACCESS
        // nothing

        // INPUT
        Map<String, String> errors = new HashMap<>();
        User user = usersModel.getByEmailAndPassword(email, password);
        if (user == null) {
            errors.put("email", "");
            errors.put("password", ErrorMessages.EMAIL_OR_PASSWORD_INVALID);
        }
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        // BUSINESS
        usersModel.startSession(user);

        // LOG
        RailinfoLogger.info(request, user + " logged in");

        return user;
    }

    public void logout(Http.RequestHeader request, User user) {
        // ACCESS
        // nothing

        // INPUT
        // nothing

        // BUSINESS
        usersModel.killSessions(user);

        // LOG
        RailinfoLogger.info(request, user + " logged out");
    }
}
