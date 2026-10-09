package ci.montour.admin;

import ci.montour.admin.AdminDtos.StatsJour;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Les chiffres du jour affichés en bas du poste agent (maquette 5). */
@RestController
public class StatsAgentController {

    private final AdminService adminService;

    public StatsAgentController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/api/agent/stats/jour")
    public StatsJour statsDuJour(@AuthenticationPrincipal Jwt jwt) {
        return adminService.statsDuJour(jwt.getClaimAsString("etablissement"));
    }
}
