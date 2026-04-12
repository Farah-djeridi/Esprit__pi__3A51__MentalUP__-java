package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Reservation;
import services.ServiceReservation;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationAdminController implements Initializable {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String>  colActivite;
    @FXML private TableColumn<Reservation, String>  colEtudiant;
    @FXML private TableColumn<Reservation, String>  colPlace;
    @FXML private TableColumn<Reservation, String>  colDate;
    @FXML private TableColumn<Reservation, Void>    colActions;
    @FXML private Label lblTotal;
    @FXML private Label lblTotalHeader;

    private ServiceReservation serviceReservation;
    private ObservableList<Reservation> reservationsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceReservation = new ServiceReservation();
        reservationsList = FXCollections.observableArrayList();

        configurerColonnes();
        chargerReservations();
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReservation"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    Label lbl = new Label("#" + item);
                    lbl.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; " +
                                 "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 12px;");
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Colonne Activité avec titre + dates
        colActivite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Reservation r = getTableView().getItems().get(getIndex());
                if (r == null) { setGraphic(null); return; }
                javafx.scene.layout.VBox vb = new javafx.scene.layout.VBox(2);
                Label titre = new Label(r.getTitreActivite() != null ? r.getTitreActivite() : "—");
                titre.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");
                setGraphic(vb);
                vb.getChildren().add(titre);
            }
        });

        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomEtudiant"));
        colEtudiant.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label("👤 " + item);
                lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748;");
                setGraphic(lbl);
            }
        });

        colPlace.setCellValueFactory(new PropertyValueFactory<>("place"));
        colPlace.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-background-color: #e8f4f8; -fx-text-fill: #2d3748; " +
                             "-fx-padding: 4 10; -fx-background-radius: 8; -fx-font-weight: bold;");
                setGraphic(lbl);
                setAlignment(Pos.CENTER);
            }
        });

        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Reservation r = getTableView().getItems().get(getIndex());
                if (r == null || r.getDateReservation() == null) { setGraphic(null); return; }
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                Label lbl = new Label("📅 " + r.getDateReservation().format(fmt));
                lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
                setGraphic(lbl);
            }
        });

        // Colonne Actions (modifier + supprimer)
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnSupp = new Button("Supprimer");

            {
                btnEdit.setStyle(
                    "-fx-background-color: #2d3748; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);");
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(
                    "-fx-background-color: #4a5568; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 6, 0, 0, 3);"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle(
                    "-fx-background-color: #2d3748; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);"));
                btnEdit.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    ouvrirModification(r);
                });

                btnSupp.setStyle(
                    "-fx-background-color: #e53e3e; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);");
                btnSupp.setOnMouseEntered(e -> btnSupp.setStyle(
                    "-fx-background-color: #c53030; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 6, 0, 0, 3);"));
                btnSupp.setOnMouseExited(e -> btnSupp.setStyle(
                    "-fx-background-color: #e53e3e; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 7 14; -fx-background-radius: 20; -fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);"));
                btnSupp.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer la réservation #" + r.getIdReservation() + " ?", ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) {
                            try {
                                serviceReservation.supprimerReservation(r.getIdReservation());
                                chargerReservations();
                            } catch (SQLException ex) {
                                new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    HBox box = new HBox(8, btnEdit, btnSupp);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void chargerReservations() {
        try {
            List<Reservation> list = serviceReservation.getAllReservations();
            reservationsList.setAll(list);
            reservationTable.setItems(reservationsList);
            lblTotal.setText(String.valueOf(list.size()));
            lblTotalHeader.setText(list.size() + " réservation(s)");

            // Hauteur dynamique: header (35px) + chaque ligne (50px) + min 100px
            double rowHeight = 50;
            double headerHeight = 35;
            double minHeight = 100;
            double newHeight = Math.max(minHeight, headerHeight + list.size() * rowHeight);
            reservationTable.setPrefHeight(newHeight);
            reservationTable.setMinHeight(newHeight);
            reservationTable.setMaxHeight(newHeight);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur chargement: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void ouvrirActivites() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GestionActivite.fxml"));
            Stage stage = (Stage) reservationTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Gestion des Activités - MentalUp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirModification(Reservation r) {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Modifier la réservation #" + r.getIdReservation());

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(20);
        root.setStyle("-fx-background-color: white; -fx-padding: 30;");

        Label titre = new Label("✏️ Modifier la réservation #" + r.getIdReservation());
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // Champ nom étudiant
        Label lblNom = new Label("Nom de l'étudiant:");
        lblNom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        TextField tfNom = new TextField(r.getNomEtudiant());
        tfNom.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #e2e8f0; " +
                       "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Champ place
        Label lblPlace = new Label("Place (ex: A1, H8):");
        lblPlace.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        TextField tfPlace = new TextField(r.getPlace());
        tfPlace.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #e2e8f0; " +
                         "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Champ date
        Label lblDate = new Label("Date de réservation:");
        lblDate.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        DatePicker dpDate = new DatePicker(r.getDateReservation());
        dpDate.setStyle("-fx-font-size: 13px;");
        dpDate.setMaxWidth(Double.MAX_VALUE);

        // Boutons
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnAnnuler.setOnAction(e -> popup.close());

        Button btnSauvegarder = new Button("✅ Sauvegarder");
        btnSauvegarder.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-font-size: 13px; " +
                                "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnSauvegarder.setOnAction(e -> {
            if (tfNom.getText().trim().isEmpty() || tfPlace.getText().trim().isEmpty() || dpDate.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Tous les champs sont obligatoires!", ButtonType.OK).showAndWait();
                return;
            }
            try {
                r.setNomEtudiant(tfNom.getText().trim());
                r.setPlace(tfPlace.getText().trim().toUpperCase());
                r.setDateReservation(dpDate.getValue());
                serviceReservation.modifierReservation(r);
                popup.close();
                chargerReservations();
                new Alert(Alert.AlertType.INFORMATION, "✅ Réservation modifiée avec succès!", ButtonType.OK).showAndWait();
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage(), ButtonType.OK).showAndWait();
            }
        });

        HBox footerBtns = new HBox(15, btnAnnuler, btnSauvegarder);
        footerBtns.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        root.getChildren().addAll(titre, lblNom, tfNom, lblPlace, tfPlace, lblDate, dpDate, footerBtns);

        popup.setScene(new Scene(root, 420, 380));
        popup.showAndWait();
    }
}
