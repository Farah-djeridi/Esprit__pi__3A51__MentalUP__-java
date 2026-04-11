package Controllors;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Sujet;
import services.ServiceSujet;

public class ControllerEditSujet {

    @FXML private TextField titreField;
    @FXML private TextArea contenuArea;
    @FXML private CheckBox anonymeCheckBox;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    private ServiceSujet serviceSujet;
    private Sujet sujet;

    public void setSujet(Sujet s, ServiceSujet service) {
        this.sujet = s;
        this.serviceSujet = service;

        titreField.setText(s.getTitre());
        contenuArea.setText(s.getContenu());
        anonymeCheckBox.setSelected(s.isAnonyme());
    }

    @FXML
    public void initialize() {
        submitButton.setOnAction(e -> updateDiscussion());
        cancelButton.setOnAction(e -> closeWindow());
    }

    private void updateDiscussion() {
        if (sujet == null) {
            showAlert("Erreur", "Aucun sujet à modifier", Alert.AlertType.ERROR);
            closeWindow();
            return;
        }

        String titre = titreField.getText().trim();
        String contenu = contenuArea.getText().trim();

        // ========== CONTRÔLES DE SAISIE AVEC ALERTES ==========

        if (titre.isEmpty()) {
            showAlert("Erreur de saisie", "Veuillez saisir un titre", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (titre.length() < 3) {
            showAlert("Erreur de saisie", "Le titre doit contenir au moins 3 caractères", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (titre.length() > 100) {
            showAlert("Erreur de saisie", "Le titre ne peut pas dépasser 100 caractères", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (contenu.isEmpty()) {
            showAlert("Erreur de saisie", "Veuillez saisir le contenu", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        if (contenu.length() < 10) {
            showAlert("Erreur de saisie", "Le contenu doit contenir au moins 10 caractères", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        if (contenu.length() > 5000) {
            showAlert("Erreur de saisie", "Le contenu ne peut pas dépasser 5000 caractères", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        // Si toutes les validations sont passées, mettre à jour
        sujet.setTitre(titre);
        sujet.setContenu(contenu);
        sujet.setAnonyme(anonymeCheckBox.isSelected());

        try {
            serviceSujet.update(sujet);
            showAlert("Succès", "Discussion modifiée avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de modifier la discussion: " + e.getMessage(), Alert.AlertType.ERROR);
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