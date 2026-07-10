package git.xlamid.eventmanagerservice.exception.model.validation;

public abstract class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}