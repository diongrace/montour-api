package ci.montour.ticket;

import ci.montour.accueil.Guichet;
import ci.montour.accueil.ServiceAccueil;
import ci.montour.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id")
    private ServiceAccueil service;

    private int numero;

    private String code;

    private LocalDate jour;

    @Enumerated(EnumType.STRING)
    private StatutTicket statut;

    @Column(name = "jeton_client")
    private UUID jeton;

    private LocalDateTime creeLe;

    private LocalDateTime appeleLe;

    private LocalDateTime termineLe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guichet_id")
    private Guichet guichet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Utilisateur agent;

    protected Ticket() {
        // requis par JPA
    }

    public Ticket(ServiceAccueil service, int numero, LocalDate jour, LocalDateTime maintenant) {
        this.service = service;
        this.numero = numero;
        this.code = service.getPrefixe() + "-" + String.format("%03d", numero);
        this.jour = jour;
        this.statut = StatutTicket.EN_ATTENTE;
        this.jeton = UUID.randomUUID();
        this.creeLe = maintenant;
    }

    // Les règles de transition vivent dans l'entité : aucun code ne peut les contourner.

    public void appeler(Guichet guichet, Utilisateur agent, LocalDateTime maintenant) {
        verifier(StatutTicket.EN_ATTENTE, StatutTicket.APPELE);
        this.statut = StatutTicket.APPELE;
        this.guichet = guichet;
        this.agent = agent;
        this.appeleLe = maintenant;
    }

    public void marquerServi(LocalDateTime maintenant) {
        verifier(StatutTicket.APPELE, StatutTicket.SERVI);
        this.statut = StatutTicket.SERVI;
        this.termineLe = maintenant;
    }

    public void marquerAbsent(LocalDateTime maintenant) {
        verifier(StatutTicket.APPELE, StatutTicket.ABSENT);
        this.statut = StatutTicket.ABSENT;
        this.termineLe = maintenant;
    }

    public void annuler(LocalDateTime maintenant) {
        verifier(StatutTicket.EN_ATTENTE, StatutTicket.ANNULE);
        this.statut = StatutTicket.ANNULE;
        this.termineLe = maintenant;
    }

    private void verifier(StatutTicket attendu, StatutTicket cible) {
        if (statut != attendu) {
            throw new TransitionInterditeException(statut, cible);
        }
    }

    public Long getId() {
        return id;
    }

    public ServiceAccueil getService() {
        return service;
    }

    public int getNumero() {
        return numero;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getJour() {
        return jour;
    }

    public StatutTicket getStatut() {
        return statut;
    }

    public UUID getJeton() {
        return jeton;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }

    public LocalDateTime getAppeleLe() {
        return appeleLe;
    }

    public LocalDateTime getTermineLe() {
        return termineLe;
    }

    public Guichet getGuichet() {
        return guichet;
    }

    public Utilisateur getAgent() {
        return agent;
    }
}
