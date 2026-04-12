package controllers;

import Models.Dossier;
import Services.ServiceDossier;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ConsulterDossiersController {

    @FXML private ListView<Dossier> dossierList;

    @FXML private TextField searchField;

    @FXML private Label patientLabel;
    @FXML private Label dateLabel;

    @FXML private TextArea notesArea;
    @FXML private ComboBox<String> niveauRisqueBox;
    @FXML
    private HBox navHome;

    private ServiceDossier service = new ServiceDossier();

    private Dossier selected;

    @FXML
    public void initialize() {

        niveauRisqueBox.getItems().addAll("Faible", "Moyen", "Élevé");

        loadData();

        dossierList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> showDetails(newVal));
    }


    @FXML
    public void loadData() {

        ObservableList<Dossier> list =
                FXCollections.observableArrayList(service.getAll());

        dossierList.setItems(list);


        dossierList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Dossier d, boolean empty) {
                super.updateItem(d, empty);

                if (empty || d == null) {
                    setText(null);
                } else {
                    setText(
                            "Dossier #" + d.getId() +
                                    " | Risque: " + d.getNiveauRisque()
                    );
                }
            }
        });
    }


    private void showDetails(Dossier d) {

        if (d == null) return;

        selected = d;

        patientLabel.setText("Patient ID: " + d.getPatientId());
        dateLabel.setText("Date: " + d.getDateCreation());

        notesArea.setText(d.getNotesGenerales());
        niveauRisqueBox.setValue(d.getNiveauRisque());
    }


    @FXML
    public void handleUpdate() {

        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un dossier !");
            return;
        }

        selected.setNotesGenerales(notesArea.getText());
        selected.setNiveauRisque(niveauRisqueBox.getValue());

        service.update(selected);

        showAlert("Succès", "Dossier modifié !");
        loadData();
    }


    @FXML
    public void handleDelete() {

        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un dossier !");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer ce dossier ?");

        if (confirm.showAndWait().get() != ButtonType.OK) return;

        service.delete(selected);

        showAlert("Succès", "Dossier supprimé !");
        loadData();
    }


    @FXML
    public void handleSearch() {

        String keyword = searchField.getText();

        ObservableList<Dossier> list =
                FXCollections.observableArrayList(service.search(keyword));

        dossierList.setItems(list);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }


    private void loadPage(MouseEvent event, String path) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(path.substring(1));

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
    void goConsulterDossiers(MouseEvent event) {
        loadPage(event, "/gui/ConsulterDossiers.fxml");
    }

    @FXML
    public void goVoirRendezVous(MouseEvent event) {
        loadPage(event, "/gui/VoirRendezVous.fxml");
    }

    @FXML
    void goNouveauDossier(MouseEvent event) {
        loadPage(event, "/gui/NouveauDossier.fxml");
    }

    @FXML
    void goCalendrier(MouseEvent event) {
        loadPage(event, "/gui/Calendrier.fxml");
    }

    @FXML
    void goActivites(MouseEvent event) {}

    @FXML
    void goRessources(MouseEvent event) {}

    @FXML
    void goStats(MouseEvent event) {}

    @FXML
    void toggleRdvMenu(MouseEvent event) {}

    @FXML
    void toggleDossiersMenu(MouseEvent event) {}
    @FXML
    void logout(MouseEvent  event) {
        System.out.println("Déconnexion...");
    }

}