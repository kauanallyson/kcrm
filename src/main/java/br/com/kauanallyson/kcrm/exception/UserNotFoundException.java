package br.com.kauanallyson.kcrm.exception;

import java.util.UUID;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID id) {
        super(ErrorCode.USER_NOT_FOUND, "User not found: " + id);
    }
}
