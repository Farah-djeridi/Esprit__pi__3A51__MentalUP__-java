package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchTo(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/" + fxmlFile));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur chargement FXML : " + fxmlFile + " -> " + e.getMessage());
        }
    }

    public static void goToLogin()    { switchTo("Login.fxml",    "Connexion — MentalUp"); }
    public static void goToRegister() { switchTo("Register.fxml", "Creer un compte"); }
    public static void goToProfile()  { switchTo("Profile.fxml",  "Mon Profil"); }

    /** Redirige vers la page d'accueil selon le role de l'utilisateur connecte */
    public static void goToHome() {
        models.User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            goToLogin();
            return;
        }
        String role = user.getRole() != null ? user.getRole().toLowerCase() : "";
        switch (role) {
            case "admin":
                switchTo("HomeAdmin.fxml", "Administration — MentalUp");
                break;
            case "psychologue":
                switchTo("homepsy.fxml", "Espace Psychologue — MentalUp");
                break;
            default: // etudiant
                switchTo("home.fxml", "Accueil — MentalUp");
                break;
        }
    }
}
