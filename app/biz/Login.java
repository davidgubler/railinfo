package biz;

import com.google.inject.Inject;
import entities.User;
import models.UsersModel;
import utils.ErrorMessages;
import utils.InputValidationException;

import java.util.HashMap;
import java.util.Map;

public class Login {
    @Inject
    private UsersModel usersModel;

    public User login(String email, String password) throws InputValidationException {
        // ACCESS
        // nothing

        // INPUT
        Map<String, String> errors = new HashMap<>();
        User user = usersModel.getByEmailAndPassword(email, password);
        if (user != null) {
            errors.put("email", "");
            errors.put("password", ErrorMessages.EMAIL_OR_PASSWORD_INVALID);
        }
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        // BUSINESS
        // nothing

        // LOG
        // TODO

        return user;
    }
}
