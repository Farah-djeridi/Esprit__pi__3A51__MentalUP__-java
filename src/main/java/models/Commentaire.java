package models;

import java.sql.Date;

public class Commentaire {

    private int id;
    private String contenu;
    private Date dateCommentaire;
    private boolean isAnonyme;
    private int nbLikes;
    private int nbDislikes;
    private double scoreToxicite;
    private boolean estToxique;
    private int userId;
    private int sujetId;

    private String userName;

    public Commentaire() {
        this.scoreToxicite = 0.0;
        this.estToxique = false;
    }

    public Commentaire(String contenu, boolean isAnonyme, int userId, int sujetId) {
        this.contenu = contenu;
        this.isAnonyme = isAnonyme;
        this.userId = userId;
        this.sujetId = sujetId;
        this.nbLikes = 0;
        this.nbDislikes = 0;
        this.scoreToxicite = 0.0;
        this.estToxique = false;
        this.dateCommentaire = new Date(System.currentTimeMillis());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Date getDateCommentaire() { return dateCommentaire; }
    public void setDateCommentaire(Date dateCommentaire) { this.dateCommentaire = dateCommentaire; }

    public boolean isAnonyme() { return isAnonyme; }
    public void setAnonyme(boolean anonyme) { isAnonyme = anonyme; }

    public int getNbLikes() { return nbLikes; }
    public void setNbLikes(int nbLikes) { this.nbLikes = nbLikes; }

    public int getNbDislikes() { return nbDislikes; }
    public void setNbDislikes(int nbDislikes) { this.nbDislikes = nbDislikes; }

    public double getScoreToxicite() { return scoreToxicite; }
    public void setScoreToxicite(double scoreToxicite) { this.scoreToxicite = scoreToxicite; }

    public boolean isEstToxique() { return estToxique; }
    public void setEstToxique(boolean estToxique) { this.estToxique = estToxique; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSujetId() { return sujetId; }
    public void setSujetId(int sujetId) { this.sujetId = sujetId; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserInitials() {
        if (userName == null || userName.isEmpty()) {
            return "U";
        }
        String[] parts = userName.split(" ");
        if (parts.length >= 2) {
            return parts[0].substring(0, 1) + parts[1].substring(0, 1);
        }
        return userName.substring(0, Math.min(2, userName.length())).toUpperCase();
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", userId=" + userId +
                ", sujetId=" + sujetId +
                '}';
    }
}