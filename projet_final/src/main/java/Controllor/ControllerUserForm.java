package Controllor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import services.ServiceUser;
import utils.PasswordUtils;

public class ControllerUserForm {

    @FXML private Label     lblTitle;
    @FXML private Label     lblPassHelper;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button    btnEtudiant;
    @FXML private Button    btnPsy;
    @FXML private Button    btnAdmin;
    @FXML private Button    btnSave;
    @FXML private Label     lblError;

    private final ServiceUser service = new ServiceUser();
    private User editingUser = null;
    private String selectedRole = "etudiant";
    private Runnable onSaved;

    private static final String ROLE_ON  = "-fx-background-color: #8E44AD; -fx-text-fill: white; " +
            "-fx-background-radius: 8; -fx-pref-height: 38; -fx-cursor: hand;";
    private static final String ROLE_OFF = "-fx-background-color: #F0F6FF; -fx-text-fill: #7A9CB8; " +
            "-fx-border-color: #C8DDF0; -fx-border-radius: 8; -fx-background-radius: 8; " +
            "-fx-pref-height: 38; -fx-cursor: hand;";

    public void setUser(User user) {
        this.editingUser = user;
        if (user != null) {
            // Mode édition
            lblTitle.setText("Modifier l'utilisateur");
            lblPassHelper.setVisible(true);
            txtPrenom.setText(user.getPrenom() != null ? user.getPrenom() : "");
            txtNom.setText(user.getNom() != null ? user.getNom() : "");
            txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            selectedRole = user.getRole() != null ? user.getRole().toLowerCase() : "etudiant";
            updateRoleButtons();
        } else {
            // Mode ajout
            lblTitle.setText("Nouvel utilisateur");
            lblPassHelper.setVisible(false);
        }
    }

    public void setOnSaved(Runnable callback) { this.onSaved = callback; }

    @FXML public void selectEtudiant() { selectedRole = "etudiant";    updateRoleButtons(); }
    @FXML public void selectPsy()      { selectedRole = "psychologue"; updateRoleButtons(); }
    @FXML public void selectAdmin()    { selectedRole = "admin";       updateRoleButtons(); }

    private void updateRoleButtons() {
        btnEtudiant.setStyle(selectedRole.equals("etudiant")    ? ROLE_ON : ROLE_OFF);
        btnPsy.setStyle(     selectedRole.equals("psychologue") ? ROLE_ON : ROLE_OFF);
        btnAdmin.setStyle(   selectedRole.equals("admin")       ? ROLE_ON : ROLE_OFF);
    }

    @FXML
    public void handleSave() {
        lblError.setText("");
        String prenom = txtPrenom.getText().trim();
        String nom    = txtNom.getText().trim();
        String email  = txtEmail.getText().trim();
        String pass   = txtPassword.getText();

        // Validations
        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty()) {
            lblError.setText("Prénom, nom et email sont obligatoires."); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            lblError.setText("Email invalide."); return;
        }
        if (editingUser == null && pass.isEmpty()) {
            lblError.setText("Le mot de passe est obligatoire pour un nouvel utilisateur."); return;
        }
        if (!pass.isEmpty() && pass.length() < 8) {
            lblError.setText("Mot de passe : minimum 8 caractères."); return;
        }

        String roleSymfony = switch (selectedRole) {
            case "psychologue" -> "ROLE_PSYCHOLOGUE";
            case "admin"       -> "ROLE_ADMIN";
            default            -> "ROLE_ETUDIANT";
        };

        if (editingUser == null) {
            // AJOUTER
            if (service.emailExistsForOther(email, -1) || service.getUserByEmail(email) != null) {
                lblError.setText("Cet email est déjà utilisé."); return;
            }
            User newUser = new User();
            newUser.setPrenom(prenom); newUser.setNom(nom); newUser.setEmail(email);
            newUser.setMotDePasse(pass); newUser.setRole(selectedRole);
            newUser.setRoles("[\"" + roleSymfony + "\"]");
            service.register(newUser);
        } else {
            // MODIFIER
            if (service.emailExistsForOther(email, editingUser.getId())) {
                lblError.setText("Cet email est déjà utilisé."); return;
            }
            editingUser.setPrenom(prenom); editingUser.setNom(nom); editingUser.setEmail(email);
            editingUser.setRole(selectedRole);
            editingUser.setRoles("[\"" + roleSymfony + "\"]");
            String newPass = pass.isEmpty() ? null : pass;
            service.updateProfile(editingUser, newPass);
        }

        if (onSaved != null) onSaved.run();
        closeDialog();
    }

    @FXML
    public void handleCancel() { closeDialog(); }

    private void closeDialog() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}
