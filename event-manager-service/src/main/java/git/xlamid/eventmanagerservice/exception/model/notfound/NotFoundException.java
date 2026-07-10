package git.xlamid.eventmanagerservice.exception.model.notfound;

public abstract class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}