package controllers;

import Models.Dossier;
import Services.ServiceDossier;
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


import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.MouseEvent;



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
    private int psychologueId = 2; // connecté
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

        if (selectedPatient == null || notes == null || notes.trim().isEmpty() || risque == null || date == null) {
            showAlert("Erreur de saisie", "Veuillez remplir tous les champs obligatoires (Patient, Date, Risque, Notes).");
            return;
        }

        int patientId = patientMap.get(selectedPatient);
        int psychologueId = 2; // simulé (connecté)

        Dossier d = new Dossier();
        d.setPatientId(patientId);
        d.setPsychologueId(psychologueId);
        d.setNotesGenerales(notes);
        d.setNiveauRisque(risque);
        d.setAiSummary(generatedSummary);
        d.setDateCreation(Date.valueOf(date));

        new ServiceDossier().add(d);
        showAlert("Succès", "Dossier ajouté avec succès !");
        clearFields();
    }

    @FXML
    void handleGenerateIA(ActionEvent event) {
        String notes = notesField.getText();
        if (notes == null || notes.trim().length() < 50) {
            showAlert("Action impossible", "Les notes doivent contenir au moins 50 caractères pour être résumées par l'IA.");
            return;
        }

        // Simuler un résumé strict
        generatedSummary = "RÉSUMÉ : " + (notes.length() > 100 ? notes.substring(0, 100) + "..." : notes);
        aiSummaryArea.setText(generatedSummary);
    }

    @FXML
    void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/DashboardPsyVue.fxml"));
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


    private void loadPage(MouseEvent event, String path) {
        try {
            java.net.URL url = getClass().getResource(path);

            if (url == null) {
                System.out.println("FXML introuvable: " + path);
                return;
            }

            Parent root = FXMLLoader.load(url);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goHome(MouseEvent event) {
        loadPage(event, "/gui/DashboardPsyVue.fxml");
    }

    @FXML
    public void goVoirRendezVous(MouseEvent event) {
        loadPage(event, "/gui/VoirRendezVous.fxml");
    }


    @FXML
    public void goCalendrier(MouseEvent event) {
        loadPage(event, "/gui/Calendrier.fxml");
    }

    @FXML
    public void goConsulterDossiers(MouseEvent event) {
        loadPage(event, "/gui/ConsulterDossiers.fxml");
    }

    @FXML
    public void goNouveauDossier(MouseEvent event) {
        loadPage(event, "/gui/NouveauDossier.fxml");
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

    @FXML public void goActivites(MouseEvent event) { System.out.println("Activités"); }
    @FXML public void goRessources(MouseEvent event) { System.out.println("Ressources"); }
    @FXML public void goStats(MouseEvent event) { System.out.println("Statistiques"); }

    @FXML
    public void logout(ActionEvent event) {
        System.out.println("Déconnexion");
    }




}