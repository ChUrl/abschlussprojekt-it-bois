package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class BadPayloadException extends EventException {

    private static final long serialVersionUID = -3978242017847155629L;

    public BadPayloadException(String info) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Payload konnte nicht übersetzt werden.", info);
    }
}

