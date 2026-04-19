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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

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

    @FXML private HBox navHome;
    @FXML private VBox submenuRdv;
    @FXML private VBox submenuDossiers;
    @FXML private Label arrowRdv;
    @FXML private Label arrowDossiers;

    private ServiceDossier service = new ServiceDossier();
    private Dossier selected;

    @FXML
    public void initialize() {
        niveauRisqueBox.getItems().addAll("Faible", "Modéré", "Élevé", "Critique");

        // Sous-menus visibles par défaut sur cette page
        if (submenuRdv != null) {
            submenuRdv.setVisible(false);
            submenuRdv.setManaged(false);
        }
        if (submenuDossiers != null) {
            submenuDossiers.setVisible(true);
            submenuDossiers.setManaged(true);
        }

        loadData();

        dossierList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> showDetails(newVal));

        // Style de la liste
        dossierList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }

    @FXML
    public void loadData() {
        ObservableList<Dossier> list =
                FXCollections.observableArrayList(service.getAll());
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

                    // Selection logic
                    updateStyle(root, title, isSelected());

                    root.getChildren().addAll(infos, btnDel);
                    setGraphic(root);
                    setText(null);

                    // Hover effect (only if not selected)
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

        // Afficher les données IA si présentes
        if (aiSummaryArea != null) {
            String summary = d.getAiSummary();
            aiSummaryArea.setText(summary != null && !summary.isEmpty()
                    ? summary
                    : "Cliquez sur 'Générer' pour obtenir un résumé IA du dossier");
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

        if (notes == null || notes.trim().length() < 50) {
            showAlert("Attention", "Le dossier doit contenir au moins 50 caractères de notes pour être résumé par l'IA.");
            return;
        }

        // Simulation d'un résumé IA strict et concis
        String summary = "RÉSUMÉ IA (" + (risque != null ? risque.toUpperCase() : "N/A") + ") : " 
                         + (notes.length() > 150 ? notes.substring(0, 150) + "..." : notes);

        selected.setNotesGenerales(notes);
        selected.setAiSummary(summary);
        service.update(selected);

        if (aiSummaryArea != null) {
            aiSummaryArea.setText(summary);
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
        
        // Vider les champs après modification
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

    private void loadPage(MouseEvent event, String path) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(path.substring(1));
            if (url == null) {
                System.err.println("FXML introuvable: " + path);
                return;
            }
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void goHome(MouseEvent event)             { loadPage(event, "/gui/DashboardPsyVue.fxml"); }
    @FXML public void goConsulterDossiers(MouseEvent event){ loadPage(event, "/gui/ConsulterDossiers.fxml"); }
    @FXML public void goVoirRendezVous(MouseEvent event)   { loadPage(event, "/gui/VoirRendezVous.fxml"); }
    @FXML public void goNouveauDossier(MouseEvent event)   { loadPage(event, "/gui/NouveauDossier.fxml"); }
    @FXML public void goCalendrier(MouseEvent event)       { loadPage(event, "/gui/Calendrier.fxml"); }
    @FXML public void goActivites(MouseEvent event)  {}
    @FXML public void goRessources(MouseEvent event) {}
    @FXML public void goStats(MouseEvent event)      {}

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

    @FXML public void logout(ActionEvent event) { System.out.println("Déconnexion..."); }
    @FXML public void logout(MouseEvent event)  { System.out.println("Déconnexion..."); }
}
