package Controllor;

import Models.Dossier;
import Services.ServiceDossier;
import services.ServiceRendezVous;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.MyDataBase;


import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.MouseEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class NouveauDossierController {

    @FXML private Label currentDate;
    @FXML private ComboBox<String> patientComboBox;
    @FXML private ComboBox<String> niveauRisqueBox;
    @FXML private DatePicker datePicker;
    @FXML private TextArea notesField;
    @FXML private TextArea aiSummaryArea;
    private String generatedSummary = "";

    @FXML private VBox submenuRdv;
    @FXML private VBox submenuDossiers;
    @FXML private Label arrowRdv;
    @FXML private Label arrowDossiers;

    @FXML private Label sidebarPatientsCount;
    @FXML private Label sidebarRdvToday;
    @FXML private Label sidebarDossiersTotal;

    private boolean rdvOpen = false;
    private boolean dossiersOpen = true;

    @FXML
    private HBox navHome;

    private Map<String, Integer> patientMap = new HashMap<>();
    private int psychologueId = 2; // Default fallback
    private ServiceDossier service = new ServiceDossier();

    public void loadPatients() {
        String query = "SELECT id, nom, prenom FROM user WHERE role = 'etudiant'";

        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                String nomComplet = rs.getString("nom") + " " + rs.getString("prenom");

                patientMap.put(nomComplet, id);
                patientComboBox.getItems().add(nomComplet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void initialize() {
        if (utils.SessionManager.getInstance().isLoggedIn()) {
            psychologueId = utils.SessionManager.getInstance().getCurrentUser().getId();
        }

        if (submenuRdv != null) {
            submenuRdv.setVisible(false);
            submenuRdv.setManaged(false);
        }
        if (submenuDossiers != null) {
            submenuDossiers.setVisible(true);
            submenuDossiers.setManaged(true);
        }

        loadPatients();
        datePicker.setValue(LocalDate.now());

        if (currentDate != null) {
            currentDate.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        // Stats sidebar
        int dossiersCount = service.getByPsychologueId(psychologueId).size();
        sidebarPatientsCount.setText(String.valueOf(dossiersCount));
        sidebarDossiersTotal.setText(String.valueOf(dossiersCount));
        
        ServiceRendezVous sRdv = new ServiceRendezVous();
        long today = sRdv.getByPsychologueId(psychologueId).stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now()))
                .count();
        sidebarRdvToday.setText(String.valueOf(today));

        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            String query = "SELECT id, nom, prenom FROM user WHERE role='PATIENT'";
            PreparedStatement ps = cnx.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String fullName = rs.getString("nom") + " " + rs.getString("prenom");
                patientComboBox.getItems().add(fullName);
                patientMap.put(fullName, rs.getInt("id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        String selectedPatient = patientComboBox.getValue();
        String notes = notesField.getText();
        String risque = niveauRisqueBox.getValue();
        LocalDate date = datePicker.getValue();

        boolean hasError = false;
        if (selectedPatient == null) {
            patientComboBox.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            hasError = true;
        } else patientComboBox.setStyle("");

        if (notes == null || notes.trim().isEmpty()) {
            notesField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            hasError = true;
        } else notesField.setStyle("");

        if (risque == null) {
            niveauRisqueBox.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            hasError = true;
        } else niveauRisqueBox.setStyle("");

        if (date == null) {
            datePicker.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            hasError = true;
        } else datePicker.setStyle("");

        if (hasError) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires (Patient, Date, Risque, Notes).");
            return;
        }

        int patientId = patientMap.get(selectedPatient);

        Dossier d = new Dossier();
        d.setPatientId(patientId);
        d.setPsychologueId(this.psychologueId);
        d.setNotesGenerales(notes);
        d.setNiveauRisque(risque);
        d.setAiSummary(generatedSummary);
        d.setDateCreation(Date.valueOf(date));

        new ServiceDossier().add(d);
        showAlert("Succès", "Dossier ajouté avec succès !");
        clearFields();
    }



    private String extractContent(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);


            if (root.has("error")) {
                return "API Error: " + root.path("error").path("message").asText();
            }


            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.isEmpty()) {
                return "Erreur: réponse IA invalide (pas de choices)";
            }

            JsonNode message = choices.get(0).path("message");
            JsonNode content = message.path("content");

            if (content.isMissingNode() || content.asText().isEmpty()) {
                return "Erreur: contenu IA vide";
            }

            return content.asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur parsing JSON IA";
        }
    }



    @FXML
    void handleGenerateIA(ActionEvent event) {

        String notes = notesField.getText();

        if (notes == null || notes.trim().length() < 50) {
            showAlert("Action impossible",
                    "Les notes doivent contenir au moins 50 caractères pour générer un résumé IA.");
            return;
        }


        callGroqAI(notes);
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

    private void callGroqAI(String notes) {
        try {
            String apiKey = getGroqApiKey();

            String prompt = "Résume ce dossier médical de manière claire et concise :\n"
                    + notes;

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

            String summary = extractContent(result);

            generatedSummary = summary;
            aiSummaryArea.setText(summary);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur IA", "Impossible de générer le résumé IA.");
        }
    }


    @FXML
    void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardPsyVue.fxml"));
            Parent root = loader.load();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        patientComboBox.setValue(null);
        datePicker.setValue(java.time.LocalDate.now());
        niveauRisqueBox.setValue(null);
        notesField.clear();
        generatedSummary = "";
        if (aiSummaryArea != null) {
            aiSummaryArea.setText("Rédigez au moins 50 caractères dans les notes pour générer un résumé.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }


    private void loadPage(javafx.event.Event event, String path) {
        try {
            java.net.URL url = getClass().getResource(path);

            if (url == null) {
                System.out.println("FXML introuvable: " + path);
                return;
            }

            Parent root = FXMLLoader.load(url);

            Stage stage = null;
            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }
            if (stage != null) {
                stage.setScene(new Scene(root));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goHome(javafx.event.Event event) {
        loadPage(event, "/DashboardPsyVue.fxml");
    }

    @FXML
    public void goVoirRendezVous(javafx.event.Event event) {
        loadPage(event, "/VoirRendezVous.fxml");
    }


    @FXML
    public void goCalendrier(javafx.event.Event event) {
        loadPage(event, "/Calendrier.fxml");
    }

    @FXML
    public void goConsulterDossiers(javafx.event.Event event) {
        loadPage(event, "/ConsulterDossiers.fxml");
    }

    @FXML
    public void goNouveauDossier(javafx.event.Event event) {
        loadPage(event, "/NouveauDossier.fxml");
    }

    @FXML
    public void toggleRdvMenu(MouseEvent event) {
        rdvOpen = !rdvOpen;
        if (submenuRdv != null) {
            submenuRdv.setVisible(rdvOpen);
            submenuRdv.setManaged(rdvOpen);
        }
        if (arrowRdv != null) {
            arrowRdv.setText(rdvOpen ? "▼" : "▶");
        }
    }

    @FXML
    public void toggleDossiersMenu(MouseEvent event) {
        dossiersOpen = !dossiersOpen;
        if (submenuDossiers != null) {
            submenuDossiers.setVisible(dossiersOpen);
            submenuDossiers.setManaged(dossiersOpen);
        }
        if (arrowDossiers != null) {
            arrowDossiers.setText(dossiersOpen ? "▼" : "▶");
        }
    }
    @FXML public void goActivites(javafx.event.Event event)  { System.out.println("Activités"); }
    @FXML public void goRessources(javafx.event.Event event) { System.out.println("Ressources"); }
    @FXML public void goStats(javafx.event.Event event)      { System.out.println("Statistiques"); }

    @FXML
    public void logout(javafx.event.Event event) {
        utils.SessionManager.getInstance().logout();
        loadPage(event, "/Login.fxml");
    }
}