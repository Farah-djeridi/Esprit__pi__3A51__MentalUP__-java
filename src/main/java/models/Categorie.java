package models;

import java.sql.Timestamp;

public class Categorie {
    private int id;
    private String nom;
    private String description;
    private Timestamp dateCreation;

    public Categorie() {
    }

    public Categorie(int id, String nom, String description, Timestamp dateCreation) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.dateCreation = dateCreation;
    }

    public Categorie(String nom, String description) {
        this.nom = nom;
        this.description = description;
        this.dateCreation = new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return nom; // useful for ComboBox
    }
}
