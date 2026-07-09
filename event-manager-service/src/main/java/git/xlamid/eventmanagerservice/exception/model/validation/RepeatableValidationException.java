package git.xlamid.eventmanagerservice.exception.model.validation;

public class RepeatableValidationException extends ValidationException{

    public RepeatableValidationException(String message) {
        super(message);
    }
}