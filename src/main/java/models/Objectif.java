package models;

import java.sql.Date;

public class Objectif {

    private int id;
    private Date dateDebut;
    private Date dateFin;
    private String titre;
    private String description;
    private Date dateCreation;
    private String statutObjectif;
    private int progression;
    private String typeObjectif;
    private double valeurCible;
    private Integer idActivite;
    private int userId;

    public Objectif() {
    }

    public Objectif(int id, Date dateDebut, Date dateFin, String titre, String description,
                    Date dateCreation, String statutObjectif, int progression,
                    String typeObjectif, double valeurCible, Integer idActivite, int userId) {
        this.id = id;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.titre = titre;
        this.description = description;
        this.dateCreation = dateCreation;
        this.statutObjectif = statutObjectif;
        this.progression = progression;
        this.typeObjectif = typeObjectif;
        this.valeurCible = valeurCible;
        this.idActivite = idActivite;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public String getStatutObjectif() {
        return statutObjectif;
    }

    public int getProgression() {
        return progression;
    }

    public String getTypeObjectif() {
        return typeObjectif;
    }

    public double getValeurCible() {
        return valeurCible;
    }

    public Integer getIdActivite() {
        return idActivite;
    }

    public int getUserId() {
        return userId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public void setStatutObjectif(String statutObjectif) {
        this.statutObjectif = statutObjectif;
    }

    public void setProgression(int progression) {
        this.progression = progression;
    }

    public void setTypeObjectif(String typeObjectif) {
        this.typeObjectif = typeObjectif;
    }

    public void setValeurCible(double valeurCible) {
        this.valeurCible = valeurCible;
    }

    public void setIdActivite(Integer idActivite) {
        this.idActivite = idActivite;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Alias pour compatibilité avec le reste du projet
    public void setStatut(String statut) {
        this.statutObjectif = statut;
    }

    public String getStatut() {
        return statutObjectif;
    }

    @Override
    public String toString() {
        return "Objectif{" +
                "id=" + id +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dateCreation=" + dateCreation +
                ", statutObjectif='" + statutObjectif + '\'' +
                ", progression=" + progression +
                ", typeObjectif='" + typeObjectif + '\'' +
                ", valeurCible=" + valeurCible +
                ", idActivite=" + idActivite +
                ", userId=" + userId +
                '}';
    }
}