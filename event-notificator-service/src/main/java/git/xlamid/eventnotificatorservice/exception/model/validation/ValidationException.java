package git.xlamid.eventnotificatorservice.exception.model.validation;

public abstract class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}