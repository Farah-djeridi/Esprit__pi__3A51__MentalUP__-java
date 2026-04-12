package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import models.Objectif;
import models.SuiviMentale;
import services.ServiceObjectif;
import services.ServiceSuiviMentale;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class SuiviMentaleController {

    @FXML private Button ajouterTabButton;
    @FXML private Button statistiquesTabButton;
    @FXML private Button historiqueTabButton;
    @FXML private Button chatTabButton;
    @FXML private Button pdfButton;

    @FXML private ScrollPane ajoutSectionScroll;
    @FXML private ScrollPane historiqueSectionScroll;
    @FXML private ScrollPane statistiquesSectionScroll;

    @FXML private VBox ajoutSection;
    @FXML private VBox historiqueSection;
    @FXML private VBox statistiquesSection;
    @FXML private VBox chatSection;

    @FXML private DatePicker dateSuiviPicker;
    @FXML private ComboBox<String> humeurCombo;
    @FXML private ComboBox<String> qualiteSommeilCombo;
    @FXML private TextField heureSommeilField;
    @FXML private TextField stressField;
    @FXML private TextField energieField;
    @FXML private TextArea journalArea;

    @FXML private Button saveButton;
    @FXML private Button clearButton;

    @FXML private Label messageLabel;
    @FXML private Label globalMessageLabel;
    @FXML private Label conseilLabel;
    @FXML private Label objectifLieLabel;

    @FXML private FlowPane historiqueContainer;

    @FXML private DatePicker searchDateSuiviPicker;
    @FXML private ComboBox<String> searchHumeurCombo;
    @FXML private ComboBox<String> searchQualiteSommeilCombo;
    @FXML private TextField searchJournalField;

    @FXML private Label totalSuivisLabel;
    @FXML private Label stressMoyenLabel;
    @FXML private Label energieMoyenneLabel;
    @FXML private Label sommeilMoyenLabel;
    @FXML private Label humeurDominanteLabel;
    @FXML private Label scoreMentalMoyenLabel;

    @FXML private PieChart pieChartHumeursStats;
    @FXML private BarChart<String, Number> barChartMoyennes;
    @FXML private CategoryAxis xAxisMoyennes;
    @FXML private NumberAxis yAxisMoyennes;
    @FXML private LineChart<String, Number> lineChartEvolutionScore;
    @FXML private CategoryAxis xAxisEvolution;
    @FXML private NumberAxis yAxisEvolution;

    private final ServiceSuiviMentale suiviService = new ServiceSuiviMentale();
    private final ServiceObjectif objectifService = new ServiceObjectif();

    private Integer editingSuiviId = null;
    private final int currentUserId = 1;

    @FXML
    public void initialize() {
        humeurCombo.setItems(FXCollections.observableArrayList(
                "Très mal", "Neutre", "Bien", "Très bien"
        ));
        humeurCombo.setValue("Neutre");

        qualiteSommeilCombo.setItems(FXCollections.observableArrayList(
                "Terrible", "Mauvais", "Moyen", "Bon", "Excellent"
        ));
        qualiteSommeilCombo.setValue("Moyen");

        if (searchHumeurCombo != null) {
            searchHumeurCombo.setItems(FXCollections.observableArrayList(
                    "Toutes", "Très mal", "Neutre", "Bien", "Très bien"
            ));
            searchHumeurCombo.setValue("Toutes");
        }

        if (searchQualiteSommeilCombo != null) {
            searchQualiteSommeilCombo.setItems(FXCollections.observableArrayList(
                    "Toutes", "Terrible", "Mauvais", "Moyen", "Bon", "Excellent"
            ));
            searchQualiteSommeilCombo.setValue("Toutes");
        }

        dateSuiviPicker.setValue(LocalDate.now());

        dateSuiviPicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setDisable(false);
                        } else {
                            setDisable(item.isAfter(LocalDate.now()));
                        }
                    }
                };
            }
        });

        if (xAxisMoyennes != null) {
            xAxisMoyennes.setAnimated(false);
        }
        if (yAxisMoyennes != null) {
            yAxisMoyennes.setAnimated(false);
            yAxisMoyennes.setAutoRanging(true);
            yAxisMoyennes.setForceZeroInRange(true);
        }
        if (xAxisEvolution != null) {
            xAxisEvolution.setAnimated(false);
            xAxisEvolution.setTickLabelRotation(-20);
        }
        if (yAxisEvolution != null) {
            yAxisEvolution.setAnimated(false);
            yAxisEvolution.setAutoRanging(true);
            yAxisEvolution.setForceZeroInRange(true);
        }
        if (barChartMoyennes != null) {
            barChartMoyennes.setAnimated(false);
            barChartMoyennes.setLegendVisible(false);
        }
        if (lineChartEvolutionScore != null) {
            lineChartEvolutionScore.setAnimated(false);
            lineChartEvolutionScore.setLegendVisible(false);
            lineChartEvolutionScore.setCreateSymbols(true);
        }
        if (pieChartHumeursStats != null) {
            pieChartHumeursStats.setLegendVisible(false);
            pieChartHumeursStats.setLabelsVisible(true);
        }

        if (conseilLabel != null) {
            conseilLabel.setText("Prenez quelques minutes pour respirer profondément aujourd’hui.");
        }

        refreshAll();
        verifierAjoutAutorise();
        afficherObjectifLie();
        showAjouterSection();
    }

    @FXML
    public void showAjouterSection() {
        setActiveSection("ajout");
    }

    @FXML
    public void showHistoriqueSection() {
        refreshHistorique();
        setActiveSection("historique");
    }

    @FXML
    public void showStatistiquesSection() {
        refreshStatistiques();
        setActiveSection("stats");
    }

    @FXML
    public void showChatSection() {
        setActiveSection("chat");
    }

    @FXML
    public void onFilterChanged(ActionEvent event) {
        refreshHistorique();
    }

    @FXML
    public void onJournalSearchChanged(KeyEvent event) {
        refreshHistorique();
    }

    @FXML
    public void resetFiltresHistorique() {
        if (searchDateSuiviPicker != null) {
            searchDateSuiviPicker.setValue(null);
        }
        if (searchHumeurCombo != null) {
            searchHumeurCombo.setValue("Toutes");
        }
        if (searchQualiteSommeilCombo != null) {
            searchQualiteSommeilCombo.setValue("Toutes");
        }
        if (searchJournalField != null) {
            searchJournalField.clear();
        }
        refreshHistorique();
    }

    private void setActiveSection(String section) {
        boolean ajout = "ajout".equals(section);
        boolean hist = "historique".equals(section);
        boolean stats = "stats".equals(section);
        boolean chat = "chat".equals(section);

        ajoutSectionScroll.setVisible(ajout);
        ajoutSectionScroll.setManaged(ajout);

        historiqueSectionScroll.setVisible(hist);
        historiqueSectionScroll.setManaged(hist);

        statistiquesSectionScroll.setVisible(stats);
        statistiquesSectionScroll.setManaged(stats);

        chatSection.setVisible(chat);
        chatSection.setManaged(chat);

        updateTabStyles(section);
    }

    private void updateTabStyles(String activeTab) {
        String normalStyle =
                "-fx-background-color: #f8fbff;" +
                        "-fx-text-fill: #234b7d;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-border-color: #d7e5f7;" +
                        "-fx-border-radius: 24;" +
                        "-fx-background-radius: 24;" +
                        "-fx-padding: 11 24;" +
                        "-fx-cursor: hand;";

        String activeStyle =
                "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 24;" +
                        "-fx-padding: 11 24;" +
                        "-fx-cursor: hand;";

        ajouterTabButton.setStyle(normalStyle);
        statistiquesTabButton.setStyle(normalStyle);
        historiqueTabButton.setStyle(normalStyle);
        chatTabButton.setStyle(normalStyle);
        pdfButton.setStyle(normalStyle);

        switch (activeTab) {
            case "ajout" -> ajouterTabButton.setStyle(activeStyle);
            case "stats" -> statistiquesTabButton.setStyle(activeStyle);
            case "historique" -> historiqueTabButton.setStyle(activeStyle);
            case "chat" -> chatTabButton.setStyle(activeStyle);
        }
    }

    @FXML
    public void saveSuivi() {
        try {
            Objectif objectifEnCours = objectifService.getObjectifEnCoursByUser(currentUserId);

            if (objectifEnCours == null) {
                afficherMessage("Impossible d'ajouter un suivi : aucun objectif en cours.", true);
                return;
            }

            if (dateSuiviPicker.getValue() == null) {
                afficherMessage("La date est obligatoire.", true);
                return;
            }

            if (dateSuiviPicker.getValue().isAfter(LocalDate.now())) {
                afficherMessage("La date du suivi ne doit pas être au futur.", true);
                return;
            }

            if (humeurCombo.getValue() == null || humeurCombo.getValue().trim().isEmpty()) {
                afficherMessage("Veuillez choisir une humeur.", true);
                return;
            }

            if (qualiteSommeilCombo.getValue() == null || qualiteSommeilCombo.getValue().trim().isEmpty()) {
                afficherMessage("Veuillez choisir une qualité du sommeil.", true);
                return;
            }

            if (heureSommeilField.getText() == null || heureSommeilField.getText().trim().isEmpty()) {
                afficherMessage("Veuillez saisir les heures de sommeil.", true);
                return;
            }

            if (stressField.getText() == null || stressField.getText().trim().isEmpty()) {
                afficherMessage("Veuillez saisir le stress.", true);
                return;
            }

            if (energieField.getText() == null || energieField.getText().trim().isEmpty()) {
                afficherMessage("Veuillez saisir l'énergie.", true);
                return;
            }

            if (journalArea.getText() == null || journalArea.getText().trim().isEmpty()) {
                afficherMessage("Veuillez saisir le journal émotionnel.", true);
                return;
            }

            double heures;
            int stress;
            int energie;

            try {
                heures = Double.parseDouble(heureSommeilField.getText().trim());
            } catch (Exception e) {
                afficherMessage("Heures de sommeil invalides.", true);
                return;
            }

            try {
                stress = Integer.parseInt(stressField.getText().trim());
            } catch (Exception e) {
                afficherMessage("Taux de stress invalide.", true);
                return;
            }

            try {
                energie = Integer.parseInt(energieField.getText().trim());
            } catch (Exception e) {
                afficherMessage("Niveau d'énergie invalide.", true);
                return;
            }

            if (heures < 0 || heures > 24) {
                afficherMessage("Les heures de sommeil doivent être entre 0 et 24.", true);
                return;
            }

            if (stress < 0 || stress > 10) {
                afficherMessage("Le stress doit être entre 0 et 10.", true);
                return;
            }

            if (energie < 0 || energie > 10) {
                afficherMessage("L'énergie doit être entre 0 et 10.", true);
                return;
            }

            Objectif objectifLie = objectifService.getObjectifEnCoursByUser(currentUserId);

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Confirmer l'enregistrement du suivi");
            confirmation.setContentText(
                    "Ce suivi sera lié à l'objectif : " + safeTexte(objectifLie.getTitre()) +
                            "\nType : " + safeTexte(objectifLie.getTypeObjectif()) +
                            "\nValeur cible : " + objectifLie.getValeurCible() +
                            "\n\nVoulez-vous continuer ?"
            );

            Optional<ButtonType> choix = confirmation.showAndWait();
            if (choix.isEmpty() || choix.get() != ButtonType.OK) {
                return;
            }

            SuiviMentale s = new SuiviMentale();
            s.setDateDeSuivi(Date.valueOf(dateSuiviPicker.getValue()));
            s.setHumeur(humeurCombo.getValue());
            s.setQualiteDuSommeil(qualiteSommeilCombo.getValue());
            s.setHeureDeSommeil(heures);
            s.setTauxDeStress(stress);
            s.setTauxDeStressGlobale(stress);
            s.setNiveauDenergie(energie);
            s.setJournalEmotionnelle(journalArea.getText().trim());
            s.setUserId(currentUserId);
            s.setObjectifId(objectifLie.getId());
            s.setScoreMentale(calculerScoreMental(
                    s.getHumeur(),
                    s.getQualiteDuSommeil(),
                    s.getTauxDeStress(),
                    s.getNiveauDenergie(),
                    s.getHeureDeSommeil()
            ));

            if (editingSuiviId == null) {
                suiviService.add(s);
                afficherGlobalMessage("Suivi ajouté avec succès.", false);
            } else {
                s.setId(editingSuiviId);
                suiviService.update(s);
                afficherGlobalMessage("Suivi modifié avec succès.", false);
            }

            clearForm();
            mettreAJourProgressionObjectif(objectifLie.getId());
            refreshAll();
            verifierAjoutAutorise();
            afficherObjectifLie();
            showHistoriqueSection();

        } catch (Exception e) {
            afficherMessage("Erreur lors de l'enregistrement : " + e.getMessage(), true);
        }
    }

    @FXML
    public void clearForm() {
        editingSuiviId = null;
        dateSuiviPicker.setValue(LocalDate.now());
        humeurCombo.setValue("Neutre");
        qualiteSommeilCombo.setValue("Moyen");
        heureSommeilField.clear();
        stressField.clear();
        energieField.clear();
        journalArea.clear();
        saveButton.setText("💾 Enregistrer");
        afficherMessage("", false);
        afficherObjectifLie();
    }

    private void refreshAll() {
        refreshHistorique();
        refreshStatistiques();
    }

    private void refreshHistorique() {
        historiqueContainer.getChildren().clear();

        List<SuiviMentale> liste = suiviService.getByUser(currentUserId);

        LocalDate dateRecherche = (searchDateSuiviPicker != null) ? searchDateSuiviPicker.getValue() : null;
        String humeurRecherche = (searchHumeurCombo != null && searchHumeurCombo.getValue() != null)
                ? searchHumeurCombo.getValue().trim()
                : "Toutes";
        String sommeilRecherche = (searchQualiteSommeilCombo != null && searchQualiteSommeilCombo.getValue() != null)
                ? searchQualiteSommeilCombo.getValue().trim()
                : "Toutes";
        String journalRecherche = (searchJournalField != null && searchJournalField.getText() != null)
                ? searchJournalField.getText().trim().toLowerCase()
                : "";

        if (liste == null || liste.isEmpty()) {
            Label vide = new Label("Aucun suivi mentale enregistré.");
            vide.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 15px; -fx-font-weight: bold;");
            historiqueContainer.getChildren().add(vide);
            return;
        }

        int nbResultats = 0;

        for (SuiviMentale s : liste) {
            if (dateRecherche != null) {
                if (s.getDateDeSuivi() == null || !s.getDateDeSuivi().toLocalDate().equals(dateRecherche)) {
                    continue;
                }
            }

            if (!"Toutes".equalsIgnoreCase(humeurRecherche)) {
                if (s.getHumeur() == null || !s.getHumeur().equalsIgnoreCase(humeurRecherche)) {
                    continue;
                }
            }

            if (!"Toutes".equalsIgnoreCase(sommeilRecherche)) {
                if (s.getQualiteDuSommeil() == null || !s.getQualiteDuSommeil().equalsIgnoreCase(sommeilRecherche)) {
                    continue;
                }
            }

            if (!journalRecherche.isEmpty()) {
                String journal = s.getJournalEmotionnelle() == null ? "" : s.getJournalEmotionnelle().toLowerCase();
                if (!journal.contains(journalRecherche)) {
                    continue;
                }
            }

            nbResultats++;

            VBox card = new VBox(14);
            card.setPrefWidth(430);
            card.setMinWidth(430);
            card.setMaxWidth(430);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #dbe7f5;" +
                            "-fx-border-radius: 20;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(30,58,95,0.06), 10, 0, 0, 2);"
            );

            HBox top = new HBox(10);

            Label titre = new Label("Suivi du " + s.getDateDeSuivi());
            titre.setStyle("-fx-text-fill: #1e3a5f; -fx-font-size: 18px; -fx-font-weight: 900;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label score = new Label("Score : " + s.getScoreMentale());
            score.setStyle(
                    "-fx-background-color: #eaf3ff;" +
                            "-fx-text-fill: #2b5b9d;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: 900;" +
                            "-fx-background-radius: 999;" +
                            "-fx-padding: 7 13;"
            );

            top.getChildren().addAll(titre, spacer, score);

            String humeurText = s.getHumeur() == null ? "-" : s.getHumeur();
            String qualiteText = s.getQualiteDuSommeil() == null ? "-" : s.getQualiteDuSommeil();
            String journalText = (s.getJournalEmotionnelle() == null || s.getJournalEmotionnelle().trim().isEmpty())
                    ? "Aucun journal saisi."
                    : s.getJournalEmotionnelle();

            Label l1 = new Label("Humeur : " + humeurText);
            l1.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: 700;");

            Label l2 = new Label("Sommeil : " + qualiteText + " / " + s.getHeureDeSommeil() + " h");
            l2.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: 700;");

            Label l3 = new Label("Stress : " + s.getTauxDeStress() + " / 10");
            l3.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: 700;");

            Label l4 = new Label("Énergie : " + s.getNiveauDenergie() + " / 10");
            l4.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: 700;");

            Label l5 = new Label("Journal : " + journalText);
            l5.setWrapText(true);
            l5.setMaxWidth(Double.MAX_VALUE);
            l5.setStyle("-fx-text-fill: #334155; -fx-font-size: 14px; -fx-font-weight: 500;");

            Button modifierBtn = new Button("Modifier");
            modifierBtn.setStyle(
                    "-fx-background-color: #eef4fb;" +
                            "-fx-text-fill: #2b5b9d;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: 800;" +
                            "-fx-background-radius: 18;" +
                            "-fx-padding: 10 18;" +
                            "-fx-cursor: hand;"
            );
            modifierBtn.setOnAction(e -> chargerSuiviDansFormulaire(s));

            Button supprimerBtn = new Button("Supprimer");
            supprimerBtn.setStyle(
                    "-fx-background-color: #fff1f1;" +
                            "-fx-text-fill: #d94b4b;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: 800;" +
                            "-fx-background-radius: 18;" +
                            "-fx-padding: 10 18;" +
                            "-fx-cursor: hand;"
            );
            supprimerBtn.setOnAction(e -> confirmerSuppression(s));

            HBox actions = new HBox(12, modifierBtn, supprimerBtn);

            card.getChildren().addAll(top, l1, l2, l3, l4, l5, actions);
            historiqueContainer.getChildren().add(card);
        }

        if (nbResultats == 0) {
            Label vide = new Label("Aucun suivi ne correspond aux filtres.");
            vide.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 15px; -fx-font-weight: bold;");
            historiqueContainer.getChildren().add(vide);
        }
    }

    private void refreshStatistiques() {
        List<SuiviMentale> liste = suiviService.getByUser(currentUserId);
        if (liste == null) {
            liste = new ArrayList<SuiviMentale>();
        }

        totalSuivisLabel.setText(String.valueOf(liste.size()));

        if (liste.isEmpty()) {
            stressMoyenLabel.setText("0");
            energieMoyenneLabel.setText("0");
            sommeilMoyenLabel.setText("0");
            if (humeurDominanteLabel != null) humeurDominanteLabel.setText("-");
            if (scoreMentalMoyenLabel != null) scoreMentalMoyenLabel.setText("0");
            viderCharts();
            return;
        }

        double sommeStress = 0;
        double sommeEnergie = 0;
        double sommeSommeil = 0;
        double sommeScore = 0;

        int tresMal = 0;
        int neutre = 0;
        int bien = 0;
        int tresBien = 0;

        for (SuiviMentale s : liste) {
            sommeStress += s.getTauxDeStress();
            sommeEnergie += s.getNiveauDenergie();
            sommeSommeil += s.getHeureDeSommeil();
            sommeScore += s.getScoreMentale();

            if (s.getHumeur() != null) {
                if ("Très mal".equalsIgnoreCase(s.getHumeur())) tresMal++;
                else if ("Neutre".equalsIgnoreCase(s.getHumeur())) neutre++;
                else if ("Bien".equalsIgnoreCase(s.getHumeur())) bien++;
                else if ("Très bien".equalsIgnoreCase(s.getHumeur())) tresBien++;
            }
        }

        double moyenneStress = sommeStress / liste.size();
        double moyenneEnergie = sommeEnergie / liste.size();
        double moyenneSommeil = sommeSommeil / liste.size();
        double moyenneScore = sommeScore / liste.size();

        stressMoyenLabel.setText(String.format("%.2f", moyenneStress));
        energieMoyenneLabel.setText(String.format("%.2f", moyenneEnergie));
        sommeilMoyenLabel.setText(String.format("%.2f", moyenneSommeil));

        if (humeurDominanteLabel != null) {
            humeurDominanteLabel.setText(getHumeurDominante(tresMal, neutre, bien, tresBien));
        }

        if (scoreMentalMoyenLabel != null) {
            scoreMentalMoyenLabel.setText(String.format("%.2f", moyenneScore));
        }

        chargerPieChartHumeurs(tresMal, neutre, bien, tresBien);
        chargerBarChartMoyennes(moyenneStress, moyenneEnergie, moyenneSommeil, moyenneScore);
        chargerLineChartEvolution(liste);
    }

    private void viderCharts() {
        if (pieChartHumeursStats != null) {
            pieChartHumeursStats.setData(FXCollections.observableArrayList());
        }
        if (barChartMoyennes != null) {
            barChartMoyennes.getData().clear();
        }
        if (lineChartEvolutionScore != null) {
            lineChartEvolutionScore.getData().clear();
        }
    }

    private void chargerPieChartHumeurs(int tresMal, int neutre, int bien, int tresBien) {
        if (pieChartHumeursStats == null) {
            return;
        }

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        if (tresMal > 0) data.add(new PieChart.Data("Très mal", tresMal));
        if (neutre > 0) data.add(new PieChart.Data("Neutre", neutre));
        if (bien > 0) data.add(new PieChart.Data("Bien", bien));
        if (tresBien > 0) data.add(new PieChart.Data("Très bien", tresBien));

        pieChartHumeursStats.setData(data);
        pieChartHumeursStats.setLegendVisible(false);
        pieChartHumeursStats.setLabelsVisible(true);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String[] colors = {"#D6C6E1", "#CBBBAF", "#B7C9E2", "#C7D7C0"};
                int i = 0;
                for (PieChart.Data item : pieChartHumeursStats.getData()) {
                    Node node = item.getNode();
                    if (node != null) {
                        node.setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
                    }
                    i++;
                }
            }
        });
    }

    private void chargerBarChartMoyennes(double stress, double energie, double sommeil, double score) {
        if (barChartMoyennes == null) {
            return;
        }

        barChartMoyennes.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.getData().add(new XYChart.Data<String, Number>("Stress", stress));
        series.getData().add(new XYChart.Data<String, Number>("Énergie", energie));
        series.getData().add(new XYChart.Data<String, Number>("Sommeil", sommeil));
        series.getData().add(new XYChart.Data<String, Number>("Score", score));

        barChartMoyennes.getData().add(series);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (Node node : barChartMoyennes.lookupAll(".default-color0.chart-bar")) {
                    node.setStyle("-fx-bar-fill: #AAB8C7;");
                }
            }
        });
    }

    private void chargerLineChartEvolution(List<SuiviMentale> liste) {
        if (lineChartEvolutionScore == null) {
            return;
        }

        lineChartEvolutionScore.getData().clear();

        TreeMap<LocalDate, List<Integer>> grouped = new TreeMap<LocalDate, List<Integer>>();

        for (SuiviMentale s : liste) {
            if (s.getDateDeSuivi() == null) {
                continue;
            }

            LocalDate date = s.getDateDeSuivi().toLocalDate();
            if (!grouped.containsKey(date)) {
                grouped.put(date, new ArrayList<Integer>());
            }
            grouped.get(date).add(s.getScoreMentale());
        }

        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();

        for (Map.Entry<LocalDate, List<Integer>> entry : grouped.entrySet()) {
            double somme = 0;
            for (Integer val : entry.getValue()) {
                somme += val;
            }
            double moyenne = somme / entry.getValue().size();
            series.getData().add(new XYChart.Data<String, Number>(entry.getKey().toString(), moyenne));
        }

        lineChartEvolutionScore.getData().add(series);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Node line = lineChartEvolutionScore.lookup(".chart-series-line");
                if (line != null) {
                    line.setStyle("-fx-stroke: #8FA6B8; -fx-stroke-width: 2px;");
                }

                for (Node node : lineChartEvolutionScore.lookupAll(".chart-line-symbol")) {
                    node.setStyle("-fx-background-color: #8FA6B8, white;");
                }
            }
        });
    }

    private void chargerSuiviDansFormulaire(SuiviMentale s) {
        editingSuiviId = s.getId();

        if (s.getDateDeSuivi() != null) {
            dateSuiviPicker.setValue(s.getDateDeSuivi().toLocalDate());
        } else {
            dateSuiviPicker.setValue(LocalDate.now());
        }

        humeurCombo.setValue(s.getHumeur() == null ? "Neutre" : s.getHumeur());
        qualiteSommeilCombo.setValue(s.getQualiteDuSommeil() == null ? "Moyen" : s.getQualiteDuSommeil());
        heureSommeilField.setText(String.valueOf(s.getHeureDeSommeil()));
        stressField.setText(String.valueOf(s.getTauxDeStress()));
        energieField.setText(String.valueOf(s.getNiveauDenergie()));
        journalArea.setText(s.getJournalEmotionnelle() == null ? "" : s.getJournalEmotionnelle());

        saveButton.setText("Mettre à jour");
        afficherGlobalMessage("Suivi prêt à être modifié.", false);
        showAjouterSection();
    }

    private void confirmerSuppression(SuiviMentale s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce suivi ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            suiviService.delete(s);

            Objectif objectif = objectifService.getById(s.getObjectifId());
            if (objectif != null) {
                mettreAJourProgressionObjectif(objectif.getId());
            }

            refreshAll();
            verifierAjoutAutorise();
            afficherGlobalMessage("Suivi supprimé avec succès.", false);
        }
    }

    private void verifierAjoutAutorise() {
        Objectif objectifEnCours = objectifService.getObjectifEnCoursByUser(currentUserId);
        boolean autorise = objectifEnCours != null;
        saveButton.setDisable(!autorise);

        if (!autorise) {
            afficherGlobalMessage("Ajout désactivé : aucun objectif en cours pour cet utilisateur.", true);
        }

        afficherObjectifLie();
    }

    private void afficherObjectifLie() {
        if (objectifLieLabel == null) {
            return;
        }

        Objectif objectifEnCours = objectifService.getObjectifEnCoursByUser(currentUserId);

        if (objectifEnCours == null) {
            objectifLieLabel.setText("Aucun objectif en cours.");
            return;
        }

        objectifLieLabel.setText(
                "Ce suivi sera lié à l'objectif : " + safeTexte(objectifEnCours.getTitre()) +
                        " | Type : " + safeTexte(objectifEnCours.getTypeObjectif()) +
                        " | Cible : " + objectifEnCours.getValeurCible()
        );
    }

    private void mettreAJourProgressionObjectif(int objectifId) {
        Objectif objectif = objectifService.getById(objectifId);

        if (objectif == null) {
            return;
        }

        List<SuiviMentale> suivis = suiviService.getByObjectif(objectifId);

        if (suivis == null || suivis.isEmpty()) {
            objectifService.updateProgression(objectifId, 0);
            return;
        }

        String type = objectif.getTypeObjectif() == null ? "" : objectif.getTypeObjectif().trim().toLowerCase();
        double cible = objectif.getValeurCible();

        if (cible <= 0) {
            objectifService.updateProgression(objectifId, 0);
            return;
        }

        double somme = 0;

        for (SuiviMentale s : suivis) {
            switch (type) {
                case "stress":
                    somme += s.getTauxDeStress();
                    break;
                case "stress_global":
                    somme += s.getTauxDeStressGlobale();
                    break;
                case "sommeil":
                    somme += s.getHeureDeSommeil();
                    break;
                case "energie":
                    somme += s.getNiveauDenergie();
                    break;
                case "score_mentale":
                    somme += s.getScoreMentale();
                    break;
                default:
                    somme += s.getScoreMentale();
                    break;
            }
        }

        double moyenne = somme / suivis.size();
        int progression;

        if ("stress".equals(type) || "stress_global".equals(type)) {
            if (moyenne <= cible) {
                progression = 100;
            } else {
                progression = (int) Math.round((cible / moyenne) * 100.0);
            }
        } else {
            progression = (int) Math.round((moyenne / cible) * 100.0);
        }

        progression = Math.max(0, Math.min(100, progression));
        objectifService.updateProgression(objectifId, progression);
    }

    private int calculerScoreMental(String humeur, String qualite, int stress, int energie, double sommeil) {
        int scoreHumeur = switch (humeur.toLowerCase()) {
            case "très mal" -> 20;
            case "neutre" -> 50;
            case "bien" -> 75;
            case "très bien" -> 100;
            default -> 50;
        };

        int scoreQualite = switch (qualite.toLowerCase()) {
            case "terrible" -> 20;
            case "mauvais" -> 35;
            case "moyen" -> 60;
            case "bon" -> 80;
            case "excellent" -> 100;
            default -> 50;
        };

        int scoreStress = Math.max(0, 100 - (stress * 10));
        int scoreEnergie = Math.min(100, energie * 10);

        int scoreSommeil;
        if (sommeil >= 7 && sommeil <= 8) scoreSommeil = 100;
        else if (sommeil >= 6) scoreSommeil = 75;
        else if (sommeil >= 5) scoreSommeil = 55;
        else scoreSommeil = 30;

        return (scoreHumeur + scoreQualite + scoreStress + scoreEnergie + scoreSommeil) / 5;
    }

    private String getHumeurDominante(int tresMal, int neutre, int bien, int tresBien) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("Très mal", tresMal);
        map.put("Neutre", neutre);
        map.put("Bien", bien);
        map.put("Très bien", tresBien);

        String best = "-";
        int max = -1;

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                best = entry.getKey();
            }
        }

        return best;
    }

    @FXML
    public void exporterPDF() {
        afficherGlobalMessage("Export PDF à connecter ensuite.", false);
    }

    private void afficherMessage(String msg, boolean erreur) {
        messageLabel.setText(msg == null ? "" : msg);
        if (erreur) {
            messageLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #1e8449; -fx-font-weight: bold;");
        }
    }

    private void afficherGlobalMessage(String msg, boolean erreur) {
        globalMessageLabel.setText(msg == null ? "" : msg);

        if (msg == null || msg.isEmpty()) {
            globalMessageLabel.setStyle(
                    "-fx-padding: 0;" +
                            "-fx-font-size: 0;" +
                            "-fx-background-color: transparent;"
            );
            return;
        }

        if (erreur) {
            globalMessageLabel.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #c0392b;" +
                            "-fx-background-color: #fff1f1;" +
                            "-fx-background-radius: 18;" +
                            "-fx-padding: 8 16;"
            );
        } else {
            globalMessageLabel.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #1e8449;" +
                            "-fx-background-color: #eafaf1;" +
                            "-fx-background-radius: 18;" +
                            "-fx-padding: 8 16;"
            );
        }
    }

    private String safeTexte(String valeur) {
        return valeur == null ? "" : valeur;
    }
}