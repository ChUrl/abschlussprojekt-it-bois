package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class UserExistsException extends EventException {

    private static final long serialVersionUID = -8150634358760194625L;

    public UserExistsException(String info) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "User existiert bereits.", info);
    }
}

