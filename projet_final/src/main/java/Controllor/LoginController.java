package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import models.User;
import services.ServiceUser;
import utils.SceneManager;
import utils.SessionManager;

public class LoginController {

    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;
    @FXML private TextField     txtPrenom;
    @FXML private TextField     txtNom;
    @FXML private TextField     txtEmailReg;
    @FXML private PasswordField txtPasswordReg;
    @FXML private Label         lblErrorReg;
    @FXML private Button        btnEtudiant;
    @FXML private Button        btnPsy;
    @FXML private VBox          loginForm;
    @FXML private VBox          registerForm;
    @FXML private Button        tabLogin;
    @FXML private Button        tabRegister;
    @FXML private ImageView     logoImage;

    private final ServiceUser service = new ServiceUser();
    private String selectedRole = "etudiant";

    private static final String TAB_ACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #2C5F8A; -fx-font-size: 14px; " +
                    "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; " +
                    "-fx-border-color: transparent transparent #2C5F8A transparent; -fx-border-width: 0 0 2.5 0;";
    private static final String TAB_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #7A9CB8; -fx-font-size: 14px; " +
                    "-fx-padding: 10 20; -fx-cursor: hand; -fx-border-color: transparent; -fx-border-width: 0;";
    private static final String ROLE_ACTIVE =
            "-fx-background-color: #2C5F8A; -fx-text-fill: white; -fx-background-radius: 8; " +
                    "-fx-pref-height: 40; -fx-font-size: 13px; -fx-cursor: hand;";
    private static final String ROLE_INACTIVE =
            "-fx-background-color: #F0F6FF; -fx-text-fill: #7A9CB8; -fx-border-color: #C8DDF0; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-pref-height: 40; -fx-font-size: 13px; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
            // ✅ Clip circulaire — rend le logo rond
            Circle clip = new Circle(50, 50, 50);
            logoImage.setClip(clip);
        } catch (Exception ignored) {}
    }

    @FXML public void switchToLogin() {
        loginForm.setVisible(true);   loginForm.setManaged(true);
        registerForm.setVisible(false); registerForm.setManaged(false);
        tabLogin.setStyle(TAB_ACTIVE); tabRegister.setStyle(TAB_INACTIVE);
    }

    @FXML public void switchToRegister() {
        loginForm.setVisible(false);  loginForm.setManaged(false);
        registerForm.setVisible(true); registerForm.setManaged(true);
        tabRegister.setStyle(TAB_ACTIVE); tabLogin.setStyle(TAB_INACTIVE);
    }

    @FXML public void selectEtudiant() {
        selectedRole = "etudiant";
        btnEtudiant.setStyle(ROLE_ACTIVE); btnPsy.setStyle(ROLE_INACTIVE);
    }

    @FXML public void selectPsy() {
        selectedRole = "psychologue";
        btnPsy.setStyle(ROLE_ACTIVE); btnEtudiant.setStyle(ROLE_INACTIVE);
    }

    @FXML
    public void handleLogin() {
        lblError.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
        lblError.setText("");
        String email = txtEmail.getText().trim();
        String pass  = txtPassword.getText();
        if (email.isEmpty() || pass.isEmpty()) { lblError.setText("Veuillez remplir tous les champs."); return; }
        if (!email.contains("@"))              { lblError.setText("Email invalide."); return; }
        User user = service.login(email, pass);
        if (user == null) { lblError.setText("Email ou mot de passe incorrect."); return; }
        SessionManager.getInstance().setCurrentUser(user);
        String role = user.getRole() != null ? user.getRole().toLowerCase() : "";
        if (role.equals("psychologue")) SceneManager.switchTo("HomePsy.fxml", "Dashboard Psychologue");
        else if (role.equals("admin"))  SceneManager.switchTo("HomeAdmin.fxml", "Dashboard Admin");
        else                            SceneManager.switchTo("Home.fxml", "Dashboard Étudiant");
    }

    @FXML
    public void handleRegister() {
        lblErrorReg.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
        lblErrorReg.setText("");
        String prenom = txtPrenom.getText().trim(), nom = txtNom.getText().trim();
        String email  = txtEmailReg.getText().trim(), pass = txtPasswordReg.getText();
        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            lblErrorReg.setText("Tous les champs sont obligatoires."); return; }
        if (!email.contains("@") || !email.contains(".")) {
            lblErrorReg.setText("Email invalide."); return; }
        if (pass.length() < 8) { lblErrorReg.setText("Mot de passe : 8 caractères minimum."); return; }
        User user = new User();
        user.setPrenom(prenom); user.setNom(nom); user.setEmail(email);
        user.setMotDePasse(pass); user.setRole(selectedRole);
        user.setRoles("[\"" + (selectedRole.equals("psychologue") ? "ROLE_PSYCHOLOGUE" : "ROLE_ETUDIANT") + "\"]");
        if (!service.register(user)) { lblErrorReg.setText("Cet email est déjà utilisé."); return; }
        switchToLogin();
        lblError.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12px;");
        lblError.setText("Compte créé ! Connectez-vous.");
    }
}