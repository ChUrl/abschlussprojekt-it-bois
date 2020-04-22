package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class IdMismatchException extends EventException {

    private static final long serialVersionUID = 7944077617758922089L;

    public IdMismatchException(String info) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Ids stimmen nicht überein.", info);
    }
}

