package Controllor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    @FXML private TableView<User>          tableUsers;
    @FXML private TableColumn<User, Void>  colAvatar;
    @FXML private TableColumn<User, String> colNomPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, Void>  colRole;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, Void>  colActions;

    @FXML private TextField txtSearch;
    @FXML private Label     lblTotalUsers;
    @FXML private Label     lblPage;
    @FXML private Label     lblPageNum;
    @FXML private Button    btnPrev;
    @FXML private Button    btnNext;

    @FXML private VBox      detailPane;
    @FXML private Label     detailInitials;
    @FXML private Label     detailName;
    @FXML private Label     detailEmail;
    @FXML private Label     detailRoleBadge;
    @FXML private Label     detailId;

    @FXML private ImageView logoImage;

    private final ServiceUser service = new ServiceUser();
    private ObservableList<User> allUsers   = FXCollections.observableArrayList();
    private ObservableList<User> filtered   = FXCollections.observableArrayList();
    private User selectedUser = null;

    private static final int PAGE_SIZE = 8;
    private int currentPage = 0;
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        try { logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png"))); }
        catch (Exception ignored) {}

        setupColumns();
        loadUsers();

        // Sélection → panneau détail
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, user) -> {
            if (user != null) showDetail(user);
        });
    }

    // ===== COLONNES =====
    private void setupColumns() {
        // Colonne avatar (initiales colorées)
        colAvatar.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                User u = (User) getTableRow().getItem();
                javafx.scene.layout.StackPane sp = new javafx.scene.layout.StackPane();
                javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(18);
                c.setFill(javafx.scene.paint.Color.web(roleColor(u.getRole())));
                String init = initials(u);
                Label lbl = new Label(init);
                lbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                sp.getChildren().addAll(c, lbl);
                setGraphic(sp);
            }
        });

        // Nom/Prénom + ID
        colNomPrenom.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                (d.getValue().getPrenom() != null ? d.getValue().getPrenom() : "") + " " +
                        (d.getValue().getNom() != null ? d.getValue().getNom() : "")));
        colNomPrenom.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setGraphic(null); return; }
                User u = getTableRow() != null ? (User) getTableRow().getItem() : null;
                VBox vb = new VBox(2);
                Label n = new Label(name.trim()); n.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2C3E50;");
                Label id = new Label("ID: #" + (u != null ? u.getId() : "")); id.setStyle("-fx-font-size: 11px; -fx-text-fill: #A0B4C8;");
                vb.getChildren().addAll(n, id);
                setGraphic(vb);
            }
        });

        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) { setText(null); return; }
                setText(email);
                setStyle("-fx-font-size: 12px; -fx-text-fill: #5A6C7D;");
            }
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Badge rôle coloré
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                User u = (User) getTableRow().getItem();
                Label badge = new Label(roleName(u.getRole()));
                badge.setStyle("-fx-background-color: " + roleBg(u.getRole()) + ";" +
                        "-fx-text-fill: " + roleColor(u.getRole()) + ";" +
                        "-fx-background-radius: 8; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge);
            }
        });

        // Boutons Modifier / Réinitialiser
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit  = new Button("✏️ Modifier");
            private final Button btnReset = new Button("🔑 Réinit.");
            {
                btnEdit.setStyle("-fx-background-color: #EAF0FF; -fx-text-fill: #2C5F8A; -fx-border-color: #AED6F1; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 4 8;");
                btnReset.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #E67E22; -fx-border-color: #FAD7A0; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 4 8;");
                btnEdit.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    openUserForm(u);
                });
                btnReset.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    resetPassword(u);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, btnEdit, btnReset);
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });
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
        refreshTable();
    }

    private void refreshTable() {
        int total = filtered.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int from  = currentPage * PAGE_SIZE;
        int to    = Math.min(from + PAGE_SIZE, total);

        tableUsers.setItems(FXCollections.observableArrayList(
                total > 0 ? filtered.subList(from, to) : List.of()));

        lblTotalUsers.setText(total + " utilisateurs au total");
        lblPage.setText("Page " + (currentPage + 1) + " sur " + pages + " (" + total + " utilisateurs)");
        lblPageNum.setText((currentPage + 1) + " / " + pages);
        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(currentPage >= pages - 1);
    }

    // ===== FILTRES =====
    @FXML public void filterAll()         { currentFilter = "all";         applyFilter(); }
    @FXML public void filterEtudiants()   { currentFilter = "etudiant";    applyFilter(); }
    @FXML public void filterPsychologues(){ currentFilter = "psychologue"; applyFilter(); }
    @FXML public void filterAdmins()      { currentFilter = "admin";       applyFilter(); }
    @FXML public void handleSearch()      { applyFilter(); }
    @FXML public void prevPage()          { if (currentPage > 0) { currentPage--; refreshTable(); } }
    @FXML public void nextPage()          {
        int pages = (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
        if (currentPage < pages - 1) { currentPage++; refreshTable(); }
    }

    // ===== DÉTAIL =====
    private void showDetail(User u) {
        selectedUser = u;
        detailInitials.setText(initials(u));
        detailName.setText((u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : ""));
        detailEmail.setText(u.getEmail() != null ? u.getEmail() : "");
        detailRoleBadge.setText(roleName(u.getRole()));
        detailRoleBadge.setStyle("-fx-background-color: " + roleBg(u.getRole()) + ";" +
                "-fx-text-fill: " + roleColor(u.getRole()) + ";" +
                "-fx-background-radius: 8; -fx-padding: 3 10; -fx-font-size: 12px; -fx-font-weight: bold;");
        detailId.setText("#" + u.getId());
    }

    // ===== CRUD =====
    @FXML public void handleAdd()           { openUserForm(null); }
    @FXML public void handleEditSelected()  { if (selectedUser != null) openUserForm(selectedUser); }

    @FXML public void handleDeleteSelected() {
        if (selectedUser == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer " + selectedUser.getPrenom() + " " + selectedUser.getNom() + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                service.delete(selectedUser);
                selectedUser = null;
                detailName.setText("Sélectionnez un utilisateur");
                detailEmail.setText("");
                detailRoleBadge.setText("");
                detailId.setText("");
                detailInitials.setText("--");
                loadUsers();
            }
        });
    }

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

    private void resetPassword(User u) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Réinitialiser le mot de passe");
        info.setHeaderText("Mot de passe réinitialisé");
        info.setContentText("Un email de réinitialisation serait envoyé à : " + u.getEmail());
        info.showAndWait();
    }

    // ===== NAVIGATION =====
    @FXML public void goToDashboard() { SceneManager.switchTo("HomeAdmin.fxml", "Dashboard Admin"); }
    @FXML public void handleLogout()  { SessionManager.getInstance().logout(); SceneManager.goToLogin(); }

    @FXML private void onNavHover(MouseEvent e) {
        ((HBox) e.getSource()).setStyle("-fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
    }
    @FXML private void onNavExit(MouseEvent e) {
        ((HBox) e.getSource()).setStyle("-fx-background-color: transparent; " +
                "-fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
    }

    // ===== HELPERS =====
    private String initials(User u) {
        String i = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty()) i += u.getPrenom().charAt(0);
        if (u.getNom() != null && !u.getNom().isEmpty()) i += u.getNom().charAt(0);
        return i.toUpperCase();
    }
    private String roleName(String role) {
        if (role == null) return "ÉTUDIANT";
        return switch (role.toLowerCase()) {
            case "psychologue" -> "PSYCHOLOGUE";
            case "admin"       -> "ADMIN";
            default            -> "ÉTUDIANT";
        };
    }
    private String roleColor(String role) {
        if (role == null) return "#2980B9";
        return switch (role.toLowerCase()) {
            case "psychologue" -> "#1E8449";
            case "admin"       -> "#C0392B";
            default            -> "#2980B9";
        };
    }
    private String roleBg(String role) {
        if (role == null) return "#E8F5FF";
        return switch (role.toLowerCase()) {
            case "psychologue" -> "#E8FFF0";
            case "admin"       -> "#FFF0F0";
            default            -> "#E8F5FF";
        };
    }
}
