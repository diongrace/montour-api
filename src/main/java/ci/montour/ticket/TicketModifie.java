package ci.montour.ticket;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement publié à chaque changement de ticket. Il est diffusé par WebSocket
 * seulement après la validation (commit) de la transaction.
 */
public record TicketModifie(
        Long serviceId,
        String etablissementSlug,
        UUID jeton,
        String code,
        StatutTicket statut,
        String guichet,
        long enAttente,
        LocalDateTime heure) {
}
