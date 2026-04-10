package models;

import java.sql.Date;

public class SuiviMentale {

    private int id;
    private int scoreMentale;
    private int tauxDeStress;
    private int tauxDeStressGlobale;
    private Date dateDeSuivi;
    private String qualiteDuSommeil;
    private String journalEmotionnelle;
    private double heureDeSommeil;
    private String humeur;
    private int niveauDenergie;
    private int userId;
    private int objectifId;

    public SuiviMentale() {
    }

    public SuiviMentale(int id, int scoreMentale, int tauxDeStress, int tauxDeStressGlobale,
                        Date dateDeSuivi, String qualiteDuSommeil, String journalEmotionnelle,
                        double heureDeSommeil, String humeur, int niveauDenergie,
                        int userId, int objectifId) {
        this.id = id;
        this.scoreMentale = scoreMentale;
        this.tauxDeStress = tauxDeStress;
        this.tauxDeStressGlobale = tauxDeStressGlobale;
        this.dateDeSuivi = dateDeSuivi;
        this.qualiteDuSommeil = qualiteDuSommeil;
        this.journalEmotionnelle = journalEmotionnelle;
        this.heureDeSommeil = heureDeSommeil;
        this.humeur = humeur;
        this.niveauDenergie = niveauDenergie;
        this.userId = userId;
        this.objectifId = objectifId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getScoreMentale() {
        return scoreMentale;
    }

    public void setScoreMentale(int scoreMentale) {
        this.scoreMentale = scoreMentale;
    }

    public int getTauxDeStress() {
        return tauxDeStress;
    }

    public void setTauxDeStress(int tauxDeStress) {
        this.tauxDeStress = tauxDeStress;
    }

    public int getTauxDeStressGlobale() {
        return tauxDeStressGlobale;
    }

    public void setTauxDeStressGlobale(int tauxDeStressGlobale) {
        this.tauxDeStressGlobale = tauxDeStressGlobale;
    }

    public Date getDateDeSuivi() {
        return dateDeSuivi;
    }

    public void setDateDeSuivi(Date dateDeSuivi) {
        this.dateDeSuivi = dateDeSuivi;
    }

    public String getQualiteDuSommeil() {
        return qualiteDuSommeil;
    }

    public void setQualiteDuSommeil(String qualiteDuSommeil) {
        this.qualiteDuSommeil = qualiteDuSommeil;
    }

    public String getJournalEmotionnelle() {
        return journalEmotionnelle;
    }

    public void setJournalEmotionnelle(String journalEmotionnelle) {
        this.journalEmotionnelle = journalEmotionnelle;
    }

    public double getHeureDeSommeil() {
        return heureDeSommeil;
    }

    public void setHeureDeSommeil(double heureDeSommeil) {
        this.heureDeSommeil = heureDeSommeil;
    }

    public String getHumeur() {
        return humeur;
    }

    public void setHumeur(String humeur) {
        this.humeur = humeur;
    }

    public int getNiveauDenergie() {
        return niveauDenergie;
    }

    public void setNiveauDenergie(int niveauDenergie) {
        this.niveauDenergie = niveauDenergie;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getObjectifId() {
        return objectifId;
    }

    public void setObjectifId(int objectifId) {
        this.objectifId = objectifId;
    }

    @Override
    public String toString() {
        return "SuiviMentale{" +
                "id=" + id +
                ", scoreMentale=" + scoreMentale +
                ", tauxDeStress=" + tauxDeStress +
                ", tauxDeStressGlobale=" + tauxDeStressGlobale +
                ", dateDeSuivi=" + dateDeSuivi +
                ", qualiteDuSommeil='" + qualiteDuSommeil + '\'' +
                ", journalEmotionnelle='" + journalEmotionnelle + '\'' +
                ", heureDeSommeil=" + heureDeSommeil +
                ", humeur='" + humeur + '\'' +
                ", niveauDenergie=" + niveauDenergie +
                ", userId=" + userId +
                ", objectifId=" + objectifId +
                '}';
    }
}