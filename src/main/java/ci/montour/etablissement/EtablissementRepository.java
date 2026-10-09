package ci.montour.etablissement;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtablissementRepository extends JpaRepository<Etablissement, Long> {

    Optional<Etablissement> findBySlug(String slug);
}
