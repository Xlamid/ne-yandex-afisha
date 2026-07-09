package git.xlamid.eventmanagerservice.exception.handler;

import git.xlamid.eventmanagerservice.exception.dto.ResponseExceptionDto;
import git.xlamid.eventmanagerservice.exception.model.notfound.NotFoundException;
import git.xlamid.eventmanagerservice.exception.model.validation.RepeatableValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseExceptionDto> handleNotFoundException(NotFoundException e) {
        log.error("Not found exception", e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseExceptionDto(
                        "Not found exception",
                        e.getMessage(),
                        OffsetDateTime.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseExceptionDto> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation exception", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseExceptionDto(
                        "Validation exception",
                        getReadableMessage(e),
                        OffsetDateTime.now()
                ));
    }

    private String getReadableMessage(MethodArgumentNotValidException e) {
        return e.getBindingResult()
                .getFieldErrors().stream()
                .map(fieldError ->
                        fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }

    @ExceptionHandler(RepeatableValidationException.class)
    public ResponseEntity<ResponseExceptionDto> handleBadRequestException(RepeatableValidationException e) {
        log.error("Repeatable validation exception", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseExceptionDto(
                        "Repeatable validation exception",
                        e.getMessage(),
                        OffsetDateTime.now()
                ));
    }

    @ExceptionHandler(exception = {MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ResponseExceptionDto> handleBadRequestException(
            MethodArgumentTypeMismatchException e
    ) {
        log.error("Bad request exception", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseExceptionDto(
                        "Bad request exception",
                        e.getMessage(),
                        OffsetDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseExceptionDto> handleException(Exception e) {
        log.error("Server exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseExceptionDto(
                        "Server exception",
                        e.getMessage(),
                        OffsetDateTime.now()
                ));
    }
}