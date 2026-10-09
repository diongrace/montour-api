package ci.montour.common;

import ci.montour.ticket.TransitionInterditeException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Transforme les exceptions en réponses JSON au format standard "Problem Details" (RFC 9457).
 * Le frontend lit toujours le champ "detail" pour afficher le message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ProblemDetail gererApiException(ApiException e) {
        return ProblemDetail.forStatusAndDetail(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(TransitionInterditeException.class)
    public ProblemDetail gererTransition(TransitionInterditeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail gererValidation(MethodArgumentNotValidException e) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erreur -> erreurs.put(erreur.getField(), erreur.getDefaultMessage()));
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Certaines données sont invalides");
        probleme.setProperty("erreurs", erreurs);
        return probleme;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail gererDoublon(DataIntegrityViolationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Cette donnée existe déjà");
    }
}
