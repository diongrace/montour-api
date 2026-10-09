package ci.montour.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ci.montour.accueil.ServiceAccueil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Tests unitaires : pas de Spring, pas de base, juste les règles de l'entité. */
class TicketTest {

    private final ServiceAccueil consultation = new ServiceAccueil(null, "Consultation", "C", 5, true);
    private final LocalDateTime maintenant = LocalDateTime.of(2026, 10, 8, 10, 0);

    private Ticket nouveauTicket() {
        return new Ticket(consultation, 7, LocalDate.of(2026, 10, 8), maintenant);
    }

    @Test
    void unNouveauTicketEstEnAttenteAvecUnCodeLisible() {
        Ticket ticket = nouveauTicket();

        assertThat(ticket.getCode()).isEqualTo("C-007");
        assertThat(ticket.getStatut()).isEqualTo(StatutTicket.EN_ATTENTE);
        assertThat(ticket.getJeton()).isNotNull();
    }

    @Test
    void unTicketAppelePuisServiEnregistreLesHeures() {
        Ticket ticket = nouveauTicket();

        ticket.appeler(null, null, maintenant.plusMinutes(10));
        ticket.marquerServi(maintenant.plusMinutes(15));

        assertThat(ticket.getStatut()).isEqualTo(StatutTicket.SERVI);
        assertThat(ticket.getAppeleLe()).isEqualTo(maintenant.plusMinutes(10));
        assertThat(ticket.getTermineLe()).isEqualTo(maintenant.plusMinutes(15));
    }

    @Test
    void onNePeutPasServirUnTicketQuiNaPasEteAppele() {
        Ticket ticket = nouveauTicket();

        assertThatThrownBy(() -> ticket.marquerServi(maintenant))
                .isInstanceOf(TransitionInterditeException.class);
    }

    @Test
    void onNePeutPasAnnulerUnTicketDejaAppele() {
        Ticket ticket = nouveauTicket();
        ticket.appeler(null, null, maintenant);

        assertThatThrownBy(() -> ticket.annuler(maintenant))
                .isInstanceOf(TransitionInterditeException.class);
    }
}
