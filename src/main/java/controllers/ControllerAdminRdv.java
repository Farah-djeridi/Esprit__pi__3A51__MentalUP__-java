package controllers;


import Models.RendezVous;
import Services.ServiceRendezVous;
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

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import validators.RendezVousValidator;

public class ControllerAdminRdv {

    @FXML private VBox      rdvListContainer;
    @FXML private Label     statTotal, statLibres, statReserves, statConfirmes, statAujourdhui;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtreStatut, filtreType, triOptions;
    @FXML private ImageView logoImage;

    private final ServiceRendezVous service = new ServiceRendezVous();
    private List<RendezVous> tousLesRdv;

    @FXML
    public void initialize() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));

        // Remplir les ComboBox
        filtreStatut.getItems().addAll("Tous", "libre", "réservé", "confirmé", "annulé", "en attente");
        filtreStatut.setValue("Tous");

        filtreType.getItems().addAll("Tous", "consultation", "suivi", "urgence", "bilan");
        filtreType.setValue("Tous");

        triOptions.getItems().addAll(
            "Date ↑ (plus ancien)", "Date ↓ (plus récent)",
            "Heure ↑", "Heure ↓", "Statut A→Z", "Type A→Z"
        );
        triOptions.setValue("Date ↓ (plus récent)");

        chargerEtAfficher();
    }


    private void chargerEtAfficher() {
        tousLesRdv = service.getAll();
        mettreAJourStats(tousLesRdv);
        appliquerFiltresEtTri();
    }

    private void mettreAJourStats(List<RendezVous> list) {
        long total     = list.size();
        long libres    = list.stream().filter(r -> "libre".equalsIgnoreCase(r.getStatut()) || "disponible".equalsIgnoreCase(r.getStatut())).count();
        long reserves  = list.stream().filter(r -> "réservé".equalsIgnoreCase(r.getStatut()) || "en attente".equalsIgnoreCase(r.getStatut())).count();
        long confirmes = list.stream().filter(r -> "confirmé".equalsIgnoreCase(r.getStatut())).count();
        long aujourdhui = list.stream().filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now())).count();

        statTotal.setText(String.valueOf(total));
        statLibres.setText(String.valueOf(libres));
        statReserves.setText(String.valueOf(reserves));
        statConfirmes.setText(String.valueOf(confirmes));
        statAujourdhui.setText(String.valueOf(aujourdhui));
    }

    private void appliquerFiltresEtTri() {
        String search  = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String statut  = filtreStatut.getValue();
        String type    = filtreType.getValue();
        String tri     = triOptions.getValue();

        List<RendezVous> filtres = tousLesRdv.stream()
            .filter(r -> {
                // Recherche texte
                if (!search.isEmpty()) {
                    String concat = (r.getTypeRdv()  != null ? r.getTypeRdv().toLowerCase()  : "") + " " +
                                    (r.getStatut()   != null ? r.getStatut().toLowerCase()   : "") + " " +
                                    (r.getDate()     != null ? r.getDate().toString()         : "") + " " +
                                    (r.getLieu()     != null ? r.getLieu().toLowerCase()      : "");
                    if (!concat.contains(search)) return false;
                }
                // Filtre statut
                if (!"Tous".equals(statut) && statut != null) {
                    if (!statut.equalsIgnoreCase(r.getStatut())) return false;
                }
                // Filtre type
                if (!"Tous".equals(type) && type != null) {
                    if (!type.equalsIgnoreCase(r.getTypeRdv())) return false;
                }
                return true;
            })
            .collect(Collectors.toList());

        // Tri
        if (tri != null) {
            switch (tri) {
                case "Date ↑ (plus ancien)"  -> filtres.sort(Comparator.comparing(r -> r.getDate() != null ? r.getDate() : Date.valueOf("1970-01-01")));
                case "Date ↓ (plus récent)"  -> filtres.sort(Comparator.comparing((RendezVous r) -> r.getDate() != null ? r.getDate() : Date.valueOf("1970-01-01")).reversed());
                case "Heure ↑"               -> filtres.sort(Comparator.comparing(r -> r.getHeureDebut() != null ? r.getHeureDebut() : Time.valueOf("00:00:00")));
                case "Heure ↓"               -> filtres.sort(Comparator.comparing((RendezVous r) -> r.getHeureDebut() != null ? r.getHeureDebut() : Time.valueOf("00:00:00")).reversed());
                case "Statut A→Z"            -> filtres.sort(Comparator.comparing(r -> r.getStatut() != null ? r.getStatut() : ""));
                case "Type A→Z"              -> filtres.sort(Comparator.comparing(r -> r.getTypeRdv() != null ? r.getTypeRdv() : ""));
            }
        }

        afficherCartes(filtres);
    }


    private void afficherCartes(List<RendezVous> list) {
        rdvListContainer.getChildren().clear();

        if (list.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous trouvé.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-padding: 20 0;");
            rdvListContainer.getChildren().add(empty);
            return;
        }

        for (RendezVous r : list) {
            rdvListContainer.getChildren().add(createRdvCard(r));
        }
    }

    private HBox createRdvCard(RendezVous r) {
        HBox card = new HBox(0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);" +
            "-fx-border-color: rgba(44,62,80,0.06); -fx-border-radius: 14; -fx-border-width: 1;"
        );

        // Barre couleur gauche
        String couleur = getCouleurStatut(r.getStatut());
        VBox barre = new VBox();
        barre.setPrefWidth(6);
        barre.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 14 0 0 14;");


        HBox contenu = new HBox(16);
        contenu.setAlignment(Pos.CENTER_LEFT);
        contenu.setPadding(new Insets(16, 20, 16, 18));
        HBox.setHgrow(contenu, Priority.ALWAYS);

        Label icone = new Label(getIconeType(r.getTypeRdv()));
        icone.setStyle("-fx-font-size: 26px;");


        VBox infos = new VBox(4);
        HBox.setHgrow(infos, Priority.ALWAYS);


        HBox ligne1 = new HBox(10);
        ligne1.setAlignment(Pos.CENTER_LEFT);
        Label lblDate = new Label(formatDate(r.getDate()));
        lblDate.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        Label lblHeure = new Label("•  " + formatTime(r.getHeureDebut()) + " – " + formatTime(r.getHeureFin()));
        lblHeure.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A6C7D; -fx-font-weight: 500;");
        ligne1.getChildren().addAll(lblDate, lblHeure);


        HBox ligne2 = new HBox(12);
        ligne2.setAlignment(Pos.CENTER_LEFT);
        Label lblType = new Label(r.getTypeRdv() != null ? capitalize(r.getTypeRdv()) : "—");
        lblType.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A6C7D;");
        Label lblPsy = new Label("Psy #" + r.getPsychologueId());
        lblPsy.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
        if (r.getEtudiantId() != null) {
            Label lblEtu = new Label("Étudiant #" + r.getEtudiantId());
            lblEtu.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
            ligne2.getChildren().addAll(lblType, lblPsy, lblEtu);
        } else {
            ligne2.getChildren().addAll(lblType, lblPsy);
        }

        infos.getChildren().addAll(ligne1, ligne2);

        // Badge statut
        Label badge = new Label(r.getStatut() != null ? r.getStatut().toUpperCase() : "—");
        badge.setStyle(
            "-fx-background-color: " + couleur + "22; -fx-text-fill: " + couleur + ";" +
            "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        contenu.getChildren().addAll(icone, infos, spacer);

        contenu.getChildren().add(badge);
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
    private void onAjouterRdv(ActionEvent event) {
        ouvrirDialogAjout();
    }

    private void ouvrirDialogAjout() {
        Dialog<RendezVous> dialog = buildFormDialog("Ajouter un rendez-vous", null);
        dialog.showAndWait().ifPresent(r -> {
            if (r != null) { service.add(r); chargerEtAfficher(); }
        });
    }

    private void ouvrirDialogEdition(RendezVous existing) {
        Dialog<RendezVous> dialog = buildFormDialog("Modifier le rendez-vous", existing);
        dialog.showAndWait().ifPresent(r -> {
            if (r != null) { service.update(r); chargerEtAfficher(); }
        });
    }

    private void supprimerRdv(RendezVous r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le rendez-vous");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer ce créneau du " + formatDate(r.getDate()) + " ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.delete(r.getId());
                chargerEtAfficher();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Dialog<RendezVous> buildFormDialog(String title, RendezVous existing) {
        Dialog<RendezVous> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #F0F4F8;");
        pane.setPrefWidth(420);

        ButtonType saveBtn   = new ButtonType("✔  Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler",         ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(saveBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPadding(new Insets(22, 26, 10, 26));

        Label ttl = new Label(title);
        ttl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        grid.add(ttl, 0, 0, 2, 1);

        // Date
        grid.add(fl("Date"), 0, 1);
        DatePicker dp = new DatePicker(); dp.setId("datePicker"); dp.setPrefWidth(240);
        dp.setStyle("-fx-background-radius: 8; -fx-border-color: #D0D9E0; -fx-border-radius: 8;");
        grid.add(dp, 1, 1);

        // Heure début
        grid.add(fl("Heure début"), 0, 2);
        TextField tfS = ft("09:00"); tfS.setId("heureDebut"); grid.add(tfS, 1, 2);

        // Heure fin
        grid.add(fl("Heure fin"), 0, 3);
        TextField tfE = ft("10:00"); tfE.setId("heureFin"); grid.add(tfE, 1, 3);

        // Statut
        grid.add(fl("Statut"), 0, 4);
        ComboBox<String> cbStat = new ComboBox<>(); cbStat.setId("statut");
        cbStat.getItems().addAll("libre", "réservé", "confirmé", "annulé", "en attente");
        cbStat.setValue("libre"); cbStat.setPrefWidth(240);
        cbStat.setStyle("-fx-background-radius: 8;"); grid.add(cbStat, 1, 4);

      
        grid.add(fl("Type RDV"), 0, 5);
        ComboBox<String> cbType = new ComboBox<>(); cbType.setId("typeRdv");
        cbType.getItems().addAll("consultation", "suivi", "urgence", "bilan");
        cbType.setValue("consultation"); cbType.setPrefWidth(240);
        cbType.setStyle("-fx-background-radius: 8;"); grid.add(cbType, 1, 5);

       
        grid.add(fl("Psy ID"), 0, 6);
        TextField tfPsy = ft("2"); tfPsy.setId("psyId"); grid.add(tfPsy, 1, 6);

       
        if (existing != null) {
            if (existing.getDate() != null) dp.setValue(existing.getDate().toLocalDate());
            if (existing.getHeureDebut() != null) tfS.setText(existing.getHeureDebut().toString().substring(0, 5));
            if (existing.getHeureFin()   != null) tfE.setText(existing.getHeureFin().toString().substring(0, 5));
            if (existing.getStatut()     != null) cbStat.setValue(existing.getStatut());
            if (existing.getTypeRdv()    != null) cbType.setValue(existing.getTypeRdv());
            tfPsy.setText(String.valueOf(existing.getPsychologueId()));
        }

        pane.setContent(grid);

        int existingId = existing != null ? existing.getId() : 0;
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return buildRdvFromForm(dp, tfS, tfE, cbStat, cbType, tfPsy, existingId);
            }
            return null;
        });

        return dialog;
    }

    @SuppressWarnings("unchecked")
    private RendezVous buildRdvFromForm(DatePicker dp,
                                        TextField tfS,
                                        TextField tfE,
                                        ComboBox<String> cbStat,
                                        ComboBox<String> cbType,
                                        TextField tfPsy,
                                        int id) {

        RendezVousValidator.ValidationResult result =
                new RendezVousValidator.ValidationResult();

        Time tDebut = RendezVousValidator.parseHeure(tfS.getText(), "Heure début", result);
        Time tFin   = RendezVousValidator.parseHeure(tfE.getText(), "Heure fin", result);
        int psyId   = RendezVousValidator.parseId(tfPsy.getText(), "Psy ID", result);

        RendezVous r = new RendezVous();
        r.setId(id);
        r.setDate(dp.getValue() != null ? Date.valueOf(dp.getValue()) : null);
        r.setHeureDebut(tDebut);
        r.setHeureFin(tFin);
        r.setStatut(cbStat.getValue());
        r.setTypeRdv(cbType.getValue());
        r.setPsychologueId(psyId);

        // Validation complète
        RendezVousValidator.ValidationResult full = RendezVousValidator.valider(r);
        full.getErreurs().addAll(result.getErreurs());

        if (!full.isValide()) {
            new Alert(Alert.AlertType.WARNING,
                    "⚠ Erreurs de saisie :\n\n" + full.getMessageComplet()
            ).showAndWait();
            return null;
        }

        return r;
    }


    @FXML private void onSearch(KeyEvent e)        { appliquerFiltresEtTri(); }
    @FXML private void onFiltreChange(ActionEvent e){ appliquerFiltresEtTri(); }
    @FXML private void onTriChange(ActionEvent e)  { appliquerFiltresEtTri(); }

    @FXML
    private void onFilterTotal(MouseEvent e) {
        filtreStatut.setValue("Tous");
        appliquerFiltresEtTri();
    }

    @FXML
    private void onFilterLibre(MouseEvent e) {
        filtreStatut.setValue("libre");
        appliquerFiltresEtTri();
    }

    @FXML
    private void onFilterReserve(MouseEvent e) {
        filtreStatut.setValue("réservé");
        appliquerFiltresEtTri();
    }

    @FXML
    private void onFilterConfirme(MouseEvent e) {
        filtreStatut.setValue("confirmé");
        appliquerFiltresEtTri();
    }

    @FXML
    private void onFilterAujourdhui(MouseEvent e) {
        LocalDate today = LocalDate.now();
        List<RendezVous> filtres = tousLesRdv.stream()
            .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(today))
            .collect(Collectors.toList());
        afficherCartes(filtres);
    }

    @FXML
    private void onReset(ActionEvent e) {
        searchField.clear();
        filtreStatut.setValue("Tous");
        filtreType.setValue("Tous");
        triOptions.setValue("Date ↓ (plus récent)");
        appliquerFiltresEtTri();
    }


    private void loadPage(String fxml, Object event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage;
            if (event instanceof MouseEvent me) {
                stage = (Stage) ((Node) me.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) rdvListContainer.getScene().getWindow();
            }
            stage.setScene(new Scene(root));
            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML private void onNavHomeClicked(MouseEvent e)    { loadPage("/gui/HomeAdmin.fxml", e); }
    @FXML private void onNavDossiersClicked(MouseEvent e){ loadPage("/gui/AdminDossiers.fxml", e); }
    @FXML private void onLogout(ActionEvent e)           { loadPage("/gui/HomeAdmin.fxml", e); }

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


    private String getCouleurStatut(String s) {
        if (s == null) return "#95A5A6";
        return switch (s.toLowerCase()) {
            case "libre", "disponible" -> "#27AE60";
            case "confirmé"            -> "#2980B9";
            case "réservé", "en attente" -> "#E67E22";
            case "annulé"              -> "#95A5A6";
            default                    -> "#95A5A6";
        };
    }

    private String getIconeType(String t) {
        if (t == null) return "📋";
        return switch (t.toLowerCase()) {
            case "urgence"      -> "🚨";
            case "suivi"        -> "📈";
            case "bilan"        -> "📊";
            default             -> "🏥";
        };
    }

    private String formatDate(Date date) {
        if (date == null) return "—";
        LocalDate ld = date.toLocalDate();
        String[] mois = {"jan.","fév.","mar.","avr.","mai","juin","juil.","août","sep.","oct.","nov.","déc."};
        return ld.getDayOfMonth() + " " + mois[ld.getMonthValue()-1] + " " + ld.getYear();
    }

    private String formatTime(Time t) {
        if (t == null) return "--:--";
        return t.toString().substring(0, 5);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private Label fl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5A6C7D;");
        return l;
    }

    private TextField ft(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(240);
        tf.setStyle("-fx-padding: 8; -fx-background-radius: 8; -fx-border-radius: 8;" +
                    "-fx-border-color: #D0D9E0; -fx-background-color: white;");
        return tf;
    }
}
