package Controllor;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Categorie;
import models.Ressource;
import models.User;
import services.ServiceCategorie;
import services.ServiceRessource;
import utils.SceneManager;
import utils.SessionManager;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class ControllerStudentRessources {

    @FXML private ImageView logoImage;
    @FXML private Label avatarInitials, labelUserName, badgeRdv;
    @FXML private Button menuButton, notifButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<Categorie> categoryFilter;
    @FXML private FlowPane resourcesContainer;

    private ServiceRessource serviceRessource = new ServiceRessource();
    private ServiceCategorie serviceCategorie = new ServiceCategorie();
    private List<Ressource> allRessources;

    private ContextMenu contextMenu;

    @FXML
    public void initialize() {
        // User Info
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            labelUserName.setText(user.getPrenom() + " " + user.getNom());
            String initials = "";
            if (user.getPrenom() != null && !user.getPrenom().isEmpty())
                initials += user.getPrenom().charAt(0);
            if (user.getNom() != null && !user.getNom().isEmpty())
                initials += user.getNom().charAt(0);
            avatarInitials.setText(initials.toUpperCase());
        }

        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        setupMenu();
        loadCategories();
        loadRessources();

        // Search Filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterRessources());
        categoryFilter.setOnAction(e -> filterRessources());
    }

    private void setupMenu() {
        contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");

        MenuItem modifierProfil = new MenuItem("✏️  Modifier mon profil");
        modifierProfil.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        modifierProfil.setOnAction(e -> SceneManager.switchTo("Profile.fxml", "Mon Profil"));

        MenuItem deconnexion = new MenuItem("🚪  Déconnexion");
        deconnexion.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-text-fill: #e74c3c;");
        deconnexion.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneManager.goToLogin();
        });

        contextMenu.getItems().addAll(modifierProfil, deconnexion);
    }

    @FXML
    public void onMenuClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        contextMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 5);
    }

    private void loadCategories() {
        List<Categorie> categories = serviceCategorie.getAll();
        Categorie all = new Categorie();
        all.setId(0);
        all.setNom("Toutes les catégories");
        categoryFilter.getItems().add(all);
        categoryFilter.getItems().addAll(categories);
        categoryFilter.getSelectionModel().select(0);
    }

    private void loadRessources() {
        allRessources = serviceRessource.getAll();
        displayRessources(allRessources);
    }

    private void filterRessources() {
        String searchText = searchField.getText().toLowerCase();
        Categorie selectedCat = categoryFilter.getSelectionModel().getSelectedItem();

        List<Ressource> filtered = allRessources.stream()
                .filter(r -> r.getTitre().toLowerCase().contains(searchText) || r.getDescription().toLowerCase().contains(searchText))
                .filter(r -> selectedCat == null || selectedCat.getId() == 0 || r.getCategorieId() == selectedCat.getId())
                .collect(Collectors.toList());

        displayRessources(filtered);
    }

    private void displayRessources(List<Ressource> ressources) {
        resourcesContainer.getChildren().clear();
        for (Ressource r : ressources) {
            resourcesContainer.getChildren().add(createRessourceCard(r));
        }
    }

    private VBox createRessourceCard(Ressource r) {
        VBox card = new VBox(10);
        card.getStyleClass().add("student-card");
        card.setPrefWidth(220);

        String typeText = r.getType() != null ? r.getType() : "Ressource";
        String icon = "📄";
        if (typeText.toLowerCase().contains("vidéo") || typeText.toLowerCase().contains("video")) icon = "🎥";
        else if (typeText.toLowerCase().contains("podcast") || typeText.toLowerCase().contains("audio")) icon = "🎧";
        
        Label typeLabel = new Label(icon + " " + typeText);
        typeLabel.getStyleClass().add("badge-type");

        Label catLabel = new Label(r.getCategorieNom() != null ? r.getCategorieNom() : "Non classé");
        catLabel.setStyle("-fx-text-fill: #7A8C9A; -fx-font-size: 11px;");

        HBox topBox = new HBox(10, typeLabel, catLabel);
        topBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label(r.getTitre());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(r.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A6C7D;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);

        Label viewsLabel = new Label("👀 " + r.getNbVues() + " vues");
        viewsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7A8C9A;");

        Button openBtn = new Button("Ouvrir");
        openBtn.getStyleClass().add("student-btn-primary");
        openBtn.setMaxWidth(Double.MAX_VALUE);
        openBtn.setOnAction(e -> openLink(r));

        card.getChildren().addAll(topBox, titleLabel, descLabel, viewsLabel, openBtn);

        // Hover effect
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.03); st.setToY(1.03); st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });

        return card;
    }

    private void openLink(Ressource r) {
        if (r.getLien() != null && !r.getLien().isEmpty()) {
            try {
                Desktop.getDesktop().browse(new URI(r.getLien()));
                // Increment views
                serviceRessource.incrementViews(r.getId());
                r.setNbVues(r.getNbVues() + 1);
                loadRessources(); // Refresh view
            } catch (Exception ex) {
                System.out.println("Impossible d'ouvrir le lien: " + ex.getMessage());
            }
        } else {
            System.out.println("Aucun lien pour cette ressource");
        }
    }

    // Navigation
    private void loadPage(String fxml, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void onNavHomeClicked(MouseEvent event)      { loadPage("/home.fxml", event); }
    @FXML void onNavSuiviClicked(MouseEvent event)     { System.out.println("Suivi"); }
    @FXML void onNavRdvClicked(MouseEvent event)       { System.out.println("RDV"); }
    @FXML void onNavBlogClicked(MouseEvent event)      { loadPage("/forum.fxml", event); }
    @FXML void onNavActivitesClicked(MouseEvent event) { System.out.println("Activités"); }

    @FXML
    private void onNavHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), src);
        st.setToX(1.02); st.setToY(1.02); st.play();
        src.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }

    @FXML
    private void onNavHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), src);
        st.setToX(1.0); st.setToY(1.0); st.play();
        src.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
    }

    @FXML
    private void onNotifications(ActionEvent event) {
        Button btn = (Button) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
        st.setToX(1.1); st.setToY(1.1);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }
}
