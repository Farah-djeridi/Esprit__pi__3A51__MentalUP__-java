package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import models.User;
import services.ServiceUser;
import utils.PasswordUtils;
import utils.SceneManager;
import utils.SessionManager;

import java.io.File;

public class ProfileController {

    @FXML private TextField     txtPrenom;
    @FXML private TextField     txtNom;
    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label         lblMessage;
    @FXML private Label         lblBannerName;
    @FXML private Label         lblBannerEmail;
    @FXML private Label         lblNavName;
    @FXML private Label         lblNavRole;
    @FXML private Label         avatarInitials;
    @FXML private ImageView     avatarImage;
    @FXML private ImageView     logoImage;
    @FXML private Button        menuButton;
    @FXML private StackPane     cameraOverlay;

    private final ServiceUser service = new ServiceUser();
    private ContextMenu contextMenu;

    @FXML
    public void initialize() {
        try { logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png"))); }
        catch (Exception ignored) {}

        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { SceneManager.goToLogin(); return; }

        // Remplir les champs
        txtPrenom.setText(user.getPrenom() != null ? user.getPrenom() : "");
        txtNom.setText(user.getNom() != null ? user.getNom() : "");
        txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        // Banner
        String fullName = (user.getPrenom() != null ? user.getPrenom() : "") + " " +
                (user.getNom() != null ? user.getNom() : "");
        lblBannerName.setText(fullName.trim());
        lblBannerEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        lblNavName.setText(fullName.trim());
        lblNavRole.setText(user.getRole() != null ? user.getRole() : "Étudiant");

        // Initiales avatar
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) initials += user.getPrenom().charAt(0);
        if (user.getNom() != null && !user.getNom().isEmpty()) initials += user.getNom().charAt(0);
        avatarInitials.setText(initials.toUpperCase());

        // Charger avatar si existe
        if (user.getAvatarFilename() != null && !user.getAvatarFilename().isEmpty()) {
            try {
                File f = new File(user.getAvatarFilename());
                if (f.exists()) {
                    avatarImage.setImage(new Image(f.toURI().toString()));
                    avatarInitials.setVisible(false);
                }
            } catch (Exception ignored) {}
        }

        // Menu contextuel 3 points
        contextMenu = new ContextMenu();
        MenuItem modProfil = new MenuItem("✏️  Modifier mon profil");
        modProfil.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        modProfil.setOnAction(e -> {});  // déjà sur la page profil

        MenuItem deconnexion = new MenuItem("🚪  Déconnexion");
        deconnexion.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-text-fill: #E74C3C;");
        deconnexion.setOnAction(e -> handleLogout());

        contextMenu.getItems().addAll(modProfil, deconnexion);

        // Hover sur avatar
        avatarImage.setOnMouseEntered(e -> cameraOverlay.setOpacity(1));
        avatarImage.setOnMouseExited(e -> cameraOverlay.setOpacity(0));
    }

    // Clic sur avatar → choisir une image
    @FXML
    public void handleAvatarClick(MouseEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = chooser.showOpenDialog(avatarImage.getScene().getWindow());
        if (file != null) {
            Image img = new Image(file.toURI().toString());
            avatarImage.setImage(img);
            avatarInitials.setVisible(false);
            // Sauvegarder le chemin dans l'utilisateur
            User user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                user.setAvatarFilename(file.getAbsolutePath());
                service.update(user);
            }
        }
    }

    @FXML
    public void onMenuClicked(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        contextMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 5);
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

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty()) {
            msg("Prénom, nom et email sont obligatoires.", false); return; }
        if (!email.contains("@") || !email.contains(".")) {
            msg("Email invalide.", false); return; }
        if (!email.equals(user.getEmail()) && service.emailExistsForOther(email, user.getId())) {
            msg("Cet email est déjà utilisé.", false); return; }

        String newPassToSave = null;
        if (!newPass.isEmpty()) {
            if (current.isEmpty()) { msg("Saisissez votre mot de passe actuel.", false); return; }
            if (!PasswordUtils.verifyPassword(current, user.getMotDePasse())) {
                msg("Mot de passe actuel incorrect.", false); return; }
            if (!newPass.equals(confirm)) { msg("Les mots de passe ne correspondent pas.", false); return; }
            if (newPass.length() < 8) { msg("Minimum 8 caractères.", false); return; }
            newPassToSave = newPass;
        }

        user.setPrenom(prenom); user.setNom(nom); user.setEmail(email);

        if (service.updateProfile(user, newPassToSave)) {
            SessionManager.getInstance().setCurrentUser(user);
            // Mettre à jour la bannière
            lblBannerName.setText(prenom + " " + nom);
            lblBannerEmail.setText(email);
            lblNavName.setText(prenom + " " + nom);
            msg("Profil modifié avec succès !", true);
            txtCurrentPassword.clear(); txtNewPassword.clear(); txtConfirmPassword.clear();
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
        lblMessage.setStyle("-fx-text-fill: " + (success ? "#27AE60" : "#E74C3C") + "; -fx-font-size: 12px;");
        lblMessage.setText(text);
    }
}
