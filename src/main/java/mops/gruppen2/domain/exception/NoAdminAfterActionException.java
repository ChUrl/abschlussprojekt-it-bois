package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class NoAdminAfterActionException extends EventException {

    private static final long serialVersionUID = 9059481382346544288L;

    public NoAdminAfterActionException(String info) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Die Gruppe braucht einen Admin.", info);
    }
}
