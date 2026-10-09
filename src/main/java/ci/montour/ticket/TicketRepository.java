package ci.montour.ticket;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("select coalesce(max(t.numero), 0) from Ticket t where t.service.id = :serviceId and t.jour = :jour")
    int findMaxNumero(@Param("serviceId") Long serviceId, @Param("jour") LocalDate jour);

    @EntityGraph(attributePaths = {"service", "service.etablissement", "guichet"})
    Optional<Ticket> findByJeton(UUID jeton);

    @EntityGraph(attributePaths = {"service", "service.etablissement", "guichet"})
    Optional<Ticket> findWithServiceById(Long id);

    long countByServiceIdAndJourAndStatut(Long serviceId, LocalDate jour, StatutTicket statut);

    long countByServiceIdAndJourAndStatutAndNumeroLessThan(Long serviceId, LocalDate jour, StatutTicket statut,
                                                             int numero);

    Optional<Ticket> findFirstByServiceIdAndJourAndStatutOrderByNumeroAsc(Long serviceId, LocalDate jour,
                                                                           StatutTicket statut);

    List<Ticket> findByServiceIdAndJourAndStatutOrderByNumeroAsc(Long serviceId, LocalDate jour,
                                                                 StatutTicket statut);

    Optional<Ticket> findFirstByServiceIdAndJourAndAppeleLeNotNullOrderByAppeleLeDesc(Long serviceId,
                                                                                       LocalDate jour);

    @EntityGraph(attributePaths = {"service", "guichet"})
    Optional<Ticket> findFirstByGuichetIdAndStatut(Long guichetId, StatutTicket statut);

    List<Ticket> findTop10ByServiceIdAndStatutOrderByTermineLeDesc(Long serviceId, StatutTicket statut);

    @EntityGraph(attributePaths = "guichet")
    List<Ticket> findTop6ByServiceEtablissementSlugAndJourAndAppeleLeNotNullOrderByAppeleLeDesc(String slug,
                                                                                                LocalDate jour);

    List<Ticket> findByServiceEtablissementSlugAndJour(String slug, LocalDate jour);
}
