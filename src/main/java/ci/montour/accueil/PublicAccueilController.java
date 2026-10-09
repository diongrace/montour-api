package ci.montour.accueil;

import ci.montour.accueil.AccueilDtos.EtablissementPublic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/etablissements")
public class PublicAccueilController {

    private final AccueilService accueilService;

    public PublicAccueilController(AccueilService accueilService) {
        this.accueilService = accueilService;
    }

    @GetMapping("/{slug}")
    public EtablissementPublic etablissement(@PathVariable String slug) {
        return accueilService.etablissement(slug);
    }
}
