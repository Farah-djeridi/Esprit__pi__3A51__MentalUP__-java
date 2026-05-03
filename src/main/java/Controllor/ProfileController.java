package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import models.User;
import services.ServiceUser;
import utils.AvatarService;
import utils.PasswordUtils;
import utils.SceneManager;
import utils.SessionManager;

import java.io.File;
import java.util.Optional;

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
    @FXML private Label         navAvatarInitials;
    @FXML private ImageView     avatarImage;
    @FXML private ImageView     navAvatarImage;
    @FXML private ImageView     logoImage;
    @FXML private Button        menuButton;
    @FXML private StackPane     cameraOverlay;

    private final ServiceUser service = new ServiceUser();
    private ContextMenu contextMenu;
    private Thread generationThread;
    private volatile boolean controllerActive = true;
    // Static ref to deactivate previous instance when a new one is created
    private static ProfileController currentInstance;

    @FXML
    public void initialize() {
        // Always clear any stale message from a previous session
        lblMessage.setText("");
        // Deactivate any previous instance so its pending callbacks are ignored
        if (currentInstance != null) currentInstance.controllerActive = false;
        currentInstance = this;

        // Logo
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        // Clip circulaire sur l'avatar (centrer sur les dimensions reelles de l'ImageView)
        applyCircleClip();

        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            SceneManager.goToLogin();
            return;
        }

        // Remplir les champs
        txtPrenom.setText(user.getPrenom() != null ? user.getPrenom() : "");
        txtNom.setText(user.getNom()    != null ? user.getNom()    : "");
        txtEmail.setText(user.getEmail()  != null ? user.getEmail()  : "");

        // Banniere
        String fullName = (user.getPrenom() != null ? user.getPrenom() : "") + " " +
                          (user.getNom()    != null ? user.getNom()    : "");
        lblBannerName.setText(fullName.trim());
        lblBannerEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        lblNavName.setText(fullName.trim());
        lblNavRole.setText(user.getRole() != null ? user.getRole() : "Etudiant");

        // Initiales avatar
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) initials += user.getPrenom().charAt(0);
        if (user.getNom()    != null && !user.getNom().isEmpty())    initials += user.getNom().charAt(0);
        avatarInitials.setText(initials.toUpperCase());

        // Charger avatar s'il existe deja
        loadAvatarFromPath(user.getAvatarFilename());
        loadNavAvatarFromPath(user.getAvatarFilename(), initials.toUpperCase());

        // Menu contextuel
        contextMenu = new ContextMenu();

        MenuItem modProfil = new MenuItem("  Modifier mon profil");
        modProfil.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        modProfil.setOnAction(e -> {});

        MenuItem genAvatar = new MenuItem("  Generer avatar IA");
        genAvatar.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        genAvatar.setOnAction(e -> handleGenerateAvatar());

        MenuItem choisirPhoto = new MenuItem("  Choisir une photo");
        choisirPhoto.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        choisirPhoto.setOnAction(e -> handleAvatarClick(null));

        MenuItem deconnexion = new MenuItem("  Deconnexion");
        deconnexion.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-text-fill: #E74C3C;");
        deconnexion.setOnAction(e -> handleLogout());

        contextMenu.getItems().addAll(modProfil, genAvatar, choisirPhoto, deconnexion);

        // Hover sur l'avatar
        avatarImage.setOnMouseEntered(e -> cameraOverlay.setOpacity(1));
        avatarImage.setOnMouseExited(e -> cameraOverlay.setOpacity(0));
    }

    /** Applique un clip circulaire en utilisant les dimensions reelles de l'ImageView */
    private void applyCircleClip() {
        double w = avatarImage.getFitWidth()  > 0 ? avatarImage.getFitWidth()  : 100;
        double h = avatarImage.getFitHeight() > 0 ? avatarImage.getFitHeight() : 100;
        double r = Math.min(w, h) / 2.0;
        Circle clip = new Circle(w / 2.0, h / 2.0, r);
        avatarImage.setClip(clip);
    }

    /** Charge une image avatar depuis un chemin de fichier */
    private void loadAvatarFromPath(String path) {
        if (path == null || path.isEmpty()) return;
        try {
            File f = new File(path);
            if (f.exists()) {
                Image img = new Image(f.toURI().toString());
                avatarImage.setImage(img);
                applyCircleClip();
                avatarInitials.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger l'avatar: " + e.getMessage());
        }
    }

    @FXML
    public void handleAvatarClick(MouseEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = chooser.showOpenDialog(avatarImage.getScene().getWindow());
        if (file != null) {
            avatarImage.setImage(new Image(file.toURI().toString()));
            applyCircleClip();
            avatarInitials.setVisible(false);
            User user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                user.setAvatarFilename(file.getAbsolutePath());
                service.update(user);
                loadNavAvatarFromPath(file.getAbsolutePath(), "");
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
            msg("Prenom, nom et email sont obligatoires.", false); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            msg("Email invalide.", false); return;
        }
        if (!email.equals(user.getEmail()) && service.emailExistsForOther(email, user.getId())) {
            msg("Cet email est deja utilise.", false); return;
        }

        String newPassToSave = null;
        if (!newPass.isEmpty()) {
            if (current.isEmpty()) {
                msg("Saisissez votre mot de passe actuel.", false); return;
            }
            if (!PasswordUtils.verifyPassword(current, user.getMotDePasse())) {
                msg("Mot de passe actuel incorrect.", false); return;
            }
            if (!newPass.equals(confirm)) {
                msg("Les mots de passe ne correspondent pas.", false); return;
            }
            if (newPass.length() < 8) {
                msg("Minimum 8 caracteres.", false); return;
            }
            newPassToSave = newPass;
        }

        user.setPrenom(prenom);
        user.setNom(nom);
        user.setEmail(email);

        if (service.updateProfile(user, newPassToSave)) {
            SessionManager.getInstance().setCurrentUser(user);
            lblBannerName.setText(prenom + " " + nom);
            lblBannerEmail.setText(email);
            lblNavName.setText(prenom + " " + nom);
            msg("Profil modifie avec succes !", true);
            txtCurrentPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
        } else {
            msg("Erreur lors de la sauvegarde.", false);
        }
    }

    /** Loads avatar into the top-right nav circle */
    private void loadNavAvatarFromPath(String path, String initials) {
        if (navAvatarInitials != null) navAvatarInitials.setText(initials);
        if (path == null || path.isEmpty()) return;
        try {
            java.io.File f = new java.io.File(path);
            if (!f.exists()) return;
            javafx.scene.image.Image img = new javafx.scene.image.Image(f.toURI().toString());
            if (navAvatarImage != null) {
                navAvatarImage.setImage(img);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(18, 18, 18);
                navAvatarImage.setClip(clip);
                navAvatarImage.setVisible(true);
                if (navAvatarInitials != null) navAvatarInitials.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Nav avatar load error: " + e.getMessage());
        }
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    @FXML public void goToDashboard(javafx.event.ActionEvent e) { SceneManager.goToHome(); }
    @FXML public void goToSuivi(javafx.event.ActionEvent e)     { SceneManager.switchTo("suivi_mentale.fxml", "Suivi Mental"); }
    @FXML public void goToObjectif(javafx.event.ActionEvent e)  { SceneManager.switchTo("objvue.fxml", "Objectifs"); }
    @FXML public void goToForum(javafx.event.ActionEvent e)     { SceneManager.switchTo("forum.fxml", "Forum"); }
    @FXML public void goToRdv(javafx.event.ActionEvent e)       { SceneManager.switchTo("RendezVous_Etudiant.fxml", "Rendez-vous"); }
    @FXML public void goToActivites(javafx.event.ActionEvent e) { SceneManager.switchTo("EtudiantActivites.fxml", "Activités"); }

    @FXML
    public void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.goToLogin();
    }

    @FXML
    public void handleGenerateAvatar() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Generer Avatar IA");
        dialog.setHeaderText("Decrivez votre avatar en quelques mots");
        dialog.setContentText("Ex: jeune femme aux cheveux roux, style cartoon:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(prompt -> {
            if (!prompt.trim().isEmpty()) {
                msg("Generation en cours...", true);
                String finalPrompt = prompt.trim();
                controllerActive = true;

                generationThread = new Thread(() -> {
                    // Try HuggingFace first, fall back to DiceBear
                    String filePath = AvatarService.generateAvatarFromPrompt(finalPrompt);

                    if (filePath == null) {
                        // Fallback: DiceBear cartoon avatar (no token needed)
                        filePath = generateDiceBearAvatar(finalPrompt);
                    }

                    final String finalPath = filePath;
                    javafx.application.Platform.runLater(() -> {
                        if (!controllerActive) return; // controller was replaced, ignore
                        if (finalPath != null) {
                            try {
                                File imageFile = new File(finalPath);
                                if (imageFile.exists()) {
                                    avatarImage.setImage(new Image(imageFile.toURI().toString()));
                                    applyCircleClip();
                                    avatarInitials.setVisible(false);
                                    User user = SessionManager.getInstance().getCurrentUser();
                                    if (user != null) {
                                        user.setAvatarFilename(finalPath);
                                        service.update(user);
                                    }
                                    msg("Avatar genere avec succes !", true);
                                } else {
                                    msg("Fichier image non trouve.", false);
                                }
                            } catch (Exception e) {
                                msg("Erreur: " + e.getMessage(), false);
                            }
                        } else {
                            msg("Generation echouee. Verifiez votre connexion.", false);
                        }
                    });
                });
                generationThread.setDaemon(true);
                generationThread.start();
            }
        });
    }

    /**
     * Generates a cartoon avatar using DiceBear API (free, no token needed).
     * Uses the "adventurer" style which produces colorful cartoon faces.
     */
    private String generateDiceBearAvatar(String seed) {
        try {
            // Encode seed for URL
            String encodedSeed = java.net.URLEncoder.encode(seed, java.nio.charset.StandardCharsets.UTF_8);
            String url = "https://api.dicebear.com/7.x/adventurer/png?seed=" + encodedSeed + "&size=200";

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "MentalUpApp/1.0");

            if (conn.getResponseCode() != 200) return null;

            // Save to temp file
            java.io.File tempFile = java.io.File.createTempFile("avatar_", ".png");
            try (java.io.InputStream in = conn.getInputStream();
                 java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            }
            conn.disconnect();
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("DiceBear fallback failed: " + e.getMessage());
            return null;
        }
    }

    private void msg(String text, boolean success) {
        lblMessage.setStyle("-fx-text-fill: " + (success ? "#27AE60" : "#E74C3C") + "; -fx-font-size: 12px;");
        lblMessage.setText(text);
    }
}
