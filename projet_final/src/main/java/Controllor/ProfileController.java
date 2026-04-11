package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.User;
import services.ServiceUser;
import utils.PasswordUtils;
import utils.SceneManager;
import utils.SessionManager;

public class ProfileController {

    @FXML private TextField     txtPrenom;
    @FXML private TextField     txtNom;
    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label         lblRole;
    @FXML private Label         lblMessage;

    private final ServiceUser service = new ServiceUser();

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { SceneManager.goToLogin(); return; }

        txtPrenom.setText(user.getPrenom());
        txtNom.setText(user.getNom());
        txtEmail.setText(user.getEmail());
        lblRole.setText("Connecté en tant que : " + (user.getRole() != null ? user.getRole() : "utilisateur"));
    }

    @FXML
    public void handleSave() {
        lblMessage.setText("");
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { SceneManager.goToLogin(); return; }

        String prenom  = txtPrenom.getText().trim();
        String nom     = txtNom.getText().trim();
        String email   = txtEmail.getText().trim();
        String current = txtCurrentPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirm = txtConfirmPassword.getText();

        // Validations
        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty()) {
            msg("Prénom, nom et email sont obligatoires.", false); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            msg("Email invalide.", false); return;
        }
        if (!email.equals(user.getEmail()) && service.emailExistsForOther(email, user.getId())) {
            msg("Cet email est déjà utilisé par un autre compte.", false); return;
        }

        // Changement de mot de passe (optionnel)
        String newPasswordToSave = null;
        if (!newPass.isEmpty()) {
            if (current.isEmpty()) {
                msg("Saisissez votre mot de passe actuel pour le changer.", false); return;
            }
            if (!PasswordUtils.verifyPassword(current, user.getMotDePasse())) {
                msg("Mot de passe actuel incorrect.", false); return;
            }
            if (!newPass.equals(confirm)) {
                msg("Les nouveaux mots de passe ne correspondent pas.", false); return;
            }
            if (newPass.length() < 8) {
                msg("Le nouveau mot de passe doit contenir au moins 8 caractères.", false); return;
            }
            newPasswordToSave = newPass;
        }

        user.setPrenom(prenom);
        user.setNom(nom);
        user.setEmail(email);

        if (service.updateProfile(user, newPasswordToSave)) {
            SessionManager.getInstance().setCurrentUser(user);
            msg("Profil modifié avec succès !", true);
            txtCurrentPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
        } else {
            msg("Erreur lors de la sauvegarde.", false);
        }
    }

    @FXML
    public void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    private void msg(String text, boolean success) {
        lblMessage.setStyle("-fx-text-fill: " + (success ? "#27ae60" : "#e74c3c") + "; -fx-font-size: 12px;");
        lblMessage.setText(text);
    }
}
