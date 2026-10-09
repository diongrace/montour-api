package ci.montour.ticket;

import ci.montour.ticket.TicketDtos.Appel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Diffuse les changements de tickets par WebSocket, une fois la transaction validée.
 * Si la transaction échoue, rien n'est envoyé : les téléphones n'affichent jamais un état faux.
 */
@Component
public class TicketEventPublisher {

    private final SimpMessagingTemplate messaging;

    public TicketEventPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void diffuser(TicketModifie evenement) {
        messaging.convertAndSend("/topic/services/" + evenement.serviceId(), evenement);
        messaging.convertAndSend("/topic/tickets/" + evenement.jeton(), evenement);
        if (evenement.statut() == StatutTicket.APPELE) {
            messaging.convertAndSend("/topic/etablissements/" + evenement.etablissementSlug() + "/ecran",
                    new Appel(evenement.code(), evenement.guichet(), evenement.heure()));
        }
    }
}
