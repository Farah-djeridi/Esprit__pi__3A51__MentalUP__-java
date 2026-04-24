package Controllor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.User;
import services.ServiceUser;
import utils.SceneManager;
import utils.SessionManager;

import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.Priority;  // Ajoutez aussi Priority si vous l'utilisez
import javafx.scene.layout.VBox;

public class ControllerAdminUsers {

    @FXML private FlowPane cardContainer;
    @FXML private TextField txtSearch;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblPage;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private ImageView logoImage;
    @FXML private ToggleButton toggleViewBtn;

    private final ServiceUser service = new ServiceUser();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private ObservableList<User> filtered = FXCollections.observableArrayList();

    private static final int CARDS_PER_PAGE = 6;
    private int currentPage = 0;
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        loadUsers();
    }

    // ===== CHARGEMENT =====
    private void loadUsers() {
        allUsers.setAll(service.getAll());
        applyFilter();
    }

    private void applyFilter() {
        String search = txtSearch != null ? txtSearch.getText().toLowerCase().trim() : "";
        filtered.setAll(allUsers.stream()
                .filter(u -> {
                    boolean roleOk = currentFilter.equals("all") ||
                            (u.getRole() != null && u.getRole().equalsIgnoreCase(currentFilter));
                    boolean searchOk = search.isEmpty() ||
                            (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(search)) ||
                            (u.getNom() != null && u.getNom().toLowerCase().contains(search)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(search));
                    return roleOk && searchOk;
                }).collect(Collectors.toList()));
        currentPage = 0;
        refreshCards();
    }

    private void refreshCards() {
        cardContainer.getChildren().clear();

        int total = filtered.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / CARDS_PER_PAGE));
        int from = currentPage * CARDS_PER_PAGE;
        int to = Math.min(from + CARDS_PER_PAGE, total);

        lblTotalUsers.setText(total + " utilisateurs au total");
        lblPage.setText("Page " + (currentPage + 1) + " sur " + pages);
        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(currentPage >= pages - 1);

        if (total == 0) {
            Label emptyLabel = new Label("Aucun utilisateur trouvé");
            emptyLabel.setStyle("-fx-text-fill: #A0B4C8; -fx-font-size: 14px;");
            cardContainer.getChildren().add(emptyLabel);
            return;
        }

        for (int i = from; i < to; i++) {
            User user = filtered.get(i);
            VBox card = createUserCard(user);
            cardContainer.getChildren().add(card);
        }
    }

    // ===== CRÉATION D'UNE CARTE =====
    private VBox createUserCard(User user) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #E8EFF6; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_CENTER);

        // Avatar avec initiales
        StackPane avatarStack = new StackPane();
        Circle circle = new Circle(45);
        circle.setFill(javafx.scene.paint.Color.web(getRoleColor(user.getRole())));
        Label initials = new Label(getInitials(user));
        initials.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        avatarStack.getChildren().addAll(circle, initials);

        // Nom complet
        Label name = new Label((user.getPrenom() != null ? user.getPrenom() : "") + " " +
                (user.getNom() != null ? user.getNom() : ""));
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);

        // Email
        Label email = new Label(user.getEmail() != null ? user.getEmail() : "");
        email.setStyle("-fx-font-size: 12px; -fx-text-fill: #7A9CB8;");
        email.setWrapText(true);

        // Badge rôle
        Label roleBadge = new Label(getRoleName(user.getRole()));
        roleBadge.setStyle("-fx-background-color: " + getRoleBg(user.getRole()) + ";" +
                "-fx-text-fill: " + getRoleColor(user.getRole()) + ";" +
                "-fx-background-radius: 12; -fx-padding: 4 12; " +
                "-fx-font-size: 11px; -fx-font-weight: bold;");

        // ID
        Label idLabel = new Label("ID: #" + user.getId());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #A0B4C8;");

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E8EFF6;");

        // Boutons d'action
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
        editBtn.setOnMouseClicked(e -> openUserForm(user));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #FDECEA; -fx-text-fill: #C0392B; " +
                "-fx-border-color: #F1948A; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
        deleteBtn.setOnMouseClicked(e -> deleteUser(user));

        Button resetBtn = new Button("🔑 Réinit.");
        resetBtn.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #E67E22; " +
                "-fx-border-color: #FAD7A0; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
        resetBtn.setOnMouseClicked(e -> resetPassword(user));

        buttonBox.getChildren().addAll(editBtn, deleteBtn, resetBtn);

        card.getChildren().addAll(avatarStack, name, email, roleBadge, idLabel, separator, buttonBox);

        // Animation au survol
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #E8EFF6; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);"));

        return card;
    }

    // ===== CRÉATION D'UNE CARTE POUR LA CORBEILLE =====
    private VBox createDeletedUserCard(User user) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #FDECEA; -fx-border-radius: 12; " +
                "-fx-border-width: 1;");

        HBox contentBox = new HBox(12);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        // Icône corbeille
        Label iconLabel = new Label("🗑️");
        iconLabel.setStyle("-fx-font-size: 24px;");

        VBox infoBox = new VBox(4);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label((user.getPrenom() != null ? user.getPrenom() : "") + " " +
                (user.getNom() != null ? user.getNom() : ""));
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label emailLabel = new Label(user.getEmail() != null ? user.getEmail() : "");
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7A9CB8;");

        Label deletedLabel = new Label("🗑️ Supprimé le: " + user.getDeletedAt());
        deletedLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #E74C3C;");

        infoBox.getChildren().addAll(nameLabel, emailLabel, deletedLabel);

        Button restoreBtn = new Button("♻️ Restaurer");
        restoreBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 11px; -fx-cursor: hand;");
        restoreBtn.setOnAction(e -> {
            service.restore(user);
            loadUsers(); // Recharger la liste principale
            // Fermer la fenêtre de corbeille
            Stage stage = (Stage) restoreBtn.getScene().getWindow();
            stage.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Utilisateur restauré");
            alert.setHeaderText(null);
            alert.setContentText("✅ " + user.getPrenom() + " " + user.getNom() + " a été restauré.");
            alert.showAndWait();
        });

        contentBox.getChildren().addAll(iconLabel, infoBox, restoreBtn);
        card.getChildren().add(contentBox);

        return card;
    }

    // ===== CRUD =====
    @FXML public void handleAdd() { openUserForm(null); }

    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserFormDialog.fxml"));
            Parent root = loader.load();
            ControllerUserForm ctrl = loader.getController();
            ctrl.setUser(user);
            ctrl.setOnSaved(() -> loadUsers());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer " + user.getPrenom() + " " + user.getNom() + " ?");
        confirm.setContentText("L'utilisateur sera marqué comme supprimé mais pourra être restauré.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                service.delete(user);
                loadUsers();
            }
        });
    }

    private void resetPassword(User user) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Réinitialiser le mot de passe");
        info.setHeaderText("Mot de passe réinitialisé");
        info.setContentText("Un email de réinitialisation a été envoyé à : " + user.getEmail());
        info.showAndWait();
    }

    // ===== FILTRES =====
    @FXML public void filterAll()         { currentFilter = "all";         applyFilter(); }
    @FXML public void filterEtudiants()   { currentFilter = "etudiant";    applyFilter(); }
    @FXML public void filterPsychologues(){ currentFilter = "psychologue"; applyFilter(); }
    @FXML public void filterAdmins()      { currentFilter = "admin";       applyFilter(); }
    @FXML public void handleSearch()      { applyFilter(); }

    @FXML public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            refreshCards();
        }
    }

    @FXML public void nextPage() {
        int pages = (int) Math.ceil((double) filtered.size() / CARDS_PER_PAGE);
        if (currentPage < pages - 1) {
            currentPage++;
            refreshCards();
        }
    }

    // Optionnel : basculer entre vue table et vue cartes
    @FXML public void toggleView() {
        SceneManager.switchTo("AdminUsers.fxml", "Gestion Utilisateurs");
    }

    // ===== NAVIGATION =====
    @FXML public void goToDashboard() { SceneManager.switchTo("HomeAdmin.fxml", "Dashboard Admin"); }
    @FXML public void goToSuivi() { SceneManager.switchTo("SuiviGlobal.fxml", "Suivi Global"); }
    @FXML public void handleLogout() { SessionManager.getInstance().logout(); SceneManager.goToLogin(); }

    // ===== CORBEILLE =====
    @FXML
    public void showDeletedUsers() {
        List<User> deletedUsers = service.getDeletedUsers();

        if (deletedUsers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Corbeille");
            alert.setHeaderText(null);
            alert.setContentText("📭 La corbeille est vide.");
            alert.showAndWait();
            return;
        }

        showDeletedUsersDialog(deletedUsers);
    }

    private void showDeletedUsersDialog(List<User> deletedUsers) {
        Stage dialog = new Stage();
        dialog.setTitle("🗑️ Corbeille - Utilisateurs supprimés");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F4F8FF;");

        Label title = new Label("📋 Utilisateurs supprimés");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        ListView<VBox> listView = new ListView<>();
        listView.setStyle("-fx-background-color: transparent;");

        for (User user : deletedUsers) {
            VBox card = createDeletedUserCard(user);
            listView.getItems().add(card);
        }

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #7A9CB8; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(title, listView, closeBtn);
        VBox.setVgrow(listView, Priority.ALWAYS);

        Scene scene = new Scene(root, 600, 450);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ===== HELPERS =====
    private String getInitials(User u) {
        String i = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty()) i += u.getPrenom().charAt(0);
        if (u.getNom() != null && !u.getNom().isEmpty()) i += u.getNom().charAt(0);
        return i.toUpperCase();
    }

    private String getRoleName(String role) {
        if (role == null) return "ÉTUDIANT";
        return switch (role.toLowerCase()) {
            case "psychologue" -> "PSYCHOLOGUE";
            case "admin" -> "ADMIN";
            default -> "ÉTUDIANT";
        };
    }

    private String getRoleColor(String role) {
        if (role == null) return "#2980B9";
        return switch (role.toLowerCase()) {
            case "psychologue" -> "#1E8449";
            case "admin" -> "#C0392B";
            default -> "#2980B9";
        };
    }

    private String getRoleBg(String role) {
        if (role == null) return "#E8F5FF";
        return switch (role.toLowerCase()) {
            case "psychologue" -> "#E8FFF0";
            case "admin" -> "#FFF0F0";
            default -> "#E8F5FF";
        };
    }
}