package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends EventException {

    private static final long serialVersionUID = -8150634358760194625L;

    public UserAlreadyExistsException(String info) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Der User existiert bereits.", info);
    }
}

