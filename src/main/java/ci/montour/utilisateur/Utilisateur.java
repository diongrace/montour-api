package ci.montour.utilisateur;

import ci.montour.etablissement.Etablissement;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etablissement_id")
    private Etablissement etablissement;

    private String nom;

    private String email;

    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected Utilisateur() {
        // requis par JPA
    }

    public Utilisateur(Etablissement etablissement, String nom, String email, String motDePasseHash, Role role) {
        this.etablissement = etablissement;
        this.nom = nom;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public Role getRole() {
        return role;
    }
}
