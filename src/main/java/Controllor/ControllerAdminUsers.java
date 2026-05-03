package Controllor;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import Controllor.AdminSidebarHelper;

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

public class ControllerAdminUsers {

    @FXML private FlowPane  cardContainer;
    @FXML private TextField txtSearch;
    @FXML private Label     lblTotalUsers;
    @FXML private Label     lblPage;
    @FXML private Button    btnPrev;
    @FXML private Button    btnNext;
    @FXML private ImageView logoImage;
    @FXML private Button toggleViewBtn;
    private boolean isListView = false;
    @FXML private Label avatarInitials;
    @FXML private Label labelUserName;
    // Stats 3D
    @FXML private Label statTotal;
    @FXML private Label statEtudiants;
    @FXML private Label statPsy;
    @FXML private Label statSupprimes;

    private final ServiceUser service = new ServiceUser();
    private ObservableList<User> allUsers  = FXCollections.observableArrayList();
    private ObservableList<User> filtered  = FXCollections.observableArrayList();
    private static final int CARDS_PER_PAGE = 6;
    private int    currentPage   = 0;
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        try { logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png"))); } catch (Exception ignored) {}
        loadUsers();
    }

    private void loadUsers() {
        allUsers.setAll(service.getAll());
        // Mettre à jour les stats 3D
        if (statTotal    != null) statTotal.setText(String.valueOf(allUsers.size()));
        if (statEtudiants != null) statEtudiants.setText(String.valueOf(
            allUsers.stream().filter(u -> "etudiant".equalsIgnoreCase(u.getRole())).count()));
        if (statPsy != null) statPsy.setText(String.valueOf(
            allUsers.stream().filter(u -> "psychologue".equalsIgnoreCase(u.getRole())).count()));
        if (statSupprimes != null) statSupprimes.setText(String.valueOf(service.getDeletedUsers().size()));
        applyFilter();
    }

    private void applyFilter() {
        String search = txtSearch != null ? txtSearch.getText().toLowerCase().trim() : "";
        filtered.setAll(allUsers.stream().filter(u -> {
            boolean roleOk   = currentFilter.equals("all") || (u.getRole() != null && u.getRole().equalsIgnoreCase(currentFilter));
            boolean searchOk = search.isEmpty() ||
                (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(search)) ||
                (u.getNom()    != null && u.getNom().toLowerCase().contains(search)) ||
                (u.getEmail()  != null && u.getEmail().toLowerCase().contains(search));
            return roleOk && searchOk;
        }).collect(Collectors.toList()));
        currentPage = 0;
        refreshCards();
    }

