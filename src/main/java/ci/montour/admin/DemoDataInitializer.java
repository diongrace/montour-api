package ci.montour.admin;

import ci.montour.config.MontourProperties;
import ci.montour.etablissement.Etablissement;
import ci.montour.etablissement.EtablissementRepository;
import ci.montour.utilisateur.Role;
import ci.montour.utilisateur.Utilisateur;
import ci.montour.utilisateur.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crée les comptes de démonstration au premier démarrage.
 * Les mots de passe sont hachés par BCrypt ici, jamais écrits en clair dans un script SQL.
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final EtablissementRepository etablissements;
    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder passwordEncoder;
    private final MontourProperties properties;

    public DemoDataInitializer(EtablissementRepository etablissements, UtilisateurRepository utilisateurs,
                               PasswordEncoder passwordEncoder, MontourProperties properties) {
        this.etablissements = etablissements;
        this.utilisateurs = utilisateurs;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (utilisateurs.count() > 0) {
            return;
        }
        etablissements.findBySlug("centre-demo").ifPresent(this::creerComptes);
    }

    private void creerComptes(Etablissement etablissement) {
        String hash = passwordEncoder.encode(properties.demo().motDePasse());
        utilisateurs.save(new Utilisateur(etablissement, "Admin Démo", "admin@montour.ci", hash, Role.ADMIN));
        utilisateurs.save(new Utilisateur(etablissement, "Awa Koné", "agent@montour.ci", hash, Role.AGENT));
    }
}
