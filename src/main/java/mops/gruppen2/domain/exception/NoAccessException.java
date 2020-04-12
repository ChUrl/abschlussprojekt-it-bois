package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class NoAccessException extends EventException {

    private static final long serialVersionUID = 1696988497122834654L;

    public NoAccessException(String info) {
        super(HttpStatus.FORBIDDEN, "Hier hast du keinen Zugriff.", info);
    }
}

