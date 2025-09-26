package com.nachapa.api.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.nachapa.api.exceptions.constants.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.Map;

import static com.nachapa.api.exceptions.constants.ErrorConstants.CPF_ALREADY_REGISTERED_CODE;
import static com.nachapa.api.exceptions.constants.ErrorConstants.CPF_CANNOT_BE_CHANGED_CODE;
import static com.nachapa.api.exceptions.constants.ErrorConstants.EMAIL_ALREADY_REGISTERED_CODE;
import static com.nachapa.api.exceptions.constants.ErrorConstants.ERROR_DEACTIVATE_USER_CODE;
import static com.nachapa.api.exceptions.constants.ErrorConstants.ERROR_VALUE_NOT_VALID_CODE;
import static com.nachapa.api.exceptions.constants.ErrorConstants.ERROR_VALUE_NOT_VALID_DESERIALIZE_CODE;
import static com.nachapa.api.exceptions.constants.ErrorConstants.INVALID_CREDENTIALS_CODE;
import static com.nachapa.api.exceptions.constants.ErrorConstants.INVALID_DESERIALIZATION_SNIPPET;
import static com.nachapa.api.exceptions.constants.ErrorConstants.JWT_KEY_MISSING_CODE;
import static com.nachapa.api.exceptions.constants.ErrorConstants.USER_NOT_FOUND_CODE;


@ControllerAdvice
public class RestExceptionHandler {

    private ResponseEntity<RestErrorMessage> buildErrorResponse(String errorCode, HttpStatus status) {
        Map.Entry<String, String> errorEntry = ErrorConstants.getError(errorCode);
        return ResponseEntity.status(status).body(new RestErrorMessage(errorEntry.getKey(), errorEntry.getValue()));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    private ResponseEntity<RestErrorMessage> emailAlreadyRegisteredExceptionExceptionHandler(EmailAlreadyRegisteredException e) {
        return buildErrorResponse(EMAIL_ALREADY_REGISTERED_CODE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JwtKeyMissingException.class)
    private ResponseEntity<RestErrorMessage> jwtKeyMissingExceptionExceptionHandler(JwtKeyMissingException e) {
        return buildErrorResponse(JWT_KEY_MISSING_CODE, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestErrorMessage> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        RestErrorMessage threatResponse = new RestErrorMessage();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            threatResponse.setCode(ERROR_VALUE_NOT_VALID_CODE);
            threatResponse.setMessage(error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(threatResponse);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<RestErrorMessage> invalidFormatExceptionHandler(InvalidFormatException ex) {
        if (ex.getLocalizedMessage().contains(INVALID_DESERIALIZATION_SNIPPET)) {
            return buildErrorResponse(ERROR_VALUE_NOT_VALID_DESERIALIZE_CODE, HttpStatus.BAD_REQUEST);
        }
        return buildErrorResponse(ERROR_VALUE_NOT_VALID_CODE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<RestErrorMessage> ioExceptionHandler(IOException ex) {
        RestErrorMessage threatResponse = new RestErrorMessage("erro", ex.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(threatResponse);
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    private ResponseEntity<RestErrorMessage> invalidCredentialsExceptionHandler(InvalidCredentialsException e) {
        return buildErrorResponse(INVALID_CREDENTIALS_CODE, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CpfAlreadyRegisteredException.class)
    private ResponseEntity<RestErrorMessage> CpfAlreadyRegisteredExceptionHandler(CpfAlreadyRegisteredException e) {
        return buildErrorResponse(CPF_ALREADY_REGISTERED_CODE, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<RestErrorMessage> UserNotFoundExceptionHandler(UserNotFoundException e) {
        return buildErrorResponse(USER_NOT_FOUND_CODE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ErrorDeactivateUserException.class)
    private ResponseEntity<RestErrorMessage> errorDeactivateUserExceptionHandler(ErrorDeactivateUserException e) {
        return buildErrorResponse(ERROR_DEACTIVATE_USER_CODE, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CpfCannotBeChangedException.class)
    private ResponseEntity<RestErrorMessage> cpfCannotBeChangedExceptionHandler(CpfCannotBeChangedException e) {
        return buildErrorResponse(CPF_CANNOT_BE_CHANGED_CODE, HttpStatus.NOT_FOUND);
    }

}