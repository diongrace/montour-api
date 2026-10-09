package ci.montour.accueil;

import java.util.List;

public final class AccueilDtos {

    private AccueilDtos() {
    }

    public record EtablissementPublic(String nom, String adresse, List<ServicePublic> services) {
    }

    public record ServicePublic(Long id, String nom, String prefixe, boolean ouvert, long enAttente,
                                int attenteEstimeeMin) {
    }
}
