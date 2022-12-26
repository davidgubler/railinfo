package entities;

public interface User {
    String getId();

    String getName();

    String getEmail();

    boolean checkPassword(String password);
}
