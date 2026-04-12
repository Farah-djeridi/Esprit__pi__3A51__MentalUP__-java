package controllers;

import Models.Dossier;
import Services.ServiceDossier;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import validators.DossierValidator;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ControllerAdminDossiers {


    @FXML private VBox      dossierListContainer;
    @FXML private Label     statTotalDossiers, statRisqueEleve, statRisqueMoyen, statRisqueFaible;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtreRisque, triOptions;
    @FXML private ImageView logoImage;

    private final ServiceDossier service = new ServiceDossier();
    private List<Dossier> tousLesDossiers;


    @FXML
    public void initialize() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));

        filtreRisque.getItems().addAll("Tous", "faible", "moyen", "élevé");
        filtreRisque.setValue("Tous");

        triOptions.getItems().addAll(
            "Date ↓ (plus récent)", "Date ↑ (plus ancien)",
            "Risque A→Z", "Patient ID ↑"
        );
        triOptions.setValue("Date ↓ (plus récent)");

        chargerEtAfficher();
    }


    private void chargerEtAfficher() {
        tousLesDossiers = service.getAll();
        mettreAJourStats(tousLesDossiers);
        appliquerFiltresEtTri();
    }

    private void mettreAJourStats(List<Dossier> list) {
        long total  = list.size();
        long eleve  = list.stream().filter(d -> "élevé".equalsIgnoreCase(d.getNiveauRisque())).count();
        long moyen  = list.stream().filter(d -> "moyen".equalsIgnoreCase(d.getNiveauRisque())).count();
        long faible = list.stream().filter(d -> "faible".equalsIgnoreCase(d.getNiveauRisque())).count();

        statTotalDossiers.setText(String.valueOf(total));
        statRisqueEleve.setText(String.valueOf(eleve));
        statRisqueMoyen.setText(String.valueOf(moyen));
        statRisqueFaible.setText(String.valueOf(faible));
    }

    private void appliquerFiltresEtTri() {
        String search = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String risque = filtreRisque.getValue();
        String tri    = triOptions.getValue();

        List<Dossier> filtres = tousLesDossiers.stream()
            .filter(d -> {
                if (!search.isEmpty()) {
                    String concat = "patient " + d.getPatientId() + " " +
                                    (d.getNiveauRisque() != null ? d.getNiveauRisque().toLowerCase() : "") + " " +
                                    (d.getDateCreation() != null ? d.getDateCreation().toString() : "");
                    if (!concat.contains(search)) return false;
                }
                if (!"Tous".equals(risque) && risque != null) {
                    if (!risque.equalsIgnoreCase(d.getNiveauRisque())) return false;
                }
                return true;
            })
            .collect(Collectors.toList());

        if (tri != null) {
            switch (tri) {
                case "Date ↓ (plus récent)"  -> filtres.sort(Comparator.comparing((Dossier d) -> d.getDateCreation() != null ? d.getDateCreation() : Date.valueOf("1970-01-01")).reversed());
                case "Date ↑ (plus ancien)"  -> filtres.sort(Comparator.comparing(d -> d.getDateCreation() != null ? d.getDateCreation() : Date.valueOf("1970-01-01")));
                case "Risque A→Z"            -> filtres.sort(Comparator.comparing(d -> d.getNiveauRisque() != null ? d.getNiveauRisque() : ""));
                case "Patient ID ↑"          -> filtres.sort(Comparator.comparingInt(Dossier::getPatientId));
            }
        }

        afficherCartes(filtres);
    }


    private void afficherCartes(List<Dossier> list) {
        dossierListContainer.getChildren().clear();

        if (list.isEmpty()) {
            Label empty = new Label("Aucun dossier trouvé.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-padding: 20 0;");
            dossierListContainer.getChildren().add(empty);
            return;
        }

        for (Dossier d : list) {
            dossierListContainer.getChildren().add(createDossierCard(d));
        }
    }

    private HBox createDossierCard(Dossier d) {
        HBox card = new HBox(0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);" +
            "-fx-border-color: rgba(44,62,80,0.06); -fx-border-radius: 14; -fx-border-width: 1;"
        );

        String couleur = getCouleurRisque(d.getNiveauRisque());

        // Barre gauche
        VBox barre = new VBox();
        barre.setPrefWidth(6);
        barre.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 14 0 0 14;");


        HBox contenu = new HBox(16);
        contenu.setAlignment(Pos.CENTER_LEFT);
        contenu.setPadding(new Insets(16, 20, 16, 18));
        HBox.setHgrow(contenu, Priority.ALWAYS);


        Label icone = new Label(getIconeRisque(d.getNiveauRisque()));
        icone.setStyle("-fx-font-size: 26px;");
        VBox infos = new VBox(4);
        HBox.setHgrow(infos, Priority.ALWAYS);

        HBox ligne1 = new HBox(10);
        ligne1.setAlignment(Pos.CENTER_LEFT);
        Label lblId = new Label("Dossier #" + d.getId());
        lblId.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        Label lblDate = new Label("•  " + formatDate(d.getDateCreation()));
        lblDate.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A6C7D; -fx-font-weight: 500;");
        ligne1.getChildren().addAll(lblId, lblDate);

        HBox ligne2 = new HBox(12);
        ligne2.setAlignment(Pos.CENTER_LEFT);
        Label lblPatient = new Label("Patient #" + d.getPatientId());
        lblPatient.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A6C7D;");
        Label lblPsy = new Label("Psy #" + d.getPsychologueId());
        lblPsy.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");


        if (d.getAiSummary() != null && !d.getAiSummary().isEmpty()) {
            Label lblAi = new Label("🤖 IA");
            lblAi.setStyle(
                "-fx-background-color: rgba(142,68,173,0.12); -fx-text-fill: #8E44AD;" +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 2 8;"
            );
            ligne2.getChildren().addAll(lblPatient, lblPsy, lblAi);
        } else {
            ligne2.getChildren().addAll(lblPatient, lblPsy);
        }

        // Note confidentialité
        Label lblConfidentiel = new Label("🔒 Notes confidentielles — non affichées");
        lblConfidentiel.setStyle("-fx-font-size: 11px; -fx-text-fill: #B7950B; -fx-font-style: italic;");

        infos.getChildren().addAll(ligne1, ligne2, lblConfidentiel);

        // Badge risque
        Label badge = new Label(d.getNiveauRisque() != null ? d.getNiveauRisque().toUpperCase() : "—");
        badge.setStyle(
            "-fx-background-color: " + couleur + "22; -fx-text-fill: " + couleur + ";" +
            "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✏");
        btnEdit.setStyle(
            "-fx-background-color: rgba(44,62,80,0.08); -fx-text-fill: #2C3E50;" +
            "-fx-background-radius: 8; -fx-padding: 7 12; -fx-cursor: hand; -fx-font-size: 13px;"
        );
        btnEdit.setOnAction(e -> ouvrirDialogEdition(d));

        Button btnDelete = new Button("🗑");
        btnDelete.setStyle(
            "-fx-background-color: rgba(231,76,60,0.1); -fx-text-fill: #E74C3C;" +
            "-fx-background-radius: 8; -fx-padding: 7 12; -fx-cursor: hand; -fx-font-size: 13px;"
        );
        btnDelete.setOnAction(e -> supprimerDossier(d));

        actions.getChildren().addAll(btnEdit, btnDelete);
        contenu.getChildren().addAll(icone, infos, spacer, badge, actions);
        card.getChildren().addAll(barre, contenu);

        // Hover
        card.setOnMouseEntered(ev -> card.setStyle(
            "-fx-background-color: #FAFCFF; -fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(44,62,80,0.12), 12, 0, 0, 3);" +
            "-fx-border-color: rgba(44,62,80,0.1); -fx-border-radius: 14; -fx-border-width: 1;"
        ));
        card.setOnMouseExited(ev -> card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);" +
            "-fx-border-color: rgba(44,62,80,0.06); -fx-border-radius: 14; -fx-border-width: 1;"
        ));

        return card;
    }


    @FXML
    private void onAjouterDossier(ActionEvent event) {
        ouvrirDialogAjout();
    }

    private void ouvrirDialogAjout() {
        Dialog<Dossier> dialog = buildFormDialog("Ajouter un dossier", null);
        dialog.showAndWait().ifPresent(d -> {
            if (d != null) { service.add(d); chargerEtAfficher(); }
        });
    }

    private void ouvrirDialogEdition(Dossier existing) {
        Dialog<Dossier> dialog = buildFormDialog("Modifier le dossier", existing);
        dialog.showAndWait().ifPresent(d -> {
            if (d != null) { service.update(d); chargerEtAfficher(); }
        });
    }

    private void supprimerDossier(Dossier d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le dossier");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer le dossier #" + d.getId() + " ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(d);
                chargerEtAfficher();
            }
        });
    }

    private Dialog<Dossier> buildFormDialog(String title, Dossier existing) {
        Dialog<Dossier> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #F0F4F8;");
        pane.setPrefWidth(440);

        ButtonType saveBtn   = new ButtonType("✔  Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler",         ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(saveBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPadding(new Insets(22, 26, 10, 26));

        Label ttl = new Label(title);
        ttl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        grid.add(ttl, 0, 0, 2, 1);

        // Date création
        grid.add(fl("Date création"), 0, 1);
        DatePicker dp = new DatePicker(LocalDate.now()); dp.setPrefWidth(250);
        dp.setStyle("-fx-background-radius: 8; -fx-border-color: #D0D9E0; -fx-border-radius: 8;");
        grid.add(dp, 1, 1);

        // Niveau de risque
        grid.add(fl("Niveau de risque"), 0, 2);
        ComboBox<String> cbRisque = new ComboBox<>();
        cbRisque.getItems().addAll("faible", "moyen", "élevé");
        cbRisque.setValue("faible"); cbRisque.setPrefWidth(250);
        cbRisque.setStyle("-fx-background-radius: 8;");
        grid.add(cbRisque, 1, 2);

        // Patient ID
        grid.add(fl("Patient ID"), 0, 3);
        TextField tfPatient = ft("1"); tfPatient.setPrefWidth(250);
        grid.add(tfPatient, 1, 3);

        // Psychologue ID
        grid.add(fl("Psy ID"), 0, 4);
        TextField tfPsy = ft("2"); tfPsy.setPrefWidth(250);
        grid.add(tfPsy, 1, 4);

        // ⚠ NOTE : pas de champ "Notes générales" — confidentielles
        Label noteConf = new Label("🔒 Les notes générales sont gérées uniquement par le psychologue.");
        noteConf.setStyle("-fx-font-size: 11px; -fx-text-fill: #B7950B; -fx-font-style: italic;");
        grid.add(noteConf, 0, 5, 2, 1);

        // Pré-remplir si édition
        if (existing != null) {
            if (existing.getDateCreation() != null) dp.setValue(existing.getDateCreation().toLocalDate());
            if (existing.getNiveauRisque() != null) cbRisque.setValue(existing.getNiveauRisque());
            tfPatient.setText(String.valueOf(existing.getPatientId()));
            tfPsy.setText(String.valueOf(existing.getPsychologueId()));
        }

        pane.setContent(grid);

        int existingId = existing != null ? existing.getId() : 0;
        String existingNotes    = existing != null ? existing.getNotesGenerales() : null;
        String existingAiSum    = existing != null ? existing.getAiSummary()      : null;
        String existingAiKeys   = existing != null ? existing.getAiKeyPoints()    : null;

        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                DossierValidator.ValidationResult result =
                        new DossierValidator.ValidationResult();

                int patientId = DossierValidator.parseId(tfPatient.getText(), "Patient ID", result);
                int psyId     = DossierValidator.parseId(tfPsy.getText(), "Psy ID", result);

                Dossier d = new Dossier();
                d.setId(existingId);
                d.setDateCreation(dp.getValue() != null ? Date.valueOf(dp.getValue()) : null);
                d.setNiveauRisque(cbRisque.getValue());
                d.setPatientId(patientId);
                d.setPsychologueId(psyId);
                d.setNotesGenerales(existingNotes);
                d.setAiSummary(existingAiSum);
                d.setAiKeyPoints(existingAiKeys);

                DossierValidator.ValidationResult full = DossierValidator.valider(d);
                if (!full.isValide()) {
                    new Alert(Alert.AlertType.WARNING,
                            "⚠ Erreurs de saisie :\n\n" + full.getMessageComplet()
                    ).showAndWait();
                    return null;
                }
                return d;
            }
            return null;
        });
        return dialog;
    }

    // ══════════════════════════════════════════════════════
    //  RECHERCHE + FILTRES + TRI
    // ══════════════════════════════════════════════════════
    @FXML private void onSearch(KeyEvent e)         { appliquerFiltresEtTri(); }
    @FXML private void onFiltreChange(ActionEvent e){ appliquerFiltresEtTri(); }
    @FXML private void onTriChange(ActionEvent e)   { appliquerFiltresEtTri(); }

    @FXML
    private void onReset(ActionEvent e) {
        searchField.clear();
        filtreRisque.setValue("Tous");
        triOptions.setValue("Date ↓ (plus récent)");
        appliquerFiltresEtTri();
    }

    private void loadPage(String fxml, Object event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage;
            if (event instanceof MouseEvent me)
                stage = (Stage) ((Node) me.getSource()).getScene().getWindow();
            else
                stage = (Stage) dossierListContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML private void onNavHomeClicked(MouseEvent e)  { loadPage("/gui/HomeAdmin.fxml", e); }
    @FXML private void onNavRdvClicked(MouseEvent e)   { loadPage("/gui/AdminRdv.fxml", e); }
    @FXML private void onLogout(ActionEvent e)         { loadPage("/gui/HomeAdmin.fxml", e); }

    @FXML private void onNavHoverEnter(MouseEvent event) {
        ((HBox) event.getSource()).setStyle(
            "-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;"
        );
    }
    @FXML private void onNavHoverExit(MouseEvent event) {
        ((HBox) event.getSource()).setStyle(
            "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;"
        );
    }


    private String getCouleurRisque(String r) {
        if (r == null) return "#95A5A6";
        return switch (r.toLowerCase()) {
            case "élevé"  -> "#E74C3C";
            case "moyen"  -> "#E67E22";
            case "faible" -> "#27AE60";
            default       -> "#95A5A6";
        };
    }

    private String getIconeRisque(String r) {
        if (r == null) return "📋";
        return switch (r.toLowerCase()) {
            case "élevé"  -> "🔴";
            case "moyen"  -> "🟠";
            case "faible" -> "🟢";
            default       -> "📋";
        };
    }

    private String formatDate(Date date) {
        if (date == null) return "—";
        LocalDate ld = date.toLocalDate();
        String[] mois = {"jan.","fév.","mar.","avr.","mai","juin","juil.","août","sep.","oct.","nov.","déc."};
        return ld.getDayOfMonth() + " " + mois[ld.getMonthValue()-1] + " " + ld.getYear();
    }

    private Label fl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5A6C7D;");
        return l;
    }

    private TextField ft(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(250);
        tf.setStyle("-fx-padding: 8; -fx-background-radius: 8; -fx-border-radius: 8;" +
                    "-fx-border-color: #D0D9E0; -fx-background-color: white;");
        return tf;
    }
}
