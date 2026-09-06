package pl.fabrykaterapii.reservations.api;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(NoSuchElementException.class) ResponseEntity<?> notFound(NoSuchElementException e){return error(HttpStatus.NOT_FOUND,e.getMessage());}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> invalid(IllegalArgumentException e){return error(HttpStatus.UNPROCESSABLE_ENTITY,e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){return error(HttpStatus.BAD_REQUEST,e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).toList());}
 private ResponseEntity<Map<String,Object>> error(HttpStatus status,Object message){return ResponseEntity.status(status).body(Map.of("status",status.value(),"error",status.getReasonPhrase(),"message",message));}
}
