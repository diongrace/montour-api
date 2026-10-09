package ci.montour.accueil;

import ci.montour.etablissement.Etablissement;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Un service de l'établissement (Consultation, Pharmacie...) avec sa propre file d'attente.
 * Nommé ServiceAccueil pour ne pas entrer en conflit avec l'annotation @Service de Spring.
 */
@Entity
@Table(name = "service_accueil")
public class ServiceAccueil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etablissement_id")
    private Etablissement etablissement;

    private String nom;

    private String prefixe;

    private boolean ouvert;

    private int dureeParDefautMin;

    protected ServiceAccueil() {
        // requis par JPA
    }

    public ServiceAccueil(Etablissement etablissement, String nom, String prefixe, int dureeParDefautMin,
                          boolean ouvert) {
        this.etablissement = etablissement;
        this.nom = nom;
        this.prefixe = prefixe;
        this.dureeParDefautMin = dureeParDefautMin;
        this.ouvert = ouvert;
    }

    public void modifier(String nom, String prefixe, int dureeParDefautMin, boolean ouvert) {
        this.nom = nom;
        this.prefixe = prefixe;
        this.dureeParDefautMin = dureeParDefautMin;
        this.ouvert = ouvert;
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

    public String getPrefixe() {
        return prefixe;
    }

    public boolean isOuvert() {
        return ouvert;
    }

    public int getDureeParDefautMin() {
        return dureeParDefautMin;
    }
}
