package controllers;

import Models.RendezVous;
import Services.ServiceRendezVous;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.scene.Scene;
import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;
import Services.PDFService;
import Models.Dossier;
import Services.ServiceDossier;


public class RendezVousController {

    @FXML private VBox cardsContainer;
    @FXML private Label noResultsLabel;
    @FXML private HBox navHome;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtreStatut;
    @FXML private ComboBox<String> filtreType;
    @FXML private DatePicker filtreDate;

    @FXML private Label totalRdv;
    @FXML private Label rdvAVenir;
    @FXML private Label rdvCeMois;


    @FXML private VBox submenuRdv;
    @FXML private VBox submenuDossiers;
    @FXML private Label arrowRdv;
    @FXML private Label arrowDossiers;

    private ServiceRendezVous service = new ServiceRendezVous();
    private ServiceDossier serviceDossier = new ServiceDossier();
    private PDFService pdfService = new PDFService();
    private List<RendezVous> allRdv;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        filtreStatut.getItems().addAll("libre", "réservé", "confirmé", "en attente");
        filtreType.getItems().addAll("consultation", "suivi", "urgence", "bilan");

      
        if (submenuRdv != null) {
            submenuRdv.setVisible(true);
            submenuRdv.setManaged(true);
            if (arrowRdv != null) arrowRdv.setText("˅");
        }
        if (submenuDossiers != null) {
            submenuDossiers.setVisible(false);
            submenuDossiers.setManaged(false);
        }

