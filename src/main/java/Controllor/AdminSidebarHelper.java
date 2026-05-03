package Controllor;

import utils.SceneManager;
import utils.SessionManager;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;

public class AdminSidebarHelper {

    public static void goToAccueil()       { SceneManager.switchTo("HomeAdmin.fxml",        "Dashboard Admin"); }
    public static void goToSuiviMental()   { SceneManager.switchTo("AdminSuiviMental.fxml", "Suivis Mentaux"); }
    public static void goToObjectifs()     { SceneManager.switchTo("AdminObjectif.fxml",    "Objectifs"); }
    public static void goToForum()         { SceneManager.switchTo("AdminSujet.fxml",       "Forum - Sujets"); }
    public static void goToCommentaires()  { SceneManager.switchTo("AdminCommentaire.fxml", "Forum - Commentaires"); }
    public static void goToRendezVous()    { SceneManager.switchTo("AdminRdv.fxml",         "Gestion des Rendez-vous"); }
    public static void goToUtilisateurs()  { SceneManager.switchTo("AdminUsers.fxml",       "Gestion des Utilisateurs"); }
    public static void goToDossiers()      { SceneManager.switchTo("AdminDossiers.fxml",    "Dossiers Medicaux"); }
    public static void goToContenus()      { SceneManager.switchTo("AdminRessources.fxml",  "Gestion des Ressources"); }
    public static void goToActivites()     { SceneManager.switchTo("GestionActivite.fxml",  "Gestion des Activites"); }
    public static void goToReservations()  { SceneManager.switchTo("GestionReservations.fxml", "Gestion des Reservations"); }
    public static void goToStats()         { SceneManager.switchTo("StatsAdmin.fxml",       "Statistiques"); }
    public static void logout() {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    /** Appeler depuis initialize() de chaque controller pour cacher les sous-menus */
    public static void initSubmenus(VBox submenuSuivi, VBox submenuForum) {
        if (submenuSuivi != null) { submenuSuivi.setVisible(false); submenuSuivi.setManaged(false); }
        if (submenuForum != null) { submenuForum.setVisible(false); submenuForum.setManaged(false); }
    }

    public static void toggleSubmenu(VBox submenu, Label arrow) {
        if (submenu == null) return;
        boolean open = submenu.isVisible();
        submenu.setVisible(!open); submenu.setManaged(!open);
        if (arrow != null) arrow.setText(open ? ">" : "v");
    }

    public static void navHoverEnter(MouseEvent e) {
        javafx.scene.layout.HBox src = (javafx.scene.layout.HBox) e.getSource();
        src.setStyle(src.getStyle().replace("transparent", "rgba(52,73,94,0.5)"));
    }
    public static void navHoverExit(MouseEvent e) {
        javafx.scene.layout.HBox src = (javafx.scene.layout.HBox) e.getSource();
        src.setStyle(src.getStyle().replace("rgba(52,73,94,0.5)", "transparent"));
    }
}

