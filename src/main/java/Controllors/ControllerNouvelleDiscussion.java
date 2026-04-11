package Controllors;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Sujet;
import services.ServiceSujet;

public class ControllerNouvelleDiscussion {

    @FXML private TextField titreField;
    @FXML private TextArea contenuArea;
    @FXML private CheckBox anonymeCheckBox;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    private ServiceSujet serviceSujet;
    private int userId;

    public void setServiceSujet(ServiceSujet service) {
        this.serviceSujet = service;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    @FXML
    public void initialize() {
        submitButton.setOnAction(e -> submitDiscussion());
        cancelButton.setOnAction(e -> closeWindow());
    }

    private void submitDiscussion() {
        String titre = titreField.getText().trim();
        String contenu = contenuArea.getText().trim();

        // ========== CONTRÔLES DE SAISIE AVEC ALERTES ==========

        if (titre.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un titre", Alert.AlertType.ERROR);
            return;
        }

        if (titre.length() < 3) {
            showAlert("Erreur", "Le titre doit contenir au moins 3 caractères", Alert.AlertType.ERROR);
            return;
        }

        if (titre.length() > 100) {
            showAlert("Erreur", "Le titre ne peut pas dépasser 100 caractères", Alert.AlertType.ERROR);
            return;
        }

        if (contenu.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir le contenu", Alert.AlertType.ERROR);
            return;
        }

        if (contenu.length() < 10) {
            showAlert("Erreur", "Le contenu doit contenir au moins 10 caractères", Alert.AlertType.ERROR);
            return;
        }

        if (contenu.length() > 5000) {
            showAlert("Erreur", "Le contenu ne peut pas dépasser 5000 caractères", Alert.AlertType.ERROR);
            return;
        }

        // Création du sujet
        Sujet sujet = new Sujet();
        sujet.setTitre(titre);
        sujet.setContenu(contenu);
        sujet.setAnonyme(anonymeCheckBox.isSelected());
        sujet.setIdUser(userId);
        sujet.setNbLikes(0);
        sujet.setNbDislikes(0);
        sujet.setNbVues(0);
        sujet.setScoreToxicite(0.0);
        sujet.setEstToxique(false);

        try {
            serviceSujet.add(sujet);
            showAlert("Succès", "Discussion créée avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de créer la discussion: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}