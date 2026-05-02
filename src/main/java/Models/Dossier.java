package Models;

import javafx.beans.property.*;
import java.sql.Date;

public class Dossier {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<Date> dateCreation = new SimpleObjectProperty<>();
    private final StringProperty notesGenerales = new SimpleStringProperty();
    private final StringProperty niveauRisque = new SimpleStringProperty();
    private final IntegerProperty patientId = new SimpleIntegerProperty();
    private final IntegerProperty psychologueId = new SimpleIntegerProperty();
    private final StringProperty aiSummary = new SimpleStringProperty();
    private String patientNom;

    public Dossier() {}


    public Dossier(Date dateCreation, String notesGenerales, String niveauRisque,
                   int patientId, int psychologueId, String aiSummary) {
        this.dateCreation.set(dateCreation);
        this.notesGenerales.set(notesGenerales);
        this.niveauRisque.set(niveauRisque);
        this.patientId.set(patientId);
        this.psychologueId.set(psychologueId);
        this.aiSummary.set(aiSummary);
    }


    public Dossier(int id, Date dateCreation, String notesGenerales, String niveauRisque,
                   int patientId, int psychologueId, String aiSummary) {
        this.id.set(id);
        this.dateCreation.set(dateCreation);
        this.notesGenerales.set(notesGenerales);
        this.niveauRisque.set(niveauRisque);
        this.patientId.set(patientId);
        this.psychologueId.set(psychologueId);
        this.aiSummary.set(aiSummary);
    }

    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<Date> dateCreationProperty() { return dateCreation; }
    public StringProperty notesGeneralesProperty() { return notesGenerales; }
    public StringProperty niveauRisqueProperty() { return niveauRisque; }
    public IntegerProperty patientIdProperty() { return patientId; }
    public IntegerProperty psychologueIdProperty() { return psychologueId; }
    public StringProperty aiSummaryProperty() { return aiSummary; }


    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }

    public Date getDateCreation() { return dateCreation.get(); }
    public void setDateCreation(Date value) { dateCreation.set(value); }

    public String getNotesGenerales() { return notesGenerales.get(); }
    public void setNotesGenerales(String value) { notesGenerales.set(value); }

    public String getNiveauRisque() { return niveauRisque.get(); }
    public void setNiveauRisque(String value) { niveauRisque.set(value); }

    public int getPatientId() { return patientId.get(); }
    public void setPatientId(int value) { patientId.set(value); }

    public int getPsychologueId() { return psychologueId.get(); }
    public void setPsychologueId(int value) { psychologueId.set(value); }

    public String getAiSummary() { return aiSummary.get(); }
    public void setAiSummary(String value) { aiSummary.set(value); }


    public String getPatientNom() {
        return patientNom;
    }

    public void setPatientNom(String patientNom) {
        this.patientNom = patientNom;
    }
    @Override
    public String toString() {
        return "Dossier{" +
                "id=" + getId() +
                ", dateCreation=" + getDateCreation() +
                ", patientId=" + getPatientId() +
                ", psychologueId=" + getPsychologueId() +
                ", niveauRisque='" + getNiveauRisque() + '\'' +
                '}';
    }
}