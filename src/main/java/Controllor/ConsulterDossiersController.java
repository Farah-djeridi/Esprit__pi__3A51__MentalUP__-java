package Controllor;

import Models.Dossier;
import Models.RendezVous;
import Services.PDFService;
import Services.ServiceDossier;
import javafx.scene.layout.StackPane;
import services.ServiceRendezVous;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.util.Optional;
import java.util.List;
import javafx.collections.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConsulterDossiersController {

    @FXML private ListView<Dossier> dossierList;
    @FXML private TextField searchField;

    @FXML private Label patientLabel;
    @FXML private Label dateLabel;

    @FXML private TextArea notesArea;
    @FXML private ComboBox<String> niveauRisqueBox;

    // IA fields
    @FXML private TextArea aiSummaryArea;
    @FXML private Button btnGenererIA;
    @FXML private StackPane contentArea;

    @FXML private HBox navHome;
    @FXML private VBox submenuRdv;
    @FXML private VBox submenuDossiers;
    @FXML private Label arrowRdv;
    @FXML private Label arrowDossiers;
    @FXML private Button btnExporterTop;

    private ServiceDossier service = new ServiceDossier();
    private PDFService pdfService = new PDFService();
    private ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private Dossier selected;
    private int currentPsyId = 6; // Default fallback

    @FXML
    public void initialize() {
        if (utils.SessionManager.getInstance().isLoggedIn()) {
            currentPsyId = utils.SessionManager.getInstance().getCurrentUser().getId();
        }

        niveauRisqueBox.getItems().addAll("Faible", "Modéré", "Élevé", "Critique");


        if (submenuRdv != null) {
            submenuRdv.setVisible(false);
            submenuRdv.setManaged(false);
        }
        if (submenuDossiers != null) {
            submenuDossiers.setVisible(true);
            submenuDossiers.setManaged(true);
        }

        loadData();

        dossierList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (btnExporterTop != null) btnExporterTop.setDisable(false);
                showDetails(newVal);
            } else {
                if (btnExporterTop != null) btnExporterTop.setDisable(true);
            }
        });

        dossierList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }

    @FXML
    public void loadData() {
        ObservableList<Dossier> list =
                FXCollections.observableArrayList(service.getByPsychologueId(currentPsyId));
        applyListView(list);
    }

    private void applyListView(ObservableList<Dossier> list) {
        dossierList.setItems(list);
        dossierList.setCellFactory(lv -> new ListCell<Dossier>() {
            @Override
            protected void updateItem(Dossier d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox root = new HBox(12);
                    root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    root.setStyle("-fx-padding: 10 14; -fx-background-radius: 10;");

                    VBox infos = new VBox(4);
                    HBox.setHgrow(infos, Priority.ALWAYS);

                    Label title = new Label((d.getPatientNom() != null ? d.getPatientNom() : "Patient #" + d.getPatientId()));
                    title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f2942;");

                    HBox sub = new HBox(8);
                    Label idLabel = new Label("N° " + d.getId());
                    idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-background-color: #f1f5f9; -fx-padding: 2 6; -fx-background-radius: 4;");

                    String risqueColor = getRisqueColor(d.getNiveauRisque());
                    Label risque = new Label(d.getNiveauRisque() != null ? d.getNiveauRisque().toUpperCase() : "N/A");
                    risque.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + risqueColor + "; -fx-padding: 2 8; -fx-background-radius: 4;");

                    sub.getChildren().addAll(idLabel, risque);
                    infos.getChildren().addAll(title, sub);

                    Button btnDel = new Button("✕");
                    btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand;");
                    btnDel.setOnAction(e -> {
                        dossierList.getSelectionModel().select(d);
                        handleDelete();
                    });


                    updateStyle(root, title, isSelected());

                    root.getChildren().addAll(infos, btnDel);
                    setGraphic(root);
                    setText(null);


                    setOnMouseEntered(e -> {
                        if (!isSelected()) root.setStyle("-fx-padding: 10 14; -fx-background-radius: 10; -fx-background-color: #f1f5f9;");
                    });
                    setOnMouseExited(e -> {
                        if (!isSelected()) root.setStyle("-fx-padding: 10 14; -fx-background-radius: 10; -fx-background-color: transparent;");
                    });
                    setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (getGraphic() instanceof HBox root) {
                    VBox infos = (VBox) root.getChildren().get(0);
                    Label title = (Label) infos.getChildren().get(0);
                    updateStyle(root, title, selected);
                }
            }

            private void updateStyle(HBox root, Label title, boolean selected) {
                if (selected) {
                    root.setStyle("-fx-padding: 10 14; -fx-background-radius: 10; -fx-background-color: #e0f2fe; -fx-border-color: #0284c7; -fx-border-width: 0 0 0 4;");
                    title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");
                } else {
                    root.setStyle("-fx-padding: 10 14; -fx-background-radius: 10; -fx-background-color: transparent;");
                    title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f2942;");
                }
            }
        });
    }

    private String getRisqueColor(String risque) {
        if (risque == null) return "#94a3b8";
        switch (risque.toLowerCase()) {
            case "faible":   return "#2e7d32";
            case "modéré":   return "#e65100";
            case "élevé":    return "#c62828";
            case "critique": return "#b71c1c";
            default:         return "#94a3b8";
        }
    }

    private void showDetails(Dossier d) {
        if (d == null) return;
        selected = d;

        patientLabel.setText(d.getPatientNom() != null ? d.getPatientNom() : "Patient #" + d.getPatientId());
        dateLabel.setText(d.getDateCreation() != null ? d.getDateCreation().toString() : "—");
        notesArea.setText(d.getNotesGenerales());
        niveauRisqueBox.setValue(d.getNiveauRisque());


        if (aiSummaryArea != null) {
            String summary = d.getAiSummary();
            aiSummaryArea.setText(summary != null && !summary.isEmpty()
                    ? summary
                    : "Cliquez sur 'Générer' pour obtenir un résumé IA du dossier");
        }
    }


    private String extractContent(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);


            if (root.has("error")) {
                JsonNode error = root.path("error");


                String msg = error.has("message")
                        ? error.path("message").asText()
                        : error.asText();

                return "API Error: " + msg;
            }


            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.isEmpty()) {
                return "Erreur IA: réponse vide\n\n" + json;
            }

            JsonNode message = choices.get(0).path("message");
            JsonNode content = message.path("content");

            if (content.isMissingNode() || content.asText().isEmpty()) {
                return "Erreur IA: contenu introuvable\n\n" + json;
            }

            return content.asText();

        } catch (Exception e) {
            return "Erreur parsing IA: " + e.getMessage();
        }
    }

    private String getGroqApiKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("groq.properties")) {

            if (input == null) {
                System.out.println("Fichier groq.properties introuvable !");
                return null;
            }

            Properties props = new Properties();
            props.load(input);
            return props.getProperty("groq.api.key");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @FXML
    public void handleExportPatientPDF() {
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un dossier d'abord !");
            return;
        }

        try {
            List<RendezVous> history = serviceRdv.getByEtudiantId(selected.getPatientId());
            String fileName = "Dossier_" + selected.getPatientNom().replace(" ", "_") + ".pdf";
            String path = System.getProperty("user.home") + "\\Desktop\\" + fileName;

            pdfService.generatePatientDossierPDF(selected, history, path);
            showAlert("Succès", "Le dossier de " + selected.getPatientNom() + " a été exporté sur votre bureau.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    @FXML
    public void handleDownloadPDF() {
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un dossier d'abord !");
            return;
        }

        try {

            List<RendezVous> history = serviceRdv.getByEtudiantId(selected.getPatientId());

            String patientName = selected.getPatientNom() != null ? selected.getPatientNom() : "Patient_" + selected.getPatientId();
            String fileName = "Dossier_" + patientName.replace(" ", "_") + ".pdf";


            String desktopPath = System.getProperty("user.home") + "\\Desktop\\" + fileName;

            pdfService.generatePatientDossierPDF(selected, history, desktopPath);
            showAlert("Succès", "Le dossier PDF a été généré sur votre bureau :\n" + desktopPath);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    @FXML
    public void genererIA(ActionEvent event) {

        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un dossier d'abord !");
            return;
        }

        String notes = notesArea.getText();
        String risque = niveauRisqueBox.getValue();

        if (notes == null || notes.trim().length() < 20) {
            showAlert("Attention", "Notes trop courtes pour générer un résumé IA.");
            return;
        }

        try {
            String apiKey = getGroqApiKey();

            if (apiKey == null || apiKey.isEmpty()) {
                showAlert("Erreur", "Clé API Groq introuvable !");
                return;
            }

            String prompt = """
                   
                             Tu es un assistant médical.
                             Résume uniquement le dossier médical suivant de manière claire, neutre et professionnelle et brieve.
                             Ne donne aucune recommandation, aucun conseil, aucune interprétation.
                    
                             Niveau de risque: %s
                             Notes: %s
                            
        """.formatted(risque, notes);

            String body = """
        {
          "model": "llama-3.3-70b-versatile",
          "messages": [
            {
              "role": "user",
              "content": "%s"
            }
          ],
          "temperature": 0.3
        }
        """.formatted(
                    prompt.replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
            );

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String result = response.body();

            System.out.println("GROQ RESPONSE = " + result); // 🔥 DEBUG IMPORTANT

            String summary = extractContent(result);

            selected.setAiSummary(summary);
            service.update(selected);

            aiSummaryArea.setText(summary);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur IA", "Impossible de générer le résumé.");
        }
    }

    @FXML
    public void handleUpdate() {
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un dossier !");
            return;
        }

        String notes = notesArea.getText();
        String risque = niveauRisqueBox.getValue();

        if (notes == null || notes.trim().isEmpty() || risque == null) {
            showAlert("Erreur de saisie", "Tous les champs (Notes et Niveau de risque) doivent être remplis.");
            return;
        }

        selected.setNotesGenerales(notes);
        selected.setNiveauRisque(risque);
        service.update(selected);
        showAlert("Succès", "Dossier modifié avec succès !");


        selected = null;
        dossierList.getSelectionModel().clearSelection();
        resetDetails();

        loadData();
    }

    @FXML
    public void handleDelete() {
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un dossier !");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le dossier");
        confirm.setContentText("Voulez-vous vraiment supprimer ce dossier ? Cette action est irréversible.");
        if (confirm.showAndWait().get() != ButtonType.OK) return;
        service.delete(selected);
        showAlert("Succès", "Dossier supprimé !");
        selected = null;
        resetDetails();
        loadData();
    }

    private void resetDetails() {
        patientLabel.setText("—");
        dateLabel.setText("—");
        notesArea.clear();
        niveauRisqueBox.setValue(null);
        if (aiSummaryArea != null)
            aiSummaryArea.setText("Cliquez sur 'Générer' pour obtenir un résumé IA du dossier");
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        ObservableList<Dossier> list =
                FXCollections.observableArrayList(service.search(keyword));
        applyListView(list);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void loadPage(javafx.event.Event event, String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("FXML introuvable: " + path);
                return;
            }
            Parent root = FXMLLoader.load(url);
            Stage stage = null;
            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.setMinWidth(900);
                stage.setMinHeight(600);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void goHome(javafx.event.Event event)             { loadPage(event, "/DashboardPsyVue.fxml"); }
    @FXML public void goConsulterDossiers(javafx.event.Event event){ loadPage(event, "/ConsulterDossiers.fxml"); }
    @FXML public void goVoirRendezVous(javafx.event.Event event)   { loadPage(event, "/VoirRendezVous.fxml"); }
    @FXML public void goNouveauDossier(javafx.event.Event event)   { loadPage(event, "/NouveauDossier.fxml"); }
    @FXML public void goCalendrier(javafx.event.Event event)       { loadPage(event, "/Calendrier.fxml"); }
    @FXML public void goActivites(javafx.event.Event event)  {}
    @FXML public void goRessources(javafx.event.Event event) {}
    @FXML public void goStats(javafx.event.Event event)      {}

    @FXML public void toggleRdvMenu(MouseEvent event) {
        if (submenuRdv != null) {
            boolean s = !submenuRdv.isVisible();
            submenuRdv.setVisible(s);
            submenuRdv.setManaged(s);
            if (arrowRdv != null) arrowRdv.setText(s ? "˅" : "›");
        }
    }

    @FXML public void toggleDossiersMenu(MouseEvent event) {
        if (submenuDossiers != null) {
            boolean s = !submenuDossiers.isVisible();
            submenuDossiers.setVisible(s);
            submenuDossiers.setManaged(s);
            if (arrowDossiers != null) arrowDossiers.setText(s ? "˅" : "›");
        }
    }

    @FXML
    public void logout(javafx.event.Event event) {
        utils.SessionManager.getInstance().logout();
        loadPage(event, "/Login.fxml");
    }
}