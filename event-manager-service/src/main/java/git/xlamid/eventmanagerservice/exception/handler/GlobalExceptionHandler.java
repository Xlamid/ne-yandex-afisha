package git.xlamid.eventmanagerservice.exception.handler;

import git.xlamid.eventmanagerservice.exception.dto.ExceptionMessageResponseDto;
import git.xlamid.eventmanagerservice.exception.model.exists.ExistsException;
import git.xlamid.eventmanagerservice.exception.model.notfound.NotFoundException;
import git.xlamid.eventmanagerservice.exception.model.validation.RepeatableValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionMessageResponseDto> handleNotFoundException(NotFoundException e) {
        log.error("Not found exception", e);
        return createResponse(
                HttpStatus.NOT_FOUND,
                "Not found exception",
                e.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionMessageResponseDto> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation exception", e);
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Validation exception",
                getReadableMessage(e)
        );
    }

    private String getReadableMessage(MethodArgumentNotValidException e) {
        return e.getBindingResult()
                .getFieldErrors().stream()
                .map(fieldError ->
                        fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }

    @ExceptionHandler(RepeatableValidationException.class)
    public ResponseEntity<ExceptionMessageResponseDto> handleBadRequestException(RepeatableValidationException e) {
        log.error("Repeatable validation exception", e);
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Repeatable validation exception",
                e.getMessage()
        );
    }

    @ExceptionHandler(exception = {MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ExceptionMessageResponseDto> handleBadRequestException(
            MethodArgumentTypeMismatchException e
    ) {
        log.error("Bad request exception", e);
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Bad request exception",
                e.getMessage()
        );
    }

    @ExceptionHandler(exception = {ExistsException.class})
    public ResponseEntity<ExceptionMessageResponseDto> handleExistsException(ExistsException e) {
        log.error("Already exists exception", e);
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Already exists exception",
                e.getMessage()
        );
    }

    @ExceptionHandler(exception = {BadCredentialsException.class})
    public ResponseEntity<ExceptionMessageResponseDto> handleUnauthorizeException(BadCredentialsException e) {
        log.error("Unauthorize exception", e);
        return createResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorize exception",
                e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionMessageResponseDto> handleException(Exception e) {
        log.error("Server exception", e);
        return createResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Server exception",
                e.getMessage()
        );
    }

    private ResponseEntity<ExceptionMessageResponseDto> createResponse(HttpStatus httpStatus,
                                                                       String message,
                                                                       String detailedMessage) {
        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionMessageResponseDto(
                        message,
                        detailedMessage,
                        OffsetDateTime.now()
                ));
    }
}