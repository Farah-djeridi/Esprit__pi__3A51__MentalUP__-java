package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Reservation;
import services.ServiceReservation;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationAdminController implements Initializable {

    @FXML private VBox reservationsVBox;
    @FXML private Label lblTotal;
    @FXML private Label lblAujourdhui;
    @FXML private Label lblSemaine;
    @FXML private HBox chartBarContainer;
    @FXML private VBox chartDonutContainer;
    @FXML private GridPane calendarGrid;
    @FXML private Label lblMoisAnnee;

    private ServiceReservation serviceReservation;
    private List<Reservation> allReservations = new java.util.ArrayList<>();
    private java.time.YearMonth currentMonth = java.time.YearMonth.now();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceReservation = new ServiceReservation();
        chargerReservations();
    }

    // ─── Chargement ──────────────────────────────────────────────────────────

    private void chargerReservations() {
        try {
            allReservations = serviceReservation.getAllReservations();
            reservationsVBox.getChildren().clear();
            lblTotal.setText(String.valueOf(allReservations.size()));

            // Stats
            LocalDate today = LocalDate.now();
            long nbAujourdhui = allReservations.stream()
                .filter(r -> r.getDateReservation() != null && r.getDateReservation().equals(today)).count();
            long nbSemaine = allReservations.stream()
                .filter(r -> r.getDateReservation() != null &&
                    !r.getDateReservation().isBefore(today.minusDays(7))).count();
            if (lblAujourdhui != null) lblAujourdhui.setText(String.valueOf(nbAujourdhui));
            if (lblSemaine   != null) lblSemaine.setText(String.valueOf(nbSemaine));

            // Graphiques
            if (chartBarContainer  != null) dessinerBarChart();
            if (chartDonutContainer != null) dessinerDonut();
            if (calendarGrid       != null) dessinerCalendrier();

            if (allReservations.isEmpty()) {
                Label vide = new Label("Aucune réservation trouvée");
                vide.setStyle("-fx-font-size: 14px; -fx-text-fill: #a0aec0; -fx-padding: 40;");
                vide.setMaxWidth(Double.MAX_VALUE); vide.setAlignment(Pos.CENTER);
                reservationsVBox.getChildren().add(vide);
            } else {
                for (Reservation r : allReservations)
                    reservationsVBox.getChildren().add(creerCarte(r));
            }
        } catch (SQLException e) {
            afficherToast("Erreur chargement: " + e.getMessage(), "#e53e3e", "❌");
        }
    }

    // ─── Popup toutes les réservations d'un jour ─────────────────────────────

    private void ouvrirPopupJour(LocalDate date, List<Reservation> reservations) {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Réservations du " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 4);");
        root.setPrefWidth(420);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color: #5a67d8; -fx-background-radius: 14 14 0 0;");
        Label titreHeader = new Label("📅 " + date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy",
                java.util.Locale.FRENCH)));
        titreHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label countBadge = new Label(reservations.size() + " réservation(s)");
        countBadge.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; " +
                            "-fx-font-size: 11px; -fx-padding: 3 10; -fx-background-radius: 10;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.close());
        header.getChildren().addAll(titreHeader, sp, countBadge, btnClose);

        // Liste des réservations
        VBox liste = new VBox(8);
        liste.setPadding(new Insets(16));

        for (Reservation r : reservations) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 14, 12, 14));
            row.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 10;");

            // Statut couleur
            String bColor = switch (r.getStatut()) {
                case "ACCEPTEE" -> "#38a169";
                case "REFUSEE"  -> "#e53e3e";
                default         -> "#ed8936";
            };
            Region colorBar = new Region();
            colorBar.setPrefWidth(4); colorBar.setPrefHeight(40);
            colorBar.setStyle("-fx-background-color: " + bColor + "; -fx-background-radius: 2;");

            VBox infos = new VBox(3);
            HBox.setHgrow(infos, Priority.ALWAYS);
            Label nomAct = new Label(r.getTitreActivite() != null ? r.getTitreActivite() : "—");
            nomAct.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
            Label nomEtu = new Label("👤 " + r.getNomEtudiant() + "  🪑 " + r.getPlace());
            nomEtu.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
            infos.getChildren().addAll(nomAct, nomEtu);

            // Boutons rapides
            Button btnOk = new Button("✓");
            btnOk.setStyle("-fx-background-color: " + ("ACCEPTEE".equals(r.getStatut()) ? "#38a169" : "#e2e8f0") +
                           "; -fx-text-fill: " + ("ACCEPTEE".equals(r.getStatut()) ? "white" : "#4a5568") +
                           "; -fx-font-size: 13px; -fx-pref-width: 32; -fx-pref-height: 32; " +
                           "-fx-background-radius: 50%; -fx-cursor: hand;");
            btnOk.setOnAction(e -> {
                changerStatut(r, "ACCEPTEE", null);
                popup.close();
                ouvrirPopupJour(date, allReservations.stream()
                    .filter(res -> date.equals(res.getDateReservation()))
                    .collect(java.util.stream.Collectors.toList()));
            });

            Button btnNon = new Button("✗");
            btnNon.setStyle("-fx-background-color: " + ("REFUSEE".equals(r.getStatut()) ? "#e53e3e" : "#e2e8f0") +
                            "; -fx-text-fill: " + ("REFUSEE".equals(r.getStatut()) ? "white" : "#4a5568") +
                            "; -fx-font-size: 13px; -fx-pref-width: 32; -fx-pref-height: 32; " +
                            "-fx-background-radius: 50%; -fx-cursor: hand;");
            btnNon.setOnAction(e -> {
                changerStatut(r, "REFUSEE", null);
                popup.close();
                ouvrirPopupJour(date, allReservations.stream()
                    .filter(res -> date.equals(res.getDateReservation()))
                    .collect(java.util.stream.Collectors.toList()));
            });

            row.getChildren().addAll(colorBar, infos, btnOk, btnNon);
            liste.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(liste);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(350);
        scroll.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        root.getChildren().addAll(header, scroll);
        popup.setScene(new javafx.scene.Scene(root, 440, 420));
        popup.showAndWait();
    }

    // ─── Popup Validation réservation ────────────────────────────────────────

    private void ouvrirPopupValidation(Reservation r) {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 5);");
        root.setPrefWidth(380);

        // Icône statut actuel
        String statutActuel = r.getStatut();
        String iconeStatut = switch (statutActuel) {
            case "ACCEPTEE" -> "✅";
            case "REFUSEE"  -> "❌";
            default         -> "⏳";
        };

        Label icone = new Label(iconeStatut);
        icone.setStyle("-fx-font-size: 40px;");

        Label titre = new Label("Réservation #" + r.getIdReservation());
        titre.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // Infos
        VBox infos = new VBox(8);
        infos.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 10; -fx-padding: 14;");
        infos.setMaxWidth(Double.MAX_VALUE);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        infos.getChildren().addAll(
            infoRow("🎭 Activité",    r.getTitreActivite() != null ? r.getTitreActivite() : "—"),
            infoRow("👤 Étudiant",    r.getNomEtudiant()),
            infoRow("🪑 Place",       r.getPlace()),
            infoRow("📅 Date",        r.getDateReservation() != null ? r.getDateReservation().format(fmt) : "—"),
            infoRow("📌 Statut",      statutActuel)
        );

        // Boutons
        Button btnAccepter = new Button("✅  Accepter");
        btnAccepter.setMaxWidth(Double.MAX_VALUE);
        btnAccepter.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-font-size: 14px; " +
                             "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAccepter.setDisable("ACCEPTEE".equals(statutActuel));
        btnAccepter.setOnAction(e -> {
            changerStatut(r, "ACCEPTEE", popup);
        });

        Button btnRefuser = new Button("❌  Refuser");
        btnRefuser.setMaxWidth(Double.MAX_VALUE);
        btnRefuser.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 14px; " +
                            "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: hand;");
        btnRefuser.setDisable("REFUSEE".equals(statutActuel));
        btnRefuser.setOnAction(e -> {
            changerStatut(r, "REFUSEE", popup);
        });

        Button btnAttente = new Button("⏳  Remettre en attente");
        btnAttente.setMaxWidth(Double.MAX_VALUE);
        btnAttente.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAttente.setDisable("EN_ATTENTE".equals(statutActuel));
        btnAttente.setOnAction(e -> changerStatut(r, "EN_ATTENTE", popup));

        Button btnFermer = new Button("Fermer");
        btnFermer.setMaxWidth(Double.MAX_VALUE);
        btnFermer.setStyle("-fx-background-color: #edf2f7; -fx-text-fill: #718096; -fx-font-size: 13px; " +
                           "-fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> popup.close());

        HBox actionBtns = new HBox(10, btnAccepter, btnRefuser);
        HBox.setHgrow(btnAccepter, Priority.ALWAYS);
        HBox.setHgrow(btnRefuser, Priority.ALWAYS);

        root.getChildren().addAll(icone, titre, infos, actionBtns, btnAttente, btnFermer);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popup.setScene(scene);
        popup.show();

        // Centrer
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        popup.setX(screen.getWidth() / 2 - 190);
        popup.setY(screen.getHeight() / 2 - 200);
    }

    private void changerStatut(Reservation r, String nouveauStatut, Stage popup) {
        try {
            serviceReservation.mettreAJourStatut(r.getIdReservation(), nouveauStatut);
            r.setStatut(nouveauStatut);
            if (popup != null) popup.close();
            chargerReservations();
            String msg = switch (nouveauStatut) {
                case "ACCEPTEE"   -> "Réservation acceptée ✅";
                case "REFUSEE"    -> "Réservation refusée ❌";
                default           -> "Statut mis à jour";
            };
            String couleur = switch (nouveauStatut) {
                case "ACCEPTEE" -> "#38a169";
                case "REFUSEE"  -> "#e53e3e";
                default         -> "#718096";
            };
            afficherToast(msg, couleur, nouveauStatut.equals("ACCEPTEE") ? "✅" : "❌");
        } catch (SQLException ex) {
            afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌");
        }
    }

    private HBox infoRow(String label, String valeur) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096; -fx-min-width: 100;");
        Label val = new Label(valeur != null ? valeur : "—");
        val.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    // ─── Navigation mois ─────────────────────────────────────────────────────

    @FXML
    private void moisPrecedent() {
        currentMonth = currentMonth.minusMonths(1);
        dessinerCalendrier();
    }

    @FXML
    private void moisSuivant() {
        currentMonth = currentMonth.plusMonths(1);
        dessinerCalendrier();
    }

    // ─── Calendrier ───────────────────────────────────────────────────────────

    private void dessinerCalendrier() {
        calendarGrid.getChildren().clear();

        String[] moisNoms = {"Janvier","Février","Mars","Avril","Mai","Juin",
                             "Juillet","Août","Septembre","Octobre","Novembre","Décembre"};
        lblMoisAnnee.setText(moisNoms[currentMonth.getMonthValue() - 1] + " " + currentMonth.getYear());

        // ── En-têtes jours ────────────────────────────────────────────────────
        String[] jours = {"LUN","MAR","MER","JEU","VEN","SAM","DIM"};
        for (int i = 0; i < 7; i++) {
            Label h = new Label(jours[i]);
            h.setMaxWidth(Double.MAX_VALUE);
            h.setAlignment(Pos.CENTER);
            boolean weekend = (i >= 5);
            h.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; " +
                       "-fx-text-fill: " + (weekend ? "#e53e3e" : "#a0aec0") + "; " +
                       "-fx-background-color: white; -fx-padding: 10 0 8 0; " +
                       "-fx-border-color: transparent transparent #f0f4f8 transparent; " +
                       "-fx-border-width: 0 0 2 0;");
            calendarGrid.add(h, i, 0);
        }

        // ── Grouper réservations par date ─────────────────────────────────────
        java.util.Map<LocalDate, List<Reservation>> parDate = new java.util.HashMap<>();
        for (Reservation r : allReservations) {
            if (r.getDateReservation() != null)
                parDate.computeIfAbsent(r.getDateReservation(),
                        k -> new java.util.ArrayList<>()).add(r);
        }

        LocalDate premier = currentMonth.atDay(1);
        int debutCol = premier.getDayOfWeek().getValue() - 1;
        int nbJours  = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        // Cellules vides avant le 1er
        for (int i = 0; i < debutCol; i++) {
            VBox empty = new VBox();
            empty.setMinHeight(65);
            empty.setPadding(new Insets(7, 8, 6, 8));
            empty.setStyle("-fx-background-color: #fafbfc;");
            calendarGrid.add(empty, i, 1);
        }

        int col = debutCol, row = 1;
        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate date = currentMonth.atDay(jour);
            List<Reservation> resJour = parDate.getOrDefault(date, List.of());
            boolean isToday   = date.equals(today);
            boolean isWeekend = date.getDayOfWeek().getValue() >= 6;
            boolean hasRes    = !resJour.isEmpty();

            VBox cell = new VBox(4);
            cell.setMinHeight(65);
            cell.setPadding(new Insets(7, 8, 6, 8));
            cell.setMaxWidth(Double.MAX_VALUE);

            // Style cellule
            String bgColor = isToday ? "#eff6ff"
                           : isWeekend ? "#fef9f9"
                           : "white";
            String leftBorder = hasRes ? "-fx-border-color: #5a67d8 transparent transparent transparent; -fx-border-width: 3 0 0 0;"
                                       : isToday ? "-fx-border-color: #4299e1 transparent transparent transparent; -fx-border-width: 3 0 0 0;"
                                       : "";
            cell.setStyle("-fx-background-color: " + bgColor + "; " + leftBorder);

            // ── Numéro du jour ────────────────────────────────────────────────
            HBox numRow = new HBox();
            numRow.setAlignment(Pos.CENTER_LEFT);

            Label numLbl = new Label(String.valueOf(jour));
            if (isToday) {
                numLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; " +
                                "-fx-background-color: #4299e1; -fx-background-radius: 50%; " +
                                "-fx-min-width: 24; -fx-min-height: 24; -fx-max-width: 24; -fx-max-height: 24; " +
                                "-fx-alignment: CENTER;");
            } else {
                numLbl.setStyle("-fx-font-size: 12px; " +
                                "-fx-font-weight: " + (hasRes ? "bold" : "normal") + "; " +
                                "-fx-text-fill: " + (isWeekend ? "#e53e3e" : hasRes ? "#2d3748" : "#718096") + ";");
            }

            // Point indicateur si réservations
            if (hasRes && !isToday) {
                numRow.getChildren().add(numLbl);
                // Petit point sous le numéro
                Region dot = new Region();
                dot.setPrefSize(4, 4);
                dot.setStyle("-fx-background-color: #5a67d8; -fx-background-radius: 50%;");
                VBox numCol = new VBox(2);
                numCol.setAlignment(Pos.CENTER);
                numCol.getChildren().addAll(numLbl, dot);
                numRow.getChildren().clear();
                numRow.getChildren().add(numCol);
            } else {
                numRow.getChildren().add(numLbl);
            }
            cell.getChildren().add(numRow);

            // ── Bouton réservations ───────────────────────────────────────────
            if (!resJour.isEmpty()) {
                // Compter par statut
                long nbAttente  = resJour.stream().filter(r -> "EN_ATTENTE".equals(r.getStatut())).count();
                long nbAcceptee = resJour.stream().filter(r -> "ACCEPTEE".equals(r.getStatut())).count();
                long nbRefusee  = resJour.stream().filter(r -> "REFUSEE".equals(r.getStatut())).count();

                // Couleur dominante
                String btnColor = nbAttente > 0 ? "#5a67d8"
                                : nbAcceptee > 0 ? "#38a169"
                                : "#e53e3e";

                Button btnRes = new Button(resJour.size() + " réservation" + (resJour.size() > 1 ? "s" : ""));
                btnRes.setMaxWidth(Double.MAX_VALUE);
                btnRes.setStyle("-fx-background-color: " + btnColor + "; -fx-text-fill: white; " +
                                "-fx-font-size: 9px; -fx-font-weight: bold; " +
                                "-fx-padding: 3 6; -fx-background-radius: 5; -fx-cursor: hand;");
                btnRes.setOnMouseEntered(e -> btnRes.setOpacity(0.85));
                btnRes.setOnMouseExited(e  -> btnRes.setOpacity(1.0));
                final LocalDate d = date;
                final List<Reservation> rj = resJour;
                btnRes.setOnAction(e -> ouvrirPopupJour(d, rj));
                cell.getChildren().add(btnRes);
            }

            // Hover effect
            cell.setOnMouseEntered(e -> cell.setStyle(
                    "-fx-background-color: " + (isToday ? "#dbeafe" : "#f0f4ff") + "; " +
                    "-fx-cursor: hand; " + leftBorder));
            cell.setOnMouseExited(e -> cell.setStyle(
                    "-fx-background-color: " + bgColor + "; " + leftBorder));

            calendarGrid.add(cell, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }

        // Cellules vides après le dernier jour
        while (col > 0 && col < 7) {
            VBox empty = new VBox();
            empty.setMinHeight(65);
            empty.setPadding(new Insets(7, 8, 6, 8));
            empty.setStyle("-fx-background-color: #fafbfc;");
            calendarGrid.add(empty, col, row);
            col++;
        }
    }

    // ─── Bar Chart (réservations par activité) ────────────────────────────────

    private void dessinerBarChart() {
        chartBarContainer.getChildren().clear();
        chartBarContainer.setAlignment(Pos.BOTTOM_LEFT);
        chartBarContainer.setSpacing(8);
        chartBarContainer.setPadding(new Insets(10, 10, 10, 10));

        // Compter par activité
        java.util.Map<String, Long> counts = allReservations.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                r -> r.getTitreActivite() != null ? r.getTitreActivite() : "?",
                java.util.stream.Collectors.counting()));

        if (counts.isEmpty()) return;
        long max = counts.values().stream().mapToLong(v -> v).max().orElse(1);
        double maxH = 120.0;

        String[] colors = {"#4299e1","#48bb78","#ed8936","#9f7aea","#f56565","#38b2ac"};
        int i = 0;
        for (java.util.Map.Entry<String, Long> e : counts.entrySet()) {
            double h = (e.getValue() / (double) max) * maxH;
            VBox col = new VBox(4);
            col.setAlignment(Pos.BOTTOM_CENTER);
            col.setPrefWidth(50);

            Label valLbl = new Label(String.valueOf(e.getValue()));
            valLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

            Region bar = new Region();
            bar.setPrefWidth(36); bar.setPrefHeight(h);
            String color = colors[i % colors.length];
            bar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4 4 0 0;");

            String titre = e.getKey().length() > 8 ? e.getKey().substring(0, 8) + "…" : e.getKey();
            Label nameLbl = new Label(titre);
            nameLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #718096;");
            nameLbl.setWrapText(true); nameLbl.setMaxWidth(50);
            nameLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            col.getChildren().addAll(valLbl, bar, nameLbl);
            chartBarContainer.getChildren().add(col);
            i++;
        }
    }

    // ─── Donut Chart (répartition par étudiant) ───────────────────────────────

    private void dessinerDonut() {
        chartDonutContainer.getChildren().clear();

        java.util.Map<String, Long> counts = allReservations.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                r -> r.getNomEtudiant() != null ? r.getNomEtudiant() : "?",
                java.util.stream.Collectors.counting()));

        if (counts.isEmpty()) return;
        long total = allReservations.size();

        String[] colors = {"#48bb78","#4299e1","#ed8936","#9f7aea","#f56565","#38b2ac"};

        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(160, 160);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        double startAngle = 0;
        int ci = 0;
        java.util.List<double[]> segments = new java.util.ArrayList<>();
        for (long v : counts.values()) {
            double sweep = (v / (double) total) * 360.0;
            segments.add(new double[]{startAngle, sweep, ci});
            startAngle += sweep; ci++;
        }

        // Dessiner les segments
        ci = 0;
        for (double[] seg : segments) {
            gc.setFill(javafx.scene.paint.Color.web(colors[(int)seg[2] % colors.length]));
            gc.fillArc(10, 10, 140, 140, seg[0], seg[1], javafx.scene.shape.ArcType.ROUND);
        }
        // Trou central (donut)
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillOval(40, 40, 80, 80);

        // Légende
        VBox legende = new VBox(6);
        ci = 0;
        for (java.util.Map.Entry<String, Long> e : counts.entrySet()) {
            double pct = (e.getValue() / (double) total) * 100;
            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER_LEFT);
            Region dot = new Region();
            dot.setPrefSize(10, 10);
            dot.setStyle("-fx-background-color: " + colors[ci % colors.length] +
                         "; -fx-background-radius: 50%;");
            String nom = e.getKey().length() > 12 ? e.getKey().substring(0, 12) + "…" : e.getKey();
            Label lbl = new Label(nom + ": " + e.getValue() + " (" + String.format("%.1f", pct) + "%)");
            lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #4a5568;");
            row.getChildren().addAll(dot, lbl);
            legende.getChildren().add(row);
            ci++;
        }

        HBox donutRow = new HBox(20, canvas, legende);
        donutRow.setAlignment(Pos.CENTER_LEFT);
        chartDonutContainer.getChildren().add(donutRow);
    }

    // ─── Carte réservation ───────────────────────────────────────────────────

    private HBox creerCarte(Reservation r) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");

        // Badge ID
        Label idLbl = new Label("#" + r.getIdReservation());
        idLbl.setMinWidth(50);
        idLbl.setAlignment(Pos.CENTER);
        idLbl.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; " +
                       "-fx-font-size: 13px; -fx-font-weight: bold; " +
                       "-fx-padding: 6 12; -fx-background-radius: 20;");

        // Activité
        VBox vbActivite = new VBox(3);
        vbActivite.setMinWidth(180);
        Label lblActiviteTitre = new Label(r.getTitreActivite() != null ? r.getTitreActivite() : "—");
        lblActiviteTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label lblActiviteSub = new Label("Activité");
        lblActiviteSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        vbActivite.getChildren().addAll(lblActiviteSub, lblActiviteTitre);

        // Étudiant
        VBox vbEtudiant = new VBox(3);
        vbEtudiant.setMinWidth(150);
        Label lblEtudiantSub = new Label("Étudiant");
        lblEtudiantSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        Label lblEtudiantVal = new Label("👤 " + (r.getNomEtudiant() != null ? r.getNomEtudiant() : "—"));
        lblEtudiantVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748;");
        vbEtudiant.getChildren().addAll(lblEtudiantSub, lblEtudiantVal);

        // Place
        VBox vbPlace = new VBox(3);
        vbPlace.setMinWidth(80);
        Label lblPlaceSub = new Label("Place");
        lblPlaceSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        Label lblPlaceVal = new Label(r.getPlace() != null ? r.getPlace() : "—");
        lblPlaceVal.setStyle("-fx-background-color: #e8f4f8; -fx-text-fill: #2d3748; " +
                             "-fx-font-size: 14px; -fx-font-weight: bold; " +
                             "-fx-padding: 4 12; -fx-background-radius: 8;");
        vbPlace.getChildren().addAll(lblPlaceSub, lblPlaceVal);

        // Date
        VBox vbDate = new VBox(3);
        vbDate.setMinWidth(130);
        Label lblDateSub = new Label("Date réservation");
        lblDateSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label lblDateVal = new Label(r.getDateReservation() != null ? "📅 " + r.getDateReservation().format(fmt) : "—");
        lblDateVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");
        vbDate.getChildren().addAll(lblDateSub, lblDateVal);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge statut
        String statut = r.getStatut();
        String statutColor = switch (statut) {
            case "ACCEPTEE" -> "#38a169";
            case "REFUSEE"  -> "#e53e3e";
            default         -> "#ed8936";
        };
        String statutText = switch (statut) {
            case "ACCEPTEE" -> "✅ Acceptée";
            case "REFUSEE"  -> "❌ Refusée";
            default         -> "⏳ En attente";
        };
        Label lblStatut = new Label(statutText);
        lblStatut.setStyle("-fx-background-color: " + statutColor + "22; -fx-text-fill: " + statutColor + "; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 20;");

        // Boutons
        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 12px; " +
                         "-fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;");
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnEdit.setOnMouseExited(e  -> btnEdit.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnEdit.setOnAction(e -> ouvrirModification(r));
        btnEdit.setMinWidth(110);

        Button btnDel = new Button("🗑 Supprimer");
        btnDel.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 12px; " +
                        "-fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;");
        btnDel.setOnMouseEntered(e -> btnDel.setStyle("-fx-background-color: #c53030; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnDel.setOnMouseExited(e  -> btnDel.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-background-radius: 20; -fx-cursor: hand;"));
        btnDel.setOnAction(e -> afficherConfirmationSuppression(r));
        btnDel.setMinWidth(110);

        HBox actions = new HBox(10, btnEdit, btnDel);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(idLbl, vbActivite, vbEtudiant, vbPlace, vbDate, spacer, lblStatut, actions);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.13), 12, 0, 0, 4);"));
        card.setOnMouseExited(e  -> card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);"));

        return card;
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    @FXML
    private void ouvrirActivites() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GestionActivite.fxml"));
            Stage stage = (Stage) reservationsVBox.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Gestion des Activités - MentalUp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── Popup Modification ──────────────────────────────────────────────────

    private void ouvrirModification(Reservation r) {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Modifier la réservation #" + r.getIdReservation());
        popup.setResizable(false);

        final String nomInitial   = r.getNomEtudiant();
        final String placeInitial = r.getPlace();
        final java.time.LocalDate dateInitiale = r.getDateReservation();

        VBox root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titreLabel = new Label("✏  Modifier la réservation  #" + r.getIdReservation());
        titreLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        root.getChildren().add(titreLabel);

        // Carte blanche
        VBox card = new VBox(16);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        VBox boxNom   = fieldBox("👤  Nom de l'étudiant", r.getNomEtudiant());
        VBox boxPlace = fieldBox("🪑  Place (ex: A1, H8)", r.getPlace());
        TextField tfNom   = (TextField) boxNom.getChildren().get(1);
        TextField tfPlace = (TextField) boxPlace.getChildren().get(1);

        VBox boxDate = new VBox(6);
        Label lblDate = new Label("📅  Date de réservation");
        lblDate.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        DatePicker dpDate = new DatePicker(r.getDateReservation());
        dpDate.setMaxWidth(Double.MAX_VALUE);
        dpDate.setStyle("-fx-font-size: 13px; -fx-background-color: white;");
        boxDate.getChildren().addAll(lblDate, dpDate);

        card.getChildren().addAll(boxNom, boxPlace, boxDate);
        root.getChildren().add(card);

        // Boutons
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setMaxWidth(Double.MAX_VALUE);
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 13; -fx-cursor: hand; -fx-background-radius: 10;");
        btnAnnuler.setOnAction(e -> popup.close());

        Button btnModifier = new Button("✅  Modifier");
        btnModifier.setMaxWidth(Double.MAX_VALUE);
        btnModifier.setStyle("-fx-background-color: #2d3748; -fx-text-fill: white; -fx-font-size: 13px; " +
                             "-fx-font-weight: bold; -fx-padding: 13; -fx-cursor: hand; -fx-background-radius: 10;");
        btnModifier.setOnAction(e -> {
            if (tfNom.getText().trim().isEmpty() || tfPlace.getText().trim().isEmpty() || dpDate.getValue() == null) {
                afficherToast("Tous les champs sont obligatoires!", "#ed8936", "⚠️"); return;
            }
            boolean aucun = tfNom.getText().trim().equals(nomInitial)
                    && tfPlace.getText().trim().toUpperCase().equals(placeInitial)
                    && dpDate.getValue().equals(dateInitiale);
            if (aucun) { afficherToast("Aucune modification détectée!", "#ed8936", "⚠️"); return; }
            try {
                r.setNomEtudiant(tfNom.getText().trim());
                r.setPlace(tfPlace.getText().trim().toUpperCase());
                r.setDateReservation(dpDate.getValue());
                serviceReservation.modifierReservation(r);
                popup.close();
                chargerReservations();
                afficherToast("Réservation modifiée avec succès!", "#27ae60", "✅");
            } catch (SQLException ex) {
                afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌");
            }
        });

        HBox btns = new HBox(12, btnAnnuler, btnModifier);
        HBox.setHgrow(btnAnnuler, Priority.ALWAYS);
        HBox.setHgrow(btnModifier, Priority.ALWAYS);
        root.getChildren().add(btns);

        popup.setScene(new Scene(root, 440, 420));
        popup.showAndWait();
    }

    // ─── Popup Suppression ───────────────────────────────────────────────────

    private void afficherConfirmationSuppression(Reservation r) {
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Confirmer la suppression");
        popup.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #f5f7fa;");

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        Label icone = new Label("🗑");
        icone.setStyle("-fx-font-size: 42px;");
        Label titre = new Label("Supprimer la réservation");
        titre.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label msg = new Label("Voulez-vous vraiment supprimer\nla réservation #" + r.getIdReservation() + " ?");
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; -fx-text-alignment: center;");
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 11 35; -fx-cursor: hand; -fx-background-radius: 10;");
        btnAnnuler.setOnAction(e -> popup.close());

        Button btnSupp = new Button("🗑  Supprimer");
        btnSupp.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 13px; " +
                         "-fx-font-weight: bold; -fx-padding: 11 35; -fx-cursor: hand; -fx-background-radius: 10;");
        btnSupp.setOnAction(e -> {
            try {
                serviceReservation.supprimerReservation(r.getIdReservation());
                popup.close();
                chargerReservations();
                afficherToast("Réservation #" + r.getIdReservation() + " supprimée!", "#e53e3e", "🗑");
            } catch (SQLException ex) {
                popup.close();
                afficherToast("Erreur: " + ex.getMessage(), "#e53e3e", "❌");
            }
        });

        HBox btns = new HBox(15, btnAnnuler, btnSupp);
        btns.setAlignment(Pos.CENTER);
        card.getChildren().addAll(icone, titre, msg, btns);
        root.getChildren().add(card);

        popup.setScene(new Scene(root, 400, 300));
        popup.showAndWait();
    }

    // ─── Toast ───────────────────────────────────────────────────────────────

    private void afficherToast(String message, String couleur, String icone) {
        Stage toast = new Stage();
        toast.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        toast.setAlwaysOnTop(true);

        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        Label ico = new Label(icone); ico.setStyle("-fx-font-size: 18px;");
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        lbl.setMaxWidth(300); lbl.setWrapText(true);
        box.getChildren().addAll(ico, lbl);

        Scene scene = new Scene(box);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        toast.setScene(scene);
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        toast.setX(screen.getMaxX() - 380);
        toast.setY(screen.getMaxY() - 100);
        toast.show();

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), box);
        fade.setDelay(javafx.util.Duration.millis(2000));
        fade.setFromValue(1.0); fade.setToValue(0.0);
        fade.setOnFinished(e -> toast.close());
        fade.play();
    }

    // ─── Utilitaires ─────────────────────────────────────────────────────────

    private VBox fieldBox(String labelText, String value) {
        VBox box = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");
        TextField tf = new TextField(value == null ? "" : value);
        tf.setStyle("-fx-padding: 11; -fx-font-size: 13px; -fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: white;");
        tf.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(lbl, tf);
        return box;
    }
}
