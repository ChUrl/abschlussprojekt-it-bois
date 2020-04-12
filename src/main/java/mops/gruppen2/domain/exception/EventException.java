package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EventException extends ResponseStatusException {

    private static final long serialVersionUID = 6784052016028094340L;

    public EventException(HttpStatus status, String msg, String info) {
        super(status, info.isBlank() ? "" : msg + "    (" + info + ")");
    }

}