    private void refreshCards() {
        cardContainer.getChildren().clear();
        int total = filtered.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / CARDS_PER_PAGE));
        int from  = currentPage * CARDS_PER_PAGE;
        int to    = Math.min(from + CARDS_PER_PAGE, total);
        lblTotalUsers.setText(total + " utilisateurs au total");
        lblPage.setText("Page " + (currentPage + 1) + " sur " + pages);
        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(currentPage >= pages - 1);
        if (total == 0) {
            Label empty = new Label("Aucun utilisateur trouvé");
            empty.setStyle("-fx-text-fill: #A0B4C8; -fx-font-size: 14px;");
            cardContainer.getChildren().add(empty); return;
        }
        for (int i = from; i < to; i++) cardContainer.getChildren().add(createUserCard(filtered.get(i)));
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E8EFF6; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");
        card.setPrefWidth(280); card.setPadding(new Insets(20)); card.setAlignment(Pos.TOP_CENTER);

        StackPane avatarStack = new StackPane();
        Circle circle = new Circle(45);
        circle.setFill(javafx.scene.paint.Color.web(getRoleColor(user.getRole())));
        Label initials = new Label(getInitials(user));
        initials.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        avatarStack.getChildren().addAll(circle, initials);

        Label name = new Label((user.getPrenom() != null ? user.getPrenom() : "") + " " + (user.getNom() != null ? user.getNom() : ""));
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;"); name.setWrapText(true); name.setAlignment(Pos.CENTER);

        Label email = new Label(user.getEmail() != null ? user.getEmail() : "");
        email.setStyle("-fx-font-size: 12px; -fx-text-fill: #7A9CB8;"); email.setWrapText(true);

        Label roleBadge = new Label(getRoleName(user.getRole()));
        roleBadge.setStyle("-fx-background-color: " + getRoleBg(user.getRole()) + "; -fx-text-fill: " + getRoleColor(user.getRole()) + "; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label idLabel = new Label("ID: #" + user.getId());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #A0B4C8;");

        HBox buttonBox = new HBox(10); buttonBox.setAlignment(Pos.CENTER);
        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
        editBtn.setOnMouseClicked(e -> openUserForm(user));
        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #FDECEA; -fx-text-fill: #C0392B; -fx-border-color: #F1948A; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
        deleteBtn.setOnMouseClicked(e -> deleteUser(user));
        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(avatarStack, name, email, roleBadge, idLabel, new Separator(), buttonBox);
        return card;
    }

    @FXML public void handleAdd() { openUserForm(null); }

    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserFormDialog.fxml"));
            Parent root = loader.load();
            ControllerUserForm ctrl = loader.getController();
            ctrl.setUser(user); ctrl.setOnSaved(() -> loadUsers());
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
        confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { service.delete(user); loadUsers(); } });
    }

    @FXML public void filterAll()          { currentFilter = "all";         applyFilter(); }
    @FXML public void filterEtudiants()    { currentFilter = "etudiant";    applyFilter(); }
    @FXML public void filterPsychologues() { currentFilter = "psychologue"; applyFilter(); }
    @FXML public void filterAdmins()       { currentFilter = "admin";       applyFilter(); }
    @FXML public void handleSearch()       { applyFilter(); }
    @FXML public void onSearch()           { applyFilter(); }
    @FXML public void goToTrash()          { showDeletedUsers(); }
    @FXML public void onAddUser()          { handleAdd(); }

    @FXML public void prevPage() { if (currentPage > 0) { currentPage--; refreshCards(); } }
    @FXML public void nextPage() {
        int pages = (int) Math.ceil((double) filtered.size() / CARDS_PER_PAGE);
        if (currentPage < pages - 1) { currentPage++; refreshCards(); }
    }

    @FXML
    public void toggleView() {
        isListView = !isListView;
        if (toggleViewBtn != null) {
            toggleViewBtn.setText(isListView ? "Vue Cartes" : "Vue Liste");
        }
        refreshCards();
    }

    @FXML public void goToDashboard() { SceneManager.switchTo("HomeAdmin.fxml", "Dashboard Admin"); }
    @FXML public void goToSuivi()     { SceneManager.switchTo("HomeAdmin.fxml", "Dashboard Admin"); }
    @FXML public void handleLogout()  { SessionManager.getInstance().logout(); SceneManager.goToLogin(); }

    @FXML
    public void showDeletedUsers() {
        List<User> deletedUsers = service.getDeletedUsers();
        if (deletedUsers.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "📭 La corbeille est vide.").showAndWait(); return;
        }
        Stage dialog = new Stage();
        dialog.setTitle("🗑️ Corbeille");
        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox root = new VBox(15); root.setPadding(new Insets(20)); root.setStyle("-fx-background-color: #F4F8FF;");
        Label title = new Label("📋 Utilisateurs supprimés");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        VBox list = new VBox(10);
        for (User u : deletedUsers) {
            HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            Label nameL = new Label(u.getPrenom() + " " + u.getNom() + " — " + u.getEmail());
            nameL.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50;");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Button restoreBtn = new Button("♻️ Restaurer");
            restoreBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
            restoreBtn.setOnAction(e -> { service.restore(u); loadUsers(); dialog.close(); });
            row.getChildren().addAll(nameL, sp, restoreBtn);
            list.getChildren().add(row);
        }
        ScrollPane scroll = new ScrollPane(list); scroll.setFitToWidth(true);
        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #7A9CB8; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());
        root.getChildren().addAll(title, scroll, closeBtn);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        dialog.setScene(new Scene(root, 600, 450));
        dialog.showAndWait();
    }

    private String getInitials(User u) {
        String i = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty()) i += u.getPrenom().charAt(0);
        if (u.getNom()    != null && !u.getNom().isEmpty())    i += u.getNom().charAt(0);
        return i.toUpperCase();
    }
    private String getRoleName(String role) {
        if (role == null) return "ÉTUDIANT";
        return switch (role.toLowerCase()) { case "psychologue" -> "PSYCHOLOGUE"; case "admin" -> "ADMIN"; default -> "ÉTUDIANT"; };
    }
    private String getRoleColor(String role) {
        if (role == null) return "#2980B9";
        return switch (role.toLowerCase()) { case "psychologue" -> "#1E8449"; case "admin" -> "#C0392B"; default -> "#2980B9"; };
    }
    private String getRoleBg(String role) {
        if (role == null) return "#E8F5FF";
        return switch (role.toLowerCase()) { case "psychologue" -> "#E8FFF0"; case "admin" -> "#FFF0F0"; default -> "#E8F5FF"; };
    }

    @FXML public void onNavHomeClicked(MouseEvent e)         { AdminSidebarHelper.goToAccueil(); }
    @FXML public void onNavSuiviClicked(MouseEvent e)        { AdminSidebarHelper.goToSuiviMental(); }
    @FXML public void onNavForumClicked(MouseEvent e)        { AdminSidebarHelper.goToForum(); }
    @FXML public void onNavRdvClicked(MouseEvent e)          { AdminSidebarHelper.goToRendezVous(); }
    @FXML public void onNavDossiersClicked(MouseEvent e)     { AdminSidebarHelper.goToDossiers(); }
    @FXML public void onNavUtilisateursClicked(MouseEvent e) { AdminSidebarHelper.goToUtilisateurs(); }
    @FXML public void onNavContenusClicked(MouseEvent e)     { AdminSidebarHelper.goToContenus(); }
    @FXML public void onNavActivitesClicked(MouseEvent e)    { AdminSidebarHelper.goToActivites(); }
    @FXML public void onNavReservationsClicked(MouseEvent e) { AdminSidebarHelper.goToReservations(); }
    @FXML public void onNavHoverEnter(MouseEvent e)          { }
    @FXML public void onNavHoverExit(MouseEvent e)           { }
    @FXML public void onLogout(ActionEvent e)                { AdminSidebarHelper.logout(); }
    @FXML public void onNavSuiviStatsClicked(MouseEvent e)    { AdminSidebarHelper.goToSuiviMental(); }
    @FXML public void onNavObjectifsClicked(MouseEvent e)     { AdminSidebarHelper.goToObjectifs(); }
    @FXML public void onNavSujetsClicked(MouseEvent e)        { AdminSidebarHelper.goToForum(); }
    @FXML public void onNavCommentairesClicked(MouseEvent e)  { AdminSidebarHelper.goToCommentaires(); }
    @FXML public void onSubmenuHoverEnter(MouseEvent e)       { }
    @FXML public void onSubmenuHoverExit(MouseEvent e)        { }
}