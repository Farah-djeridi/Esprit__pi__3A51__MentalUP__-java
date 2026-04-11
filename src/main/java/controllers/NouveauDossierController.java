package controllers;

import Models.Dossier;
import Services.ServiceDossier;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import javafx.scene.control.*;
import javafx.event.ActionEvent;
import utils.MyDataBase;


import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.MouseEvent;



public class NouveauDossierController {

    public DatePicker currentDate;
    @FXML private ComboBox<String> patientComboBox;
    @FXML private ComboBox<String> niveauRisqueBox;
    @FXML private DatePicker datePicker;
    @FXML private TextArea notesField;
    @FXML private Label pageTitle;


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

        loadPatients();
        datePicker.setValue(LocalDate.now());

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

        if (selectedPatient == null) {
            System.out.println("⚠️ Veuillez sélectionner un patient !");
            return;
        }

        int patientId = patientMap.get(selectedPatient);

        int psychologueId = 2; // simulé (connecté)

        Dossier d = new Dossier();
        d.setPatientId(patientId);
        d.setPsychologueId(psychologueId);
        d.setNotesGenerales(notesField.getText());
        d.setNiveauRisque(niveauRisqueBox.getValue());

        // DATE depuis DatePicker
        d.setDateCreation(Date.valueOf(datePicker.getValue()));

        new ServiceDossier().add(d);

        System.out.println("✅ Dossier ajouté !");

        showAlert("Succès", "Dossier ajouté avec succès !");

        clearFields();
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

        niveauRisqueBox.setValue(null);
        notesField.clear();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    @FXML
    void goHome(javafx.scene.input.MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/DashboardPsyVue.fxml"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}