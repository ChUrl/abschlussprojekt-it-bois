package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class InvalidInviteException extends EventException {

    private static final long serialVersionUID = 2643001101459427944L;

    public InvalidInviteException(String info) {
        super(HttpStatus.NOT_FOUND, "Der Einladungslink ist ungültig oder die Gruppe wurde gelöscht.", info);
    }
}

