package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class NoInviteExistException extends EventException {

    private static final long serialVersionUID = -8092076461455840693L;

    public NoInviteExistException(String info) {
        super(HttpStatus.NOT_FOUND, "Für diese Gruppe existiert kein Link.", info);
    }
}

