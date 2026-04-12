package controllers;

import Models.RendezVous;
import Services.ServiceRendezVous;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class RendezVousController {

    @FXML private VBox cardsContainer;
    @FXML private Label noResultsLabel;
    @FXML
    private HBox navHome;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private ComboBox<String> filtreType;
    @FXML private DatePicker filtreDate;

    @FXML private Label totalRdv, rdvAVenir, rdvCeMois;

    private ServiceRendezVous service = new ServiceRendezVous() {};

    private List<RendezVous> allRdv;


    @FXML
    public void initialize() {

        filtreStatut.getItems().addAll("libre", "réservé", "confirmé");
        filtreType.getItems().addAll("consultation", "suivi");
        filtreStatut.getItems().add("en attente");
        loadData();
    }

    private void loadData() {
        allRdv = service.getAll();
        afficherCards(allRdv);
        updateStats();
    }

    private void afficherCards(List<RendezVous> list) {

        cardsContainer.getChildren().clear();

        if (list.isEmpty()) {
            noResultsLabel.setVisible(true);
            noResultsLabel.setManaged(true);
            return;
        }

        noResultsLabel.setVisible(false);
        noResultsLabel.setManaged(false);

        for (RendezVous r : list) {

            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: white; -fx-border-color: #E8ECF0; -fx-border-radius: 10; -fx-background-radius: 10;");

            Label date = new Label("📅 " + r.getDate());
            Label heure = new Label("⏰ " + r.getHeureDebut() + " - " + r.getHeureFin());
            Label type = new Label("Type: " + r.getTypeRdv());

            Label statut = new Label(r.getStatut());
            statut.setStyle(getColorStyle(r.getStatut()));

            // 🔘 Buttons
            Button btnConfirm = new Button("Confirmer");
            Button btnDelete = new Button("Supprimer");

            btnConfirm.setStyle("-fx-background-color: blue; -fx-text-fill: white;");
            btnDelete.setStyle("-fx-background-color: red; -fx-text-fill: white;");

            btnConfirm.setOnAction(e -> {
                service.confirmerRdv(r.getId());
                loadData();
            });

            btnDelete.setOnAction(e -> {
                service.delete(r.getId());
                loadData();
            });
            HBox actions = new HBox(10, btnConfirm, btnDelete);

            card.getChildren().addAll(date, heure, type, statut, actions);

            cardsContainer.getChildren().add(card);
        }
    }

    // ================= COLORS =================
    private String getColorStyle(String statut) {
        switch (statut.toLowerCase()) {
            case "libre":
                return "-fx-background-color: green; -fx-text-fill: white; -fx-padding:5;";
            case "réservé":
                return "-fx-background-color: orange; -fx-text-fill: white; -fx-padding:5;";
            case "confirmé":
                return "-fx-background-color: blue; -fx-text-fill: white; -fx-padding:5;";
            default:
                return "";
        }
    }



    @FXML
    public void rechercherRendezVous() {
        appliquerFiltres();
    }

    @FXML
    public void filtrerParStatut() {
        appliquerFiltres();
    }

    @FXML
    public void filtrerParType() {
        appliquerFiltres();
    }

    @FXML
    public void filtrerParDate() {
        appliquerFiltres();
    }

    @FXML
    public void reinitialiserFiltres() {
        searchField.clear();
        filtreStatut.setValue(null);
        filtreType.setValue(null);
        filtreDate.setValue(null);
        afficherCards(allRdv);
    }

    private void appliquerFiltres() {

        List<RendezVous> filtered = allRdv.stream()
                .filter(r -> searchField.getText().isEmpty() ||
                        r.getTypeRdv().toLowerCase().contains(searchField.getText().toLowerCase()))
                .filter(r -> filtreStatut.getValue() == null ||
                        r.getStatut().equals(filtreStatut.getValue()))
                .filter(r -> filtreType.getValue() == null ||
                        r.getTypeRdv().equals(filtreType.getValue()))
                .filter(r -> filtreDate.getValue() == null ||
                        r.getDate().toLocalDate().equals(filtreDate.getValue()))
                .collect(Collectors.toList());

        afficherCards(filtered);
    }

    private void updateStats() {

        totalRdv.setText(String.valueOf(allRdv.size()));

        long avenir = allRdv.stream()
                .filter(r -> r.getDate().toLocalDate().isAfter(LocalDate.now()))
                .count();

        rdvAVenir.setText(String.valueOf(avenir));

        long mois = allRdv.stream()
                .filter(r -> r.getDate().toLocalDate().getMonth() == LocalDate.now().getMonth())
                .count();

        rdvCeMois.setText(String.valueOf(mois));
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
    void goNouveauDossier(MouseEvent event) {
        loadPage(event, "/gui/NouveauDossier.fxml");
    }

    @FXML
    void goCalendrier(MouseEvent event) {
        loadPage(event, "/gui/Calendrier.fxml");
    }

    @FXML
    void goConsulterDossiers(MouseEvent event) {
        loadPage(event, "/gui/ConsulterDossiers.fxml");
    }

    @FXML
    public void toggleRdvMenu() {}

    @FXML
    public void toggleDossiersMenu() {}



    @FXML
    public void goActivites() {}

    @FXML
    public void goRessources() {}

    @FXML
    public void goStats() {}





    @FXML
    public void logout() {}

    // ================= ADD RDV =================
    @FXML
    public void nouveauRendezVous() {
        System.out.println("Ajouter RDV (popup à faire)");
    }
}