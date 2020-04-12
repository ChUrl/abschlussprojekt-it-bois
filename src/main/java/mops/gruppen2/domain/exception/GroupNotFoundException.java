package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class GroupNotFoundException extends EventException {

    private static final long serialVersionUID = -4738218416842951106L;

    public GroupNotFoundException(String info) {
        super(HttpStatus.NOT_FOUND, "Die Gruppe existiert nicht oder wurde gelöscht.", info);
    }
}

