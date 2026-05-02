package Controllor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Categorie;
import models.Ressource;
import services.ServiceCategorie;
import services.ServiceRessource;
import utils.SessionManager;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.util.Callback;
import javafx.geometry.Pos;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import services.ModerationService;

public class ControllerAdminRessources {

    @FXML private ImageView logoImage;

    // --- Tab Ressources ---
    @FXML private TextField resTitre, resLien, resImage, searchResField;
    @FXML private TextArea resDescription;
    @FXML private ComboBox<String> resType;
    @FXML private ComboBox<Categorie> resCategorie;
    @FXML private TableView<Ressource> tableRessources;
    @FXML private TableColumn<Ressource, Integer> colResVues;
    @FXML private TableColumn<Ressource, String> colResTitre, colResType, colResCat, colResImage;
    @FXML private TableColumn<Ressource, Void> colResActions;
    @FXML private TableColumn<Ressource, Ressource> colResIA;

    @FXML private Label statTotalRes, statTotalVues, statPopCat, statTopRes;
    @FXML private Label statSafe, statWarning, statToxic;
    @FXML private BarChart<String, Number> viewsChart;
    @FXML private PieChart categoryChart;

    // --- Tab Categories ---
    @FXML private TextField catNom;
    @FXML private TextArea catDescription;
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, Integer> colCatId;
    @FXML private TableColumn<Categorie, String> colCatNom, colCatDesc;
    @FXML private TableColumn<Categorie, Timestamp> colCatDate;
    @FXML private TableColumn<Categorie, Void> colCatActions;

    private ServiceCategorie serviceCategorie = new ServiceCategorie();
    private ServiceRessource serviceRessource = new ServiceRessource();

    private ObservableList<Categorie> categoriesList = FXCollections.observableArrayList();
    private ObservableList<Ressource> ressourcesList = FXCollections.observableArrayList();
    
    private ModerationService moderationService = new ModerationService();

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        // Init Tables
        colCatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCatNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCatDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCatDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        colResTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colResType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colResCat.setCellValueFactory(new PropertyValueFactory<>("categorieNom"));
        colResVues.setCellValueFactory(new PropertyValueFactory<>("nbVues"));

        setupTableActions();

        resType.setItems(FXCollections.observableArrayList("Vidéo", "Article", "Podcast", "Autre"));

        loadCategories();
        loadRessources();

