package Controllor;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import models.User;
import services.ServiceUser;
import utils.SceneManager;
import utils.SessionManager;

public class ControllerHomeAdmin {

    @FXML private HBox navAccueil, navSuivi, navForum, navRdv, navActivites, navContenus, navDossiers, navUtilisateurs;
    @FXML private HBox navSuiviStats, navObjectifs, navSujets, navCommentaires;
    @FXML private VBox submenuSuivi, submenuForum;
    @FXML private Label arrowSuivi, arrowForum;
    @FXML private Label statUsers, statRdvs, statTopics, statMood;
    @FXML private Label labelDate, labelUserName, avatarInitials;
    @FXML private Button notifButton, logoutButton;
    @FXML private ImageView logoImage;

    private boolean suiviOpen = false;
    private boolean forumOpen = false;
    private final ServiceUser service = new ServiceUser();

    @FXML
    public void initialize() {
        labelDate.setText(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter
                        .ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH)));

        submenuSuivi.setVisible(false); submenuSuivi.setManaged(false);
        submenuForum.setVisible(false); submenuForum.setManaged(false);
        arrowSuivi.setText(">"); arrowForum.setText(">");

        try { logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png"))); }
        catch (Exception ignored) {}

        // Infos admin connectÃ©
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            labelUserName.setText(user.getPrenom() + " " + user.getNom());
            String init = "";
            if (user.getPrenom() != null && !user.getPrenom().isEmpty()) init += user.getPrenom().charAt(0);
            if (user.getNom() != null && !user.getNom().isEmpty()) init += user.getNom().charAt(0);
            avatarInitials.setText(init.toUpperCase());
        }

        // Stats rÃ©elles depuis la base
        try {
            int totalUsers = service.getAll().size();
            statUsers.setText(String.valueOf(totalUsers));
        } catch (Exception ignored) {}
    }

    @FXML void toggleSuiviMenu(MouseEvent event) {
        suiviOpen = !suiviOpen;
        submenuSuivi.setVisible(suiviOpen); submenuSuivi.setManaged(suiviOpen);
        arrowSuivi.setText(suiviOpen ? "v" : ">");
    }

    @FXML void toggleForumMenu(MouseEvent event) {
        forumOpen = !forumOpen;
        submenuForum.setVisible(forumOpen); submenuForum.setManaged(forumOpen);
        arrowForum.setText(forumOpen ? "v" : ">");
    }

    // âœ… Navigation vers CRUD Utilisateurs
    @FXML void onNavUtilisateursClicked(MouseEvent event) {
        SceneManager.switchTo("AdminUsers.fxml", "Gestion des Utilisateurs");
    }

    @FXML void onNavHomeClicked(MouseEvent event)     { setActiveNav(navAccueil); }
    @FXML void onNavRdvClicked(MouseEvent event)      { setActiveNav(navRdv); }
    @FXML void onNavActivitesClicked(MouseEvent event){ setActiveNav(navActivites); }
    @FXML void onNavContenusClicked(MouseEvent event) { setActiveNav(navContenus); }
    @FXML void onNavDossiersClicked(MouseEvent event) { setActiveNav(navDossiers); }
    @FXML void onNavSuiviStatsClicked(MouseEvent event)  { System.out.println("Stats suivi"); }
    @FXML void onNavObjectifsClicked(MouseEvent event)   { System.out.println("Objectifs"); }
    @FXML void onNavSujetsClicked(MouseEvent event)      { System.out.println("Sujets"); }
    @FXML void onNavCommentairesClicked(MouseEvent event){ System.out.println("Commentaires"); }

    @FXML private void onNavHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        if (!src.getStyle().contains("#34495E"))
            src.setStyle("-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }
    @FXML private void onNavHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        if (!src.getStyle().contains("#34495E"))
            src.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }
    @FXML private void onSubmenuHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        src.setStyle("-fx-background-color: #34495E; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
    }
    @FXML private void onSubmenuHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        src.setStyle("-fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;");
    }

    @FXML private void onNotifications(ActionEvent event) { System.out.println("Notifications"); }

    // âœ… DÃ©connexion
    @FXML private void onLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    @FXML private void onAddUser(ActionEvent event) {
        SceneManager.switchTo("AdminUsers.fxml", "Gestion des Utilisateurs");
    }
    @FXML private void onCreateRdv(ActionEvent event)    { System.out.println("CrÃ©er RDV"); }
    @FXML private void onModerateForum(ActionEvent event){ System.out.println("ModÃ©rer forum"); }
    @FXML private void onExportData(ActionEvent event)   { System.out.println("Exporter"); }
    @FXML private void onViewStats(ActionEvent event) { SceneManager.switchTo("StatsAdmin.fxml", "Statistiques"); }

    private void setActiveNav(HBox active) {
        HBox[] all = {navAccueil, navSuivi, navForum, navRdv, navActivites, navContenus, navDossiers, navUtilisateurs};
        for (HBox n : all)
            if (n != null) n.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
        if (active != null)
            active.setStyle("-fx-background-color: #34495E; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }
}
