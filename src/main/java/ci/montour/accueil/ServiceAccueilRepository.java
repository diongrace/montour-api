package ci.montour.accueil;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ServiceAccueilRepository extends JpaRepository<ServiceAccueil, Long> {

    List<ServiceAccueil> findByEtablissementSlugOrderByIdAsc(String slug);

    /**
     * Verrouille la ligne du service jusqu'à la fin de la transaction :
     * deux prises de ticket simultanées sur le même service passent l'une après l'autre.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ServiceAccueil> findWithLockById(Long id);
}
