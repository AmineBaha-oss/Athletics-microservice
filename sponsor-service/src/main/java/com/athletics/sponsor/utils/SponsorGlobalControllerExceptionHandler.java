package com.athletics.sponsor.utils;





import com.athletics.sponsor.utils.exceptions.InsufficientSponsorAmountException;
import com.athletics.sponsor.utils.exceptions.InvalidInputException;
import com.athletics.sponsor.utils.exceptions.NotFoundException;
import com.athletics.sponsor.utils.exceptions.SponsorIdentityClashException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestControllerAdvice
@Slf4j
public class SponsorGlobalControllerExceptionHandler {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public SponsorHttpErrorInfo handleNotFoundException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(NOT_FOUND, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public SponsorHttpErrorInfo handleInvalidInputException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InsufficientSponsorAmountException.class)
    public SponsorHttpErrorInfo handleInsufficientSponsorAmountException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);



    }

    // New handler for SponsorIdentityClashException
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(SponsorIdentityClashException.class)
    public SponsorHttpErrorInfo handleSponsorIdentityClashException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }



    private SponsorHttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, WebRequest request, Exception ex) {
        final String path = request.getDescription(false);
        // final String path = request.getPath().pathWithinApplication().value();
        final String message = ex.getMessage();
        log.debug("message is: " + message);

        log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);

        return new SponsorHttpErrorInfo(httpStatus, path, message);
    }
}
