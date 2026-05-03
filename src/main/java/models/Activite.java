package models;

import java.time.LocalDate;

public class Activite {
    private int idActivite;
    private String titre;
    private String description;
    private String type;
    private String adresse;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double latitude;
    private double longitude;

    public Activite() {
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Activite(int idActivite, String titre, String description, String type,
                    String adresse, LocalDate dateDebut, LocalDate dateFin,
                    double latitude, double longitude) {
        this.idActivite = idActivite;
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.adresse = adresse;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Activite(String titre, String description, String type,
                    String adresse, LocalDate dateDebut, LocalDate dateFin) {
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.adresse = adresse;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Activite{" +
                "idActivite=" + idActivite +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", adresse='" + adresse + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
