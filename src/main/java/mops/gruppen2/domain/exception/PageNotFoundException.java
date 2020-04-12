package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class PageNotFoundException extends EventException {

    private static final long serialVersionUID = 2374509005158710104L;

    public PageNotFoundException(String info) {
        super(HttpStatus.NOT_FOUND, "Die Seite wurde nicht gefunden!", info);
    }
}

