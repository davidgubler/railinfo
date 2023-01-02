package utils;

public class NotAllowedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String object;

    @Override
    public String getMessage() {
        return object + " not found";
    }
}
