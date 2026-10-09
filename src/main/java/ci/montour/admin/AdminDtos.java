package ci.montour.admin;

import ci.montour.utilisateur.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record ServiceRequest(
            @NotBlank(message = "Le nom est obligatoire") @Size(max = 100) String nom,
            @NotBlank(message = "Le préfixe est obligatoire")
            @Pattern(regexp = "[A-Z]{1,3}", message = "1 à 3 lettres majuscules") String prefixe,
            @Min(value = 1, message = "Entre 1 et 120 minutes")
            @Max(value = 120, message = "Entre 1 et 120 minutes") int dureeParDefautMin,
            boolean ouvert) {
    }

    public record GuichetRequest(@NotBlank(message = "Le nom est obligatoire") @Size(max = 60) String nom,
                                 boolean actif) {
    }

    public record AgentRequest(
            @NotBlank(message = "Le nom est obligatoire") String nom,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            @Size(min = 8, message = "8 caractères minimum") String motDePasse,
            @NotNull(message = "Le rôle est obligatoire") Role role) {
    }

    public record GuichetAdmin(Long id, String nom, boolean actif) {
    }

    public record ServiceAdmin(Long id, String nom, String prefixe, boolean ouvert, int dureeParDefautMin,
                               long enAttente, List<GuichetAdmin> guichets) {
    }

    public record AgentAdmin(Long id, String nom, String email, Role role) {
    }

    public record StatsJour(long ticketsPris, long servis, long absents, long annules, long enAttente,
                            int attenteMoyenneMin, long serviceMoyenSecondes) {
    }
}
