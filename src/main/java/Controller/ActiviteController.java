package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Activite;
import services.ServiceActivite;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ActiviteController implements Initializable {

    @FXML
    private TextField nomField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField adresseField;
    @FXML
    private DatePicker dateDebutField;
    @FXML
    private DatePicker dateFinField;

    @FXML
    private TableView<Activite> activiteTable;
    @FXML
    private TableColumn<Activite, Integer> idColumn;
    @FXML
    private TableColumn<Activite, String> titreColumn;
    @FXML
    private TableColumn<Activite, String> descriptionColumn;
    @FXML
    private TableColumn<Activite, String> typeColumn;
    @FXML
    private TableColumn<Activite, String> adresseColumn;
    @FXML
    private TableColumn<Activite, LocalDate> dateDebutColumn;
    @FXML
    private TableColumn<Activite, LocalDate> dateFinColumn;

    @FXML private Button btnVuePublique;
    @FXML private Button btnAjouter;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private ServiceActivite serviceActivite;
    private ObservableList<Activite> activitesList;
    private FilteredList<Activite> filteredList;
    private Activite activiteSelectionnee;
    private String valeurInitialeTitre;
    private String valeurInitialeDescription;
    private String valeurInitialeType;
    private String valeurInitialeAdresse;
    private LocalDate valeurInitialeDateDebut;
    private LocalDate valeurInitialeDateFin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceActivite = new ServiceActivite();
        activitesList = FXCollections.observableArrayList();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("idActivite"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

        chargerActivites();
        configurerRechercheEtTri();

        activiteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                activiteSelectionnee = newSelection;
                remplirChamps(newSelection);
                sauvegarderValeursInitiales();
                // Désactiver Ajouter quand une activité est sélectionnée
                if (btnAjouter != null) {
                    btnAjouter.setDisable(true);
                    btnAjouter.setStyle("-fx-background-color: #a0aec0; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: default; -fx-background-radius: 8;");
                }
            }
        });

        ajouterValidationTempsReel();
    }

    private void ajouterValidationTempsReel() {
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 3) {
                nomField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5; -fx-background-radius: 5;");
            } else {
                nomField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 5; -fx-background-radius: 5;");
            }
        });

        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 40) {
                descriptionField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5; -fx-background-radius: 5;");
            } else if (newValue.length() > 1000) {
                descriptionField.setStyle("-fx-border-color: orange; -fx-border-width: 2px; -fx-border-radius: 5; -fx-background-radius: 5;");
            } else {
                descriptionField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 5; -fx-background-radius: 5;");
            }
        });

        dateDebutField.valueProperty().addListener((observable, oldValue, newValue) -> {
            validerDates();
        });

        dateFinField.valueProperty().addListener((observable, oldValue, newValue) -> {
            validerDates();
        });
    }

    private void validerDates() {
        LocalDate dateDebut = dateDebutField.getValue();
        LocalDate dateFin = dateFinField.getValue();

        if (dateDebut != null && dateFin != null) {
            if (dateDebut.isAfter(dateFin)) {
                dateDebutField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                dateFinField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                dateDebutField.setStyle("");
                dateFinField.setStyle("");
            }
        }
    }

    private void chargerActivites() {
        try {
            List<Activite> activites = serviceActivite.getAllActivites();
            activitesList.clear();
            activitesList.addAll(activites);
            // Ne pas réassigner ici, filteredList est déjà liée
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Erreur lors du chargement des activités: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configurerRechercheEtTri() {
        // Options de tri
        sortCombo.setItems(FXCollections.observableArrayList(
                "Titre A→Z", "Titre Z→A",
                "Date début ↑", "Date début ↓",
                "Type A→Z"
        ));

        // Recherche en temps réel
        filteredList = new FilteredList<>(activitesList, a -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(activite -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return activite.getTitre().toLowerCase().contains(lower)
                    || activite.getType().toLowerCase().contains(lower)
                    || activite.getAdresse().toLowerCase().contains(lower)
                    || activite.getDescription().toLowerCase().contains(lower);
            });
        });

        // Tri
        SortedList<Activite> sortedList = new SortedList<>(filteredList);
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Titre A→Z"    -> sortedList.setComparator(Comparator.comparing(Activite::getTitre));
                case "Titre Z→A"    -> sortedList.setComparator(Comparator.comparing(Activite::getTitre).reversed());
                case "Date début ↑" -> sortedList.setComparator(Comparator.comparing(Activite::getDateDebut));
                case "Date début ↓" -> sortedList.setComparator(Comparator.comparing(Activite::getDateDebut).reversed());
                case "Type A→Z"     -> sortedList.setComparator(Comparator.comparing(Activite::getType));
            }
        });

        activiteTable.setItems(sortedList);
    }

    private void remplirChamps(Activite activite) {
        nomField.setText(activite.getTitre());
        descriptionField.setText(activite.getDescription());
        typeField.setText(activite.getType());
        adresseField.setText(activite.getAdresse());
        dateDebutField.setValue(activite.getDateDebut());
        dateFinField.setValue(activite.getDateFin());
    }

    private void sauvegarderValeursInitiales() {
        valeurInitialeTitre = nomField.getText();
        valeurInitialeDescription = descriptionField.getText();
        valeurInitialeType = typeField.getText();
        valeurInitialeAdresse = adresseField.getText();
        valeurInitialeDateDebut = dateDebutField.getValue();
        valeurInitialeDateFin = dateFinField.getValue();
    }

    private boolean valeursOntChange() {
        return !nomField.getText().equals(valeurInitialeTitre) ||
                !descriptionField.getText().equals(valeurInitialeDescription) ||
                !typeField.getText().equals(valeurInitialeType) ||
                !adresseField.getText().equals(valeurInitialeAdresse) ||
                !dateDebutField.getValue().equals(valeurInitialeDateDebut) ||
                !dateFinField.getValue().equals(valeurInitialeDateFin);
    }

    @FXML
    private void ajouterActivite() {
        if (!validerChamps()) {
            return;
        }

        try {
            Activite activite = new Activite(
                    nomField.getText().trim(),
                    descriptionField.getText().trim(),
                    typeField.getText().trim(),
                    adresseField.getText().trim(),
                    dateDebutField.getValue(),
                    dateFinField.getValue()
            );

            serviceActivite.ajouterActivite(activite);
            afficherAlerte("Succès", "Activité ajoutée avec succès!", Alert.AlertType.INFORMATION);
            chargerActivites();
            reinitialiserChamps();
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void modifierActivite() {
        if (activiteSelectionnee == null) {
            afficherAlerte("Attention", "Veuillez sélectionner une activité à modifier.", Alert.AlertType.WARNING);
            return;
        }

        if (!valeursOntChange()) {
            afficherAlerte("Attention", "Aucune modification détectée. Veuillez modifier au moins un champ.", Alert.AlertType.WARNING);
            return;
        }

        if (!validerChamps()) {
            return;
        }

        try {
            activiteSelectionnee.setTitre(nomField.getText().trim());
            activiteSelectionnee.setDescription(descriptionField.getText().trim());
            activiteSelectionnee.setType(typeField.getText().trim());
            activiteSelectionnee.setAdresse(adresseField.getText().trim());
            activiteSelectionnee.setDateDebut(dateDebutField.getValue());
            activiteSelectionnee.setDateFin(dateFinField.getValue());

            serviceActivite.modifierActivite(activiteSelectionnee);
            afficherAlerte("Succès", "Activité modifiée avec succès!", Alert.AlertType.INFORMATION);
            chargerActivites();
            reinitialiserChamps();
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void supprimerActivite() {
        if (activiteSelectionnee == null) {
            afficherAlerte("Attention", "Veuillez sélectionner une activité à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'activité");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette activité?\nToutes les réservations associées seront également supprimées.");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                serviceActivite.supprimerActivite(activiteSelectionnee.getIdActivite());
                afficherAlerte("Succès", "Activité supprimée avec succès!", Alert.AlertType.INFORMATION);
                chargerActivites();
                reinitialiserChamps();
            } catch (SQLException e) {
                afficherAlerte("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void reinitialiserChamps() {
        nomField.clear();
        descriptionField.clear();
        typeField.clear();
        adresseField.clear();
        dateDebutField.setValue(null);
        dateFinField.setValue(null);
        activiteSelectionnee = null;
        activiteTable.getSelectionModel().clearSelection();

        nomField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 5; -fx-background-radius: 5;");
        descriptionField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 5; -fx-background-radius: 5;");
        dateDebutField.setStyle("");
        dateFinField.setStyle("");

        // Réactiver le bouton Ajouter
        if (btnAjouter != null) {
            btnAjouter.setDisable(false);
            btnAjouter.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 8;");
        }
    }

    private boolean validerChamps() {
        if (nomField.getText().trim().isEmpty() || nomField.getText().trim().length() < 3) {
            afficherAlerte("Erreur de validation", "Le titre doit contenir au moins 3 caractères.", Alert.AlertType.ERROR);
            return false;
        }

        if (descriptionField.getText().trim().isEmpty() || descriptionField.getText().trim().length() < 40) {
            afficherAlerte("Erreur de validation", "La description doit contenir au moins 40 caractères.", Alert.AlertType.ERROR);
            return false;
        }

        if (descriptionField.getText().trim().length() > 1000) {
            afficherAlerte("Erreur de validation", "La description ne doit pas dépasser 1000 caractères.", Alert.AlertType.ERROR);
            return false;
        }

        if (typeField.getText().trim().isEmpty()) {
            afficherAlerte("Erreur de validation", "Le type est obligatoire.", Alert.AlertType.ERROR);
            return false;
        }

        if (adresseField.getText().trim().isEmpty()) {
            afficherAlerte("Erreur de validation", "L'adresse est obligatoire.", Alert.AlertType.ERROR);
            return false;
        }

        if (dateDebutField.getValue() == null) {
            afficherAlerte("Erreur de validation", "La date de début est obligatoire.", Alert.AlertType.ERROR);
            return false;
        }

        if (dateFinField.getValue() == null) {
            afficherAlerte("Erreur de validation", "La date de fin est obligatoire.", Alert.AlertType.ERROR);
            return false;
        }

        if (dateDebutField.getValue().isAfter(dateFinField.getValue())) {
            afficherAlerte("Erreur de validation", "La date de début doit être inférieure à la date de fin.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void ouvrirVuePublique() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EtudiantActivites.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("MentalUp - Espace Étudiant");
            stage.setScene(new Scene(root, 1280, 850));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la vue publique: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void ouvrirReservations() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GestionReservations.fxml"));
            Stage stage = (Stage) activiteTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Gestion des Réservations - MentalUp");
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir les réservations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
