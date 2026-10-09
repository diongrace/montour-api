package ci.montour.ticket;

import ci.montour.ticket.TicketDtos.Appel;
import ci.montour.ticket.TicketDtos.TicketEtat;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Routes publiques : pas de connexion, le jeton du ticket sert de preuve. */
@RestController
@RequestMapping("/api/public")
public class PublicTicketController {

    private final TicketService ticketService;

    public PublicTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/services/{serviceId}/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketEtat prendreTicket(@PathVariable Long serviceId) {
        return ticketService.prendreTicket(serviceId);
    }

    @GetMapping("/tickets/{jeton}")
    public TicketEtat etat(@PathVariable UUID jeton) {
        return ticketService.etat(jeton);
    }

    @DeleteMapping("/tickets/{jeton}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annuler(@PathVariable UUID jeton) {
        ticketService.annuler(jeton);
    }

    @GetMapping("/etablissements/{slug}/ecran")
    public List<Appel> derniersAppels(@PathVariable String slug) {
        return ticketService.derniersAppels(slug);
    }
}
