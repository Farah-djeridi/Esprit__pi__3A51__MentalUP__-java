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

    // IA labels
    @FXML private Label aiSummaryLabel;
    @FXML private Label aiKeyPointsLabel;
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
                    // Custom card cell
                    VBox cell = new VBox(4);
                    cell.setStyle("-fx-padding: 10 14; -fx-background-radius: 8;");

                    Label title = new Label("Dossier #" + d.getId());
                    title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f2942; -fx-font-family: 'Segoe UI';");

                    String risqueColor = getRisqueColor(d.getNiveauRisque());
                    Label risque = new Label("● " + (d.getNiveauRisque() != null ? d.getNiveauRisque() : "N/A"));
                    risque.setStyle("-fx-font-size: 11px; -fx-text-fill: " + risqueColor + "; -fx-font-family: 'Segoe UI';");

                    Label date = new Label(d.getDateCreation() != null ? d.getDateCreation().toString() : "");
                    date.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI';");

                    cell.getChildren().addAll(title, risque, date);
                    setGraphic(cell);
                    setText(null);

                    // Hover effect
                    setOnMouseEntered(e -> cell.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-background-color: #f0f7ff;"));
                    setOnMouseExited(e -> cell.setStyle("-fx-padding: 10 14; -fx-background-radius: 8;"));
                    setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
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

        patientLabel.setText("Patient ID : " + d.getPatientId());
        dateLabel.setText(d.getDateCreation() != null ? d.getDateCreation().toString() : "—");
        notesArea.setText(d.getNotesGenerales());
        niveauRisqueBox.setValue(d.getNiveauRisque());

        // Afficher les données IA si présentes
        if (aiSummaryLabel != null) {
            String summary = d.getAiSummary();
            aiSummaryLabel.setText(summary != null && !summary.isEmpty()
                    ? summary
                    : "Cliquez sur 'Générer' pour obtenir un résumé IA du dossier");
        }
        if (aiKeyPointsLabel != null) {
            String keyPoints = d.getAiKeyPoints();
            aiKeyPointsLabel.setText(keyPoints != null && !keyPoints.isEmpty()
                    ? keyPoints
                    : "Les points clés apparaîtront ici après génération");
        }
    }

    @FXML
    public void genererIA(ActionEvent event) {
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un dossier d'abord !");
            return;
        }

        String notes = selected.getNotesGenerales();
        String risque = selected.getNiveauRisque();

        if (notes == null || notes.trim().isEmpty()) {
            showAlert("Attention", "Ce dossier ne contient pas encore de notes à analyser.");
            return;
        }

        // Simulation d'un résumé IA basé sur les données du dossier
        String summary = genererResume(notes, risque);
        String keyPoints = genererPointsCles(notes, risque);

        selected.setAiSummary(summary);
        selected.setAiKeyPoints(keyPoints);
        service.update(selected);

        if (aiSummaryLabel != null) aiSummaryLabel.setText(summary);
        if (aiKeyPointsLabel != null) aiKeyPointsLabel.setText(keyPoints);
    }

    private String genererResume(String notes, String risque) {
        // Résumé automatique simple basé sur le contenu
        int len = notes.length();
        String niveauStr = risque != null ? risque.toLowerCase() : "indéterminé";
        return "Analyse du dossier : niveau de risque " + niveauStr + ". "
                + "Notes de " + len + " caractères rédigées par le praticien. "
                + "Suivi recommandé selon le protocole standard pour ce niveau de risque.";
    }

    private String genererPointsCles(String notes, String risque) {
        StringBuilder sb = new StringBuilder();
        sb.append("• Niveau de risque : ").append(risque != null ? risque : "Non défini").append("\n");
        sb.append("• Longueur des notes : ").append(notes.length()).append(" caractères\n");
        if (notes.toLowerCase().contains("anxiété") || notes.toLowerCase().contains("anxiete"))
            sb.append("• Mention : anxiété détectée\n");
        if (notes.toLowerCase().contains("dépression") || notes.toLowerCase().contains("depression"))
            sb.append("• Mention : dépression détectée\n");
        if (notes.toLowerCase().contains("suivi"))
            sb.append("• Suivi en cours mentionné\n");
        sb.append("• Action : révision du dossier recommandée");
        return sb.toString();
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
        showAlert("Succès", "Dossier modifié avec succès !");
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
        if (aiSummaryLabel != null)
            aiSummaryLabel.setText("Cliquez sur 'Générer' pour obtenir un résumé IA du dossier");
        if (aiKeyPointsLabel != null)
            aiKeyPointsLabel.setText("Les points clés apparaîtront ici après génération");
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
