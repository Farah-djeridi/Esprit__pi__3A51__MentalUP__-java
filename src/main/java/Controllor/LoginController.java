package Controllor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import models.User;
import services.ServiceUser;
import utils.EmailService;
import utils.GoogleAuthService;
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
    @FXML private VBox          bannerBan;
    @FXML private Label         lblBanTimer;
    private javafx.animation.Timeline bannerTimeline;

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
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-pref-height: 40; " +
                    "-fx-font-size: 13px; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
            Circle clip = new Circle(50, 50, 50);
            logoImage.setClip(clip);
        } catch (Exception ignored) {}
    }

    @FXML public void switchToLogin() {
        loginForm.setVisible(true);    loginForm.setManaged(true);
        registerForm.setVisible(false); registerForm.setManaged(false);
        tabLogin.setStyle(TAB_ACTIVE); tabRegister.setStyle(TAB_INACTIVE);
    }

    @FXML public void switchToRegister() {
        loginForm.setVisible(false);   loginForm.setManaged(false);
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

    // =========================================================================
    // CONNEXION CLASSIQUE
    // =========================================================================

    @FXML
    public void handleLogin() {
        lblError.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
        lblError.setText("");

        String email = txtEmail.getText().trim();
        String pass  = txtPassword.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs."); return;
        }
        if (!email.contains("@")) {
            lblError.setText("Email invalide."); return;
        }

        User user = service.login(email, pass);
        if (user == null) {
            if (service.isAccountLocked(email)) {
                showBanBanner(email);
            } else {
                lblError.setText("Email ou mot de passe incorrect.");
            }
            return;
        }

        SessionManager.getInstance().setCurrentUser(user);
        String code = EmailService.generateCode();
        lblError.setStyle("-fx-text-fill: #2C5F8A; -fx-font-size: 12px;");
        lblError.setText("Envoi du code en cours...");

        new Thread(() -> {
            boolean sent = EmailService.sendCode(user.getEmail(), code);
            javafx.application.Platform.runLater(() -> {
                if (sent) {
                    goToTwoFactor(code);
                } else {
                    lblError.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
                    lblError.setText("Erreur envoi email. Verifiez votre connexion.");
                }
            });
        }).start();
    }

    // =========================================================================
    // CONNEXION / INSCRIPTION GOOGLE
    // =========================================================================

    @FXML
    public void handleGoogleLogin() {
        lblError.setStyle("-fx-text-fill: #2C5F8A; -fx-font-size: 12px;");
        lblError.setText("Connexion Google en cours...");

        GoogleAuthService.authenticate(
                googleUser -> {
                    // Verifier si l'utilisateur existe deja en base
                    User existing = service.getUserByEmail(googleUser.getEmail());
                    if (existing != null) {
                        // Deja inscrit -> connexion directe (pas de 2FA pour Google)
                        SessionManager.getInstance().setCurrentUser(existing);
                        SceneManager.goToHome();
                    } else {
                        // Nouveau -> afficher message d'erreur suggérant l'inscription
                        lblError.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
                        lblError.setText("Compte Google non trouve. Inscrivez-vous d'abord.");
                    }
                },
                errMsg -> {
                    lblError.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
                    lblError.setText("Erreur Google : " + errMsg);
                }
        );
    }

    @FXML
    public void handleGoogleRegister() {
        lblErrorReg.setStyle("-fx-text-fill: #2C5F8A; -fx-font-size: 12px;");
        lblErrorReg.setText("Connexion Google en cours...");

        GoogleAuthService.authenticate(
                googleUser -> {
                    // Verifier si l'email existe deja
                    User existing = service.getUserByEmail(googleUser.getEmail());
                    if (existing != null) {
                        // Compte deja existant -> connecter directement
                        SessionManager.getInstance().setCurrentUser(existing);
                        lblErrorReg.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12px;");
                        lblErrorReg.setText("Compte existant, connexion en cours...");
                        javafx.animation.PauseTransition pause =
                                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                        pause.setOnFinished(e -> SceneManager.goToHome());
                        pause.play();
                        return;
                    }

                    // Appliquer le role selectionne
                    googleUser.setRole(selectedRole);
                    googleUser.setRoles("[\"" + (selectedRole.equals("psychologue")
                            ? "ROLE_PSYCHOLOGUE" : "ROLE_ETUDIANT") + "\"]");

                    // Inscrire l'utilisateur Google
                    boolean ok = service.registerGoogleUser(googleUser);
                    if (ok) {
                        User saved = service.getUserByEmail(googleUser.getEmail());
                        SessionManager.getInstance().setCurrentUser(saved);
                        lblErrorReg.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12px;");
                        lblErrorReg.setText("Compte cree avec Google !");
                        javafx.animation.PauseTransition pause =
                                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                        pause.setOnFinished(e -> SceneManager.goToHome());
                        pause.play();
                    } else {
                        lblErrorReg.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
                        lblErrorReg.setText("Erreur lors de la creation du compte Google.");
                    }
                },
                errMsg -> {
                    lblErrorReg.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
                    lblErrorReg.setText("Erreur Google : " + errMsg);
                }
        );
    }

    // =========================================================================
    // INSCRIPTION CLASSIQUE
    // =========================================================================

    @FXML
    public void handleRegister() {
        lblErrorReg.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
        lblErrorReg.setText("");
        String prenom = txtPrenom.getText().trim(), nom = txtNom.getText().trim();
        String email  = txtEmailReg.getText().trim(), pass = txtPasswordReg.getText();
        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            lblErrorReg.setText("Tous les champs sont obligatoires."); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            lblErrorReg.setText("Email invalide."); return;
        }
        if (pass.length() < 8) {
            lblErrorReg.setText("Mot de passe : 8 caracteres minimum."); return;
        }
        User user = new User();
        user.setPrenom(prenom); user.setNom(nom); user.setEmail(email);
        user.setMotDePasse(pass); user.setRole(selectedRole);
        user.setRoles("[\"" + (selectedRole.equals("psychologue")
                ? "ROLE_PSYCHOLOGUE" : "ROLE_ETUDIANT") + "\"]");
        if (!service.register(user)) {
            lblErrorReg.setText("Cet email est deja utilise."); return;
        }
        switchToLogin();
        lblError.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12px;");
        lblError.setText("Compte cree ! Connectez-vous.");
    }

    @FXML public void goToForgotPassword() {
        SceneManager.switchTo("ForgotPassword.fxml", "Mot de passe oublie");
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================

    private void goToTwoFactor(String code) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TwoFactor.fxml"));
            Parent root = loader.load();
            TwoFactorController ctrl = loader.getController();
            ctrl.setExpectedCode(code);
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            // Register this stage as the primary stage so SceneManager.goToHome() works
            SceneManager.setPrimaryStage(stage);
            stage.setScene(new Scene(root));
            stage.setTitle("Verification — MentalUp");
        } catch (Exception e) {
            e.printStackTrace();
            lblError.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
            lblError.setText("Erreur : " + e.getMessage());
        }
    }

    private void showBanBanner(String email) {
        User locked = service.getUserByEmailAdmin(email);
        if (locked == null || locked.getLockedUntil() == null) return;

        // Capturer le temps de fin de ban au moment de l appel
        final long banEndTime = locked.getLockedUntil().getTime();

        bannerBan.setVisible(true);
        bannerBan.setManaged(true);
        txtEmail.setDisable(true);
        txtPassword.setDisable(true);
        lblError.setText("");

        if (bannerTimeline != null) bannerTimeline.stop();
        bannerTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    long remaining = banEndTime - System.currentTimeMillis();
                    if (remaining <= 0) {
                        // Ban termine : reset tout
                        bannerBan.setVisible(false); bannerBan.setManaged(false);
                        txtEmail.setDisable(false);  txtPassword.setDisable(false);
                        txtPassword.clear();
                        lblError.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12px;");
                        lblError.setText("Compte debloque. Vous pouvez reessayer.");
                        bannerTimeline.stop();
                    } else {
                        long sec = remaining / 1000;
                        lblBanTimer.setText(String.format(
                                "Compte bloque apres 3 tentatives — reessayez dans %d:%02d",
                                sec / 60, sec % 60));
                    }
                }));
        bannerTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        bannerTimeline.play();
    }
}
