package Controllor;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.SuiviMentale;
import services.SuiviMentaleAdminService;

import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import utils.SceneManager;
import Controllor.AdminSidebarHelper;
import utils.SessionManager;
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

    @FXML private LineChart<String, Number> lineChartScoreMental;
    @FXML private PieChart pieChartHumeurs;
    @FXML private BarChart<String, Number> barChartSommeil;

    @FXML private CategoryAxis xAxisScoreMental;
    @FXML private NumberAxis yAxisScoreMental;
    @FXML private CategoryAxis xAxisSommeil;
    @FXML private NumberAxis yAxisSommeil;

    @FXML private javafx.scene.image.ImageView logoImage;
    @FXML private Label avatarInitials;
    @FXML private Label labelUserName;

    private final SuiviMentaleAdminService service = new SuiviMentaleAdminService();

    @FXML
    public void initialize() {
        txtRechercheUserId.clear();
        txtRechercheDate.clear();
        txtRechercheHumeur.clear();

        configurerAxes();
        chargerSuivisParDefaut();
        chargerStatistiques();
        chargerCharts();
    }

    private void configurerAxes() {
        if (xAxisScoreMental != null) {
            xAxisScoreMental.setAnimated(false);
            xAxisScoreMental.setTickLabelRotation(0);
            xAxisScoreMental.setTickLabelGap(8);
        }

        if (yAxisScoreMental != null) {
            yAxisScoreMental.setAnimated(false);
            yAxisScoreMental.setAutoRanging(true);
            yAxisScoreMental.setForceZeroInRange(false);
        }

        if (xAxisSommeil != null) {
            xAxisSommeil.setAnimated(false);
            xAxisSommeil.setTickLabelRotation(-20);
            xAxisSommeil.setTickLabelGap(10);
        }

        if (yAxisSommeil != null) {
            yAxisSommeil.setAnimated(false);
            yAxisSommeil.setAutoRanging(true);
            yAxisSommeil.setForceZeroInRange(false);
        }
    }

    private void chargerSuivisParDefaut() {
        try {
            List<SuiviMentale> liste = service.afficherTous();
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

    private void chargerCharts() {
        chargerLineChartScoreMental();
        chargerPieChartHumeurs();
        chargerBarChartSommeil();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                appliquerCouleursCalmesCharts();
            }
        });
    }

    private void chargerLineChartScoreMental() {
        if (lineChartScoreMental == null) {
            return;
        }

        lineChartScoreMental.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.setName("Score mental");

        List<Map<String, Object>> data = service.getEvolutionScoreMental();

        for (Map<String, Object> row : data) {
            String date = String.valueOf(row.get("date"));
            Number score = (Number) row.get("score");
            series.getData().add(new XYChart.Data<String, Number>(date, score));
        }

        lineChartScoreMental.getData().add(series);
    }

    private void chargerPieChartHumeurs() {
        if (pieChartHumeurs == null) {
            return;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        List<Map<String, Object>> data = service.getRepartitionHumeurs();

        for (Map<String, Object> row : data) {
            String humeur = String.valueOf(row.get("humeur"));
            Number total = (Number) row.get("total");
            pieData.add(new PieChart.Data(humeur, total.doubleValue()));
        }

        pieChartHumeurs.setData(pieData);
        pieChartHumeurs.setTitle("Humeurs dominantes");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Node title = pieChartHumeurs.lookup(".chart-title");
                if (title != null) {
                    title.setStyle("-fx-text-fill: #223A5E; -fx-font-size: 16px; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void chargerBarChartSommeil() {
        if (barChartSommeil == null) {
            return;
        }

        barChartSommeil.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.setName("Sommeil moyen");

        List<Map<String, Object>> data = service.getMoyenneSommeilParUser();

        for (Map<String, Object> row : data) {
            String user = String.valueOf(row.get("user_name"));
            Number moyenne = (Number) row.get("moyenne_sommeil");
            series.getData().add(new XYChart.Data<String, Number>(user, moyenne));
        }

        barChartSommeil.getData().add(series);
    }

    private void appliquerCouleursCalmesCharts() {
        if (lineChartScoreMental != null && !lineChartScoreMental.getData().isEmpty()) {
            Node line = lineChartScoreMental.lookup(".chart-series-line");
            if (line != null) {
                line.setStyle("-fx-stroke: #8FA6B8; -fx-stroke-width: 2px;");
            }

            for (Node node : lineChartScoreMental.lookupAll(".chart-line-symbol")) {
                node.setStyle("-fx-background-color: #8FA6B8, white;");
            }
        }

        if (pieChartHumeurs != null && pieChartHumeurs.getData() != null) {
            String[] pieColors = {
                    "#A8C3BC",
                    "#B7C9E2",
                    "#CBBBAF",
                    "#C7D7C0",
                    "#D6C6E1",
                    "#BFCAD6"
            };

            int i = 0;
            for (PieChart.Data data : pieChartHumeurs.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + pieColors[i % pieColors.length] + ";");
                }
                i++;
            }
        }

        if (barChartSommeil != null) {
            for (Node node : barChartSommeil.lookupAll(".default-color0.chart-bar")) {
                node.setStyle("-fx-bar-fill: #AAB8C7;");
            }
        }

        if (lineChartScoreMental != null) {
            styliserAxes(lineChartScoreMental);
        }
        if (barChartSommeil != null) {
            styliserAxes(barChartSommeil);
        }
    }

    private void styliserAxes(Chart chart) {
        for (Node node : chart.lookupAll(".axis")) {
            node.setStyle("-fx-tick-label-fill: #5B6B7A;");
        }
        for (Node node : chart.lookupAll(".axis-label")) {
            node.setStyle("-fx-text-fill: #50627A; -fx-font-weight: bold;");
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
        txtRechercheUserId.clear();
        txtRechercheDate.clear();
        txtRechercheHumeur.clear();
        chargerSuivisParDefaut();
    }

    private void afficherStatsParUser(List<Map<String, Object>> liste) {
        containerStatsUsers.getChildren().clear();

        for (Map<String, Object> row : liste) {
            VBox card = new VBox(8);
            card.setPrefWidth(230);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #E5E7EB;" +
                            "-fx-border-radius: 16;" +
                            "-fx-background-radius: 16;" +
                            "-fx-padding: 16;"
            );

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
            emptyCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
            );

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
        errorCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

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
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

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
                createBadge("Stress " + s.getTauxDeStress() + "/10", "#D4C4A8", "#4B5563"),
                createBadge("Énergie " + s.getNiveauDenergie() + "/10", "#B7C9E2", "#4B5563"),
                createBadge(safe(s.getHumeur()), "#C7D7C0", "#4B5563")
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

        btnModifier.setStyle(
                "-fx-background-color: #8E9AAF;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 14;"
        );

        btnSupprimer.setStyle(
                "-fx-background-color: #B98C8C;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 14;"
        );

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
        badge.setMinHeight(30);
        badge.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-padding: 6 14;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;"
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
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.setTitle("Modifier suivi");
        dialog.setHeaderText("Modification du suivi de l'utilisateur");

        dialog.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 20;"
        );

        ButtonType okButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 10;");

        TextField humeurField = createStyledField(s.getHumeur());
        TextField dateField = createStyledField(String.valueOf(s.getDateDeSuivi()));
        TextArea journalField = new TextArea(s.getJournalEmotionnelle());
        TextField stressField = createStyledField(String.valueOf(s.getTauxDeStress()));
        TextField sommeilField = createStyledField(s.getQualiteDuSommeil());
        TextField energieField = createStyledField(String.valueOf(s.getNiveauDenergie()));
        TextField heureSommeilField = createStyledField(String.valueOf(s.getHeureDeSommeil()));

        journalField.setPrefHeight(80);
        journalField.setStyle(
                "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #DCE3EA;" +
                        "-fx-padding: 10;"
        );

        content.getChildren().addAll(
                createPopupLabel("Humeur"), humeurField,
                createPopupLabel("Date (yyyy-mm-dd)"), dateField,
                createPopupLabel("Journal émotionnel"), journalField,
                createPopupLabel("Taux de stress"), stressField,
                createPopupLabel("Qualité du sommeil"), sommeilField,
                createPopupLabel("Niveau d'énergie"), energieField,
                createPopupLabel("Heures de sommeil"), heureSommeilField
        );

        dialog.getDialogPane().setContent(content);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #8E9AAF, #7D8CA3);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle(
                "-fx-background-color: #E8EDF2;" +
                        "-fx-text-fill: #4B5563;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == okButtonType) {
            try {
                s.setHumeur(humeurField.getText().trim());
                s.setDateDeSuivi(Date.valueOf(dateField.getText().trim()));
                s.setJournalEmotionnelle(journalField.getText().trim());
                s.setTauxDeStress(Integer.parseInt(stressField.getText().trim()));
                s.setQualiteDuSommeil(sommeilField.getText().trim());
                s.setNiveauDenergie(Integer.parseInt(energieField.getText().trim()));
                s.setHeureDeSommeil(Double.parseDouble(heureSommeilField.getText().trim()));

                service.modifier(s);
                rechercherSuivis();
                chargerStatistiques();
                chargerCharts();
                showSuccess("Le suivi a été modifié avec succès.");

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
        confirm.setHeaderText("Supprimer ce suivi ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 20;"
        );

        Button ok = (Button) confirm.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Supprimer");
        ok.setStyle(
                "-fx-background-color: #B98C8C;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        Button cancel = (Button) confirm.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancel.setText("Annuler");
        cancel.setStyle(
                "-fx-background-color: #E8EDF2;" +
                        "-fx-text-fill: #4B5563;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(s);
                rechercherSuivis();
                chargerStatistiques();
                chargerCharts();
                showSuccess("Le suivi a été supprimé avec succès.");
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    private TextField createStyledField(String value) {
        TextField field = new TextField(value);
        field.setStyle(
                "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #DCE3EA;" +
                        "-fx-padding: 10;"
        );
        return field;
    }

    private Label createPopupLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        return label;
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 20;"
        );

        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        ok.setStyle(
                "-fx-background-color: linear-gradient(to right, #9BB7A5, #89A393);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        alert.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 20;"
        );

        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        ok.setStyle(
                "-fx-background-color: #B98C8C;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
    @FXML private void onNavHomeClicked(MouseEvent event)         { AdminSidebarHelper.goToAccueil(); }
    @FXML private void onNavSuiviStatsClicked(MouseEvent event)   { AdminSidebarHelper.goToSuiviMental(); }
    @FXML private void onNavObjectifsClicked(MouseEvent event)    { AdminSidebarHelper.goToObjectifs(); }
    @FXML private void onNavSuiviClicked(MouseEvent event)        { AdminSidebarHelper.goToSuiviMental(); }
    @FXML private void onNavForumClicked(MouseEvent event)        { AdminSidebarHelper.goToForum(); }
    @FXML private void onNavRdvClicked(MouseEvent event)          { AdminSidebarHelper.goToRendezVous(); }
    @FXML private void onNavDossiersClicked(MouseEvent event)     { AdminSidebarHelper.goToDossiers(); }
    @FXML private void onNavUtilisateursClicked(MouseEvent event) { AdminSidebarHelper.goToUtilisateurs(); }
    @FXML private void onNavContenusClicked(MouseEvent event)     { AdminSidebarHelper.goToContenus(); }
    @FXML private void onNavActivitesClicked(MouseEvent event)    { AdminSidebarHelper.goToActivites(); }
    @FXML private void onNavReservationsClicked(MouseEvent event) { AdminSidebarHelper.goToReservations(); }
    @FXML private void onNavHoverEnter(MouseEvent event)          { }
    @FXML private void onNavHoverExit(MouseEvent event)           { }

    @FXML private void onLogout(ActionEvent event) {
        AdminSidebarHelper.logout();
    }
    @FXML public void onNavSujetsClicked(MouseEvent e)        { AdminSidebarHelper.goToForum(); }
    @FXML public void onNavCommentairesClicked(MouseEvent e)  { AdminSidebarHelper.goToCommentaires(); }
    @FXML public void onSubmenuHoverEnter(MouseEvent e)       { }
    @FXML public void onSubmenuHoverExit(MouseEvent e)        { }
}
