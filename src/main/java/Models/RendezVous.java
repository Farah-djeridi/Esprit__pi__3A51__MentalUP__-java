package Models;

import java.sql.Date;
import java.sql.Time;

public class RendezVous {

    private int id;
    private Date date;
    private Time heureDebut;
    private Time heureFin;
    private String typeRdv;
    private String statut;
    private String lienMeet;
    private String lieu;
    private String telephone;
    private int psychologueId;
    private Integer etudiantId; // nullable

    // 🔹 Constructor (empty)
    public RendezVous() {
    }

    // 🔹 Constructor (with parameters)
    public RendezVous(int id, Date date, Time heureDebut, Time heureFin,
                      String typeRdv, String statut, String lienMeet,
                      String lieu, String telephone,
                      int psychologueId, Integer etudiantId) {
        this.id = id;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.typeRdv = typeRdv;
        this.statut = statut;
        this.lienMeet = lienMeet;
        this.lieu = lieu;
        this.telephone = telephone;
        this.psychologueId = psychologueId;
        this.etudiantId = etudiantId;
    }

    // 🔹 Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(Time heureDebut) {
        this.heureDebut = heureDebut;
    }

    public Time getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(Time heureFin) {
        this.heureFin = heureFin;
    }

    public String getTypeRdv() {
        return typeRdv;
    }

    public void setTypeRdv(String typeRdv) {
        this.typeRdv = typeRdv;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getLienMeet() {
        return lienMeet;
    }

    public void setLienMeet(String lienMeet) {
        this.lienMeet = lienMeet;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getPsychologueId() {
        return psychologueId;
    }

    public void setPsychologueId(int psychologueId) {
        this.psychologueId = psychologueId;
    }

    public Integer getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Integer etudiantId) {
        this.etudiantId = etudiantId;
    }

    // 🔹 toString()

    // 🔹 toString()

    public String getMode() {
        return "En ligne".equalsIgnoreCase(this.lieu) ? "En ligne" : "Présentiel";
    }

    public void setMode(String mode) {
        this.lieu = mode;
    }

    public boolean isPsyJoined() {
        return "en cours".equalsIgnoreCase(this.statut);
    }

    public void setPsyJoined(boolean joined) {
        if (joined) this.statut = "en cours";
        else if ("en cours".equalsIgnoreCase(this.statut)) this.statut = "confirmé";
    }

    @Override
    public String toString() {
        return "RendezVous{" +
                "id=" + id +
                ", date=" + date +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                ", typeRdv='" + typeRdv + '\'' +
                ", statut='" + statut + '\'' +
                ", lienMeet='" + lienMeet + '\'' +
                ", lieu='" + lieu + '\'' +
                ", telephone='" + telephone + '\'' +
                ", psychologueId=" + psychologueId +
                ", etudiantId=" + etudiantId +
                '}';
    }


}