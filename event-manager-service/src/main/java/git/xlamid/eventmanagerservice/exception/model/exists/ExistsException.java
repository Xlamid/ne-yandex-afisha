package git.xlamid.eventmanagerservice.exception.model.exists;

public abstract class ExistsException extends RuntimeException {

    public ExistsException(String message) {
        super(message);
    }
}