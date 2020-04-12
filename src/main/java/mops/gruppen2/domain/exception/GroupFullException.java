package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class GroupFullException extends EventException {

    private static final long serialVersionUID = -4011141160467668713L;

    public GroupFullException(String info) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Die Gruppe hat die maximale Mitgliederanzahl bereits erreicht!", info);
    }
}

