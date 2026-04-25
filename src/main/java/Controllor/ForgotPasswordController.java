package Controllor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import models.User;
import services.ServiceUser;
import utils.EmailService;
import utils.SceneManager;

public class ForgotPasswordController {

    @FXML private TextField txtEmail;
    @FXML private Label     lblMessage;
    @FXML private ImageView logoImage;

    private final ServiceUser service = new ServiceUser();

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(
                    getClass().getResourceAsStream("/Images/logo.png")));
            Circle clip = new Circle(25, 25, 25);
            logoImage.setClip(clip);
        } catch (Exception ignored) {}
    }

    @FXML
    public void handleSend() {
        lblMessage.setText("");

        String email = txtEmail.getText().trim();

        // Validations
        if (email.isEmpty()) {
            msg("Veuillez saisir votre email.", false); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            msg("Email invalide.", false); return;
        }

        // Vérifier que l'email existe en base
        User user = service.getUserByEmail(email);
        if (user == null) {
            // Pour la sécurité on affiche le même message
            msg("Si cet email existe, un code vous sera envoyé.", true);
            return;
        }

        // Générer le code
        String code = EmailService.generateCode();

        // ✅ Afficher le code dans la console pour le test
        System.out.println("=============================");
        System.out.println("CODE RESET : " + code);
        System.out.println("Pour : " + email);
        System.out.println("=============================");

        msg("Envoi en cours...", true);

        // Essayer d'envoyer l'email dans un thread séparé
        String prenom = user.getPrenom() != null ? user.getPrenom() : "utilisateur";

        new Thread(() -> {
            // Tentative d'envoi email
            boolean sent = false;
            try {
                sent = EmailService.sendResetCode(email, prenom, code);
            } catch (Exception e) {
                System.err.println("Erreur email : " + e.getMessage());
            }

            final boolean emailSent = sent;

            javafx.application.Platform.runLater(() -> {
                // ✅ Que l'email soit envoyé ou non,
                //    on navigue quand même vers la page reset
                //    (en mode test, le code s'affiche dans la console)
                goToResetPage(email, code, emailSent);
            });

        }).start();
    }

    private void goToResetPage(String email, String code, boolean emailSent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ResetPassword.fxml"));
            Parent root = loader.load();

            ResetPasswordController ctrl = loader.getController();
            ctrl.setData(email, code, emailSent);

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réinitialisation — MentalUp");

        } catch (Exception e) {
            e.printStackTrace();
            msg("Erreur navigation : " + e.getMessage(), false);
        }
    }

    @FXML
    public void goToLogin() {
        SceneManager.goToLogin();
    }

    private void msg(String text, boolean success) {
        lblMessage.setStyle("-fx-text-fill: " +
                (success ? "#27AE60" : "#E74C3C") +
                "; -fx-font-size: 12px; -fx-font-weight: 500;");
        lblMessage.setText(text);
    }
}