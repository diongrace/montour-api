package ci.montour.accueil;

import ci.montour.accueil.AccueilDtos.EtablissementPublic;
import ci.montour.accueil.AccueilDtos.ServicePublic;
import ci.montour.common.ApiException;
import ci.montour.etablissement.Etablissement;
import ci.montour.etablissement.EtablissementRepository;
import ci.montour.ticket.TicketService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccueilService {

    private final EtablissementRepository etablissements;
    private final ServiceAccueilRepository services;
    private final TicketService ticketService;

    public AccueilService(EtablissementRepository etablissements, ServiceAccueilRepository services,
                          TicketService ticketService) {
        this.etablissements = etablissements;
        this.services = services;
        this.ticketService = ticketService;
    }

    public EtablissementPublic etablissement(String slug) {
        Etablissement etablissement = etablissements.findBySlug(slug)
                .orElseThrow(() -> ApiException.introuvable("Établissement introuvable"));
        List<ServicePublic> liste = services.findByEtablissementSlugOrderByIdAsc(slug).stream()
                .map(this::versPublic)
                .toList();
        return new EtablissementPublic(etablissement.getNom(), etablissement.getAdresse(), liste);
    }

    private ServicePublic versPublic(ServiceAccueil service) {
        long enAttente = ticketService.enAttente(service.getId());
        int attente = (int) enAttente * ticketService.dureeMoyenneMinutes(service);
        return new ServicePublic(service.getId(), service.getNom(), service.getPrefixe(), service.isOuvert(),
                enAttente, attente);
    }
}
