package mops.gruppen2.domain.exception;

import org.springframework.http.HttpStatus;

public class BadArgumentException extends EventException {

    private static final long serialVersionUID = -6757742013238625595L;

    public BadArgumentException(String info) {
        super(HttpStatus.BAD_REQUEST, "Fehlerhafter Parameter.", info);
    }
}
