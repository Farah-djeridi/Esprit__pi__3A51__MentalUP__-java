package controllers;

import Models.Dossier;
import Services.ServiceDossier;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;

public class ConsulterDossiersController {

    @FXML private ListView<Dossier> dossierList;

    @FXML private TextField searchField;

    @FXML private Label patientLabel;
    @FXML private Label dateLabel;

    @FXML private TextArea notesArea;
    @FXML private ComboBox<String> niveauRisqueBox;

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

    // 🔄 LOAD LIST
    @FXML
    public void loadData() {

        ObservableList<Dossier> list =
                FXCollections.observableArrayList(service.getAll());

        dossierList.setItems(list);

        // affichage custom dans ListView
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

    // 👁 DETAILS
    private void showDetails(Dossier d) {

        if (d == null) return;

        selected = d;

        patientLabel.setText("Patient ID: " + d.getPatientId());
        dateLabel.setText("Date: " + d.getDateCreation());

        notesArea.setText(d.getNotesGenerales());
        niveauRisqueBox.setValue(d.getNiveauRisque());
    }

    // ✏️ UPDATE (notes + risque only)
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

    // 🗑 DELETE
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

    // 🔍 SEARCH
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
}