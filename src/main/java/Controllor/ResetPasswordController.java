package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import models.User;
import services.ServiceUser;
import utils.SceneManager;

public class ResetPasswordController {

    @FXML private Label         lblEmail;
    @FXML private TextField     txtCode;
    @FXML private PasswordField txtNewPass;
    @FXML private PasswordField txtConfirmPass;
    @FXML private Label         lblMessage;
    @FXML private ImageView     logoImage;

    private final ServiceUser service = new ServiceUser();
    private String expectedCode;
    private String userEmail;

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(
                    getClass().getResourceAsStream("/Images/logo.png")));
            Circle clip = new Circle(25, 25, 25);
            logoImage.setClip(clip);
        } catch (Exception ignored) {}
    }

    /**
     * Reçoit email, code et statut envoi depuis ForgotPasswordController
     */
    public void setData(String email, String code, boolean emailSent) {
        this.userEmail    = email;
        this.expectedCode = code;
        lblEmail.setText("Code envoyé à : " + maskEmail(email));

        // ✅ Informer l'utilisateur si email pas envoyé
        if (!emailSent) {
            lblMessage.setStyle("-fx-text-fill: #E67E22; -fx-font-size: 12px;");
            lblMessage.setText("⚠️ Email non envoyé (Gmail non configuré). " +
                    "Consultez la console IntelliJ pour le code.");
        } else {
            lblMessage.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12px;");
            lblMessage.setText("✅ Code envoyé ! Vérifiez votre boîte mail.");
        }
    }

    @FXML
    public void handleReset() {
        String code    = txtCode.getText().trim();
        String newPass = txtNewPass.getText();
        String confirm = txtConfirmPass.getText();

        // Validations
        if (code.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            msg("Tous les champs sont obligatoires.", false); return;
        }
        if (!code.equals(expectedCode)) {
            msg("Code incorrect. Vérifiez votre email ou la console.", false); return;
        }
        if (newPass.length() < 8) {
            msg("Minimum 8 caractères.", false); return;
        }
        if (!newPass.equals(confirm)) {
            msg("Les mots de passe ne correspondent pas.", false); return;
        }

        // Récupérer l'utilisateur et mettre à jour le mot de passe
        User user = service.getUserByEmail(userEmail);
        if (user == null) {
            msg("Utilisateur introuvable.", false); return;
        }

        boolean success = service.updateProfile(user, newPass);

        if (success) {
            msg("✅ Mot de passe réinitialisé avec succès !", true);
            // Retour au login après 2 secondes
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(
                            javafx.util.Duration.seconds(2));
            pause.setOnFinished(e -> SceneManager.goToLogin());
            pause.play();
        } else {
            msg("Erreur lors de la sauvegarde.", false);
        }
    }

    @FXML
    public void goToLogin() {
        SceneManager.goToLogin();
    }

    private void msg(String text, boolean success) {
        lblMessage.setStyle("-fx-text-fill: " +
                (success ? "#27AE60" : "#E74C3C") +
                "; -fx-font-size: 12px;");
        lblMessage.setText(text);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String local = parts[0];
        if (local.length() <= 2) return email;
        return local.charAt(0) +
                "*".repeat(local.length() - 2) +
                local.charAt(local.length() - 1) +
                "@" + parts[1];
    }
}