package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.User;
import services.ServiceUser;
import utils.SceneManager;

public class RegisterController {

    @FXML private TextField     txtPrenom;
    @FXML private TextField     txtNom;
    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private Label         lblError;

    private final ServiceUser service = new ServiceUser();

    @FXML
    public void initialize() {
        comboRole.getItems().addAll("etudiant", "psychologue");
        comboRole.setValue("etudiant");
    }

    @FXML
    public void handleRegister() {
        lblError.setText("");
        String prenom  = txtPrenom.getText().trim();
        String nom     = txtNom.getText().trim();
        String email   = txtEmail.getText().trim();
        String pass    = txtPassword.getText();
        String confirm = txtConfirmPassword.getText();
        String role    = comboRole.getValue();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            error("Tous les champs sont obligatoires."); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            error("Email invalide."); return;
        }
        if (pass.length() < 8) {
            error("Le mot de passe doit contenir au moins 8 caractÃ¨res."); return;
        }
        if (!pass.equals(confirm)) {
            error("Les mots de passe ne correspondent pas."); return;
        }

        String roleSymfony = role.equals("psychologue") ? "ROLE_PSYCHOLOGUE" : "ROLE_ETUDIANT";

        User user = new User();
        user.setPrenom(prenom);
        user.setNom(nom);
        user.setEmail(email);
        user.setMotDePasse(pass);   // hashÃ© dans service.register()
        user.setRole(role);
        user.setRoles("[\"" + roleSymfony + "\"]");

        if (!service.register(user)) {
            error("Cet email est dÃ©jÃ  utilisÃ©."); return;
        }

        SceneManager.goToLogin();
    }

    @FXML public void goToLogin() { SceneManager.goToLogin(); }

    private void error(String msg) {
        lblError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        lblError.setText(msg);
    }
}
