package ci.montour.ticket;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Les objets que l'API envoie pour les tickets. Jamais d'entité JPA en sortie.
 */
public final class TicketDtos {

    private TicketDtos() {
    }

    /** Ce que voit le client sur son téléphone. */
    public record TicketEtat(
            UUID jeton,
            String code,
            StatutTicket statut,
            Long serviceId,
            String serviceNom,
            String etablissementSlug,
            int personnesDevant,
            int attenteEstimeeMin,
            String dernierNumeroAppele,
            String guichet,
            LocalDateTime creeLe,
            LocalDateTime appeleLe) {
    }

    /** Ce que voit l'agent pour le ticket qu'il traite. */
    public record TicketAgent(Long id, String code, StatutTicket statut, String serviceNom, String guichet,
                              LocalDateTime appeleLe) {

        static TicketAgent depuis(Ticket ticket) {
            String guichet = ticket.getGuichet() == null ? null : ticket.getGuichet().getNom();
            return new TicketAgent(ticket.getId(), ticket.getCode(), ticket.getStatut(),
                    ticket.getService().getNom(), guichet, ticket.getAppeleLe());
        }
    }

    /** Une ligne de la file d'attente côté agent. */
    public record TicketFile(Long id, String code, LocalDateTime creeLe) {
    }

    /** Un numéro appelé, affiché sur l'écran de salle. */
    public record Appel(String code, String guichet, LocalDateTime heure) {
    }

    /** Un guichet que l'agent peut choisir. */
    public record GuichetOption(Long id, String nom, Long serviceId, String serviceNom) {
    }
}
