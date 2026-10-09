package ci.montour.ticket;

import ci.montour.accueil.Guichet;
import ci.montour.accueil.GuichetRepository;
import ci.montour.accueil.ServiceAccueil;
import ci.montour.accueil.ServiceAccueilRepository;
import ci.montour.common.ApiException;
import ci.montour.ticket.TicketDtos.Appel;
import ci.montour.ticket.TicketDtos.GuichetOption;
import ci.montour.ticket.TicketDtos.TicketAgent;
import ci.montour.ticket.TicketDtos.TicketEtat;
import ci.montour.ticket.TicketDtos.TicketFile;
import ci.montour.utilisateur.Utilisateur;
import ci.montour.utilisateur.UtilisateurRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Le coeur de MonTour : toutes les règles métier des tickets.
 */
@Service
@Transactional
public class TicketService {

    private final TicketRepository tickets;
    private final ServiceAccueilRepository services;
    private final GuichetRepository guichets;
    private final UtilisateurRepository utilisateurs;
    private final ApplicationEventPublisher evenements;
    private final Clock clock;

    public TicketService(TicketRepository tickets, ServiceAccueilRepository services, GuichetRepository guichets,
                         UtilisateurRepository utilisateurs, ApplicationEventPublisher evenements, Clock clock) {
        this.tickets = tickets;
        this.services = services;
        this.guichets = guichets;
        this.utilisateurs = utilisateurs;
        this.evenements = evenements;
        this.clock = clock;
    }

    // ---------- Côté client ----------

    public TicketEtat prendreTicket(Long serviceId) {
        // Le verrou garantit que deux clients ne reçoivent jamais le même numéro.
        ServiceAccueil service = services.findWithLockById(serviceId)
                .orElseThrow(() -> ApiException.introuvable("Service introuvable"));
        if (!service.isOuvert()) {
            throw ApiException.conflit("Ce service est fermé pour le moment");
        }
        LocalDate jour = aujourdhui();
        int numero = tickets.findMaxNumero(serviceId, jour) + 1;
        Ticket ticket = tickets.save(new Ticket(service, numero, jour, maintenant()));
        publier(ticket);
        return etat(ticket);
    }

    @Transactional(readOnly = true)
    public TicketEtat etat(UUID jeton) {
        return etat(trouverParJeton(jeton));
    }

    public void annuler(UUID jeton) {
        Ticket ticket = trouverParJeton(jeton);
        ticket.annuler(maintenant());
        publier(ticket);
    }

    // ---------- Côté agent ----------

    @Transactional(readOnly = true)
    public List<GuichetOption> guichetsDisponibles(String etablissementSlug) {
        return guichets.findByServiceEtablissementSlugAndActifTrueOrderByNomAsc(etablissementSlug).stream()
                .map(g -> new GuichetOption(g.getId(), g.getNom(), g.getService().getId(), g.getService().getNom()))
                .toList();
    }

    public TicketAgent appelerSuivant(Long guichetId, String emailAgent) {
        Utilisateur agent = trouverAgent(emailAgent);
        Guichet guichet = guichets.findById(guichetId)
                .orElseThrow(() -> ApiException.introuvable("Guichet introuvable"));
        ServiceAccueil service = guichet.getService();
        verifierEtablissement(service, agent);
        if (!guichet.isActif()) {
            throw ApiException.conflit("Ce guichet est désactivé");
        }

        // Le client en cours à ce guichet est considéré comme servi.
        tickets.findFirstByGuichetIdAndStatut(guichetId, StatutTicket.APPELE).ifPresent(courant -> {
            courant.marquerServi(maintenant());
            publier(courant);
        });

        // Verrou : deux agents du même service ne peuvent pas appeler le même client.
        services.findWithLockById(service.getId());
        Ticket suivant = tickets.findFirstByServiceIdAndJourAndStatutOrderByNumeroAsc(
                        service.getId(), aujourdhui(), StatutTicket.EN_ATTENTE)
                .orElseThrow(() -> ApiException.introuvable("Aucun client en attente"));
        suivant.appeler(guichet, agent, maintenant());
        publier(suivant);
        return TicketAgent.depuis(suivant);
    }

    @Transactional(readOnly = true)
    public Optional<TicketAgent> ticketEnCours(Long guichetId) {
        return tickets.findFirstByGuichetIdAndStatut(guichetId, StatutTicket.APPELE).map(TicketAgent::depuis);
    }

    public TicketAgent rappeler(Long ticketId, String emailAgent) {
        Ticket ticket = trouverPourAgent(ticketId, emailAgent);
        if (ticket.getStatut() != StatutTicket.APPELE) {
            throw ApiException.conflit("Seul un ticket appelé peut être rappelé");
        }
        publier(ticket);
        return TicketAgent.depuis(ticket);
    }

    public TicketAgent marquerServi(Long ticketId, String emailAgent) {
        Ticket ticket = trouverPourAgent(ticketId, emailAgent);
        ticket.marquerServi(maintenant());
        publier(ticket);
        return TicketAgent.depuis(ticket);
    }

