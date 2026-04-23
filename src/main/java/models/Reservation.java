package models;

import java.time.LocalDate;

public class Reservation {
    private int idReservation;
    private int idActivite;
    private String nomEtudiant;
    private String place;
    private LocalDate dateReservation;
    private String titreActivite;
    private String statut; // "EN_ATTENTE", "ACCEPTEE", "REFUSEE"

    public Reservation() { this.statut = "EN_ATTENTE"; }

    public Reservation(int idActivite, String nomEtudiant, String place, LocalDate dateReservation) {
        this.idActivite = idActivite;
        this.nomEtudiant = nomEtudiant;
        this.place = place;
        this.dateReservation = dateReservation;
        this.statut = "EN_ATTENTE";
    }

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }
    public int getIdActivite() { return idActivite; }
    public void setIdActivite(int idActivite) { this.idActivite = idActivite; }
    public String getNomEtudiant() { return nomEtudiant; }
    public void setNomEtudiant(String nomEtudiant) { this.nomEtudiant = nomEtudiant; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public LocalDate getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDate dateReservation) { this.dateReservation = dateReservation; }
    public String getTitreActivite() { return titreActivite; }
    public void setTitreActivite(String titreActivite) { this.titreActivite = titreActivite; }
    public String getStatut() { return statut != null ? statut : "EN_ATTENTE"; }
    public void setStatut(String statut) { this.statut = statut; }
}
