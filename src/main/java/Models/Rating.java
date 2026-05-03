package models;

import java.sql.Timestamp;

public class Rating {
    private int id;
    private int etudiantId;
    private int psychologueId;
    private int note;
    private String commentaires;
    private Timestamp dateRating;

    public Rating() {}

    public Rating(int etudiantId, int psychologueId, int note, String commentaires) {
        this.etudiantId = etudiantId;
        this.psychologueId = psychologueId;
        this.note = note;
        this.commentaires = commentaires;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public int getPsychologueId() { return psychologueId; }
    public void setPsychologueId(int psychologueId) { this.psychologueId = psychologueId; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }

    public Timestamp getDateRating() { return dateRating; }
    public void setDateRating(Timestamp dateRating) { this.dateRating = dateRating; }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", etudiantId=" + etudiantId +
                ", psychologueId=" + psychologueId +
                ", note=" + note +
                '}';
    }
}
