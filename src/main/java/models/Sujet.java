package models;

import java.sql.Date;

public class Sujet {

    private int id;
    private String titre;
    private String contenu;
    private Date dateCreation;
    private boolean isAnonyme;

    private int nbLikes;
    private int nbDislikes;
    private int nbVues;

    private double scoreToxicite;
    private boolean estToxique;

    private int idUser;

    public Sujet() {}


    public Sujet(String titre, String contenu, boolean isAnonyme, int idUser) {
        this.titre = titre;
        this.contenu = contenu;
        this.isAnonyme = isAnonyme;
        this.idUser = idUser;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public boolean isAnonyme() { return isAnonyme; }
    public void setAnonyme(boolean anonyme) { isAnonyme = anonyme; }

    public int getNbLikes() { return nbLikes; }
    public void setNbLikes(int nbLikes) { this.nbLikes = nbLikes; }

    public int getNbDislikes() { return nbDislikes; }
    public void setNbDislikes(int nbDislikes) { this.nbDislikes = nbDislikes; }

    public int getNbVues() { return nbVues; }
    public void setNbVues(int nbVues) { this.nbVues = nbVues; }

    public double getScoreToxicite() { return scoreToxicite; }
    public void setScoreToxicite(double scoreToxicite) { this.scoreToxicite = scoreToxicite; }

    public boolean isEstToxique() { return estToxique; }
    public void setEstToxique(boolean estToxique) { this.estToxique = estToxique; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Sujet{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                '}';
    }
}