package models;

import java.sql.Timestamp;

public class Ressource {
    private int id;
    private String titre;
    private String description;
    private String type;
    private String lien;
    private String image;
    private Timestamp datePublication;
    private int nbVues;
    private int categorieId;
    
    // Virtual attribute for display purposes
    private String categorieNom;

    public Ressource() {
    }

    public Ressource(int id, String titre, String description, String type, String lien, String image, Timestamp datePublication, int nbVues, int categorieId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.lien = lien;
        this.image = image;
        this.datePublication = datePublication;
        this.nbVues = nbVues;
        this.categorieId = categorieId;
    }

    public Ressource(String titre, String description, String type, String lien, String image, int categorieId) {
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.lien = lien;
        this.image = image;
        this.datePublication = new Timestamp(System.currentTimeMillis());
        this.nbVues = 0;
        this.categorieId = categorieId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Timestamp getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(Timestamp datePublication) {
        this.datePublication = datePublication;
    }

    public int getNbVues() {
        return nbVues;
    }

    public void setNbVues(int nbVues) {
        this.nbVues = nbVues;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public void setCategorieNom(String categorieNom) {
        this.categorieNom = categorieNom;
    }

    @Override
    public String toString() {
        return "Ressource{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