    public TicketAgent marquerAbsent(Long ticketId, String emailAgent) {
        Ticket ticket = trouverPourAgent(ticketId, emailAgent);
        ticket.marquerAbsent(maintenant());
        publier(ticket);
        return TicketAgent.depuis(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketFile> file(Long serviceId) {
        return tickets.findByServiceIdAndJourAndStatutOrderByNumeroAsc(serviceId, aujourdhui(), StatutTicket.EN_ATTENTE)
                .stream()
                .map(t -> new TicketFile(t.getId(), t.getCode(), t.getCreeLe()))
                .toList();
    }

    // ---------- Écran de salle ----------

    @Transactional(readOnly = true)
    public List<Appel> derniersAppels(String etablissementSlug) {
        return tickets.findTop6ByServiceEtablissementSlugAndJourAndAppeleLeNotNullOrderByAppeleLeDesc(
                        etablissementSlug, aujourdhui())
                .stream()
                .map(t -> new Appel(t.getCode(), t.getGuichet() == null ? null : t.getGuichet().getNom(),
                        t.getAppeleLe()))
                .toList();
    }

    // ---------- Calculs ----------

    /** Durée moyenne de service des 10 derniers clients servis, ou la durée par défaut du service. */
    @Transactional(readOnly = true)
    public int dureeMoyenneMinutes(ServiceAccueil service) {
        List<Ticket> derniers = tickets.findTop10ByServiceIdAndStatutOrderByTermineLeDesc(service.getId(),
                StatutTicket.SERVI);
        if (derniers.isEmpty()) {
            return service.getDureeParDefautMin();
        }
        double moyenneSecondes = derniers.stream()
                .mapToLong(t -> Duration.between(t.getAppeleLe(), t.getTermineLe()).toSeconds())
                .average()
                .orElse(0);
        return Math.max(1, (int) Math.ceil(moyenneSecondes / 60));
    }

    @Transactional(readOnly = true)
    public long enAttente(Long serviceId) {
        return tickets.countByServiceIdAndJourAndStatut(serviceId, aujourdhui(), StatutTicket.EN_ATTENTE);
    }

    private TicketEtat etat(Ticket ticket) {
        ServiceAccueil service = ticket.getService();
        int devant = 0;
        if (ticket.getStatut() == StatutTicket.EN_ATTENTE) {
            devant = (int) tickets.countByServiceIdAndJourAndStatutAndNumeroLessThan(
                    service.getId(), ticket.getJour(), StatutTicket.EN_ATTENTE, ticket.getNumero());
        }
        String dernierAppele = tickets
                .findFirstByServiceIdAndJourAndAppeleLeNotNullOrderByAppeleLeDesc(service.getId(), ticket.getJour())
                .map(Ticket::getCode)
                .orElse(null);
        String guichet = ticket.getGuichet() == null ? null : ticket.getGuichet().getNom();
        return new TicketEtat(ticket.getJeton(), ticket.getCode(), ticket.getStatut(), service.getId(),
                service.getNom(), service.getEtablissement().getSlug(), devant,
                devant * dureeMoyenneMinutes(service), dernierAppele, guichet, ticket.getCreeLe(),
                ticket.getAppeleLe());
    }

    // ---------- Outils internes ----------

    private void publier(Ticket ticket) {
        ServiceAccueil service = ticket.getService();
        long attente = tickets.countByServiceIdAndJourAndStatut(service.getId(), ticket.getJour(),
                StatutTicket.EN_ATTENTE);
        String guichet = ticket.getGuichet() == null ? null : ticket.getGuichet().getNom();
        evenements.publishEvent(new TicketModifie(service.getId(), service.getEtablissement().getSlug(),
                ticket.getJeton(), ticket.getCode(), ticket.getStatut(), guichet, attente, maintenant()));
    }

    private Ticket trouverParJeton(UUID jeton) {
        return tickets.findByJeton(jeton).orElseThrow(() -> ApiException.introuvable("Ticket introuvable"));
    }

    private Ticket trouverPourAgent(Long ticketId, String emailAgent) {
        Ticket ticket = tickets.findWithServiceById(ticketId)
                .orElseThrow(() -> ApiException.introuvable("Ticket introuvable"));
        verifierEtablissement(ticket.getService(), trouverAgent(emailAgent));
        return ticket;
    }

    private Utilisateur trouverAgent(String email) {
        return utilisateurs.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Utilisateur inconnu"));
    }

    private void verifierEtablissement(ServiceAccueil service, Utilisateur agent) {
        if (!service.getEtablissement().getId().equals(agent.getEtablissement().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Ce guichet n'appartient pas à votre établissement");
        }
    }

    private LocalDate aujourdhui() {
        return LocalDate.now(clock);
    }

    private LocalDateTime maintenant() {
        return LocalDateTime.now(clock);
    }
}
