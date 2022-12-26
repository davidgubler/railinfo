package entities;

import java.util.List;

public interface User {
    String getId();

    String getName();

    String getEmail();

    List<? extends Session> getSessions();

    boolean checkPassword(String password);
}
