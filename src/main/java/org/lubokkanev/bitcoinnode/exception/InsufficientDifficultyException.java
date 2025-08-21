package org.lubokkanev.bitcoinnode.exception;

public class InsufficientDifficultyException extends InvalidBlockException {
    public InsufficientDifficultyException(String message) {
        super(message);
    }

    public InsufficientDifficultyException(String message, Throwable cause) {
        super(message, cause);
    }
}