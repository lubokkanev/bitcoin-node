package org.lubokkanev.bitcoinnode.exception;

public class InvalidBlockException extends Exception {
    public InvalidBlockException(String message) {
        super(message);
    }

    public InvalidBlockException(String message, Throwable cause) {
        super(message, cause);
    }
}