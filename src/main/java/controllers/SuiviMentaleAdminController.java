package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.SuiviMentale;
import services.SuiviMentaleAdminService;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SuiviMentaleAdminController {

    @FXML private VBox containerSuivis;
    @FXML private FlowPane containerStatsUsers;

    @FXML private TextField txtRechercheUserId;
    @FXML private TextField txtRechercheDate;
    @FXML private TextField txtRechercheHumeur;

    @FXML private Label lblTotalSuivis;
    @FXML private Label lblScoreMoyen;
    @FXML private Label lblStressMoyen;
    @FXML private Label lblEnergieMoyenne;

    private final SuiviMentaleAdminService service = new SuiviMentaleAdminService();

    private static final int DEFAULT_USER_ID = 2;

    @FXML
    public void initialize() {
        txtRechercheUserId.setText(String.valueOf(DEFAULT_USER_ID));
        chargerSuivisParDefaut();
        chargerStatistiques();
    }

    private void chargerSuivisParDefaut() {
        try {
            List<SuiviMentale> liste = service.afficherParUserId(DEFAULT_USER_ID);
            afficherCartes(liste);
        } catch (Exception e) {
            afficherErreurDansPage("Erreur de chargement", e.getMessage());
        }
    }

    private void chargerStatistiques() {
        try {
            Map<String, Double> globales = service.getStatistiquesGlobales();

            lblTotalSuivis.setText(String.valueOf(globales.getOrDefault("total_suivis", 0.0).intValue()));
            lblScoreMoyen.setText(String.format("%.1f", globales.getOrDefault("score_moyen", 0.0)));
            lblStressMoyen.setText(String.format("%.1f", globales.getOrDefault("stress_moyen", 0.0)));
            lblEnergieMoyenne.setText(String.format("%.1f", globales.getOrDefault("energie_moyenne", 0.0)));

            afficherStatsParUser(service.getStatistiquesParUser());

        } catch (Exception e) {
            containerStatsUsers.getChildren().clear();
            Label error = new Label("Erreur statistiques : " + e.getMessage());
            error.setStyle("-fx-text-fill: #DC2626;");
            containerStatsUsers.getChildren().add(error);
        }
    }

    @FXML
    private void rechercherSuivis() {
        try {
            String userIdTxt = txtRechercheUserId.getText().trim();
            String dateTxt = txtRechercheDate.getText().trim();
            String humeurTxt = txtRechercheHumeur.getText().trim();

            Integer userId = null;
            if (!userIdTxt.isEmpty()) {
                userId = Integer.parseInt(userIdTxt);
            }

            List<SuiviMentale> liste = service.rechercher(userId, dateTxt, humeurTxt);
            afficherCartes(liste);

        } catch (NumberFormatException e) {
            showError("Erreur", "Le User ID doit être un nombre.");
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void reinitialiserRecherche() {
        txtRechercheUserId.setText(String.valueOf(DEFAULT_USER_ID));
        txtRechercheDate.clear();
        txtRechercheHumeur.clear();
        chargerSuivisParDefaut();
    }

    private void afficherStatsParUser(List<Map<String, Object>> liste) {
        containerStatsUsers.getChildren().clear();

        for (Map<String, Object> row : liste) {
            VBox card = new VBox(8);
            card.setPrefWidth(230);
            card.setStyle("""
                    -fx-background-color: white;
                    -fx-border-color: #E5E7EB;
                    -fx-border-radius: 16;
                    -fx-background-radius: 16;
                    -fx-padding: 16;
                    """);

            Label title = new Label(String.valueOf(row.get("user_name")));
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #223A5E;");

            Label sub = new Label("User ID : " + row.get("user_id"));
            sub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

            Label total = new Label("Total suivis : " + row.get("total_suivis"));
            Label score = new Label("Score moyen : " + String.format("%.1f", (Double) row.get("score_moyen")));
            Label stress = new Label("Stress moyen : " + String.format("%.1f", (Double) row.get("stress_moyen")));
            Label energie = new Label("Énergie moyenne : " + String.format("%.1f", (Double) row.get("energie_moyenne")));

            total.setStyle("-fx-text-fill: #4B5563;");
            score.setStyle("-fx-text-fill: #4B5563;");
            stress.setStyle("-fx-text-fill: #4B5563;");
            energie.setStyle("-fx-text-fill: #4B5563;");

            card.getChildren().addAll(title, sub, total, score, stress, energie);
            containerStatsUsers.getChildren().add(card);
        }
    }

    private void afficherCartes(List<SuiviMentale> liste) {
        containerSuivis.getChildren().clear();

        if (liste == null || liste.isEmpty()) {
            VBox emptyCard = new VBox(8);
            emptyCard.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 18;
                    -fx-padding: 20;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);
                    """);

            Label title = new Label("Aucun suivi trouvé");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            Label desc = new Label("Vérifie le user_id, la date ou l'humeur.");
            desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

            emptyCard.getChildren().addAll(title, desc);
            containerSuivis.getChildren().add(emptyCard);
            return;
        }

        for (SuiviMentale suivi : liste) {
            containerSuivis.getChildren().add(createSuiviCardHorizontal(suivi));
        }
    }

    private void afficherErreurDansPage(String titre, String message) {
        containerSuivis.getChildren().clear();

        VBox errorCard = new VBox(8);
        errorCard.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-padding: 20;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);
                """);

        Label title = new Label(titre);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");

        Label desc = new Label(message);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        desc.setWrapText(true);

        errorCard.getChildren().addAll(title, desc);
        containerSuivis.getChildren().add(errorCard);
    }

    private HBox createSuiviCardHorizontal(SuiviMentale s) {
        HBox card = new HBox(24);
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-padding: 18;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);
                """);

        String nomUtilisateur = service.getNomUtilisateurParId(s.getUserId());

        VBox bloc1 = new VBox(8);
        bloc1.setPrefWidth(220);
        bloc1.getChildren().addAll(
                createMainText("Utilisateur", nomUtilisateur),
                createSecondaryText("User ID : " + s.getUserId()),
                createMainText("Objectif", "Objectif ID : " + s.getObjectifId()),
                createMainText("Date", String.valueOf(s.getDateDeSuivi()))
        );

        VBox bloc2 = new VBox(10);
        bloc2.setPrefWidth(280);

        FlowPane badges = new FlowPane();
        badges.setHgap(10);
        badges.setVgap(10);
        badges.getChildren().addAll(
                createBadge("Stress " + s.getTauxDeStress() + "/10", "#FBBF24", "white"),
                createBadge("Énergie " + s.getNiveauDenergie() + "/10", "#2563EB", "white"),
                createBadge(safe(s.getHumeur()), "#F59E0B", "white")
        );

        bloc2.getChildren().addAll(
                badges,
                createDetail("Score mental", String.valueOf(s.getScoreMentale())),
                createDetail("Stress global", String.valueOf(s.getTauxDeStressGlobale()))
        );

        VBox bloc3 = new VBox(8);
        HBox.setHgrow(bloc3, Priority.ALWAYS);
        bloc3.getChildren().addAll(
                createDetail("Qualité du sommeil", safe(s.getQualiteDuSommeil())),
                createDetail("Heures de sommeil", String.valueOf(s.getHeureDeSommeil())),
                createDetail("Journal émotionnel", safe(s.getJournalEmotionnelle()))
        );

        VBox blocActions = new VBox(10);
        Button btnModifier = new Button("Modifier");
        Button btnSupprimer = new Button("Supprimer");

        btnModifier.setPrefWidth(110);
        btnSupprimer.setPrefWidth(110);

        btnModifier.setStyle("""
                -fx-background-color: #2563EB;
                -fx-text-fill: white;
                -fx-background-radius: 10;
                -fx-cursor: hand;
                -fx-font-weight: bold;
                -fx-padding: 10 14;
                """);

        btnSupprimer.setStyle("""
                -fx-background-color: #EF4444;
                -fx-text-fill: white;
                -fx-background-radius: 10;
                -fx-cursor: hand;
                -fx-font-weight: bold;
                -fx-padding: 10 14;
                """);

        btnModifier.setOnAction(e -> ouvrirPopupModification(s));
        btnSupprimer.setOnAction(e -> supprimerSuivi(s));

        blocActions.getChildren().addAll(btnModifier, btnSupprimer);

        card.getChildren().addAll(bloc1, bloc2, bloc3, blocActions);
        return card;
    }

    private VBox createMainText(String title, String value) {
        VBox box = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        v.setWrapText(true);
        box.getChildren().addAll(t, v);
        return box;
    }

    private Label createSecondaryText(String value) {
        Label label = new Label(value);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        return label;
    }

    private Label createBadge(String text, String bgColor, String textColor) {
        Label badge = new Label(text);
        badge.setMinHeight(34);
        badge.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;"
        );
        return badge;
    }

    private VBox createDetail(String title, String value) {
        VBox box = new VBox(2);
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1F2937;");
        lblValue.setWrapText(true);
        box.getChildren().addAll(lblTitle, lblValue);
        return box;
    }

    private void ouvrirPopupModification(SuiviMentale s) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier suivi mental");
        dialog.setHeaderText("Modification du suivi #" + s.getId());

        ButtonType okButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);

        TextField humeurField = new TextField(s.getHumeur());
        TextField dateField = new TextField(String.valueOf(s.getDateDeSuivi()));
        TextArea journalField = new TextArea(s.getJournalEmotionnelle());
        TextField scoreField = new TextField(String.valueOf(s.getScoreMentale()));
        TextField stressField = new TextField(String.valueOf(s.getTauxDeStress()));
        TextField stressGlobalField = new TextField(String.valueOf(s.getTauxDeStressGlobale()));
        TextField sommeilField = new TextField(s.getQualiteDuSommeil());
        TextField energieField = new TextField(String.valueOf(s.getNiveauDenergie()));
        TextField heureSommeilField = new TextField(String.valueOf(s.getHeureDeSommeil()));

        journalField.setPrefHeight(100);

        content.getChildren().addAll(
                new Label("Humeur"), humeurField,
                new Label("Date de suivi (yyyy-mm-dd)"), dateField,
                new Label("Journal émotionnel"), journalField,
                new Label("Score mental"), scoreField,
                new Label("Taux de stress"), stressField,
                new Label("Taux de stress globale"), stressGlobalField,
                new Label("Qualité du sommeil"), sommeilField,
                new Label("Niveau d'énergie"), energieField,
                new Label("Heures de sommeil"), heureSommeilField
        );

        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == okButtonType) {
            try {
                s.setHumeur(humeurField.getText().trim());
                s.setDateDeSuivi(Date.valueOf(dateField.getText().trim()));
                s.setJournalEmotionnelle(journalField.getText().trim());
                s.setScoreMentale(Integer.parseInt(scoreField.getText().trim()));
                s.setTauxDeStress(Integer.parseInt(stressField.getText().trim()));
                s.setTauxDeStressGlobale(Integer.parseInt(stressGlobalField.getText().trim()));
                s.setQualiteDuSommeil(sommeilField.getText().trim());
                s.setNiveauDenergie(Integer.parseInt(energieField.getText().trim()));
                s.setHeureDeSommeil(Double.parseDouble(heureSommeilField.getText().trim()));

                service.modifier(s);
                rechercherSuivis();
                chargerStatistiques();
                showInfo("Succès", "Le suivi a été modifié avec succès.");

            } catch (IllegalArgumentException ex) {
                showError("Erreur", "Vérifie la date et les champs numériques.");
            } catch (Exception ex) {
                showError("Erreur", "Erreur lors de la modification : " + ex.getMessage());
            }
        }
    }

    private void supprimerSuivi(SuiviMentale s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer ce suivi mental ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(s);
                rechercherSuivis();
                chargerStatistiques();
                showInfo("Succès", "Le suivi a été supprimé avec succès.");
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}