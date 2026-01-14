package com.ouimet.f1.fantasy_service.exception;

public class F1FantasyException extends RuntimeException {
    public F1FantasyException(String message) {
        super(message);
    }

    public F1FantasyException(String message, Throwable cause) {
        super(message, cause);
    }
}
