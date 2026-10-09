package ci.montour.ticket;

public class TransitionInterditeException extends RuntimeException {

    public TransitionInterditeException(StatutTicket actuel, StatutTicket cible) {
        super("Impossible de passer un ticket de " + actuel + " à " + cible);
    }
}
