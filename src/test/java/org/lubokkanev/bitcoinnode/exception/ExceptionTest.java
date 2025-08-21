package org.lubokkanev.bitcoinnode.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void invalidBlockException_WithMessage_ShouldWork() {
        String message = "Invalid block";
        InvalidBlockException exception = new InvalidBlockException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void invalidBlockException_WithMessageAndCause_ShouldWork() {
        String message = "Invalid block";
        Throwable cause = new RuntimeException("Root cause");
        InvalidBlockException exception = new InvalidBlockException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void invalidTransactionException_WithMessage_ShouldWork() {
        String message = "Invalid transaction";
        InvalidTransactionException exception = new InvalidTransactionException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void invalidTransactionException_WithMessageAndCause_ShouldWork() {
        String message = "Invalid transaction";
        Throwable cause = new RuntimeException("Root cause");
        InvalidTransactionException exception = new InvalidTransactionException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void insufficientDifficultyException_WithMessage_ShouldWork() {
        String message = "Insufficient difficulty";
        InsufficientDifficultyException exception = new InsufficientDifficultyException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof InvalidBlockException);
    }

    @Test
    void insufficientDifficultyException_WithMessageAndCause_ShouldWork() {
        String message = "Insufficient difficulty";
        Throwable cause = new RuntimeException("Root cause");
        InsufficientDifficultyException exception = new InsufficientDifficultyException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof InvalidBlockException);
    }

    @Test
    void insufficientDifficultyException_IsSubclassOfInvalidBlockException() {
        InsufficientDifficultyException exception = new InsufficientDifficultyException("test");
        
        assertTrue(exception instanceof InvalidBlockException);
        assertTrue(exception instanceof Exception);
    }

    @Test
    void allExceptions_AreInstancesOfException() {
        InvalidBlockException blockException = new InvalidBlockException("test");
        InvalidTransactionException transactionException = new InvalidTransactionException("test");
        InsufficientDifficultyException difficultyException = new InsufficientDifficultyException("test");

        assertTrue(blockException instanceof Exception);
        assertTrue(transactionException instanceof Exception);
        assertTrue(difficultyException instanceof Exception);
    }
}