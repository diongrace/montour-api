package ci.montour.admin;

import ci.montour.accueil.Guichet;
import ci.montour.accueil.GuichetRepository;
import ci.montour.accueil.ServiceAccueil;
import ci.montour.accueil.ServiceAccueilRepository;
import ci.montour.admin.AdminDtos.AgentAdmin;
import ci.montour.admin.AdminDtos.AgentRequest;
import ci.montour.admin.AdminDtos.GuichetAdmin;
import ci.montour.admin.AdminDtos.GuichetRequest;
import ci.montour.admin.AdminDtos.ServiceAdmin;
import ci.montour.admin.AdminDtos.ServiceRequest;
import ci.montour.admin.AdminDtos.StatsJour;
import ci.montour.common.ApiException;
import ci.montour.etablissement.Etablissement;
import ci.montour.etablissement.EtablissementRepository;
import ci.montour.ticket.StatutTicket;
import ci.montour.ticket.Ticket;
import ci.montour.ticket.TicketRepository;
import ci.montour.ticket.TicketService;
import ci.montour.utilisateur.Utilisateur;
import ci.montour.utilisateur.UtilisateurRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configuration de l'établissement. Chaque méthode reçoit le slug de l'établissement de l'admin
 * (lu dans son JWT) : un admin ne peut jamais modifier un autre établissement.
 */
@Service
@Transactional
public class AdminService {

    private final EtablissementRepository etablissements;
    private final ServiceAccueilRepository services;
    private final GuichetRepository guichets;
    private final UtilisateurRepository utilisateurs;
    private final TicketRepository tickets;
    private final TicketService ticketService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AdminService(EtablissementRepository etablissements, ServiceAccueilRepository services,
                        GuichetRepository guichets, UtilisateurRepository utilisateurs, TicketRepository tickets,
                        TicketService ticketService, PasswordEncoder passwordEncoder, Clock clock) {
        this.etablissements = etablissements;
        this.services = services;
        this.guichets = guichets;
        this.utilisateurs = utilisateurs;
        this.tickets = tickets;
        this.ticketService = ticketService;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    // ---------- Services et guichets ----------

    @Transactional(readOnly = true)
    public List<ServiceAdmin> services(String slug) {
        return services.findByEtablissementSlugOrderByIdAsc(slug).stream().map(this::versAdmin).toList();
    }

    public ServiceAdmin creerService(String slug, ServiceRequest requete) {
        ServiceAccueil service = new ServiceAccueil(etablissement(slug), requete.nom().trim(), requete.prefixe(),
                requete.dureeParDefautMin(), requete.ouvert());
        return versAdmin(services.save(service));
    }

    public ServiceAdmin modifierService(String slug, Long serviceId, ServiceRequest requete) {
        ServiceAccueil service = serviceDe(slug, serviceId);
        service.modifier(requete.nom().trim(), requete.prefixe(), requete.dureeParDefautMin(), requete.ouvert());
        return versAdmin(service);
    }

    public ServiceAdmin ajouterGuichet(String slug, Long serviceId, GuichetRequest requete) {
        ServiceAccueil service = serviceDe(slug, serviceId);
        guichets.save(new Guichet(service, requete.nom().trim()));
        return versAdmin(service);
    }

    public GuichetAdmin modifierGuichet(String slug, Long guichetId, GuichetRequest requete) {
        Guichet guichet = guichets.findById(guichetId)
                .filter(g -> g.getService().getEtablissement().getSlug().equals(slug))
                .orElseThrow(() -> ApiException.introuvable("Guichet introuvable"));
        guichet.modifier(requete.nom().trim(), requete.actif());
        return new GuichetAdmin(guichet.getId(), guichet.getNom(), guichet.isActif());
    }

    // ---------- Agents ----------

    @Transactional(readOnly = true)
    public List<AgentAdmin> agents(String slug) {
        return utilisateurs.findByEtablissementSlugOrderByNomAsc(slug).stream()
                .map(u -> new AgentAdmin(u.getId(), u.getNom(), u.getEmail(), u.getRole()))
                .toList();
    }

    public AgentAdmin creerAgent(String slug, AgentRequest requete) {
        String email = requete.email().trim().toLowerCase();
        if (utilisateurs.existsByEmail(email)) {
            throw ApiException.conflit("Un compte existe déjà avec cet email");
        }
        Utilisateur agent = utilisateurs.save(new Utilisateur(etablissement(slug), requete.nom().trim(), email,
                passwordEncoder.encode(requete.motDePasse()), requete.role()));
        return new AgentAdmin(agent.getId(), agent.getNom(), agent.getEmail(), agent.getRole());
    }

    // ---------- Statistiques du jour ----------

    @Transactional(readOnly = true)
    public StatsJour statsDuJour(String slug) {
        List<Ticket> duJour = tickets.findByServiceEtablissementSlugAndJour(slug, LocalDate.now(clock));
        double attenteMoyenneSecondes = duJour.stream()
                .filter(t -> t.getAppeleLe() != null)
                .mapToLong(t -> Duration.between(t.getCreeLe(), t.getAppeleLe()).toSeconds())
                .average()
                .orElse(0);
        double serviceMoyenSecondes = duJour.stream()
                .filter(t -> t.getStatut() == StatutTicket.SERVI)
                .mapToLong(t -> Duration.between(t.getAppeleLe(), t.getTermineLe()).toSeconds())
                .average()
                .orElse(0);
        return new StatsJour(
                duJour.size(),
                compter(duJour, StatutTicket.SERVI),
                compter(duJour, StatutTicket.ABSENT),
                compter(duJour, StatutTicket.ANNULE),
                compter(duJour, StatutTicket.EN_ATTENTE),
                (int) Math.round(attenteMoyenneSecondes / 60),
                Math.round(serviceMoyenSecondes));
    }

    // ---------- Outils internes ----------

    private long compter(List<Ticket> liste, StatutTicket statut) {
        return liste.stream().filter(t -> t.getStatut() == statut).count();
    }

    private Etablissement etablissement(String slug) {
        return etablissements.findBySlug(slug)
                .orElseThrow(() -> ApiException.introuvable("Établissement introuvable"));
    }

    private ServiceAccueil serviceDe(String slug, Long serviceId) {
        ServiceAccueil service = services.findById(serviceId)
                .orElseThrow(() -> ApiException.introuvable("Service introuvable"));
        if (!service.getEtablissement().getSlug().equals(slug)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Ce service n'appartient pas à votre établissement");
        }
        return service;
    }

    private ServiceAdmin versAdmin(ServiceAccueil service) {
        List<GuichetAdmin> liste = guichets.findByServiceIdOrderByNomAsc(service.getId()).stream()
                .map(g -> new GuichetAdmin(g.getId(), g.getNom(), g.isActif()))
                .toList();
        return new ServiceAdmin(service.getId(), service.getNom(), service.getPrefixe(), service.isOuvert(),
                service.getDureeParDefautMin(), ticketService.enAttente(service.getId()), liste);
    }
}
