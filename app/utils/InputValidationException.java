package utils;

import java.util.Map;

public class InputValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    private final Map<String, String> errors;

    public InputValidationException(Map<String, String> errors) {
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}