package ci.montour.accueil;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "guichet")
public class Guichet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id")
    private ServiceAccueil service;

    private String nom;

    private boolean actif;

    protected Guichet() {
        // requis par JPA
    }

    public Guichet(ServiceAccueil service, String nom) {
        this.service = service;
        this.nom = nom;
        this.actif = true;
    }

    public void modifier(String nom, boolean actif) {
        this.nom = nom;
        this.actif = actif;
    }

    public Long getId() {
        return id;
    }

    public ServiceAccueil getService() {
        return service;
    }

    public String getNom() {
        return nom;
    }

    public boolean isActif() {
        return actif;
    }
}
