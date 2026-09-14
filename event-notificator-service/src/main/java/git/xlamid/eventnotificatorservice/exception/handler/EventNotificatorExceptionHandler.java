package git.xlamid.eventnotificatorservice.exception.handler;

import git.xlamid.eventcommon.exception.dto.ExceptionMessageResponseDto;
import git.xlamid.eventnotificatorservice.exception.model.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@Slf4j
@RestControllerAdvice
public class EventNotificatorExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ExceptionMessageResponseDto> handleNotValidException(ValidationException e) {
        log.error("Validation exception", e);
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "Validation exception",
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

    @ExceptionHandler(exception = {AccessDeniedException.class})
    public ResponseEntity<ExceptionMessageResponseDto> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied exception", e);
        return createResponse(
                HttpStatus.FORBIDDEN,
                "Access denied exception",
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