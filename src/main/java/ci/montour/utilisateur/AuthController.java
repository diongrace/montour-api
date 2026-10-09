package ci.montour.utilisateur;

import ci.montour.common.ApiException;
import ci.montour.utilisateur.AuthDtos.LoginRequest;
import ci.montour.utilisateur.AuthDtos.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(UtilisateurRepository utilisateurs, PasswordEncoder passwordEncoder,
                          TokenService tokenService) {
        this.utilisateurs = utilisateurs;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest requete) {
        Utilisateur utilisateur = utilisateurs.findByEmail(requete.email().trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(requete.motDePasse(), u.getMotDePasseHash()))
                // Même message que l'email ou le mot de passe soit faux : on ne révèle pas quels comptes existent.
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"));
        return new LoginResponse(tokenService.generer(utilisateur), utilisateur.getNom(), utilisateur.getRole(),
                utilisateur.getEtablissement().getSlug());
    }
}
