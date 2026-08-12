package git.xlamid.eventmanagerservice.exception.model.validation;

public class TooManyValidationException extends ValidationException{

    public TooManyValidationException(String message) {
        super(message);
    }
}