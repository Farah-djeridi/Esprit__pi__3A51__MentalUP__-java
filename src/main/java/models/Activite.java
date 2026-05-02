package models;

public class Activite {

    private int idActivite;
    private String titre;

    public Activite() {
    }

    public Activite(int idActivite, String titre) {
        this.idActivite = idActivite;
        this.titre = titre;
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

    @Override
    public String toString() {
        return titre;
    }
}