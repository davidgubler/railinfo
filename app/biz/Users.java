package biz;

import com.google.inject.Inject;
import entities.User;
import models.UsersModel;
import utils.ErrorMessages;
import utils.InputValidationException;

import java.util.HashMap;
import java.util.Map;

public class Users {
    @Inject
    private UsersModel usersModel;

    public User create(String email, String name, String password, User user) throws InputValidationException {
        // ACCESS
        // TODO

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
        // TODO
        return createUser;
    }

    public void update(User updateUser, String email, String name, String password, User user) throws InputValidationException {
        // ACCESS
        // TODO

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
        // TODO
    }

    public void delete(User deleteUser, User user) {
        // ACCESS
        // TODO

        // INPUT
        // nothing

        // BUSINESS
        usersModel.delete(deleteUser);

        // LOG
        // TODO
    }
}
