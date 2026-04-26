package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.concurrent.Task;
import models.TipData;
import services.OpenRouterChatService;
import services.WellbeingTipService;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.Objectif;
import models.SuiviMentale;
import services.CitationFilterService;
import services.ConseilThemeService;
import services.ExportPdfService;
import services.MyMemoryTranslationService;
import services.NotificationService;
import services.ServiceObjectif;
import services.ServiceSuiviMentale;
import services.ZenQuotesService;

import java.awt.Desktop;
import java.io.File;
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
    @FXML private TextArea chatArea;
    @FXML private TextField chatInputField;
    @FXML private Button sendChatButton;
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

    @FXML private Label dateErrorLabel;
    @FXML private Label humeurErrorLabel;
    @FXML private Label qualiteSommeilErrorLabel;
    @FXML private Label heureSommeilErrorLabel;
    @FXML private Label stressErrorLabel;
    @FXML private Label energieErrorLabel;
    @FXML private Label journalErrorLabel;

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
    private final WellbeingTipService wellbeingTipService = new WellbeingTipService();
    private final ServiceSuiviMentale suiviService = new ServiceSuiviMentale();
    private final ServiceObjectif objectifService = new ServiceObjectif();
    private final NotificationService notificationService = new NotificationService();
    private final ExportPdfService exportPdfService = new ExportPdfService();
    private final ZenQuotesService zenQuotesService = new ZenQuotesService();
    private final CitationFilterService citationFilterService = new CitationFilterService();
    private final ConseilThemeService conseilThemeService = new ConseilThemeService();
    private final MyMemoryTranslationService translationService = new MyMemoryTranslationService();
    private final OpenRouterChatService chatService = new OpenRouterChatService();
    private Integer editingSuiviId = null;
    private String derniereCitationAffichee = "";
    private final int currentUserId = 1;
    private long dernierRefreshCitation = 0;

    @FXML
    public void initialize() {
        if (humeurCombo != null) {
            humeurCombo.setItems(FXCollections.observableArrayList(
                    "Très mal", "Neutre", "Bien", "Très bien"
            ));
            humeurCombo.setValue("Neutre");
        }

        if (qualiteSommeilCombo != null) {
            qualiteSommeilCombo.setItems(FXCollections.observableArrayList(
                    "Terrible", "Mauvais", "Moyen", "Bon", "Excellent"
            ));
            qualiteSommeilCombo.setValue("Moyen");
        }

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

        if (dateSuiviPicker != null) {
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
        }

        if (xAxisMoyennes != null) xAxisMoyennes.setAnimated(false);
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

        resetValidationMessages();
        objectifService.terminerObjectifsExpiresByUser(currentUserId);
        refreshAll();
        verifierAjoutAutorise();
        afficherObjectifLie();
        showAjouterSection();
        verifierRappelQuotidien();

        if (conseilLabel != null) {
            conseilLabel.setText("Chargement du message bien-être...");
            chargerConseilDuJour();
        }

        if (chatArea != null) {
            chatArea.setText("🤖 Coach : Bonjour, je suis là pour vous écouter. Comment vous sentez-vous aujourd'hui ?\n\n");
            chatArea.setWrapText(true);
            chatArea.setEditable(false);
        }

        if (chatInputField != null) {
            chatInputField.setOnAction(event -> envoyerMessageChat());
        }
    }
    @FXML
    public void chargerConseilDuJour() {
        if (conseilLabel != null) {
            conseilLabel.setText("Chargement du message bien-être...");
        }

        new Thread(() -> {
            String texteFinal = chargerCitationInitialeLieeAuSuivi();

            Platform.runLater(() -> {
                if (conseilLabel != null) {
                    conseilLabel.setText(finaliserAffichageTexte(texteFinal));
                }
            });
        }).start();
    }

    @FXML
    public void actualiserCitationSeulement() {
        long maintenant = System.currentTimeMillis();

        if (maintenant - dernierRefreshCitation < 5000) {
            if (conseilLabel != null) {
                conseilLabel.setText("Veuillez attendre quelques secondes avant de réactualiser.");
            }
            return;
        }

        dernierRefreshCitation = maintenant;

        if (conseilLabel != null) {
            conseilLabel.setText("Chargement d'une nouvelle citation...");
        }

        new Thread(() -> {
            String texteFinal = chargerCitationRandomSimple();

            Platform.runLater(() -> {
                if (conseilLabel != null) {
                    conseilLabel.setText(finaliserAffichageTexte(texteFinal));
                }
            });
        }).start();
    }

    private String chargerCitationInitialeLieeAuSuivi() {
        try {
            SuiviMentale dernierSuivi = suiviService.getDernierSuiviParUser(currentUserId);
            String theme = conseilThemeService.detecterTheme(dernierSuivi);

            String[] data = zenQuotesService.getRandomQuote();
            String citation = data[0];
            String auteur = data[1];

            if ("__RATE_LIMIT__".equals(citation)) {
                return conseilThemeService.getMessageLocal(theme);
            }

            boolean okGenerale = citationFilterService.estAppropriee(citation);
            boolean okTheme = citationFilterService.correspondAuTheme(citation, theme);
            boolean nouvelleCitation = citation != null
                    && !citation.isBlank()
                    && !citation.equalsIgnoreCase(derniereCitationAffichee);

            if (okGenerale && okTheme && nouvelleCitation) {
                derniereCitationAffichee = citation;
                return formaterCitation(citation, auteur);
            }

            return conseilThemeService.getMessageLocal(theme);

        } catch (Exception e) {
            e.printStackTrace();
            return "Prenez un moment pour respirer, ralentir et écouter vos besoins aujourd’hui.";
        }
    }

    private String chargerCitationRandomSimple() {
        try {
            String[] data = zenQuotesService.getRandomQuote();
            String citation = data[0];
            String auteur = data[1];

            if ("__RATE_LIMIT__".equals(citation)) {
                return citationLocaleSecours();
            }

            if (citation != null && !citation.isBlank()) {
                if (!citation.equalsIgnoreCase(derniereCitationAffichee)) {
                    derniereCitationAffichee = citation;
                }
                return formaterCitation(citation, auteur);
            }

            return citationLocaleSecours();

        } catch (Exception e) {
            e.printStackTrace();
            return citationLocaleSecours();
        }
    }

    private String formaterCitation(String citation, String auteur) {
        try {
            String citationTraduite = translationService.traduireAnglaisVersFrancais(citation);

            if (citationTraduite == null || citationTraduite.isBlank()) {
                citationTraduite = citation;
            }

            if (auteur != null && !auteur.isBlank()) {
                return "\"" + citationTraduite + "\"\n- " + auteur;
            } else {
                return "\"" + citationTraduite + "\"";
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (auteur != null && !auteur.isBlank()) {
                return "\"" + citation + "\"\n- " + auteur;
            } else {
                return "\"" + citation + "\"";
            }
        }
    }

    private String citationLocaleSecours() {
        String[] citations = {
                "\"Prenez soin de vous un jour à la fois.\"",
                "\"Chaque petit progrès compte.\"",
                "\"Respirez profondément, vous faites de votre mieux.\"",
                "\"Le repos est aussi une forme de force.\"",
                "\"Votre bien-être mérite de l’attention chaque jour.\""
        };

        int index = (int) (System.currentTimeMillis() % citations.length);
        return citations[index];
    }

    private String finaliserAffichageTexte(String texte) {
        if (texte == null || texte.isBlank()) {
            return "\"Prenez soin de vous un jour à la fois.\"";
        }
        return texte;
    }
    @FXML
    public void envoyerMessageChat() {
        if (chatInputField == null || chatArea == null) {
            return;
        }

        String userMessage = chatInputField.getText() == null ? "" : chatInputField.getText().trim();

        if (userMessage.isEmpty()) {
            return;
        }

        chatArea.appendText("👤 Vous : " + userMessage + "\n\n");
        chatInputField.clear();

        if (sendChatButton != null) {
            sendChatButton.setDisable(true);
        }

        final String placeholder = "🤖 Coach : réflexion en cours...\n\n";
        chatArea.appendText(placeholder);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return chatService.envoyerMessage(userMessage);
            }
        };

        task.setOnSucceeded(event -> {
            String response = task.getValue();

            String currentText = chatArea.getText();
            if (currentText.endsWith(placeholder)) {
                chatArea.setText(currentText.substring(0, currentText.length() - placeholder.length()));
            }

            chatArea.appendText("🤖 Coach : " + response + "\n\n");

            if (sendChatButton != null) {
                sendChatButton.setDisable(false);
            }
        });

        task.setOnFailed(event -> {
            String currentText = chatArea.getText();
            if (currentText.endsWith(placeholder)) {
                chatArea.setText(currentText.substring(0, currentText.length() - placeholder.length()));
            }

            chatArea.appendText("🤖 Coach : Erreur lors de la communication avec le chatbot.\n\n");

            if (sendChatButton != null) {
                sendChatButton.setDisable(false);
            }

            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void verifierRappelQuotidien() {
        try {
            boolean suiviExisteAujourdHui = suiviService.hasSuiviToday(currentUserId);

            if (!suiviExisteAujourdHui) {
                boolean rappelExiste = notificationService.hasReminderNotificationToday(currentUserId);

                if (!rappelExiste) {
                    notificationService.creerNotificationRappelSuivi(currentUserId);
                    notificationService.afficherPopupRappelSuivi();
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur rappel quotidien : " + e.getMessage());
            e.printStackTrace();
        }
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
        if (searchDateSuiviPicker != null) searchDateSuiviPicker.setValue(null);
        if (searchHumeurCombo != null) searchHumeurCombo.setValue("Toutes");
        if (searchQualiteSommeilCombo != null) searchQualiteSommeilCombo.setValue("Toutes");
        if (searchJournalField != null) searchJournalField.clear();
        refreshHistorique();
    }

    private void setActiveSection(String section) {
        boolean ajout = "ajout".equals(section);
        boolean hist = "historique".equals(section);
        boolean stats = "stats".equals(section);
        boolean chat = "chat".equals(section);

        if (ajoutSectionScroll != null) {
            ajoutSectionScroll.setVisible(ajout);
            ajoutSectionScroll.setManaged(ajout);
        }

        if (historiqueSectionScroll != null) {
            historiqueSectionScroll.setVisible(hist);
            historiqueSectionScroll.setManaged(hist);
        }

        if (statistiquesSectionScroll != null) {
            statistiquesSectionScroll.setVisible(stats);
            statistiquesSectionScroll.setManaged(stats);
        }

        if (chatSection != null) {
            chatSection.setVisible(chat);
            chatSection.setManaged(chat);
        }

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

        if (ajouterTabButton != null) ajouterTabButton.setStyle(normalStyle);
        if (statistiquesTabButton != null) statistiquesTabButton.setStyle(normalStyle);
        if (historiqueTabButton != null) historiqueTabButton.setStyle(normalStyle);
        if (chatTabButton != null) chatTabButton.setStyle(normalStyle);
        if (pdfButton != null) pdfButton.setStyle(normalStyle);

        switch (activeTab) {
            case "ajout" -> {
                if (ajouterTabButton != null) ajouterTabButton.setStyle(activeStyle);
            }
            case "stats" -> {
                if (statistiquesTabButton != null) statistiquesTabButton.setStyle(activeStyle);
            }
            case "historique" -> {
                if (historiqueTabButton != null) historiqueTabButton.setStyle(activeStyle);
            }
            case "chat" -> {
                if (chatTabButton != null) chatTabButton.setStyle(activeStyle);
            }
        }
    }

    private void resetValidationMessages() {
        if (dateErrorLabel != null) dateErrorLabel.setText("");
        if (humeurErrorLabel != null) humeurErrorLabel.setText("");
        if (qualiteSommeilErrorLabel != null) qualiteSommeilErrorLabel.setText("");
        if (heureSommeilErrorLabel != null) heureSommeilErrorLabel.setText("");
        if (stressErrorLabel != null) stressErrorLabel.setText("");
        if (energieErrorLabel != null) energieErrorLabel.setText("");
        if (journalErrorLabel != null) journalErrorLabel.setText("");
    }

    private void setFieldMessage(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message == null ? "" : message);
        }
    }

    private boolean validerFormulaire(List<String> erreurs) {
        boolean valide = true;
        resetValidationMessages();

        if (dateSuiviPicker == null || dateSuiviPicker.getValue() == null) {
            setFieldMessage(dateErrorLabel, "La date est obligatoire.");
            erreurs.add("La date est obligatoire.");
            valide = false;
        } else if (dateSuiviPicker.getValue().isAfter(LocalDate.now())) {
            setFieldMessage(dateErrorLabel, "La date ne doit pas être au futur.");
            erreurs.add("La date du suivi ne doit pas être au futur.");
            valide = false;
        }

        if (humeurCombo == null || humeurCombo.getValue() == null || humeurCombo.getValue().trim().isEmpty()) {
            setFieldMessage(humeurErrorLabel, "Veuillez choisir une humeur.");
            erreurs.add("Veuillez choisir une humeur.");
            valide = false;
        }

        if (qualiteSommeilCombo == null || qualiteSommeilCombo.getValue() == null || qualiteSommeilCombo.getValue().trim().isEmpty()) {
            setFieldMessage(qualiteSommeilErrorLabel, "Veuillez choisir une qualité du sommeil.");
            erreurs.add("Veuillez choisir une qualité du sommeil.");
            valide = false;
        }

        if (heureSommeilField == null || heureSommeilField.getText() == null || heureSommeilField.getText().trim().isEmpty()) {
            setFieldMessage(heureSommeilErrorLabel, "Veuillez saisir les heures de sommeil.");
            erreurs.add("Veuillez saisir les heures de sommeil.");
            valide = false;
        } else {
            try {
                double heures = Double.parseDouble(heureSommeilField.getText().trim());
                if (heures < 0 || heures > 24) {
                    setFieldMessage(heureSommeilErrorLabel, "La valeur doit être entre 0 et 24.");
                    erreurs.add("Les heures de sommeil doivent être entre 0 et 24.");
                    valide = false;
                }
            } catch (Exception e) {
                setFieldMessage(heureSommeilErrorLabel, "Veuillez saisir un nombre valide.");
                erreurs.add("Heures de sommeil invalides.");
                valide = false;
            }
        }

        if (stressField == null || stressField.getText() == null || stressField.getText().trim().isEmpty()) {
            setFieldMessage(stressErrorLabel, "Veuillez saisir le stress.");
            erreurs.add("Veuillez saisir le stress.");
            valide = false;
        } else {
            try {
                int stress = Integer.parseInt(stressField.getText().trim());
                if (stress < 0 || stress > 10) {
                    setFieldMessage(stressErrorLabel, "Le stress doit être entre 0 et 10.");
                    erreurs.add("Le stress doit être entre 0 et 10.");
                    valide = false;
                }
            } catch (Exception e) {
                setFieldMessage(stressErrorLabel, "Veuillez saisir un entier valide.");
                erreurs.add("Taux de stress invalide.");
                valide = false;
            }
        }

        if (energieField == null || energieField.getText() == null || energieField.getText().trim().isEmpty()) {
            setFieldMessage(energieErrorLabel, "Veuillez saisir l'énergie.");
            erreurs.add("Veuillez saisir l'énergie.");
            valide = false;
        } else {
            try {
                int energie = Integer.parseInt(energieField.getText().trim());
                if (energie < 0 || energie > 10) {
                    setFieldMessage(energieErrorLabel, "L'énergie doit être entre 0 et 10.");
                    erreurs.add("L'énergie doit être entre 0 et 10.");
                    valide = false;
                }
            } catch (Exception e) {
                setFieldMessage(energieErrorLabel, "Veuillez saisir un entier valide.");
                erreurs.add("Niveau d'énergie invalide.");
                valide = false;
            }
        }

        if (journalArea == null || journalArea.getText() == null || journalArea.getText().trim().isEmpty()) {
            setFieldMessage(journalErrorLabel, "Veuillez saisir le journal émotionnel.");
            erreurs.add("Veuillez saisir le journal émotionnel.");
            valide = false;
        }

        return valide;
    }

    private Alert creerPopupConfirmationStylise(String header, String content) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(header);
        confirmation.setContentText(content);

        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8fbff);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #dbe7f5;" +
                        "-fx-border-radius: 18;" +
                        "-fx-padding: 18;"
        );

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        if (okButton != null) {
            okButton.setText("Confirmer");
            okButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #234b7d, #2f5d97);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 14;" +
                            "-fx-padding: 8 18;" +
                            "-fx-cursor: hand;"
            );
        }

        if (cancelButton != null) {
            cancelButton.setText("Annuler");
            cancelButton.setStyle(
                    "-fx-background-color: #eef2f7;" +
                            "-fx-text-fill: #475569;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 14;" +
                            "-fx-padding: 8 18;" +
                            "-fx-cursor: hand;"
            );
        }

        return confirmation;
    }

    @FXML
    public void saveSuivi() {
        try {
            Objectif objectifEnCours = objectifService.getObjectifEnCoursByUser(currentUserId);

            if (objectifEnCours == null) {
                afficherMessage("Impossible d'ajouter un suivi : aucun objectif en cours.", true);
                return;
            }

            List<String> erreurs = new ArrayList<>();
            boolean formulaireValide = validerFormulaire(erreurs);

            if (!formulaireValide) {
                afficherMessage("Veuillez corriger les champs manquants.", true);
                return;
            }

            double heures = Double.parseDouble(heureSommeilField.getText().trim());
            int stress = Integer.parseInt(stressField.getText().trim());
            int energie = Integer.parseInt(energieField.getText().trim());

            Objectif objectifLie = objectifService.getObjectifEnCoursByUser(currentUserId);

            if (objectifLie == null) {
                afficherMessage("Aucun objectif en cours trouvé.", true);
                return;
            }

            int ancienneProgression = objectifLie.getProgression();

            Alert confirmation = creerPopupConfirmationStylise(
                    "Confirmer l'enregistrement du suivi",
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

            int nouvelleProgression = mettreAJourProgressionObjectifEtRetournerValeur(objectifLie.getId());

            notificationService.notifierProgression(
                    ancienneProgression,
                    nouvelleProgression,
                    currentUserId,
                    objectifLie.getId(),
                    null
            );

            // Notification micro-exercice
            TipData tip = wellbeingTipService.getTipForSuivi(s);

            if (tip != null) {
                notificationService.notifierMicroExercice(
                        tip,
                        currentUserId,
                        objectifLie.getId(),
                        null
                );
            }

            clearForm();
            refreshAll();
            verifierAjoutAutorise();
            afficherObjectifLie();
            showHistoriqueSection();

        } catch (Exception e) {
            afficherMessage("Erreur lors de l'enregistrement : " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    @FXML
    public void clearForm() {
        editingSuiviId = null;

        if (dateSuiviPicker != null) dateSuiviPicker.setValue(LocalDate.now());
        if (humeurCombo != null) humeurCombo.setValue("Neutre");
        if (qualiteSommeilCombo != null) qualiteSommeilCombo.setValue("Moyen");
        if (heureSommeilField != null) heureSommeilField.clear();
        if (stressField != null) stressField.clear();
        if (energieField != null) energieField.clear();
        if (journalArea != null) journalArea.clear();
        if (saveButton != null) saveButton.setText("💾 Enregistrer");

        resetValidationMessages();
        afficherMessage("", false);
        afficherObjectifLie();
    }

    private void refreshAll() {
        refreshHistorique();
        refreshStatistiques();
    }

    private void refreshHistorique() {
        if (historiqueContainer == null) {
            return;
        }

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
            liste = new ArrayList<>();
        }

        if (totalSuivisLabel != null) {
            totalSuivisLabel.setText(String.valueOf(liste.size()));
        }

        if (liste.isEmpty()) {
            if (stressMoyenLabel != null) stressMoyenLabel.setText("0");
            if (energieMoyenneLabel != null) energieMoyenneLabel.setText("0");
            if (sommeilMoyenLabel != null) sommeilMoyenLabel.setText("0");
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

        if (stressMoyenLabel != null) stressMoyenLabel.setText(String.format("%.2f", moyenneStress));
        if (energieMoyenneLabel != null) energieMoyenneLabel.setText(String.format("%.2f", moyenneEnergie));
        if (sommeilMoyenLabel != null) sommeilMoyenLabel.setText(String.format("%.2f", moyenneSommeil));
        if (humeurDominanteLabel != null) humeurDominanteLabel.setText(getHumeurDominante(tresMal, neutre, bien, tresBien));
        if (scoreMentalMoyenLabel != null) scoreMentalMoyenLabel.setText(String.format("%.2f", moyenneScore));

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

        Platform.runLater(() -> {
            String[] colors = {"#D6C6E1", "#CBBBAF", "#B7C9E2", "#C7D7C0"};
            int i = 0;
            for (PieChart.Data item : pieChartHumeursStats.getData()) {
                Node node = item.getNode();
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
                }
                i++;
            }
        });
    }

    private void chargerBarChartMoyennes(double stress, double energie, double sommeil, double score) {
        if (barChartMoyennes == null) {
            return;
        }

        barChartMoyennes.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Stress", stress));
        series.getData().add(new XYChart.Data<>("Énergie", energie));
        series.getData().add(new XYChart.Data<>("Sommeil", sommeil));
        series.getData().add(new XYChart.Data<>("Score", score));

        barChartMoyennes.getData().add(series);

        Platform.runLater(() -> {
            for (Node node : barChartMoyennes.lookupAll(".default-color0.chart-bar")) {
                node.setStyle("-fx-bar-fill: #AAB8C7;");
            }
        });
    }

    private void chargerLineChartEvolution(List<SuiviMentale> liste) {
        if (lineChartEvolutionScore == null) {
            return;
        }

        lineChartEvolutionScore.getData().clear();

        TreeMap<LocalDate, List<Integer>> grouped = new TreeMap<>();

        for (SuiviMentale s : liste) {
            if (s.getDateDeSuivi() == null) {
                continue;
            }

            LocalDate date = s.getDateDeSuivi().toLocalDate();
            grouped.putIfAbsent(date, new ArrayList<>());
            grouped.get(date).add(s.getScoreMentale());
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<LocalDate, List<Integer>> entry : grouped.entrySet()) {
            double somme = 0;
            for (Integer val : entry.getValue()) {
                somme += val;
            }
            double moyenne = somme / entry.getValue().size();
            series.getData().add(new XYChart.Data<>(entry.getKey().toString(), moyenne));
        }

        lineChartEvolutionScore.getData().add(series);

        Platform.runLater(() -> {
            Node line = lineChartEvolutionScore.lookup(".chart-series-line");
            if (line != null) {
                line.setStyle("-fx-stroke: #8FA6B8; -fx-stroke-width: 2px;");
            }

            for (Node node : lineChartEvolutionScore.lookupAll(".chart-line-symbol")) {
                node.setStyle("-fx-background-color: #8FA6B8, white;");
            }
        });
    }

    private void chargerSuiviDansFormulaire(SuiviMentale s) {
        editingSuiviId = s.getId();

        if (dateSuiviPicker != null) {
            if (s.getDateDeSuivi() != null) {
                dateSuiviPicker.setValue(s.getDateDeSuivi().toLocalDate());
            } else {
                dateSuiviPicker.setValue(LocalDate.now());
            }
        }

        if (humeurCombo != null) {
            humeurCombo.setValue(s.getHumeur() == null ? "Neutre" : s.getHumeur());
        }

        if (qualiteSommeilCombo != null) {
            qualiteSommeilCombo.setValue(s.getQualiteDuSommeil() == null ? "Moyen" : s.getQualiteDuSommeil());
        }

        if (heureSommeilField != null) {
            heureSommeilField.setText(String.valueOf(s.getHeureDeSommeil()));
        }

        if (stressField != null) {
            stressField.setText(String.valueOf(s.getTauxDeStress()));
        }

        if (energieField != null) {
            energieField.setText(String.valueOf(s.getNiveauDenergie()));
        }

        if (journalArea != null) {
            journalArea.setText(s.getJournalEmotionnelle() == null ? "" : s.getJournalEmotionnelle());
        }

        if (saveButton != null) {
            saveButton.setText("Mettre à jour");
        }

        resetValidationMessages();
        afficherGlobalMessage("Suivi prêt à être modifié.", false);
        showAjouterSection();
    }

    private void confirmerSuppression(SuiviMentale s) {
        Alert alert = creerPopupConfirmationStylise(
                "Supprimer ce suivi ?",
                "Cette action est irréversible."
        );

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

        if (saveButton != null) {
            saveButton.setDisable(!autorise);
        }

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
        mettreAJourProgressionObjectifEtRetournerValeur(objectifId);
    }

    private int mettreAJourProgressionObjectifEtRetournerValeur(int objectifId) {
        Objectif objectif = objectifService.getById(objectifId);

        if (objectif == null) {
            return 0;
        }

        List<SuiviMentale> suivis = suiviService.getByObjectif(objectifId);

        if (suivis == null || suivis.isEmpty()) {
            objectifService.updateProgression(objectifId, 0);
            return 0;
        }

        String type = objectif.getTypeObjectif() == null ? "" : objectif.getTypeObjectif().trim().toLowerCase();
        double cible = objectif.getValeurCible();

        if (cible <= 0) {
            objectifService.updateProgression(objectifId, 0);
            return 0;
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
                case "score mentale":
                case "scoremental":
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

        return progression;
    }

    private int calculerScoreMental(String humeur, String qualite, int stress, int energie, double sommeil) {
        String humeurSafe = humeur == null ? "" : humeur.toLowerCase();
        String qualiteSafe = qualite == null ? "" : qualite.toLowerCase();

        int scoreHumeur = switch (humeurSafe) {
            case "très mal", "tres mal" -> 20;
            case "neutre" -> 50;
            case "bien" -> 75;
            case "très bien", "tres bien" -> 100;
            default -> 50;
        };

        int scoreQualite = switch (qualiteSafe) {
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
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
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
        try {
            List<SuiviMentale> suivis = suiviService.getByUser(currentUserId);

            if (suivis == null || suivis.isEmpty()) {
                afficherGlobalMessage("Aucun suivi à exporter.", true);
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.setInitialFileName("suivis_utilisateur_" + currentUserId + ".pdf");

            File downloadsFolder = new File(System.getProperty("user.home"), "Downloads");
            if (downloadsFolder.exists()) {
                fileChooser.setInitialDirectory(downloadsFolder);
            }

            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );

            Stage stage = (Stage) pdfButton.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);

            if (selectedFile == null) {
                afficherGlobalMessage("Export annulé.", true);
                return;
            }

            if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
            }

            String nomUtilisateur = "Amal Ghazouani";

            File pdf = exportPdfService.exporterSuivisUtilisateurEnPdfVersFichier(
                    suivis,
                    currentUserId,
                    nomUtilisateur,
                    selectedFile
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export PDF");
            alert.setHeaderText("Export terminé avec succès");
            alert.setContentText("Le fichier PDF a été enregistré ici :\n" + pdf.getAbsolutePath());

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8fbff);" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-color: #dbe7f5;" +
                            "-fx-border-radius: 18;" +
                            "-fx-padding: 18;"
            );

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #234b7d, #2f5d97);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 14;" +
                                "-fx-padding: 8 18;" +
                                "-fx-cursor: hand;"
                );
            }

            alert.showAndWait();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdf);
            }

            afficherGlobalMessage("Export PDF réalisé avec succès.", false);

        } catch (Exception e) {
            e.printStackTrace();
            afficherGlobalMessage("Erreur lors de l'export PDF : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String msg, boolean erreur) {
        if (messageLabel == null) {
            return;
        }

        messageLabel.setText(msg == null ? "" : msg);

        if (msg == null || msg.isEmpty()) {
            messageLabel.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: transparent;" +
                            "-fx-background-color: transparent;" +
                            "-fx-padding: 0;"
            );
            return;
        }

        if (erreur) {
            messageLabel.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #dc2626;" +
                            "-fx-background-color: #fff1f2;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #fecdd3;" +
                            "-fx-border-radius: 12;" +
                            "-fx-padding: 8 12;"
            );
        } else {
            messageLabel.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #15803d;" +
                            "-fx-background-color: #ecfdf5;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #bbf7d0;" +
                            "-fx-border-radius: 12;" +
                            "-fx-padding: 8 12;"
            );
        }
    }

    private void afficherGlobalMessage(String msg, boolean erreur) {
        if (globalMessageLabel == null) {
            return;
        }

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