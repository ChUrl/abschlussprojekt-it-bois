package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class LastAdminException extends EventException {

    private static final long serialVersionUID = 9059481382346544288L;

    public LastAdminException(String info) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Gruppe braucht mindestens einen Admin.", info);
    }
}
