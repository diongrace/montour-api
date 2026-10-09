package ci.montour.accueil;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuichetRepository extends JpaRepository<Guichet, Long> {

    @EntityGraph(attributePaths = "service")
    List<Guichet> findByServiceEtablissementSlugAndActifTrueOrderByNomAsc(String slug);

    List<Guichet> findByServiceIdOrderByNomAsc(Long serviceId);
}