        loadData();
    }

    public void loadData() {
        allRdv = service.getByPsychologueId(6);
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

        List<RendezVous> enAttente = list.stream()
                .filter(r -> "en attente".equalsIgnoreCase(r.getStatut()) || "réservé".equalsIgnoreCase(r.getStatut()))
                .collect(Collectors.toList());

        List<RendezVous> confirmes = list.stream()
                .filter(r -> "confirmé".equalsIgnoreCase(r.getStatut()) || "en cours".equalsIgnoreCase(r.getStatut()))
                .filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        List<RendezVous> faits = list.stream()
                .filter(r -> "terminé".equalsIgnoreCase(r.getStatut()) || 
                            (r.getDate() != null && r.getDate().toLocalDate().isBefore(LocalDate.now()) && !"libre".equalsIgnoreCase(r.getStatut())))
                .collect(Collectors.toList());

        addSection("📥 Demandes en attente de confirmation", enAttente);
        addSection("📅 Rendez-vous confirmés", confirmes);
        addSection("✅ Rendez-vous faits (Historique)", faits);
    }

    private void addSection(String title, List<RendezVous> sectionList) {
        if (sectionList.isEmpty()) return;

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e4976; -fx-padding: 10 0 5 0;");
        cardsContainer.getChildren().add(titleLabel);

        for (int i = 0; i < sectionList.size(); i++) {
            RendezVous r = sectionList.get(i);
            HBox card = buildCard(r);

            if (i > 0) {
                Separator sep = new Separator();
                sep.setStyle("-fx-background-color: #f1f5f9;");
                cardsContainer.getChildren().add(sep);
            }
            cardsContainer.getChildren().add(card);
        }
    }

    private HBox buildCard(RendezVous r) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-cursor: hand;");

       
        String color = getStatutColor(r.getStatut());
        Region indicator = new Region();
        indicator.setPrefWidth(4);
        indicator.setPrefHeight(50);
        indicator.setMinHeight(50);
        indicator.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

       
        VBox dateBox = new VBox(4);
        dateBox.setMinWidth(110);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        String dateStr = r.getDate() != null
                ? r.getDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH))
                : "—";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f2942; -fx-font-family: 'Segoe UI';");

        String heureStr = "";
        if (r.getHeureDebut() != null && r.getHeureFin() != null) {
            heureStr = r.getHeureDebut().toLocalTime().format(TIME_FMT)
                    + " – " + r.getHeureFin().toLocalTime().format(TIME_FMT);
        }
        Label heureLabel = new Label("⏱ " + heureStr);
        heureLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-family: 'Segoe UI';");

        dateBox.getChildren().addAll(dateLabel, heureLabel);

        // Type
        VBox typeBox = new VBox(4);
        typeBox.setMinWidth(130);

        Label typeLabel = new Label(capitalize(r.getTypeRdv()));
        typeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Segoe UI';");

        Label idLabel = new Label("RDV #" + r.getId());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI';");

        typeBox.getChildren().addAll(typeLabel, idLabel);

        Label statutLabel = new Label(capitalize(r.getStatut()));
        statutLabel.setStyle(getStatutStyle(r.getStatut()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

       
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnConfirm = new Button("✔ Confirmer");
        btnConfirm.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 6; "
                + "-fx-padding: 7 14; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
        btnConfirm.setOnAction(e -> {
            service.confirmerRdv(r.getId(), r.getMode());
            loadData();
        });

        Button btnDelete = new Button("✕ Supprimer");
        btnDelete.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-background-radius: 6; "
                + "-fx-padding: 7 12; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
        btnDelete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer le rendez-vous");
            confirm.setContentText("Voulez-vous vraiment supprimer ce rendez-vous ?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                service.delete(r.getId());
                loadData();
            }
        });

        if ("en attente".equalsIgnoreCase(r.getStatut()) || "réservé".equalsIgnoreCase(r.getStatut())) {
            btnDelete.setText("✕ Refuser");
            btnDelete.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation");
                confirm.setHeaderText("Refuser la demande");
                confirm.setContentText("Voulez-vous refuser cette demande ? Le créneau redeviendra libre.");
                if (confirm.showAndWait().get() == ButtonType.OK) {
                    service.refuserRdv(r.getId());
                    loadData();
                }
            });
        } else if ("confirmé".equalsIgnoreCase(r.getStatut())) {
            btnConfirm.setDisable(true);
            btnConfirm.setVisible(false);
            btnConfirm.setManaged(false);
        } else if ("libre".equalsIgnoreCase(r.getStatut()) || "disponible".equalsIgnoreCase(r.getStatut())) {
            btnConfirm.setDisable(true);
            btnConfirm.setVisible(false);
            btnConfirm.setManaged(false);
        }



        if ("confirmé".equalsIgnoreCase(r.getStatut()) && r.getTelephone() != null && !r.getTelephone().isEmpty()) {
            Button btnSms = new Button("📲 Notifier");
            btnSms.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 6; "
                    + "-fx-padding: 7 14; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            btnSms.setOnAction(e -> {
                Services.SmsService.sendSms(r.getTelephone(), 
                    "Rappel : Votre rendez-vous MentalUp est prévu pour le " + r.getDate() + " à " + r.getHeureDebut() + ".");
                btnSms.setText("✅ Envoyé");
                btnSms.setDisable(true);
            });
            actions.getChildren().add(0, btnSms);
        }

        if (("confirmé".equalsIgnoreCase(r.getStatut()) || "en cours".equalsIgnoreCase(r.getStatut())) && r.getLienMeet() != null && !r.getLienMeet().isEmpty()) {
            Button btnMeet = new Button("🎥 Rejoindre Meet");
            btnMeet.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-background-radius: 6; "
                    + "-fx-padding: 7 14; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            btnMeet.setOnAction(e -> openBrowser(r.getLienMeet()));
            actions.getChildren().add(0, btnMeet);
        }

        actions.getChildren().addAll(btnConfirm, btnDelete);

        card.getChildren().addAll(indicator, dateBox, typeBox, spacer, statutLabel, actions);

        // Hover
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #f8fafc; -fx-cursor: hand;"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-cursor: hand;"));

        return card;
    }



    private String getStatutColor(String statut) {
        if (statut == null) return "#94a3b8";
        switch (statut.toLowerCase()) {
            case "libre":     return "#4caf50";
            case "réservé":   return "#ff9800";
            case "confirmé":  return "#2196f3";
            case "en cours":  return "#10b981";
            case "terminé":   return "#64748b";
            case "en attente":return "#9c27b0";
            default:          return "#94a3b8";
        }
    }

    private String getStatutStyle(String statut) {
        String base = "-fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-font-family: 'Segoe UI';";
        if (statut == null) return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
        switch (statut.toLowerCase()) {
            case "libre":     return base + "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;";
            case "réservé":   return base + "-fx-background-color: #fff3e0; -fx-text-fill: #e65100;";
            case "confirmé":  return base + "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;";
            case "en cours":  return base + "-fx-background-color: #ecfdf5; -fx-text-fill: #059669;";
            case "terminé":   return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
            case "en attente":return base + "-fx-background-color: #f3e5f5; -fx-text-fill: #6a1b9a;";
            default:          return base + "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @FXML public void rechercherRendezVous() { appliquerFiltres(); }
    @FXML public void filtrerParStatut()     { appliquerFiltres(); }
    @FXML public void filtrerParType()       { appliquerFiltres(); }
    @FXML public void filtrerParDate()       { appliquerFiltres(); }

    @FXML
    public void reinitialiserFiltres() {
        searchField.clear();
        filtreStatut.setValue(null);
        filtreType.setValue(null);
        filtreDate.setValue(null);
        afficherCards(allRdv);
    }

    private void appliquerFiltres() {
        String search = searchField.getText().trim().toLowerCase();
        List<RendezVous> filtered = allRdv.stream()
                .filter(r -> search.isEmpty() ||
                        (r.getTypeRdv() != null && r.getTypeRdv().toLowerCase().contains(search)) ||
                        (r.getStatut() != null && r.getStatut().toLowerCase().contains(search)))
                .filter(r -> filtreStatut.getValue() == null ||
                        r.getStatut().equalsIgnoreCase(filtreStatut.getValue()))
                .filter(r -> filtreType.getValue() == null ||
                        r.getTypeRdv().equalsIgnoreCase(filtreType.getValue()))
                .filter(r -> filtreDate.getValue() == null ||
                        (r.getDate() != null && r.getDate().toLocalDate().equals(filtreDate.getValue())))
                .collect(Collectors.toList());
        afficherCards(filtered);
    }

    private void updateStats() {
        if (totalRdv != null) totalRdv.setText(String.valueOf(allRdv.size()));
        long avenir = allRdv.stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().isAfter(LocalDate.now()))
                .count();
        if (rdvAVenir != null) rdvAVenir.setText(String.valueOf(avenir));
        long mois = allRdv.stream()
                .filter(r -> r.getDate() != null &&
                        r.getDate().toLocalDate().getMonth() == LocalDate.now().getMonth() &&
                        r.getDate().toLocalDate().getYear() == LocalDate.now().getYear())
                .count();
        if (rdvCeMois != null) rdvCeMois.setText(String.valueOf(mois));
    }

    private void loadPage(MouseEvent event, String path) {
        try {
            java.net.URL url = getClass().getResource(path);
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
    @FXML public void goVoirRendezVous(MouseEvent event)   { loadPage(event, "/gui/VoirRendezVous.fxml"); }
    @FXML public void goNouveauDossier(MouseEvent event)   { loadPage(event, "/gui/NouveauDossier.fxml"); }
    @FXML public void goCalendrier(MouseEvent event)       { loadPage(event, "/gui/Calendrier.fxml"); }
    @FXML public void goConsulterDossiers(MouseEvent event){ loadPage(event, "/gui/ConsulterDossiers.fxml"); }

    @FXML public void toggleRdvMenu() {
        if (submenuRdv != null) {
            boolean s = !submenuRdv.isVisible();
            submenuRdv.setVisible(s); submenuRdv.setManaged(s);
            if (arrowRdv != null) arrowRdv.setText(s ? "˅" : "›");
        }
    }
    @FXML public void toggleDossiersMenu() {
        if (submenuDossiers != null) {
            boolean s = !submenuDossiers.isVisible();
            submenuDossiers.setVisible(s); submenuDossiers.setManaged(s);
            if (arrowDossiers != null) arrowDossiers.setText(s ? "˅" : "›");
        }
    }

    @FXML public void goActivites()  {}
    @FXML public void goRessources() {}
    @FXML public void goStats()      {}
    @FXML public void logout()       {}

    @FXML public void nouveauRendezVous() {
        System.out.println("Nouveau RDV (popup)");
    }

    @FXML
    void handleExportPlanningPDF(ActionEvent event) {
        List<String> choices = List.of("Aujourd'hui", "Cette Semaine", "Ce Mois");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Aujourd'hui", choices);
        dialog.setTitle("Exporter Planning");
        dialog.setHeaderText("Choisissez la période d'export");
        dialog.setContentText("Période :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(period -> {
            try {
                List<RendezVous> filtered;
                LocalDate now = LocalDate.now();

                if ("Aujourd'hui".equals(period)) {
                    filtered = allRdv.stream()
                        .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(now))
                        .collect(Collectors.toList());
                } else if ("Cette Semaine".equals(period)) {
                    LocalDate weekEnd = now.plusDays(7);
                    filtered = allRdv.stream()
                        .filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(now) && r.getDate().toLocalDate().isBefore(weekEnd))
                        .collect(Collectors.toList());
                } else {
                    LocalDate monthEnd = now.plusMonths(1);
                    filtered = allRdv.stream()
                        .filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(now) && r.getDate().toLocalDate().isBefore(monthEnd))
                        .collect(Collectors.toList());
                }

                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String fileName = "Planning_" + period.replace(" ", "_") + "_" + timestamp + ".pdf";
                String path = System.getProperty("user.home") + "\\Desktop\\" + fileName;
                pdfService.generatePlanningPDF(filtered, period, path);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Planning exporté sur le bureau : " + fileName);
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors de l'export : " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    @FXML
    void handleExportPatientPDF(ActionEvent event) {
        List<Dossier> dossiers = serviceDossier.getAll();
        if (dossiers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Aucun dossier patient trouvé.");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<Dossier> dialog = new ChoiceDialog<>(dossiers.get(0), dossiers);
        dialog.setTitle("Exporter Dossier Patient");
        dialog.setHeaderText("Sélectionnez un patient à exporter");
        dialog.setContentText("Patient :");

        Optional<Dossier> result = dialog.showAndWait();
        result.ifPresent(d -> {
            try {
                List<RendezVous> history = service.getByEtudiantId(d.getPatientId());
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String fileName = "Dossier_" + d.getPatientNom().replace(" ", "_") + "_" + timestamp + ".pdf";
                String path = System.getProperty("user.home") + "\\Desktop\\" + fileName;
                pdfService.generatePatientDossierPDF(d, history, path);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Dossier exporté sur le bureau : " + fileName);
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors de l'export : " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback for some systems
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}