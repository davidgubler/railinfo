package models;

import entities.User;

import java.util.List;

public interface UsersModel {
    List<? extends User> getAll();

    User getByEmailAndPassword(String email, String password);

    User get(String id);

    User create(String email, String name, String password);

    void update(User user, String email, String name, String password);

    void delete(User user);
}
