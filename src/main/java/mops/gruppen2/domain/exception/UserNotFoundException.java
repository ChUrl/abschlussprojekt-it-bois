package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends EventException {

    private static final long serialVersionUID = 8347442921199785291L;

    public UserNotFoundException(String info) {
        super(HttpStatus.NOT_FOUND, "User existiert nicht.", info);
    }
}

