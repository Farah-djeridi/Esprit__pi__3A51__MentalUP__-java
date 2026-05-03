package Controllor;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.Objectif;
import services.ServiceObjectifAdmin;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import utils.SceneManager;
import Controllor.AdminSidebarHelper;
import utils.SessionManager;

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

    @FXML private PieChart pieChartStatuts;
    @FXML private BarChart<String, Number> barChartProgressionStatut;
    @FXML private CategoryAxis xAxisProgressionStatut;
    @FXML private NumberAxis yAxisProgressionStatut;

    @FXML private javafx.scene.image.ImageView logoImage;
    @FXML private Label avatarInitials;
    @FXML private Label labelUserName;

    private final ServiceObjectifAdmin service = new ServiceObjectifAdmin();

    @FXML
    public void initialize() {
        comboStatut.getItems().addAll("En cours", "Atteint", "Annulé");

        if (xAxisProgressionStatut != null) {
            xAxisProgressionStatut.setAnimated(false);
            xAxisProgressionStatut.setTickLabelRotation(-10);
        }

        if (yAxisProgressionStatut != null) {
            yAxisProgressionStatut.setAnimated(false);
            yAxisProgressionStatut.setAutoRanging(true);
            yAxisProgressionStatut.setForceZeroInRange(false);
        }

        if (barChartProgressionStatut != null) {
            barChartProgressionStatut.setAnimated(false);
            barChartProgressionStatut.setLegendVisible(false);
        }

        chargerObjectifs();
        chargerStatistiques();
        chargerCharts();
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

    private void chargerCharts() {
        chargerPieChartStatuts();
        chargerBarChartProgressionStatut();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                appliquerCouleursCharts();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        appliquerCouleursCharts();
                    }
                });
            }
        });
    }

    private void chargerPieChartStatuts() {
        if (pieChartStatuts == null) {
            return;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        List<Map<String, Object>> data = service.getRepartitionStatuts();

        for (Map<String, Object> row : data) {
            String statut = String.valueOf(row.get("statut"));
            Number total = (Number) row.get("total");
            pieData.add(new PieChart.Data(statut, total.doubleValue()));
        }

        pieChartStatuts.setData(pieData);
        pieChartStatuts.setTitle("Statuts");
        pieChartStatuts.setLegendVisible(false);
        pieChartStatuts.setLabelsVisible(true);
    }

    private void chargerBarChartProgressionStatut() {
        if (barChartProgressionStatut == null) {
            return;
        }

        barChartProgressionStatut.getData().clear();
        barChartProgressionStatut.setLegendVisible(false);
        barChartProgressionStatut.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.setName("Progression moyenne");

        List<Map<String, Object>> data = service.getProgressionMoyenneParStatut();

        for (Map<String, Object> row : data) {
            String statut = String.valueOf(row.get("statut"));
            Number moyenne = (Number) row.get("moyenne_progression");
            XYChart.Data<String, Number> chartData = new XYChart.Data<String, Number>(statut, moyenne);
            series.getData().add(chartData);
        }

        barChartProgressionStatut.getData().add(series);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (XYChart.Data<String, Number> dataItem : series.getData()) {
                    Node node = dataItem.getNode();
                    if (node != null) {
                        node.setStyle("-fx-bar-fill: #AAB8C7;");
                    }
                }
            }
        });
    }

    private void appliquerCouleursCharts() {
        if (pieChartStatuts != null && pieChartStatuts.getData() != null) {
            String[] pieColors = {
                    "#A8C3BC",
                    "#B7C9E2",
                    "#CBBBAF",
                    "#C7D7C0",
                    "#D6C6E1",
                    "#BFCAD6"
            };

            int i = 0;
            for (PieChart.Data data : pieChartStatuts.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + pieColors[i % pieColors.length] + ";");
                }
                i++;
            }

            Node pieTitle = pieChartStatuts.lookup(".chart-title");
            if (pieTitle != null) {
                pieTitle.setStyle("-fx-text-fill: #223A5E; -fx-font-size: 16px; -fx-font-weight: bold;");
            }
        }

        if (barChartProgressionStatut != null) {
            for (Node node : barChartProgressionStatut.lookupAll(".default-color0.chart-bar")) {
                node.setStyle("-fx-bar-fill: #AAB8C7;");
            }

            for (XYChart.Series<String, Number> series : barChartProgressionStatut.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle("-fx-bar-fill: #AAB8C7;");
                    }
                }
            }

            styliserAxes(barChartProgressionStatut);
        }
    }

    private void styliserAxes(javafx.scene.chart.Chart chart) {
        for (Node node : chart.lookupAll(".axis")) {
            node.setStyle("-fx-tick-label-fill: #5B6B7A;");
        }
        for (Node node : chart.lookupAll(".axis-label")) {
            node.setStyle("-fx-text-fill: #50627A; -fx-font-weight: bold;");
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
            VBox videCard = new VBox(8);
            videCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
            );

            Label vide = new Label("Aucun objectif trouvé.");
            vide.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

            videCard.getChildren().add(vide);
            containerObjectifs.getChildren().add(videCard);
            return;
        }

        for (Objectif o : liste) {
            containerObjectifs.getChildren().add(createRow(o));
        }
    }

    private HBox createRow(Objectif o) {
        HBox row = new HBox(14);
        row.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 14;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 16;"
        );

        VBox userBox = new VBox(3);
        userBox.setPrefWidth(180);
        userBox.setMinWidth(180);

        Label lblUser = new Label(service.getNomUtilisateurParId(o.getUserId()));
        lblUser.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        lblUser.setWrapText(true);
        userBox.getChildren().add(lblUser);

        VBox titreBox = new VBox(4);
        titreBox.setPrefWidth(300);
        titreBox.setMinWidth(300);

        Label lblTitre = new Label(o.getTitre());
        lblTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        lblTitre.setWrapText(true);

        Label lblDates = new Label("Début : " + o.getDateDebut() + " | Fin : " + o.getDateFin());
        lblDates.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        lblDates.setWrapText(true);

        titreBox.getChildren().addAll(lblTitre, lblDates);

        VBox typeBox = new VBox(3);
        typeBox.setPrefWidth(110);
        typeBox.setMinWidth(110);

        Label lblType = new Label(o.getTypeObjectif() == null ? "" : o.getTypeObjectif());
        lblType.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563;");
        lblType.setWrapText(true);
        typeBox.getChildren().add(lblType);

        VBox statutBox = new VBox();
        statutBox.setPrefWidth(120);
        statutBox.setMinWidth(120);

        Label badge = createStatutBadge(o.getStatutObjectif());
        statutBox.getChildren().add(badge);

        VBox progressionBox = new VBox(6);
        progressionBox.setPrefWidth(180);
        progressionBox.setMinWidth(180);

        double progress = Math.max(0, Math.min(100, o.getProgression())) / 100.0;
        ProgressBar bar = new ProgressBar(progress);
        bar.setPrefWidth(150);
        bar.setStyle("-fx-accent: #8FA6B8;");

        Label lblProg = new Label(o.getProgression() + "%");
        lblProg.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-font-weight: bold;");

        progressionBox.getChildren().addAll(bar, lblProg);

        HBox actions = new HBox(8);
        actions.setPrefWidth(170);
        actions.setMinWidth(170);
        actions.setMaxWidth(170);
        HBox.setHgrow(actions, Priority.NEVER);

        Button btnModifier = new Button("Modifier");
        btnModifier.setPrefWidth(80);
        btnModifier.setMinWidth(80);
        btnModifier.setMaxWidth(80);
        btnModifier.setStyle(
                "-fx-background-color: #8E9AAF;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 10;"
        );

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setPrefWidth(82);
        btnSupprimer.setMinWidth(82);
        btnSupprimer.setMaxWidth(82);
        btnSupprimer.setStyle(
                "-fx-background-color: #B98C8C;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 10;"
        );

        btnModifier.setOnAction(e -> modifierObjectif(o));
        btnSupprimer.setOnAction(e -> supprimerObjectif(o));

        actions.getChildren().addAll(btnModifier, btnSupprimer);

        row.getChildren().addAll(userBox, titreBox, typeBox, statutBox, progressionBox, actions);
        return row;
    }

    private Label createStatutBadge(String statut) {
        String s = statut == null ? "" : statut.toLowerCase();

        String bg = "#D4C4A8";
        String textColor = "#4B5563";

        if (s.contains("atteint")) {
            bg = "#C7D7C0";
        } else if (s.contains("annul")) {
            bg = "#D6C6E1";
        } else if (s.contains("cours")) {
            bg = "#B7C9E2";
        }

        Label badge = new Label(statut == null ? "" : statut);
        badge.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-padding: 6 16;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;"
        );
        return badge;
    }

    private void modifierObjectif(Objectif o) {
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.setTitle("Modifier objectif");
        dialog.setHeaderText("Modification de l'objectif #" + o.getId());

        dialog.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 20;"
        );

        ButtonType okButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 10;");

        TextField titreField = createStyledField(o.getTitre());

        ComboBox<String> statutField = new ComboBox<String>();
        statutField.getItems().addAll("En cours", "Atteint", "Annulé");
        statutField.setValue(o.getStatutObjectif());
        statutField.setStyle("-fx-background-radius: 12;");

        TextField progressionField = createStyledField(String.valueOf(o.getProgression()));

        content.getChildren().addAll(
                createPopupLabel("Titre"), titreField,
                createPopupLabel("Statut"), statutField,
                createPopupLabel("Progression"), progressionField
        );

        dialog.getDialogPane().setContent(content);

        Button ok = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        ok.setStyle(
                "-fx-background-color: linear-gradient(to right, #8E9AAF, #7D8CA3);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancel.setStyle(
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
                chargerCharts();

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
        confirm.setHeaderText("Supprimer cet objectif ?");
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
                service.supprimer(o);
                chargerObjectifs();
                chargerStatistiques();
                chargerCharts();

                showInfo("Succès", "Objectif supprimé avec succès.");

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

    private void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 20;"
        );

        Button ok = (Button) a.getDialogPane().lookupButton(ButtonType.OK);
        ok.setStyle(
                "-fx-background-color: linear-gradient(to right, #9BB7A5, #89A393);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        a.showAndWait();
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 20;"
        );

        Button ok = (Button) a.getDialogPane().lookupButton(ButtonType.OK);
        ok.setStyle(
                "-fx-background-color: #B98C8C;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        a.showAndWait();
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