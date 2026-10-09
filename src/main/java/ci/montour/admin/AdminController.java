package ci.montour.admin;

import ci.montour.admin.AdminDtos.AgentAdmin;
import ci.montour.admin.AdminDtos.AgentRequest;
import ci.montour.admin.AdminDtos.GuichetAdmin;
import ci.montour.admin.AdminDtos.GuichetRequest;
import ci.montour.admin.AdminDtos.ServiceAdmin;
import ci.montour.admin.AdminDtos.ServiceRequest;
import ci.montour.admin.AdminDtos.StatsJour;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/services")
    public List<ServiceAdmin> services(@AuthenticationPrincipal Jwt jwt) {
        return adminService.services(slug(jwt));
    }

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceAdmin creerService(@Valid @RequestBody ServiceRequest requete, @AuthenticationPrincipal Jwt jwt) {
        return adminService.creerService(slug(jwt), requete);
    }

    @PutMapping("/services/{serviceId}")
    public ServiceAdmin modifierService(@PathVariable Long serviceId, @Valid @RequestBody ServiceRequest requete,
                                        @AuthenticationPrincipal Jwt jwt) {
        return adminService.modifierService(slug(jwt), serviceId, requete);
    }

    @PostMapping("/services/{serviceId}/guichets")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceAdmin ajouterGuichet(@PathVariable Long serviceId, @Valid @RequestBody GuichetRequest requete,
                                       @AuthenticationPrincipal Jwt jwt) {
        return adminService.ajouterGuichet(slug(jwt), serviceId, requete);
    }

    @PutMapping("/guichets/{guichetId}")
    public GuichetAdmin modifierGuichet(@PathVariable Long guichetId, @Valid @RequestBody GuichetRequest requete,
                                        @AuthenticationPrincipal Jwt jwt) {
        return adminService.modifierGuichet(slug(jwt), guichetId, requete);
    }

    @GetMapping("/agents")
    public List<AgentAdmin> agents(@AuthenticationPrincipal Jwt jwt) {
        return adminService.agents(slug(jwt));
    }

    @PostMapping("/agents")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentAdmin creerAgent(@Valid @RequestBody AgentRequest requete, @AuthenticationPrincipal Jwt jwt) {
        return adminService.creerAgent(slug(jwt), requete);
    }

    @GetMapping("/stats/jour")
    public StatsJour statsDuJour(@AuthenticationPrincipal Jwt jwt) {
        return adminService.statsDuJour(slug(jwt));
    }

    private String slug(Jwt jwt) {
        return jwt.getClaimAsString("etablissement");
    }
}
