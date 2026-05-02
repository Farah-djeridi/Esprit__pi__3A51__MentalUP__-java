package Controllor;

import Models.RendezVous;
import services.ServiceRendezVous;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CalendrierController {

    @FXML private Label   currentDate;
    @FXML private Label   sidebarRdvToday;
    @FXML private Label   sidebarPatientsCount;
    @FXML private Label   sidebarDossiersTotal;
    @FXML private VBox    submenuRdv;
    @FXML private VBox    submenuDossiers;
    @FXML private Label   arrowRdv;
    @FXML private Label   arrowDossiers;
    @FXML private Label   labelSemaine;
    @FXML private GridPane calendarGrid;

    private final ServiceRendezVous service = new ServiceRendezVous();
    private LocalDate currentWeekStart;
    private int psychologueId = 6; // Default fallback

    private static final int HOUR_START  = 8;
    private static final int HOUR_END    = 19;
    private static final int CELL_HEIGHT = 46;

    private boolean showLibre = true;
    private boolean showReserved = true;

    @FXML private HBox filterGreen;
    @FXML private HBox filterRed;

    @FXML
    public void initialize() {
        if (utils.SessionManager.getInstance().isLoggedIn()) {
            psychologueId = utils.SessionManager.getInstance().getCurrentUser().getId();
        }

        currentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        submenuRdv.setVisible(true);
        submenuRdv.setManaged(true);
        submenuDossiers.setVisible(false);
        submenuDossiers.setManaged(false);
        currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        buildCalendar();
        updateSidebarStats();
    }


    private void buildCalendar() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);
        labelSemaine.setText(
                currentWeekStart.format(fmt) + " – " +
                        currentWeekStart.plusDays(6).format(fmt) + " " + currentWeekStart.getYear()
        );

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();


        ColumnConstraints colH = new ColumnConstraints(58);
        colH.setHgrow(Priority.NEVER);
        calendarGrid.getColumnConstraints().add(colH);

        // Cols 1-7 = jours
        for (int d = 0; d < 7; d++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            calendarGrid.getColumnConstraints().add(col);
        }

        // Row 0 = en-têtes
        calendarGrid.getRowConstraints().add(rowConstraint(46));

        // Coin vide
        Label corner = new Label();
        corner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        corner.setStyle("-fx-background-color:#F8FAFC; -fx-border-color:#E0E7EF; -fx-border-width:0 1 1 0;");
        calendarGrid.add(corner, 0, 0);

        // En-têtes jours
        String[] jours = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
        for (int d = 0; d < 7; d++) {
            LocalDate day = currentWeekStart.plusDays(d);
            calendarGrid.add(buildDayHeader(jours[d], day), d + 1, 0);
        }

        // Rows heures
        List<RendezVous> rdvs = service.getSlotsWithVirtuals(psychologueId, currentWeekStart, currentWeekStart.plusDays(6));
        for (int h = 0; h <= (16 - 9) * 2 + 1; h++) {
            int totalMinutes = h * 30;
            int hour = 9 + (totalMinutes / 60);
            int minute = totalMinutes % 60;
            int row  = h + 1;
            if (hour > 16 || (hour == 16 && minute > 0)) break;
            calendarGrid.getRowConstraints().add(rowConstraint(CELL_HEIGHT));

            Label lh = new Label(String.format("%02d:%02d", hour, minute));
            lh.setStyle("-fx-font-size:11px; -fx-text-fill:#9aaebb; -fx-padding:6 6 0 4;" +
                    "-fx-border-color:#F0F4F8; -fx-border-width:0 1 1 0; -fx-background-color:#F8FAFC;");
            lh.setAlignment(Pos.TOP_RIGHT);
            lh.setMaxWidth(Double.MAX_VALUE);
            calendarGrid.add(lh, 0, row);

            for (int d = 0; d < 7; d++) {
                LocalDate day = currentWeekStart.plusDays(d);
                calendarGrid.add(buildCell(day, hour, minute, rdvs), d + 1, row);
            }
        }
    }

    private VBox buildDayHeader(String shortName, LocalDate day) {
        boolean isToday = day.equals(LocalDate.now());

        Label lDay = new Label(shortName.toUpperCase());
        lDay.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" +
                (isToday ? "#1a4a5f" : "#9aaebb") + ";");

        Label lNum = new Label(String.valueOf(day.getDayOfMonth()));
        lNum.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:" +
                (isToday ? "white" : "#2d3748") + ";");

        Node numNode;
        if (isToday) {
            Circle c = new Circle(18, Color.web("#1a4a5f"));
            StackPane sp = new StackPane(c, lNum);
            sp.setAlignment(Pos.CENTER);
            numNode = sp;
        } else {
            numNode = lNum;
        }

        VBox vb = new VBox(3, lDay, numNode);
        vb.setAlignment(Pos.CENTER);
        vb.setPadding(new Insets(8, 4, 8, 4));
        vb.setStyle("-fx-border-color:#E0E7EF; -fx-border-width:0 1 1 0;" +
                (isToday ? "-fx-background-color:#f0f7ff;" : "-fx-background-color:white;"));
        return vb;
    }

    private StackPane buildCell(LocalDate day, int hour, int minute, List<RendezVous> rdvs) {
        boolean isToday = day.equals(LocalDate.now());
        String bg = isToday ? "#f5faff" : "white";

        StackPane cell = new StackPane();
        cell.setMinHeight(CELL_HEIGHT);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setStyle("-fx-background-color:" + bg + "; -fx-border-color:#F0F4F8; -fx-border-width:0 1 1 0;");

        for (RendezVous r : rdvs) {
            if (r.getDate().toLocalDate().equals(day) &&
                    r.getHeureDebut().toLocalTime().getHour() == hour &&
                    r.getHeureDebut().toLocalTime().getMinute() == minute) {

                String status = r.getStatut();
                boolean isLibre = (r.getId() == -1 || "libre".equalsIgnoreCase(status));
                boolean isReserved = "réservé".equalsIgnoreCase(status) || "confirmé".equalsIgnoreCase(status) || "en attente".equalsIgnoreCase(status);

                if (isLibre && !showLibre) return cell;
                if (isReserved && !showReserved) return cell;

                if ("annulé".equalsIgnoreCase(r.getStatut())) {
                    Label lAnnule = new Label("🚫 Bloqué");
                    lAnnule.setStyle("-fx-font-size: 10px; -fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                    cell.getChildren().add(lAnnule);
                    cell.setOnMouseClicked(e -> ouvrirDialogEdition(r));
                    return cell;
                }

                VBox rdvBox = buildRdvBox(r);
                cell.getChildren().add(rdvBox);
                StackPane.setAlignment(rdvBox, Pos.TOP_LEFT);
                StackPane.setMargin(rdvBox, new Insets(2));
                cell.setOnMouseClicked(e -> ouvrirDialogEdition(r));

                return cell;
            }
        }

        cell.setOnMouseEntered(e ->
                cell.setStyle("-fx-background-color:#dff0fa; -fx-border-color:#b0d8ef;" +
                        "-fx-border-width:0 1 1 0; -fx-cursor:hand;"));
        cell.setOnMouseExited(e ->
                cell.setStyle("-fx-background-color:" + bg +
                        "; -fx-border-color:#F0F4F8; -fx-border-width:0 1 1 0;"));


        return cell;
    }

    @FXML public void toggleFilterGreen() {
        if (showLibre && !showReserved) {

            showLibre = true;
            showReserved = true;
        } else {

            showLibre = true;
            showReserved = false;
        }
        updateLegendOpacity();
        buildCalendar();
    }

    @FXML public void toggleFilterRed() {
        if (showReserved && !showLibre) {

            showLibre = true;
            showReserved = true;
        } else {

            showLibre = false;
            showReserved = true;
        }
        updateLegendOpacity();
        buildCalendar();
    }

    private void updateLegendOpacity() {
        filterGreen.setOpacity(showLibre ? 1.0 : 0.4);
        filterRed.setOpacity(showReserved ? 1.0 : 0.4);
    }

    private VBox buildRdvBox(RendezVous r) {
        String color = switch (r.getStatut()) {
            case "réservé", "confirmé", "en attente" -> "#E74C3C";
            default         -> "#27AE60";
        };

        Label lType = new Label(capitalize(r.getTypeRdv()));
        lType.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:white;");

        Label lH = new Label(
                r.getHeureDebut().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        "–" +
                        r.getHeureFin().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        lH.setStyle("-fx-font-size:10px; -fx-text-fill:rgba(255,255,255,0.88);");

        VBox box = new VBox(2, lType, lH);
        box.setPadding(new Insets(4, 6, 4, 8));
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMaxHeight(CELL_HEIGHT - 6);
        box.setStyle("-fx-background-color:" + color +
                "; -fx-background-radius:7; -fx-cursor:hand;" +
                "-fx-border-color:rgba(0,0,0,0.1); -fx-border-radius:7; -fx-border-width:0 0 0 4;" +
                "-fx-border-color:rgba(0,0,0,0.2);");

        Tooltip tip = new Tooltip(
                r.getTypeRdv() + " · " + r.getStatut() + "\n" +
                        r.getDate() + "  " +
                        r.getHeureDebut().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        " → " +
                        r.getHeureFin().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        tip.setStyle("-fx-font-size:12px;");
        Tooltip.install(box, tip);

        return box;
    }

    private RowConstraints rowConstraint(int h) {
        RowConstraints rc = new RowConstraints();
        rc.setMinHeight(h); rc.setPrefHeight(h); rc.setMaxHeight(h);
        return rc;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }


    @FXML public void semainePrec()      { currentWeekStart = currentWeekStart.minusWeeks(1); buildCalendar(); }
    @FXML public void semaineSuiv()      { currentWeekStart = currentWeekStart.plusWeeks(1);  buildCalendar(); }
    @FXML public void semaineAujourd()   { currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY); buildCalendar(); }
    @FXML public void ouvrirDialogAjout(ActionEvent e) { ouvrirDialogAjout(LocalDate.now(), 9); }

    private void ouvrirDialogAjout(LocalDate day, int hour) {
        Dialog<RendezVous> dialog = buildFormDialog("Ajouter un créneau");
        DatePicker dp     = (DatePicker)       dialog.getDialogPane().lookup("#datePicker");
        TextField tfStart = (TextField)        dialog.getDialogPane().lookup("#heureDebut");
        TextField tfEnd   = (TextField)        dialog.getDialogPane().lookup("#heureFin");
        ComboBox  cbStat  = (ComboBox)         dialog.getDialogPane().lookup("#statut");
        ComboBox  cbType  = (ComboBox)         dialog.getDialogPane().lookup("#typeRdv");

        dp.setValue(day);
        tfStart.setText(String.format("%02d:00", hour));
        tfEnd.setText(String.format("%02d:00", Math.min(hour+1, 20)));
        cbStat.setValue("libre");
        cbType.setValue("consultation");

        dialog.showAndWait().ifPresent(rdv -> { if(rdv!=null){ service.add(rdv); buildCalendar(); updateSidebarStats(); } });
    }

    private void ouvrirDialogEdition(RendezVous existing) {
        Dialog<RendezVous> dialog = buildFormDialog(existing.getId() == -1 ? "Bloquer ce créneau" : "Modifier le créneau");
        DatePicker dp     = (DatePicker) dialog.getDialogPane().lookup("#datePicker");
        TextField tfStart = (TextField)  dialog.getDialogPane().lookup("#heureDebut");
        TextField tfEnd   = (TextField)  dialog.getDialogPane().lookup("#heureFin");
        ComboBox  cbStat  = (ComboBox)   dialog.getDialogPane().lookup("#statut");
        ComboBox  cbType  = (ComboBox)   dialog.getDialogPane().lookup("#typeRdv");

        dp.setValue(existing.getDate().toLocalDate());
        tfStart.setText(existing.getHeureDebut().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        tfEnd.setText(existing.getHeureFin().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        cbStat.setValue(existing.getStatut());
        cbType.setValue(existing.getTypeRdv());

        // Empêcher modification date/heure pour les créneaux automatiques
        dp.setDisable(true); tfStart.setDisable(true); tfEnd.setDisable(true);

        if (existing.getId() != -1) {
            ButtonType deleteType = new ButtonType("🗑  Libérer", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().add(deleteType);

            ButtonType blockType = new ButtonType("🚫 Bloquer", ButtonBar.ButtonData.OTHER);
            dialog.getDialogPane().getButtonTypes().add(blockType);
        } else {
            ButtonType blockType = new ButtonType("🚫 Bloquer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().setAll(blockType, ButtonType.CANCEL);
        }

        dialog.setResultConverter(btn -> {
            if (btn != null && btn.getButtonData() == ButtonBar.ButtonData.LEFT) {
                service.delete(existing.getId());
                return null;
            }
            if (btn != null && btn.getText().contains("Bloquer")) {
                existing.setStatut("annulé");
                return existing;
            }
            if (btn != null && btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return buildRdv(dp, tfStart, tfEnd, cbStat, cbType, existing.getId());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(rdv -> {
            if (rdv != null) {
                if (rdv.getId() == -1) {
                    service.add(rdv);
                } else {
                    service.update(rdv);
                }
            }
        });
        buildCalendar();
        updateSidebarStats();
    }

    @SuppressWarnings("unchecked")
    private Dialog<RendezVous> buildFormDialog(String title) {
        Dialog<RendezVous> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color:#F0F4F8;");
        pane.setPrefWidth(400);

        ButtonType saveBtn   = new ButtonType("✔  Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler",         ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(saveBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPadding(new Insets(22, 26, 10, 26));

        Label ttl = new Label(title);
        ttl.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1a4a5f;");
        grid.add(ttl, 0, 0, 2, 1);

        // Date
        grid.add(fl("Date"), 0, 1);
        DatePicker dp = new DatePicker(); dp.setId("datePicker"); dp.setPrefWidth(220);
        dp.setStyle("-fx-background-radius:8; -fx-border-color:#D0D9E0; -fx-border-radius:8;");
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
        cbStat.getItems().addAll("libre","réservé","confirmé");
        cbStat.setValue("libre"); cbStat.setPrefWidth(220);
        cbStat.setStyle("-fx-background-radius:8;"); grid.add(cbStat, 1, 4);

        // Type
        grid.add(fl("Type RDV"), 0, 5);
        ComboBox<String> cbType = new ComboBox<>(); cbType.setId("typeRdv");
        cbType.getItems().addAll("consultation","suivi","urgence","bilan");
        cbType.setValue("consultation"); cbType.setPrefWidth(220);
        cbType.setStyle("-fx-background-radius:8;"); grid.add(cbType, 1, 5);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                return buildRdv(dp, tfS, tfE, cbStat, cbType, -1);
            return null;
        });

        return dialog;
    }

    @SuppressWarnings("unchecked")
    private RendezVous buildRdv(DatePicker dp, TextField tfS, TextField tfE,
                                ComboBox cbStat, ComboBox cbType, int id) {
        try {
            RendezVous r = new RendezVous();
            r.setId(id);
            r.setDate(Date.valueOf(dp.getValue()));
            r.setHeureDebut(Time.valueOf(tfS.getText().trim() + ":00"));
            r.setHeureFin(Time.valueOf(tfE.getText().trim() + ":00"));
            r.setStatut((String) cbStat.getValue());
            r.setTypeRdv((String) cbType.getValue());
            if (id == -1) {
                r.setPsychologueId(psychologueId);
            } else {
                r.setPsychologueId(psychologueId);
            }

            return r;
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Format heure invalide (HH:mm)").showAndWait();
            return null;
        }
    }

    private Label fl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#5A6C7D;");
        return l;
    }

    private TextField ft(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(220);
        tf.setStyle("-fx-padding:8; -fx-background-radius:8; -fx-border-radius:8;" +
                "-fx-border-color:#D0D9E0; -fx-background-color:white;");
        return tf;
    }

    private void updateSidebarStats() {
        long count = service.getAll().stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now())).count();
        sidebarRdvToday.setText(String.valueOf(count));
    }


    private void loadPage(javafx.event.Event event, String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return;
            Parent root = FXMLLoader.load(url);
            Stage stage = null;
            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }
            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setResizable(true);
                stage.setMinWidth(900);
                stage.setMinHeight(600);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void goHome(javafx.event.Event e)              { loadPage(e, "/DashboardPsyVue.fxml"); }
    @FXML public void goVoirRendezVous(javafx.event.Event e)    { loadPage(e, "/VoirRendezVous.fxml"); }
    @FXML public void goNouveauDossier(javafx.event.Event e)    { loadPage(e, "/NouveauDossier.fxml"); }
    @FXML public void goConsulterDossiers(javafx.event.Event e) { loadPage(e, "/ConsulterDossiers.fxml"); }
    @FXML public void goCalendrier(javafx.event.Event e)        { loadPage(e, "/Calendrier.fxml"); }
    @FXML public void goActivites(javafx.event.Event e)  {}
    @FXML public void goRessources(javafx.event.Event e) {}
    @FXML public void goStats(javafx.event.Event e)      {}
    @FXML public void logout(javafx.event.Event e) {
        utils.SessionManager.getInstance().logout();
        loadPage(e, "/Login.fxml");
    }

    @FXML public void toggleRdvMenu(MouseEvent e) {
        boolean s = !submenuRdv.isVisible();
        submenuRdv.setVisible(s); submenuRdv.setManaged(s);
        arrowRdv.setText(s ? "▼" : "▶");
    }
    @FXML public void toggleDossiersMenu(MouseEvent e) {
        boolean s = !submenuDossiers.isVisible();
        submenuDossiers.setVisible(s); submenuDossiers.setManaged(s);
        arrowDossiers.setText(s ? "▼" : "▶");
    }
}