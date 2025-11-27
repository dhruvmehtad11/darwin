package darwincatalog.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  public static final String ERROR = "error";

  @ExceptionHandler({
    SchemaParseException.class,
    InvalidAttributeException.class,
    NoSuchFieldException.class,
    InvalidMetricException.class,
    InvalidSqlException.class,
    IllegalArgumentException.class,
    HttpMediaTypeNotAcceptableException.class,
    HttpMessageNotReadableException.class,
    InvalidRegexException.class,
    MethodArgumentNotValidException.class,
    MissingRequestHeaderException.class,
    DuplicateRuleExistsException.class,
    CyclicDependencyException.class,
    NoHierarchyMapperException.class,
    NoAssetRegistrarFoundException.class,
    MissingSchemaVersionException.class,
    HttpRequestMethodNotSupportedException.class,
    MissingServletRequestParameterException.class,
    InvalidClassificationStatusTransition.class,
    InvalidRuleException.class
  })
  public ResponseEntity<Map<String, String>> handleClientError(Exception ex) {
    log.error("{}: ", ERROR, ex);
    Map<String, String> errorBody = new java.util.HashMap<>(Map.of(ERROR, ex.getMessage()));
    return ResponseEntity.badRequest().body(errorBody);
  }

  @ExceptionHandler({DataIntegrityViolationException.class})
  public ResponseEntity<Map<String, String>> handleDatabaseError(Exception ex) {
    log.error("{}: ", ERROR, ex);
    Map<String, String> errorBody = new HashMap<>();
    if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
      org.hibernate.exception.ConstraintViolationException hibernateEx =
          (org.hibernate.exception.ConstraintViolationException) ex.getCause();
      String sqlMessage = hibernateEx.getSQLException().getMessage();
      String constraintName = hibernateEx.getConstraintName();
      String message = "Constraint violation on: " + constraintName + " -> " + sqlMessage;
      errorBody.put(ERROR, message);
    } else {
      errorBody.put(ERROR, ex.getMessage());
    }
    return ResponseEntity.badRequest().body(errorBody);
  }

  @ExceptionHandler({InvalidClientTokenException.class})
  public ResponseEntity<Map<String, String>> handleInvalidClientTokenException(Exception ex) {
    log.error("{}: ", ERROR, ex);
    Map<String, String> errorBody = new java.util.HashMap<>(Map.of(ERROR, ex.getMessage()));
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
  }

  @ExceptionHandler({
    AssetNotFoundException.class,
    MetricNotFoundException.class,
    RuleNotFoundException.class,
    FieldNotFoundException.class,
    TagNotFoundException.class,
    MonitorNotFoundException.class,
    SchemaClassificationNotFound.class,
    SchemaClassificationStatusNotFound.class,
    SchemaNotFoundException.class,
    AssetToRegisterNotFound.class
  })
  public ResponseEntity<Map<String, String>> handleNotFoundException(Exception ex) {
    log.error("{}: ", ERROR, ex);
    Map<String, String> errorBody = new java.util.HashMap<>(Map.of(ERROR, ex.getMessage()));
    return new ResponseEntity<>(errorBody, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({RuntimeException.class, AssetException.class, DatadogException.class})
  public ResponseEntity<Map<String, String>> handleServerException(Exception ex) {
    log.error("{}: ", ERROR, ex);
    Map<String, String> errorBody = new java.util.HashMap<>(Map.of(ERROR, ex.getMessage()));
    return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
