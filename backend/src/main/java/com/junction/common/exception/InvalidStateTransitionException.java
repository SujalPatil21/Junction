package com.junction.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to transition an entity to an invalid lifecycle state.
 * E.g., executing a PROPOSED recommendation without first approving it.
 * Mirrors the lifecycle guard in src/services/recommendationLifecycleEngine.ts.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String entityType, String currentState, String targetState) {
        super("Cannot transition %s from %s to %s".formatted(entityType, currentState, targetState));
    }
}

