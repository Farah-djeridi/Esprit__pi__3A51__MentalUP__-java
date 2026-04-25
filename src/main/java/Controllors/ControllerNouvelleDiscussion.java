package Controllors;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import models.Sujet;
import services.ProfanityFilterService;
import services.ServiceBan;
import services.ServiceSujet;
import services.ToxicityAnalysisService;

import java.time.LocalDate;
import java.sql.Date;

public class ControllerNouvelleDiscussion {

    @FXML private TextField titreField;
    @FXML private TextArea contenuArea;
    @FXML private CheckBox anonymeCheckBox;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label charCountLabel;

    private ServiceSujet serviceSujet;
    private int userId;
    private ProfanityFilterService profanityFilter;
    private ToxicityAnalysisService toxicityService;
    private ServiceBan serviceBan;

    private static final int TITRE_MIN = 3;
    private static final int TITRE_MAX = 100;
    private static final int CONTENU_MIN = 10;
    private static final int CONTENU_MAX = 5000;

    public void setServiceSujet(ServiceSujet service) {
        this.serviceSujet = service;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    @FXML
    public void initialize() {
        setupValidation();
        setupButtonStyles();

        toxicityService = new ToxicityAnalysisService();

        serviceBan = new ServiceBan();

        submitButton.setOnAction(e -> submitDiscussion());
        cancelButton.setOnAction(e -> closeWindow());
    }


    public void setProfanityFilter(ProfanityFilterService filter) {
        this.profanityFilter = filter;
    }

    private void setupValidation() {
        // Validation titre en temps réel
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > TITRE_MAX) {
                titreField.setText(oldVal);
                showAlert("Attention", "Le titre ne peut pas dépasser " + TITRE_MAX + " caractères", Alert.AlertType.WARNING);
            }
            updateCharCounters();
        });

        contenuArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > CONTENU_MAX) {
                contenuArea.setText(oldVal);
                showAlert("Attention", "Le contenu ne peut pas dépasser " + CONTENU_MAX + " caractères", Alert.AlertType.WARNING);
            }
            updateCharCounters();
        });
    }

    private void updateCharCounters() {
        String contenu = contenuArea.getText();
        int contenuLength = contenu == null ? 0 : contenu.length();

        if (charCountLabel != null) {
            charCountLabel.setText(contenuLength + " / " + CONTENU_MAX + " caractères");

            if (contenuLength > CONTENU_MAX) {
                charCountLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px;");
            } else if (contenuLength < CONTENU_MIN && contenuLength > 0) {
                charCountLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 11px;");
            } else if (contenuLength >= CONTENU_MIN && contenuLength <= CONTENU_MAX) {
                charCountLabel.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 11px;");
            } else {
                charCountLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
            }
        }
    }

    private void setupButtonStyles() {
        cancelButton.setOnMouseEntered(e -> {
            cancelButton.setStyle("-fx-background-color: #F1F5F9; " +
                    "-fx-text-fill: #475569; " +
                    "-fx-font-weight: 600; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 8 22; " +
                    "-fx-background-radius: 25; " +
                    "-fx-border-color: #E2E8F0; " +
                    "-fx-border-radius: 25; " +
                    "-fx-border-width: 1;");
        });
        cancelButton.setOnMouseExited(e -> {
            cancelButton.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #6B7C8D; " +
                    "-fx-font-weight: 600; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 8 22; " +
                    "-fx-background-radius: 25; " +
                    "-fx-border-color: #E2E8F0; " +
                    "-fx-border-radius: 25; " +
                    "-fx-border-width: 1;");
        });

        submitButton.setOnMouseEntered(e -> {
            submitButton.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2C5F8A, #1E4D7B); " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 25; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 9 28; " +
                    "-fx-effect: dropshadow(gaussian, rgba(44,95,138,0.4), 10, 0, 0, 3);");
        });
        submitButton.setOnMouseExited(e -> {
            submitButton.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3A6FA8, #2C5F8A); " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 25; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 9 28; " +
                    "-fx-effect: dropshadow(gaussian, rgba(44,95,138,0.3), 8, 0, 0, 2);");
        });
    }

    private void submitDiscussion() {
            String titre = titreField.getText().trim();
            String contenu = contenuArea.getText().trim();

        // Vérifier si l'utilisateur est banni
        if (serviceBan.isUserBanned(userId)) {
            String banMessage = serviceBan.getBanMessage(userId);
            Alert banAlert = new Alert(Alert.AlertType.ERROR);
            banAlert.setTitle("Accès refusé");
            banAlert.setHeaderText("❌ Vous êtes banni du forum");
            banAlert.setContentText(banMessage);

            DialogPane dialogPane = banAlert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #F0F4FA; -fx-background-radius: 12;");

            banAlert.showAndWait();
            closeWindow();
            return;
        }

            // Validation des mots inappropriés
            if (profanityFilter != null) {
                try {
                    profanityFilter.validateText(titre, "titre");
                    profanityFilter.validateText(contenu, "contenu");
                } catch (IllegalArgumentException e) {
                    showStyledAlert("Contenu inapproprié", e.getMessage(), Alert.AlertType.WARNING);
                    return;
                }
            }




        if (titre.isEmpty()) {
            showStyledAlert("Erreur de saisie", "Veuillez saisir un titre", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (titre.length() < TITRE_MIN) {
            showStyledAlert("Erreur de saisie", "Le titre doit contenir au moins " + TITRE_MIN + " caractères", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (titre.length() > TITRE_MAX) {
            showStyledAlert("Erreur de saisie", "Le titre ne peut pas dépasser " + TITRE_MAX + " caractères", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (contenu.isEmpty()) {
            showStyledAlert("Erreur de saisie", "Veuillez saisir le contenu", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        if (contenu.length() < CONTENU_MIN) {
            showStyledAlert("Erreur de saisie", "Le contenu doit contenir au moins " + CONTENU_MIN + " caractères", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        if (contenu.length() > CONTENU_MAX) {
            showStyledAlert("Erreur de saisie", "Le contenu ne peut pas dépasser " + CONTENU_MAX + " caractères", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous publier cette discussion ?");

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F0F4FA; -fx-background-radius: 12;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 22; -fx-font-weight: bold;");

        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7C8D; -fx-cursor: hand; -fx-padding: 8 22;");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // 🔥 ANALYSE TOXICITÉ (NE BLOQUE PAS)
        double scoreToxicite = toxicityService.analyze(contenu);
        boolean estToxique = toxicityService.isToxic(contenu);

// (option debug)
        System.out.println("Score toxicité: " + scoreToxicite);

        Sujet sujet = new Sujet();
        sujet.setTitre(titre);
        sujet.setContenu(contenu);
        sujet.setAnonyme(anonymeCheckBox.isSelected());
        sujet.setIdUser(userId);
        sujet.setDateCreation(Date.valueOf(LocalDate.now()));
        sujet.setNbLikes(0);
        sujet.setNbDislikes(0);
        sujet.setNbVues(0);
        sujet.setScoreToxicite(scoreToxicite);
        sujet.setEstToxique(estToxique);

        try {
            serviceSujet.add(sujet);
            showStyledAlert("Succès", "✓ Votre discussion a été publiée avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (Exception e) {
            showStyledAlert("Erreur", "Impossible de créer la discussion:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showStyledAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F0F4FA; -fx-background-radius: 12;");

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 22; -fx-font-weight: bold;");

        alert.showAndWait();
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