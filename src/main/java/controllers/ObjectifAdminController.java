package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import models.Objectif;
import services.ServiceObjectifAdmin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ObjectifAdminController {

    @FXML private VBox containerObjectifs;
    @FXML private TextField txtRechercheTitre;
    @FXML private ComboBox<String> comboStatut;

    @FXML private Label lblTotal;
    @FXML private Label lblEnCours;
    @FXML private Label lblAtteints;
    @FXML private Label lblAnnules;

    private final ServiceObjectifAdmin service = new ServiceObjectifAdmin();

    @FXML
    public void initialize() {
        comboStatut.getItems().addAll("En cours", "Atteint", "Annulé");
        chargerObjectifs();
        chargerStatistiques();
    }

    private void chargerObjectifs() {
        try {
            afficherObjectifs(service.afficherTous());
        } catch (Exception e) {
            showError("Erreur", "Erreur lors du chargement des objectifs : " + e.getMessage());
        }
    }

    private void chargerStatistiques() {
        try {
            Map<String, Integer> stats = service.getStatistiques();

            lblTotal.setText(String.valueOf(stats.getOrDefault("total", 0)));
            lblEnCours.setText(String.valueOf(stats.getOrDefault("en_cours", 0)));
            lblAtteints.setText(String.valueOf(stats.getOrDefault("atteints", 0)));
            lblAnnules.setText(String.valueOf(stats.getOrDefault("annules", 0)));
        } catch (Exception e) {
            showError("Erreur", "Erreur lors du chargement des statistiques : " + e.getMessage());
        }
    }

    @FXML
    private void filtrerObjectifs() {
        try {
            String titre = txtRechercheTitre.getText().trim();
            String statut = comboStatut.getValue();

            List<Objectif> liste = service.filtrer(titre, statut);
            afficherObjectifs(liste);

        } catch (Exception e) {
            showError("Erreur", "Erreur lors du filtrage : " + e.getMessage());
        }
    }

    @FXML
    private void reinitialiserFiltres() {
        txtRechercheTitre.clear();
        comboStatut.setValue(null);
        chargerObjectifs();
    }

    private void afficherObjectifs(List<Objectif> liste) {
        containerObjectifs.getChildren().clear();

        if (liste == null || liste.isEmpty()) {
            Label vide = new Label("Aucun objectif trouvé.");
            vide.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
            containerObjectifs.getChildren().add(vide);
            return;
        }

        for (Objectif o : liste) {
            containerObjectifs.getChildren().add(createRow(o));
        }
    }

    private HBox createRow(Objectif o) {
        HBox row = new HBox(20);
        row.setStyle("""
                -fx-padding: 10 0 10 0;
                -fx-border-color: transparent transparent #E5E7EB transparent;
                -fx-border-width: 0 0 1 0;
                """);

        VBox userBox = new VBox(3);
        userBox.setPrefWidth(250);
        Label lblUser = new Label(service.getNomUtilisateurParId(o.getUserId()));
        lblUser.setStyle("-fx-font-size: 14px; -fx-text-fill: #1F2937;");
        userBox.getChildren().add(lblUser);

        VBox titreBox = new VBox(3);
        titreBox.setPrefWidth(420);
        Label lblTitre = new Label(o.getTitre());
        lblTitre.setStyle("-fx-font-size: 14px; -fx-text-fill: #1F2937;");
        titreBox.getChildren().add(lblTitre);

        VBox statutBox = new VBox();
        statutBox.setPrefWidth(220);
        Label badge = createStatutBadge(o.getStatutObjectif());
        statutBox.getChildren().add(badge);

        VBox progressionBox = new VBox(4);
        progressionBox.setPrefWidth(280);

        double progress = Math.max(0, Math.min(100, o.getProgression())) / 100.0;
        ProgressBar bar = new ProgressBar(progress);
        bar.setPrefWidth(180);

        Label lblProg = new Label(o.getProgression() + "%");
        lblProg.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");

        progressionBox.getChildren().addAll(bar, lblProg);

        HBox actions = new HBox(8);
        actions.setPrefWidth(130);

        Button btnModifier = new Button("✎");
        btnModifier.setStyle("""
                -fx-background-color: white;
                -fx-text-fill: #2563EB;
                -fx-border-color: #2563EB;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);

        Button btnSupprimer = new Button("🗑");
        btnSupprimer.setStyle("""
                -fx-background-color: white;
                -fx-text-fill: #EF4444;
                -fx-border-color: #EF4444;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);

        btnModifier.setOnAction(e -> modifierObjectif(o));
        btnSupprimer.setOnAction(e -> supprimerObjectif(o));

        actions.getChildren().addAll(btnModifier, btnSupprimer);

        row.getChildren().addAll(userBox, titreBox, statutBox, progressionBox, actions);
        return row;
    }

    private Label createStatutBadge(String statut) {
        String s = statut == null ? "" : statut.toLowerCase();

        String bg = "#FBBF24";

        if (s.contains("atteint")) {
            bg = "#10B981";
        } else if (s.contains("annul")) {
            bg = "#EF4444";
        } else if (s.contains("cours")) {
            bg = "#FBBF24";
        }

        Label badge = new Label(statut == null ? "" : statut);
        badge.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6 16;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;"
        );
        return badge;
    }

    private void modifierObjectif(Objectif o) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier objectif");
        dialog.setHeaderText("Modification de l'objectif #" + o.getId());

        ButtonType okButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        VBox content = new VBox(10);

        TextField titreField = new TextField(o.getTitre());

        ComboBox<String> statutField = new ComboBox<>();
        statutField.getItems().addAll("En cours", "Atteint", "Annulé");
        statutField.setValue(o.getStatutObjectif());

        TextField progressionField = new TextField(String.valueOf(o.getProgression()));

        content.getChildren().addAll(
                new Label("Titre"), titreField,
                new Label("Statut"), statutField,
                new Label("Progression"), progressionField
        );

        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == okButton) {
            try {
                String titre = titreField.getText().trim();
                String statut = statutField.getValue();
                int progression = Integer.parseInt(progressionField.getText().trim());

                if (titre.isEmpty()) {
                    showError("Erreur", "Le titre ne doit pas être vide.");
                    return;
                }

                if (statut == null || statut.isEmpty()) {
                    showError("Erreur", "Veuillez choisir un statut.");
                    return;
                }

                if (progression < 0 || progression > 100) {
                    showError("Erreur", "La progression doit être entre 0 et 100.");
                    return;
                }

                o.setTitre(titre);
                o.setStatutObjectif(statut);
                o.setProgression(progression);

                service.modifier(o);
                chargerObjectifs();
                chargerStatistiques();

                showInfo("Succès", "Objectif modifié avec succès.");

            } catch (NumberFormatException e) {
                showError("Erreur", "La progression doit être un nombre entier.");
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de la modification : " + e.getMessage());
            }
        }
    }

    private void supprimerObjectif(Objectif o) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cet objectif ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(o);
                chargerObjectifs();
                chargerStatistiques();

                showInfo("Succès", "Objectif supprimé avec succès.");

            } catch (Exception e) {
                showError("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    private void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}