        // Table listeners
        tableCategories.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) fillCatForm(newSel);
        });

        tableRessources.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) fillResForm(newSel);
        });

        // Search listener
        searchResField.textProperty().addListener((obs, oldText, newText) -> filterRessources(newText));
    }

    // --- Categories CRUD ---

    private void loadCategories() {
        categoriesList.setAll(serviceCategorie.getAll());
        tableCategories.setItems(categoriesList);
        resCategorie.setItems(categoriesList);
    }

    private void fillCatForm(Categorie c) {
        catNom.setText(c.getNom());
        catDescription.setText(c.getDescription());
    }

    @FXML
    void clearCatForm() {
        catNom.clear();
        catDescription.clear();
        tableCategories.getSelectionModel().clearSelection();
    }

    @FXML
    void addCategorie(ActionEvent event) {
        if (catNom.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire !");
            return;
        }
        Categorie c = new Categorie(catNom.getText(), catDescription.getText());
        serviceCategorie.add(c);
        loadCategories();
        clearCatForm();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée.");
    }

    @FXML
    void updateCategorie(ActionEvent event) {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une catégorie à modifier.");
            return;
        }
        if (catNom.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire !");
            return;
        }
        selected.setNom(catNom.getText());
        selected.setDescription(catDescription.getText());
        serviceCategorie.update(selected);
        loadCategories();
        clearCatForm();
        loadRessources(); // Update resource table too
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie modifiée.");
    }

    @FXML
    void deleteCategorie(ActionEvent event) {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une catégorie à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette catégorie ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            serviceCategorie.delete(selected);
            loadCategories();
            clearCatForm();
            loadRessources();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée.");
        }
    }

    // --- Ressources CRUD ---

    private void loadRessources() {
        ressourcesList.setAll(serviceRessource.getAll());
        tableRessources.setItems(ressourcesList);
        updateDashboardStats();
    }

    private void updateDashboardStats() {
        // Stats Labels
        statTotalRes.setText(String.valueOf(ressourcesList.size()));
        
        int totalVues = ressourcesList.stream().mapToInt(Ressource::getNbVues).sum();
        statTotalVues.setText(String.valueOf(totalVues));

        // IA Stats
        long safeCount = ressourcesList.stream().filter(r -> "SAFE".equals(r.getModerationStatus())).count();
        long warningCount = ressourcesList.stream().filter(r -> "WARNING".equals(r.getModerationStatus())).count();
        long toxicCount = ressourcesList.stream().filter(r -> "TOXIC".equals(r.getModerationStatus())).count();
        
        if (statSafe != null) statSafe.setText(String.valueOf(safeCount));
        if (statWarning != null) statWarning.setText(String.valueOf(warningCount));
        if (statToxic != null) statToxic.setText(String.valueOf(toxicCount));

        // Most Popular Category
        Map<String, Long> catCounts = ressourcesList.stream()
                .filter(r -> r.getCategorieNom() != null)
                .collect(Collectors.groupingBy(Ressource::getCategorieNom, Collectors.counting()));
        
        Optional<Map.Entry<String, Long>> popCat = catCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue());
        statPopCat.setText(popCat.isPresent() ? popCat.get().getKey() : "-");

        // Most Viewed Resource
        Optional<Ressource> topRes = ressourcesList.stream()
                .max(Comparator.comparingInt(Ressource::getNbVues));
        statTopRes.setText(topRes.isPresent() ? topRes.get().getTitre() : "-");

        // Bar Chart (Views per Resource)
        viewsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Ressource r : ressourcesList) {
            series.getData().add(new XYChart.Data<>(r.getTitre(), r.getNbVues()));
        }
        viewsChart.getData().add(series);

        // Pie Chart (Resources per Category)
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Long> entry : catCounts.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        categoryChart.setData(pieData);
    }

    private void setupTableActions() {
        // Thumbnail Cell
        colResImage.setCellFactory(param -> new TableCell<Ressource, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Ressource r = getTableView().getItems().get(getIndex());
                    try {
                        String imgUrl = r.getImage() != null && !r.getImage().isEmpty() ? r.getImage() : "/Images/default_res.png";
                        Image image = new Image(imgUrl, 40, 40, true, true, true);
                        imageView.setImage(image);
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                    setGraphic(imageView);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // IA Column Cell
        if (colResIA != null) {
            colResIA.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
            colResIA.setCellFactory(param -> new TableCell<Ressource, Ressource>() {
                @Override
                protected void updateItem(Ressource item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(2);
                        container.setAlignment(Pos.CENTER);
                        Label badge = new Label();
                        badge.setText(item.getModerationStatus());
                        
                        if ("SAFE".equals(item.getModerationStatus())) {
                            badge.getStyleClass().add("badge-safe");
                        } else if ("WARNING".equals(item.getModerationStatus())) {
                            badge.getStyleClass().add("badge-warning");
                        } else {
                            badge.getStyleClass().add("badge-toxic");
                        }
                        
                        Label score = new Label(String.format("%.1f%%", item.getModerationScore() * 100));
                        score.getStyleClass().add("ia-score-label");
                        
                        container.getChildren().addAll(badge, score);
                        setGraphic(container);
                    }
                }
            });
        }

        // Actions Cell
        Callback<TableColumn<Ressource, Void>, TableCell<Ressource, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Ressource, Void> call(final TableColumn<Ressource, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("✎");
                    private final Button btnDelete = new Button("🗑");
                    private final Button btnView = new Button("👁");
                    private final HBox pane = new HBox(5, btnView, btnEdit, btnDelete);

                    {
                        pane.setAlignment(Pos.CENTER);
                        btnEdit.getStyleClass().addAll("modern-btn-icon", "modern-btn-edit");
                        btnDelete.getStyleClass().addAll("modern-btn-icon", "modern-btn-delete");
                        btnView.getStyleClass().addAll("modern-btn-icon", "modern-btn-view");

                        btnEdit.setOnAction((ActionEvent event) -> {
                            Ressource r = getTableView().getItems().get(getIndex());
                            fillResForm(r);
                        });

                        btnDelete.setOnAction((ActionEvent event) -> {
                            Ressource r = getTableView().getItems().get(getIndex());
                            tableRessources.getSelectionModel().select(r);
                            deleteRessource(event);
                        });
                        
                        btnView.setOnAction((ActionEvent event) -> {
                            Ressource r = getTableView().getItems().get(getIndex());
                            showAlert(Alert.AlertType.INFORMATION, "Détails", "Titre: " + r.getTitre() + "\nLien: " + r.getLien());
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        colResActions.setCellFactory(cellFactory);

        // Actions Cell for Categories
        Callback<TableColumn<Categorie, Void>, TableCell<Categorie, Void>> catCellFactory = new Callback<>() {
            @Override
            public TableCell<Categorie, Void> call(final TableColumn<Categorie, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("✎");
                    private final Button btnDelete = new Button("🗑");
                    private final HBox pane = new HBox(5, btnEdit, btnDelete);

                    {
                        pane.setAlignment(Pos.CENTER);
                        btnEdit.getStyleClass().addAll("modern-btn-icon", "modern-btn-edit");
                        btnDelete.getStyleClass().addAll("modern-btn-icon", "modern-btn-delete");

                        btnEdit.setOnAction((ActionEvent event) -> {
                            Categorie c = getTableView().getItems().get(getIndex());
                            fillCatForm(c);
                        });

                        btnDelete.setOnAction((ActionEvent event) -> {
                            Categorie c = getTableView().getItems().get(getIndex());
                            tableCategories.getSelectionModel().select(c);
                            deleteCategorie(event);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        colCatActions.setCellFactory(catCellFactory);
    }

    private void filterRessources(String text) {
        if (text == null || text.isEmpty()) {
            tableRessources.setItems(ressourcesList);
            return;
        }
        List<Ressource> filtered = ressourcesList.stream()
                .filter(r -> r.getTitre().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
        tableRessources.setItems(FXCollections.observableArrayList(filtered));
    }

    private void fillResForm(Ressource r) {
        resTitre.setText(r.getTitre());
        resDescription.setText(r.getDescription());
        resType.setValue(r.getType());
        resLien.setText(r.getLien());
        resImage.setText(r.getImage());
        
        // Find matching category in combobox
        for (Categorie c : resCategorie.getItems()) {
            if (c.getId() == r.getCategorieId()) {
                resCategorie.setValue(c);
                break;
            }
        }
    }

    @FXML
    void clearResForm() {
        resTitre.clear();
        resDescription.clear();
        resType.setValue(null);
        resLien.clear();
        resImage.clear();
        resCategorie.setValue(null);
        tableRessources.getSelectionModel().clearSelection();
    }

    @FXML
    void addRessource(ActionEvent event) {
        if (resTitre.getText().isEmpty() || resLien.getText().isEmpty() || resType.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les champs Titre, Type et Lien sont obligatoires.");
            return;
        }

        // --- IA MODERATION ---
        services.ModerationService.ModerationResult modResult = moderationService.analyzeContent(resTitre.getText(), resDescription.getText());
        
        if ("TOXIC".equals(modResult.getStatus())) {
            showAlert(Alert.AlertType.ERROR, "Blocage IA ❌", "Contenu Toxique détecté (" + String.format("%.0f%%", modResult.getScore()*100) + "). " + modResult.getSuggestion());
            return; // Bloque l'ajout
        } else if ("WARNING".equals(modResult.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Avertissement IA ⚠️", modResult.getSuggestion());
        }
        
        int catId = 0;
        if (resCategorie.getValue() != null) {
            catId = resCategorie.getValue().getId();
        }

        Ressource r = new Ressource(
            resTitre.getText(),
            resDescription.getText(),
            resType.getValue(),
            resLien.getText(),
            resImage.getText(),
            catId
        );
        r.setModerationStatus(modResult.getStatus());
        r.setModerationScore(modResult.getScore());
        
        serviceRessource.add(r);
        loadRessources();
        clearResForm();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource ajoutée.");
    }

    @FXML
    void updateRessource(ActionEvent event) {
        Ressource selected = tableRessources.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une ressource à modifier.");
            return;
        }
        if (resTitre.getText().isEmpty() || resLien.getText().isEmpty() || resType.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les champs Titre, Type et Lien sont obligatoires.");
            return;
        }

        // --- IA MODERATION ---
        services.ModerationService.ModerationResult modResult = moderationService.analyzeContent(resTitre.getText(), resDescription.getText());
        
        if ("TOXIC".equals(modResult.getStatus())) {
            showAlert(Alert.AlertType.ERROR, "Blocage IA ❌", "Contenu Toxique détecté (" + String.format("%.0f%%", modResult.getScore()*100) + "). " + modResult.getSuggestion());
            return; // Bloque la modification
        } else if ("WARNING".equals(modResult.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Avertissement IA ⚠️", modResult.getSuggestion());
        }

        selected.setTitre(resTitre.getText());
        selected.setDescription(resDescription.getText());
        selected.setType(resType.getValue());
        selected.setLien(resLien.getText());
        selected.setImage(resImage.getText());
        selected.setModerationStatus(modResult.getStatus());
        selected.setModerationScore(modResult.getScore());
        
        if (resCategorie.getValue() != null) {
            selected.setCategorieId(resCategorie.getValue().getId());
        } else {
            selected.setCategorieId(0);
        }

        serviceRessource.update(selected);
        loadRessources();
        clearResForm();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource modifiée.");
    }

    @FXML
    void deleteRessource(ActionEvent event) {
        Ressource selected = tableRessources.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une ressource à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            serviceRessource.delete(selected);
            loadRessources();
            clearResForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource supprimée.");
        }
    }

    @FXML
    public void exportToPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le rapport PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        
        fileChooser.setInitialFileName("Rapport_Ressources.pdf");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

                // Title
                Paragraph title = new Paragraph("Rapport des Catégories et Ressources", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Categories Table
                Paragraph catTitle = new Paragraph("1. Liste des Catégories", headerFont);
                catTitle.setSpacingAfter(10);
                document.add(catTitle);

                PdfPTable catTable = new PdfPTable(3);
                catTable.setWidthPercentage(100);
                catTable.setWidths(new float[]{1f, 3f, 5f});
                
                catTable.addCell(new PdfPCell(new Phrase("ID", headerFont)));
                catTable.addCell(new PdfPCell(new Phrase("Nom", headerFont)));
                catTable.addCell(new PdfPCell(new Phrase("Description", headerFont)));

                for (Categorie c : categoriesList) {
                    catTable.addCell(new Phrase(String.valueOf(c.getId()), normalFont));
                    catTable.addCell(new Phrase(c.getNom() != null ? c.getNom() : "", normalFont));
                    catTable.addCell(new Phrase(c.getDescription() != null ? c.getDescription() : "", normalFont));
                }
                document.add(catTable);

                document.add(new Paragraph(" "));

                // Ressources Table
                Paragraph resTitle = new Paragraph("2. Liste des Ressources", headerFont);
                resTitle.setSpacingAfter(10);
                resTitle.setSpacingBefore(10);
                document.add(resTitle);

                PdfPTable resTable = new PdfPTable(5);
                resTable.setWidthPercentage(100);
                resTable.setWidths(new float[]{1f, 3f, 2f, 2f, 1f});

                resTable.addCell(new PdfPCell(new Phrase("ID", headerFont)));
                resTable.addCell(new PdfPCell(new Phrase("Titre", headerFont)));
                resTable.addCell(new PdfPCell(new Phrase("Type", headerFont)));
                resTable.addCell(new PdfPCell(new Phrase("Catégorie", headerFont)));
                resTable.addCell(new PdfPCell(new Phrase("Vues", headerFont)));

                for (Ressource r : ressourcesList) {
                    resTable.addCell(new Phrase(String.valueOf(r.getId()), normalFont));
                    resTable.addCell(new Phrase(r.getTitre() != null ? r.getTitre() : "", normalFont));
                    resTable.addCell(new Phrase(r.getType() != null ? r.getType() : "", normalFont));
                    resTable.addCell(new Phrase(r.getCategorieNom() != null ? r.getCategorieNom() : "Non classé", normalFont));
                    resTable.addCell(new Phrase(String.valueOf(r.getNbVues()), normalFont));
                }
                document.add(resTable);

                document.close();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'exportation PDF a été réalisée avec succès !");

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'exportation PDF : " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Navigation Admin ---
    private void loadPage(String fxml, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void goToDashboard(MouseEvent event) { loadPage("/HomeAdmin.fxml", event); }
    @FXML void goToUsers(MouseEvent event) { loadPage("/AdminUsers.fxml", event); }
    @FXML void goToStats(MouseEvent event) { loadPage("/StatsAdmin.fxml", event); }

    @FXML
    void handleLogout(MouseEvent event) {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onHoverEnter(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        src.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
    }

    @FXML
    private void onHoverExit(MouseEvent event) {
        HBox src = (HBox) event.getSource();
        src.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
    }
}
