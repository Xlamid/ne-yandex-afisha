package git.xlamid.eventmanagerservice.exception.model.notfound;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }
}