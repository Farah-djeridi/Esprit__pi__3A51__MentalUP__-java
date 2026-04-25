package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import models.User;
import utils.SceneManager;
import utils.SessionManager;

public class TwoFactorController {

    @FXML private Label         lblEmail;
    @FXML private TextField     txtCode1;
    @FXML private TextField     txtCode2;
    @FXML private TextField     txtCode3;
    @FXML private TextField     txtCode4;
    @FXML private TextField     txtCode5;
    @FXML private TextField     txtCode6;
    @FXML private Label         lblError;
    @FXML private Label         lblTimer;
    @FXML private Button        btnVerify;
    @FXML private Hyperlink     btnResend;
    @FXML private ImageView     logoImage;

    // Le vrai code envoyé par email
    private String expectedCode;

    // Timer pour l'expiration (5 minutes = 300 secondes)
    private javafx.animation.Timeline timer;
    private int secondsLeft = 300;

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
            Circle clip = new Circle(25, 25, 25);
            logoImage.setClip(clip);
        } catch (Exception ignored) {}

        // Afficher l'email masqué (ex: s***e@gmail.com)
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblEmail.setText("Code envoyé à : " + maskEmail(user.getEmail()));
        }

        // ✅ Navigation automatique entre les cases
        setupAutoFocus();

        // ✅ Démarrer le timer de 5 minutes
        startTimer();
    }

    /**
     * Reçoit le code généré depuis LoginController
     */
    public void setExpectedCode(String code) {
        this.expectedCode = code;
    }

    /**
     * Navigation automatique : quand tu tapes un chiffre,
     * le curseur passe automatiquement à la case suivante
     */
    private void setupAutoFocus() {
        TextField[] fields = {txtCode1, txtCode2, txtCode3, txtCode4, txtCode5, txtCode6};

        for (int i = 0; i < fields.length; i++) {
            final int index = i;
            final TextField current = fields[i];

            // Limiter à 1 caractère
            current.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.length() > 1) {
                    current.setText(newVal.substring(0, 1));
                }
                // Passer au champ suivant automatiquement
                if (newVal.length() == 1 && index < fields.length - 1) {
                    fields[index + 1].requestFocus();
                }
                // Si tous les champs remplis → vérifier automatiquement
                if (allFieldsFilled()) {
                    handleVerify();
                }
            });

            // Backspace → revenir au champ précédent
            current.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.BACK_SPACE
                        && current.getText().isEmpty() && index > 0) {
                    fields[index - 1].requestFocus();
                }
            });
        }
    }

    private boolean allFieldsFilled() {
        return !txtCode1.getText().isEmpty() && !txtCode2.getText().isEmpty() &&
                !txtCode3.getText().isEmpty() && !txtCode4.getText().isEmpty() &&
                !txtCode5.getText().isEmpty() && !txtCode6.getText().isEmpty();
    }

    private String getEnteredCode() {
        return txtCode1.getText() + txtCode2.getText() + txtCode3.getText() +
                txtCode4.getText() + txtCode5.getText() + txtCode6.getText();
    }

    /**
     * Vérifie le code saisi
     */
    @FXML
    public void handleVerify() {
        lblError.setText("");
        String entered = getEnteredCode();

        if (entered.length() < 6) {
            error("Veuillez saisir les 6 chiffres."); return;
        }

        if (entered.equals(expectedCode)) {
            // ✅ Code correct → arrêter le timer et rediriger
            if (timer != null) timer.stop();

            User user = SessionManager.getInstance().getCurrentUser();
            String role = user.getRole() != null ? user.getRole().toLowerCase() : "";

            if (role.equals("psychologue"))
                SceneManager.switchTo("HomePsy.fxml", "Dashboard Psychologue");
            else if (role.equals("admin"))
                SceneManager.switchTo("HomeAdmin.fxml", "Dashboard Admin");
            else
                SceneManager.switchTo("Home.fxml", "Dashboard Étudiant");
        } else {
            // ❌ Code incorrect
            error("Code incorrect. Vérifiez votre email.");
            clearFields();
        }
    }

    /**
     * Renvoyer un nouveau code
     */
    @FXML
    public void handleResend() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { SceneManager.goToLogin(); return; }

        // Générer et envoyer un nouveau code
        String newCode = utils.EmailService.generateCode();
        boolean sent = utils.EmailService.sendCode(user.getEmail(), newCode);

        if (sent) {
            this.expectedCode = newCode;
            secondsLeft = 300;
            lblError.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12px;");
            lblError.setText("Nouveau code envoyé !");
            clearFields();
            txtCode1.requestFocus();
        } else {
            error("Erreur d'envoi. Vérifiez votre connexion.");
        }
    }

    /**
     * Annuler → retour au login
     */
    @FXML
    public void handleCancel() {
        if (timer != null) timer.stop();
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    /**
     * Timer décomptant de 5:00 à 0:00
     */
    private void startTimer() {
        timer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(1),
                        e -> {
                            secondsLeft--;
                            int min = secondsLeft / 60;
                            int sec = secondsLeft % 60;
                            lblTimer.setText(String.format("Expire dans %d:%02d", min, sec));

                            if (secondsLeft <= 0) {
                                timer.stop();
                                lblTimer.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 13px;");
                                lblTimer.setText("Code expiré !");
                                btnVerify.setDisable(true);
                                error("Le code a expiré. Cliquez sur 'Renvoyer'.");
                            } else if (secondsLeft <= 60) {
                                lblTimer.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 13px;");
                            }
                        }
                )
        );
        timer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timer.play();
    }

    private void clearFields() {
        txtCode1.clear(); txtCode2.clear(); txtCode3.clear();
        txtCode4.clear(); txtCode5.clear(); txtCode6.clear();
        txtCode1.requestFocus();
    }

    private void error(String msg) {
        lblError.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
        lblError.setText(msg);
    }

    /**
     * Masque l'email : sirine@gmail.com → s****e@gmail.com
     */
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