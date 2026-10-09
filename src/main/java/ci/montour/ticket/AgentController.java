package ci.montour.ticket;

import ci.montour.ticket.TicketDtos.GuichetOption;
import ci.montour.ticket.TicketDtos.TicketAgent;
import ci.montour.ticket.TicketDtos.TicketFile;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Routes de l'agent. Le JWT donne son email (subject) et son établissement. */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final TicketService ticketService;

    public AgentController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/guichets")
    public List<GuichetOption> guichets(@AuthenticationPrincipal Jwt jwt) {
        return ticketService.guichetsDisponibles(jwt.getClaimAsString("etablissement"));
    }

    @PostMapping("/guichets/{guichetId}/suivant")
    public TicketAgent appelerSuivant(@PathVariable Long guichetId, @AuthenticationPrincipal Jwt jwt) {
        return ticketService.appelerSuivant(guichetId, jwt.getSubject());
    }

    @GetMapping("/guichets/{guichetId}/courant")
    public ResponseEntity<TicketAgent> ticketEnCours(@PathVariable Long guichetId) {
        return ResponseEntity.of(ticketService.ticketEnCours(guichetId));
    }

    @PostMapping("/tickets/{ticketId}/rappel")
    public TicketAgent rappeler(@PathVariable Long ticketId, @AuthenticationPrincipal Jwt jwt) {
        return ticketService.rappeler(ticketId, jwt.getSubject());
    }

    @PostMapping("/tickets/{ticketId}/servi")
    public TicketAgent servi(@PathVariable Long ticketId, @AuthenticationPrincipal Jwt jwt) {
        return ticketService.marquerServi(ticketId, jwt.getSubject());
    }

    @PostMapping("/tickets/{ticketId}/absent")
    public TicketAgent absent(@PathVariable Long ticketId, @AuthenticationPrincipal Jwt jwt) {
        return ticketService.marquerAbsent(ticketId, jwt.getSubject());
    }

    @GetMapping("/services/{serviceId}/file")
    public List<TicketFile> file(@PathVariable Long serviceId) {
        return ticketService.file(serviceId);
    }
}
