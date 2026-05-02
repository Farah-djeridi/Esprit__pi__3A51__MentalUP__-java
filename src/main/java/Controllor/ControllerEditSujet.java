package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Sujet;
import services.ProfanityFilterService;
import services.ServiceSujet;
import services.ToxicityAnalysisService;

public class ControllerEditSujet {

    @FXML private TextField titreField;
    @FXML private TextArea contenuArea;
    @FXML private CheckBox anonymeCheckBox;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label titreCharCount;
    @FXML private Label contenuCharCount;

    private ServiceSujet serviceSujet;
    private Sujet sujet;
    private ProfanityFilterService profanityFilter;
    private ToxicityAnalysisService toxicityService;

    private static final int TITRE_MIN = 3;
    private static final int TITRE_MAX = 100;
    private static final int CONTENU_MIN = 10;
    private static final int CONTENU_MAX = 5000;

    public void setSujet(Sujet s, ServiceSujet service) {
        this.sujet = s;
        this.serviceSujet = service;

        titreField.setText(s.getTitre());
        contenuArea.setText(s.getContenu());
        anonymeCheckBox.setSelected(s.isAnonyme());

        updateCharCounters();
    }

    @FXML
    public void initialize() {
        setupValidation();
        setupButtonStyles();

        toxicityService = new ToxicityAnalysisService();

        submitButton.setOnAction(e -> updateDiscussion());
        cancelButton.setOnAction(e -> closeWindow());
    }

    public void setProfanityFilter(ProfanityFilterService filter) {
        this.profanityFilter = filter;
    }

    private void setupValidation() {
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
        String titre = titreField.getText();
        String contenu = contenuArea.getText();

        int titreLength = titre == null ? 0 : titre.length();
        int contenuLength = contenu == null ? 0 : contenu.length();

        if (titreCharCount != null) {
            titreCharCount.setText(titreLength + " / " + TITRE_MAX + " caractères");
            if (titreLength > TITRE_MAX) {
                titreCharCount.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 10px;");
            } else if (titreLength < TITRE_MIN && titreLength > 0) {
                titreCharCount.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 10px;");
            } else {
                titreCharCount.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 10px;");
            }
        }

        if (contenuCharCount != null) {
            contenuCharCount.setText(contenuLength + " / " + CONTENU_MAX + " caractères");
            if (contenuLength > CONTENU_MAX) {
                contenuCharCount.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 10px;");
            } else if (contenuLength < CONTENU_MIN && contenuLength > 0) {
                contenuCharCount.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 10px;");
            } else {
                contenuCharCount.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 10px;");
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

    private void updateDiscussion() {
        if (sujet == null) {
            showAlert("Erreur", "Aucun sujet à modifier", Alert.AlertType.ERROR);
            closeWindow();
            return;
        }

        String titre = titreField.getText().trim();
        String contenu = contenuArea.getText().trim();

        if (profanityFilter != null) {
            try {
                profanityFilter.validateText(titre, "titre");
                profanityFilter.validateText(contenu, "contenu");
            } catch (IllegalArgumentException e) {
                showAlert("Contenu inapproprié", e.getMessage(), Alert.AlertType.WARNING);
                return;
            }
        }

        if (titre.isEmpty()) {
            showAlert("Erreur de saisie", "Veuillez saisir un titre", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (titre.length() < TITRE_MIN) {
            showAlert("Erreur de saisie", "Le titre doit contenir au moins " + TITRE_MIN + " caractères", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (titre.length() > TITRE_MAX) {
            showAlert("Erreur de saisie", "Le titre ne peut pas dépasser " + TITRE_MAX + " caractères", Alert.AlertType.ERROR);
            titreField.requestFocus();
            return;
        }

        if (contenu.isEmpty()) {
            showAlert("Erreur de saisie", "Veuillez saisir le contenu", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        if (contenu.length() < CONTENU_MIN) {
            showAlert("Erreur de saisie", "Le contenu doit contenir au moins " + CONTENU_MIN + " caractères", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        if (contenu.length() > CONTENU_MAX) {
            showAlert("Erreur de saisie", "Le contenu ne peut pas dépasser " + CONTENU_MAX + " caractères", Alert.AlertType.ERROR);
            contenuArea.requestFocus();
            return;
        }

        double scoreToxicite = toxicityService.analyze(contenu);
        boolean estToxique = toxicityService.isToxic(contenu);

        System.out.println("Modification - Score toxicité: " + scoreToxicite);

        boolean hasChanges = !titre.equals(sujet.getTitre()) ||
                !contenu.equals(sujet.getContenu()) ||
                anonymeCheckBox.isSelected() != sujet.isAnonyme();

        if (!hasChanges) {
            showAlert("Information", "Aucune modification détectée", Alert.AlertType.INFORMATION);
            closeWindow();
            return;
        }

        sujet.setTitre(titre);
        sujet.setContenu(contenu);
        sujet.setAnonyme(anonymeCheckBox.isSelected());

        sujet.setScoreToxicite(scoreToxicite);
        sujet.setEstToxique(estToxique);

        try {
            serviceSujet.update(sujet);
            showAlert("Succès", "Discussion modifiée avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de modifier la discussion: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
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

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
