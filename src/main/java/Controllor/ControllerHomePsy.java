package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import models.User;
import utils.SceneManager;
import utils.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ControllerHomePsy {

    @FXML private HBox navHome, navRdv, navDossiers, navActivites, navRessources, navStats;
    @FXML private VBox submenuRdv, submenuDossiers;
    @FXML private Label arrowRdv, arrowDossiers;
    @FXML private HBox navVoirRdv, navCalendrier;
    @FXML private HBox navConsulterDossiers, navNouveauDossier;
    @FXML private Label pageTitle, currentDate;
    @FXML private Label rdvCount, dossierCount, activiteCount;
    @FXML private Label sidebarPatientsCount, sidebarRdvToday, sidebarDossiersTotal;

    // ✅ Labels nom/role dans la sidebar (à ajouter dans le fxml si manquants)
    @FXML private Label lblPsyName;
    @FXML private Label lblPsyRole;

    // ✅ Bouton 3 points
    @FXML private Button menuButton;

    @FXML private ImageView logoImage;

    private boolean rdvOpen = false;
    private boolean dossiersOpen = false;
    private ContextMenu contextMenu;

    @FXML
    public void initialize() {
        // Date
        currentDate.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)));

        // Stats par défaut
        rdvCount.setText("5");
        dossierCount.setText("20");
        activiteCount.setText("8");
        sidebarPatientsCount.setText("24");
        sidebarRdvToday.setText("5");
        sidebarDossiersTotal.setText("42");

        // Sous-menus cachés
        submenuRdv.setVisible(false);    submenuRdv.setManaged(false);
        submenuDossiers.setVisible(false); submenuDossiers.setManaged(false);
        arrowRdv.setText("▶"); arrowDossiers.setText("▶");

        // Logo
        try { logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png"))); }
        catch (Exception ignored) {}

        // ✅ Afficher le nom du psy connecté
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            if (lblPsyName != null)
                lblPsyName.setText(user.getPrenom() + " " + user.getNom());
            if (lblPsyRole != null)
                lblPsyRole.setText("Psychologue");
        }

        // ✅ Menu contextuel 3 points
        contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");

        MenuItem modifierProfil = new MenuItem("✏️  Modifier mon profil");
        modifierProfil.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        modifierProfil.setOnAction(e -> SceneManager.switchTo("Profile.fxml", "Mon Profil"));

        MenuItem deconnexion = new MenuItem("🚪  Déconnexion");
        deconnexion.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-text-fill: #E74C3C;");
        deconnexion.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneManager.goToLogin();
        });

        contextMenu.getItems().addAll(modifierProfil, deconnexion);

        setActive(navHome);
    }

    // ✅ Ouvre le menu 3 points
    @FXML
    public void onMenuClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        contextMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 5);
    }

    // ✅ Déconnexion directe (bouton existant dans le FXML)
    @FXML
    void logout(ActionEvent event) {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    @FXML void toggleRdvMenu(MouseEvent event) {
        rdvOpen = !rdvOpen;
        submenuRdv.setVisible(rdvOpen); submenuRdv.setManaged(rdvOpen);
        arrowRdv.setText(rdvOpen ? "▼" : "▶");
    }

    @FXML void toggleDossiersMenu(MouseEvent event) {
        dossiersOpen = !dossiersOpen;
        submenuDossiers.setVisible(dossiersOpen); submenuDossiers.setManaged(dossiersOpen);
        arrowDossiers.setText(dossiersOpen ? "▼" : "▶");
    }

    @FXML void goHome(MouseEvent event)         { setActive(navHome); pageTitle.setText("Tableau de bord"); }
    @FXML void goActivites(MouseEvent event)    { setActive(navActivites); pageTitle.setText("Activités"); }
    @FXML void goRessources(MouseEvent event)   { setActive(navRessources); pageTitle.setText("Ressources"); }
    @FXML void goStats(MouseEvent event)        { setActive(navStats); pageTitle.setText("Statistiques"); }
    @FXML void goVoirRendezVous(MouseEvent event){ pageTitle.setText("Liste des rendez-vous"); }
    @FXML void goCalendrier(MouseEvent event)   { pageTitle.setText("Calendrier"); }
    @FXML void goConsulterDossiers(MouseEvent event){ pageTitle.setText("Consulter dossiers"); }
    @FXML void goNouveauDossier(MouseEvent event)   { pageTitle.setText("Nouveau dossier"); }

    @FXML void showNotifications(ActionEvent event) { System.out.println("Notifications"); }

    private void setActive(HBox active) {
        HBox[] all = {navHome, navRdv, navDossiers, navActivites, navRessources, navStats};
        for (HBox h : all)
            h.setStyle("-fx-padding: 11 14; -fx-background-radius: 10; -fx-cursor: hand; -fx-background-color: transparent;");
        active.setStyle("-fx-padding: 11 14; -fx-background-radius: 10; -fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.15);");
    }
}