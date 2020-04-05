package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class BadPayloadException extends EventException {

    public BadPayloadException(String info) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Die Payload konnte nicht übersetzt werden!", info);
    }
}
