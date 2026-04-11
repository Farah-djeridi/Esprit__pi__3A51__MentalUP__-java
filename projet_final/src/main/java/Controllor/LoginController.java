package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import models.User;
import services.ServiceUser;
import utils.SceneManager;
import utils.SessionManager;

public class LoginController {

    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    private final ServiceUser service = new ServiceUser();

    @FXML
    public void handleLogin() {
        lblError.setText("");
        String email    = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            error("Veuillez remplir tous les champs."); return;
        }
        if (!email.contains("@")) {
            error("Email invalide."); return;
        }

        User user = service.login(email, password);
        if (user == null) {
            error("Email ou mot de passe incorrect."); return;
        }

        SessionManager.getInstance().setCurrentUser(user);
        SceneManager.goToProfile();
    }

    @FXML public void goToRegister() { SceneManager.goToRegister(); }

    private void error(String msg) {
        lblError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        lblError.setText(msg);
    }
}
