package Controllor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Categorie;
import models.Ressource;
import services.ServiceCategorie;
import services.ServiceRessource;
import utils.SessionManager;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class ControllerAdminRessources {

    @FXML private ImageView logoImage;

    // --- Tab Ressources ---
    @FXML private TextField resTitre, resLien, resImage, searchResField;
    @FXML private TextArea resDescription;
    @FXML private ComboBox<String> resType;
    @FXML private ComboBox<Categorie> resCategorie;
    @FXML private TableView<Ressource> tableRessources;
    @FXML private TableColumn<Ressource, Integer> colResId, colResVues;
    @FXML private TableColumn<Ressource, String> colResTitre, colResType, colResCat;

    // --- Tab Categories ---
    @FXML private TextField catNom;
    @FXML private TextArea catDescription;
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, Integer> colCatId;
    @FXML private TableColumn<Categorie, String> colCatNom, colCatDesc;
    @FXML private TableColumn<Categorie, Timestamp> colCatDate;

    private ServiceCategorie serviceCategorie = new ServiceCategorie();
    private ServiceRessource serviceRessource = new ServiceRessource();

    private ObservableList<Categorie> categoriesList = FXCollections.observableArrayList();
    private ObservableList<Ressource> ressourcesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        // Init Tables
        colCatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCatNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCatDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCatDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        colResId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colResTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colResType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colResCat.setCellValueFactory(new PropertyValueFactory<>("categorieNom"));
        colResVues.setCellValueFactory(new PropertyValueFactory<>("nbVues"));

        resType.setItems(FXCollections.observableArrayList("Vidéo", "Article", "Podcast", "Autre"));

        loadCategories();
        loadRessources();

        // Table listeners
        tableCategories.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) fillCatForm(newSel);
        });

        tableRessources.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) fillResForm(newSel);
        });

        // Search listener
        searchResField.textProperty().addListener((obs, oldText, newText) -> filterRessources(newText));
    }

    // --- Categories CRUD ---

    private void loadCategories() {
        categoriesList.setAll(serviceCategorie.getAll());
        tableCategories.setItems(categoriesList);
        resCategorie.setItems(categoriesList);
    }

    private void fillCatForm(Categorie c) {
        catNom.setText(c.getNom());
        catDescription.setText(c.getDescription());
    }

    @FXML
    void clearCatForm() {
        catNom.clear();
        catDescription.clear();
        tableCategories.getSelectionModel().clearSelection();
    }

    @FXML
    void addCategorie(ActionEvent event) {
        if (catNom.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire !");
            return;
        }
        Categorie c = new Categorie(catNom.getText(), catDescription.getText());
        serviceCategorie.add(c);
        loadCategories();
        clearCatForm();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée.");
    }

    @FXML
    void updateCategorie(ActionEvent event) {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une catégorie à modifier.");
            return;
        }
        if (catNom.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire !");
            return;
        }
        selected.setNom(catNom.getText());
        selected.setDescription(catDescription.getText());
        serviceCategorie.update(selected);
        loadCategories();
        clearCatForm();
        loadRessources(); // Update resource table too
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie modifiée.");
    }

    @FXML
    void deleteCategorie(ActionEvent event) {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une catégorie à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette catégorie ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            serviceCategorie.delete(selected);
            loadCategories();
            clearCatForm();
            loadRessources();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée.");
        }
    }

    // --- Ressources CRUD ---

    private void loadRessources() {
        ressourcesList.setAll(serviceRessource.getAll());
        tableRessources.setItems(ressourcesList);
    }

    private void filterRessources(String text) {
        if (text == null || text.isEmpty()) {
            tableRessources.setItems(ressourcesList);
            return;
        }
        List<Ressource> filtered = ressourcesList.stream()
                .filter(r -> r.getTitre().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
        tableRessources.setItems(FXCollections.observableArrayList(filtered));
    }

    private void fillResForm(Ressource r) {
        resTitre.setText(r.getTitre());
        resDescription.setText(r.getDescription());
        resType.setValue(r.getType());
        resLien.setText(r.getLien());
        resImage.setText(r.getImage());
        
        // Find matching category in combobox
        for (Categorie c : resCategorie.getItems()) {
            if (c.getId() == r.getCategorieId()) {
                resCategorie.setValue(c);
                break;
            }
        }
    }

    @FXML
    void clearResForm() {
        resTitre.clear();
        resDescription.clear();
        resType.setValue(null);
        resLien.clear();
        resImage.clear();
        resCategorie.setValue(null);
        tableRessources.getSelectionModel().clearSelection();
    }

    @FXML
    void addRessource(ActionEvent event) {
        if (resTitre.getText().isEmpty() || resLien.getText().isEmpty() || resType.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les champs Titre, Type et Lien sont obligatoires.");
            return;
        }
        
        int catId = 0;
        if (resCategorie.getValue() != null) {
            catId = resCategorie.getValue().getId();
        }

        Ressource r = new Ressource(
            resTitre.getText(),
            resDescription.getText(),
            resType.getValue(),
            resLien.getText(),
            resImage.getText(),
            catId
        );
        
        serviceRessource.add(r);
        loadRessources();
        clearResForm();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource ajoutée.");
    }

    @FXML
    void updateRessource(ActionEvent event) {
        Ressource selected = tableRessources.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une ressource à modifier.");
            return;
        }
        if (resTitre.getText().isEmpty() || resLien.getText().isEmpty() || resType.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les champs Titre, Type et Lien sont obligatoires.");
            return;
        }

        selected.setTitre(resTitre.getText());
        selected.setDescription(resDescription.getText());
        selected.setType(resType.getValue());
        selected.setLien(resLien.getText());
        selected.setImage(resImage.getText());
        
        if (resCategorie.getValue() != null) {
            selected.setCategorieId(resCategorie.getValue().getId());
        } else {
            selected.setCategorieId(0);
        }

        serviceRessource.update(selected);
        loadRessources();
        clearResForm();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource modifiée.");
    }

    @FXML
    void deleteRessource(ActionEvent event) {
        Ressource selected = tableRessources.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une ressource à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            serviceRessource.delete(selected);
            loadRessources();
            clearResForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource supprimée.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Navigation Admin ---
    private void loadPage(String fxml, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void goToDashboard(MouseEvent event) { loadPage("/HomeAdmin.fxml", event); }
    @FXML void goToUsers(MouseEvent event) { loadPage("/AdminUsers.fxml", event); }
    @FXML void goToStats(MouseEvent event) { loadPage("/StatsAdmin.fxml", event); }

    @FXML
    void handleLogout(MouseEvent event) {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        src.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
    }

    @FXML
    private void onHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        src.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
    }
}
