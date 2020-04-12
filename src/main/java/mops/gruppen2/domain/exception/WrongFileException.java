package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class WrongFileException extends EventException {

    private static final long serialVersionUID = -166192514348555116L;

    public WrongFileException(String info) {
        super(HttpStatus.BAD_REQUEST, "Die Datei ist keine valide CSV-Datei!", info);
    }
}

