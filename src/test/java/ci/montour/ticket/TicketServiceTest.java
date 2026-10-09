package ci.montour.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ci.montour.accueil.GuichetRepository;
import ci.montour.accueil.ServiceAccueil;
import ci.montour.accueil.ServiceAccueilRepository;
import ci.montour.common.ApiException;
import ci.montour.ticket.TicketDtos.TicketAgent;
import ci.montour.ticket.TicketDtos.TicketEtat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests d'intégration : vrai contexte Spring, vraie base (H2 en mémoire, migrations Flyway).
 * Chaque test est annulé (rollback) à la fin : ils ne se gênent pas entre eux.
 */
@SpringBootTest
@Transactional
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ServiceAccueilRepository services;

    @Autowired
    private GuichetRepository guichets;

    private ServiceAccueil consultation;
    private ServiceAccueil laboratoire;

    @BeforeEach
    void chargerLesServicesDeDemo() {
        List<ServiceAccueil> liste = services.findByEtablissementSlugOrderByIdAsc("centre-demo");
        consultation = liste.get(0);
        laboratoire = liste.get(2);
    }

    @Test
    void lesTicketsSontNumerotesDansLOrdreEtLAttenteEstEstimee() {
        TicketEtat premier = ticketService.prendreTicket(consultation.getId());
        TicketEtat second = ticketService.prendreTicket(consultation.getId());

        assertThat(premier.code()).isEqualTo("C-001");
        assertThat(second.code()).isEqualTo("C-002");
        assertThat(second.personnesDevant()).isEqualTo(1);
        assertThat(second.attenteEstimeeMin()).isEqualTo(5);
    }

    @Test
    void unServiceFermeNeDonnePasDeTicket() {
        assertThatThrownBy(() -> ticketService.prendreTicket(laboratoire.getId()))
                .isInstanceOf(ApiException.class)
                .hasMessage("Ce service est fermé pour le moment");
    }

    @Test
    void lAgentAppelleLePremierArrive() {
        TicketEtat premier = ticketService.prendreTicket(consultation.getId());
        TicketEtat second = ticketService.prendreTicket(consultation.getId());
        Long guichetId = guichets.findByServiceIdOrderByNomAsc(consultation.getId()).get(0).getId();

        TicketAgent appele = ticketService.appelerSuivant(guichetId, "agent@montour.ci");

        assertThat(appele.code()).isEqualTo(premier.code());
        TicketEtat etatSecond = ticketService.etat(second.jeton());
        assertThat(etatSecond.personnesDevant()).isZero();
        assertThat(etatSecond.dernierNumeroAppele()).isEqualTo("C-001");
    }

    @Test
    void appelerQuandPersonneNAttendRenvoieUneErreurClaire() {
        Long guichetId = guichets.findByServiceIdOrderByNomAsc(consultation.getId()).get(0).getId();

        assertThatThrownBy(() -> ticketService.appelerSuivant(guichetId, "agent@montour.ci"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Aucun client en attente");
    }
}
