package com.athletics.sponsor.utils.exceptions;

public class InsufficientSponsorAmountException extends RuntimeException {

    public InsufficientSponsorAmountException() {
        super();
    }

    public InsufficientSponsorAmountException(String message) {
        super(message);
    }

    public InsufficientSponsorAmountException(Throwable cause) {
        super(cause);
    }

}
