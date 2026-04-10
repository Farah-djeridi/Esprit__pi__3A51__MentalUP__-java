package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Activite;
import models.Objectif;
import services.ServiceObjectif;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObjectifController {

    @FXML private Button ajouterTabButton;
    @FXML private Button historiqueTabButton;
    @FXML private Button resumeTabButton;
    @FXML private Button saveButton;

    @FXML private ScrollPane ajouterSectionScroll;
    @FXML private VBox ajouterSection;
    @FXML private VBox historiqueSection;
    @FXML private VBox resumeSection;

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> statutCombo;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<Activite> activiteCombo;
    @FXML private ComboBox<String> typeObjectifCombo;
    @FXML private TextField valeurCibleField;

    @FXML private Label messageLabel;
    @FXML private Label globalMessageLabel;

    @FXML private VBox historiqueContainer;

    @FXML private TextField searchTitreField;
    @FXML private DatePicker searchDateCreationPicker;
    @FXML private ComboBox<String> searchStatutCombo;

    @FXML private Label totalObjectifsLabel;
    @FXML private Label objectifsAtteintsLabel;
    @FXML private Label objectifsEnCoursLabel;

    private final ServiceObjectif service = new ServiceObjectif();
    private Integer editingObjectifId = null;

    // à remplacer plus tard par l'utilisateur connecté
    private final int currentUserId = 1;

    @FXML
    public void initialize() {
        statutCombo.setItems(FXCollections.observableArrayList(
                "en cours", "terminé", "annulé"
        ));
        statutCombo.setValue("en cours");

        typeObjectifCombo.setItems(FXCollections.observableArrayList(
                "stress",
                "sommeil",
                "energie",
                "score_mentale",
                "stress_global"
        ));
        typeObjectifCombo.setValue("stress");

        activiteCombo.setItems(FXCollections.observableArrayList(service.getActivites()));

        searchStatutCombo.setItems(FXCollections.observableArrayList(
                "", "en cours", "terminé", "annulé"
        ));
        searchStatutCombo.setValue("");

        afficherGlobalMessage("", false);
        refreshAll();
        setActiveTab("ajouter");
    }

    @FXML
    public void showAjouterSection() {
        setActiveTab("ajouter");
    }

    @FXML
    public void showHistoriqueSection() {
        refreshHistorique();
        setActiveTab("historique");
    }

    @FXML
    public void showResumeSection() {
        refreshResume();
        setActiveTab("resume");
    }

    private void setActiveTab(String tab) {
        boolean isAjouter = "ajouter".equals(tab);
        boolean isHistorique = "historique".equals(tab);
        boolean isResume = "resume".equals(tab);

        ajouterSectionScroll.setVisible(isAjouter);
        ajouterSectionScroll.setManaged(isAjouter);

        historiqueSection.setVisible(isHistorique);
        historiqueSection.setManaged(isHistorique);

        resumeSection.setVisible(isResume);
        resumeSection.setManaged(isResume);

        updateTabStyles(tab);
    }

    private void updateTabStyles(String activeTab) {
        String normalStyle =
                "-fx-background-color: #f8fbff;" +
                        "-fx-text-fill: #234b7d;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-border-color: #d7e5f7;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 18;" +
                        "-fx-cursor: hand;";

        String activeStyle =
                "-fx-background-color: linear-gradient(to right, #3b82f6, #2C5F8A);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 18;" +
                        "-fx-cursor: hand;";

        ajouterTabButton.setStyle(normalStyle);
        historiqueTabButton.setStyle(normalStyle);
        resumeTabButton.setStyle(normalStyle);

        switch (activeTab) {
            case "ajouter" -> ajouterTabButton.setStyle(activeStyle);
            case "historique" -> historiqueTabButton.setStyle(activeStyle);
            case "resume" -> resumeTabButton.setStyle(activeStyle);
        }
    }

    @FXML
    public void ajouterObjectif() {
        try {
            if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
                afficherMessage("Le titre est obligatoire.", true);
                return;
            }

            if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
                afficherMessage("La description est obligatoire.", true);
                return;
            }

            if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
                afficherMessage("Les dates sont obligatoires.", true);
                return;
            }

            if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                afficherMessage("La date de fin doit être après la date de début.", true);
                return;
            }

            Activite activiteSelectionnee = activiteCombo.getValue();
            if (activiteSelectionnee == null) {
                afficherMessage("Veuillez choisir une activité.", true);
                return;
            }

            if (typeObjectifCombo.getValue() == null) {
                afficherMessage("Veuillez choisir un type d’objectif.", true);
                return;
            }

            if (statutCombo.getValue() == null || statutCombo.getValue().trim().isEmpty()) {
                afficherMessage("Veuillez choisir un statut.", true);
                return;
            }

            if (valeurCibleField.getText() == null || valeurCibleField.getText().trim().isEmpty()) {
                afficherMessage("La valeur cible est obligatoire.", true);
                return;
            }

            double valeurCible;
            try {
                valeurCible = Double.parseDouble(valeurCibleField.getText().trim());
            } catch (NumberFormatException e) {
                afficherMessage("La valeur cible doit être numérique.", true);
                return;
            }

            String statutChoisi = statutCombo.getValue().trim().toLowerCase();

            // règle métier : un seul objectif "en cours" à la fois
            Objectif objectifEnCours = service.getObjectifEnCoursByUser(currentUserId);

            if ("en cours".equals(statutChoisi)) {
                if (editingObjectifId == null) {
                    if (objectifEnCours != null) {
                        afficherMessage("Vous avez déjà un objectif en cours. Terminez ou annulez-le avant d'en ajouter un autre.", true);
                        return;
                    }
                } else {
                    if (objectifEnCours != null && objectifEnCours.getId() != editingObjectifId) {
                        afficherMessage("Vous avez déjà un autre objectif en cours. Impossible d'en avoir plusieurs.", true);
                        return;
                    }
                }
            }

            Objectif obj = new Objectif();
            obj.setTitre(titreField.getText().trim());
            obj.setDescription(descriptionArea.getText().trim());
            obj.setStatutObjectif(statutChoisi);
            obj.setDateDebut(Date.valueOf(dateDebutPicker.getValue()));
            obj.setDateFin(Date.valueOf(dateFinPicker.getValue()));
            obj.setTypeObjectif(typeObjectifCombo.getValue());
            obj.setValeurCible(valeurCible);
            obj.setIdActivite(activiteSelectionnee.getIdActivite());
            obj.setUserId(currentUserId);

            if (editingObjectifId == null) {
                obj.setDateCreation(Date.valueOf(LocalDate.now()));
                obj.setProgression(0);

                service.add(obj);
                afficherGlobalMessage("Objectif ajouté avec succès.", false);
            } else {
                Objectif ancien = service.getById(editingObjectifId);

                obj.setId(editingObjectifId);
                if (ancien != null && ancien.getDateCreation() != null) {
                    obj.setDateCreation(ancien.getDateCreation());
                    obj.setProgression(ancien.getProgression());
                } else {
                    obj.setDateCreation(Date.valueOf(LocalDate.now()));
                    obj.setProgression(0);
                }

                service.update(obj);
                afficherGlobalMessage("Objectif modifié avec succès.", false);
            }

            clearForm();
            refreshAll();
            setActiveTab("historique");

        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    public void annulerFormulaire() {
        clearForm();
        afficherMessage("", false);
    }

    @FXML
    public void filtrerHistorique() {
        refreshHistorique();
    }

    @FXML
    public void resetFiltresHistorique() {
        searchTitreField.clear();
        searchDateCreationPicker.setValue(null);
        searchStatutCombo.setValue("");
        refreshHistorique();
    }

    private void refreshAll() {
        refreshHistorique();
        refreshResume();
    }

    private void refreshHistorique() {
        historiqueContainer.getChildren().clear();

        List<Objectif> objectifs = service.getByUser(currentUserId);
        List<Objectif> filtres = new ArrayList<>();

        String titreRecherche = searchTitreField != null ? searchTitreField.getText().trim().toLowerCase() : "";
        LocalDate dateRecherche = searchDateCreationPicker != null ? searchDateCreationPicker.getValue() : null;
        String statutRecherche = searchStatutCombo != null && searchStatutCombo.getValue() != null
                ? searchStatutCombo.getValue().trim().toLowerCase()
                : "";

        for (Objectif o : objectifs) {
            boolean match = true;

            if (!titreRecherche.isEmpty() &&
                    (o.getTitre() == null || !o.getTitre().toLowerCase().contains(titreRecherche))) {
                match = false;
            }

            if (dateRecherche != null) {
                if (o.getDateCreation() == null || !o.getDateCreation().toLocalDate().equals(dateRecherche)) {
                    match = false;
                }
            }

            if (!statutRecherche.isEmpty()) {
                if (o.getStatutObjectif() == null || !o.getStatutObjectif().equalsIgnoreCase(statutRecherche)) {
                    match = false;
                }
            }

            if (match) {
                filtres.add(o);
            }
        }

        if (filtres.isEmpty()) {
            Label vide = new Label("Aucun objectif trouvé.");
            vide.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: 700;");
            historiqueContainer.getChildren().add(vide);
            return;
        }

        for (Objectif o : filtres) {
            VBox card = new VBox(10);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-color: #dbe7f5;" +
                            "-fx-border-radius: 18;" +
                            "-fx-padding: 16;" +
                            "-fx-effect: dropshadow(gaussian, rgba(30,58,95,0.05), 8, 0, 0, 2);"
            );

            HBox topRow = new HBox(10);

            Label titre = new Label(o.getTitre() == null ? "(Sans titre)" : o.getTitre());
            titre.setStyle("-fx-text-fill: #1e3a5f; -fx-font-size: 18px; -fx-font-weight: 900;");

            Region spacerTop = new Region();
            HBox.setHgrow(spacerTop, Priority.ALWAYS);

            Label statutBadge = new Label(normalizeStatut(o.getStatutObjectif()));
            statutBadge.setStyle(getStatutBadgeStyle(o.getStatutObjectif()));

            topRow.getChildren().addAll(titre, spacerTop, statutBadge);

            Label details = new Label(
                    "Type : " + safe(o.getTypeObjectif())
                            + "   |   Cible : " + o.getValeurCible()
                            + "   |   Créé le : " + safeDate(o.getDateCreation())
            );
            details.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-weight: 700;");

            Label dates = new Label("Période : " + safeDate(o.getDateDebut()) + " → " + safeDate(o.getDateFin()));
            dates.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-weight: 700;");

            Label desc = new Label(safe(o.getDescription()));
            desc.setWrapText(true);
            desc.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px; -fx-font-weight: 500;");

            double progress = Math.max(0, Math.min(1, o.getProgression() / 100.0));
            ProgressBar progressBar = new ProgressBar(progress);
            progressBar.setPrefWidth(520);
            progressBar.setStyle("-fx-accent: #2f5d97;");

            Label progressionLabel = new Label("Progression : " + o.getProgression() + "%");
            progressionLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #184b9b;");

            Button modifierBtn = new Button("Modifier");
            modifierBtn.setStyle(
                    "-fx-background-color: #eef4fb;" +
                            "-fx-text-fill: #2b5b9d;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 800;" +
                            "-fx-background-radius: 18;" +
                            "-fx-padding: 9 16;" +
                            "-fx-cursor: hand;"
            );
            modifierBtn.setOnAction(e -> confirmerModification(o));

            Button supprimerBtn = new Button("Supprimer");
            supprimerBtn.setStyle(
                    "-fx-background-color: #fff1f1;" +
                            "-fx-text-fill: #d94b4b;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 800;" +
                            "-fx-background-radius: 18;" +
                            "-fx-padding: 9 16;" +
                            "-fx-cursor: hand;"
            );
            supprimerBtn.setOnAction(e -> confirmerSuppression(o));

            HBox actions = new HBox(10, modifierBtn, supprimerBtn);

            card.getChildren().addAll(topRow, details, dates, desc, progressionLabel, progressBar, actions);
            historiqueContainer.getChildren().add(card);
        }
    }

    private void refreshResume() {
        List<Objectif> objectifs = service.getByUser(currentUserId);

        int total = objectifs.size();
        int atteints = 0;
        int enCours = 0;

        for (Objectif o : objectifs) {
            if (isTermine(o.getStatutObjectif())) {
                atteints++;
            } else if ("en cours".equalsIgnoreCase(o.getStatutObjectif())) {
                enCours++;
            }
        }

        totalObjectifsLabel.setText(String.valueOf(total));

        if (objectifsAtteintsLabel != null) {
            objectifsAtteintsLabel.setText(String.valueOf(atteints));
        }

        if (objectifsEnCoursLabel != null) {
            objectifsEnCoursLabel.setText(String.valueOf(enCours));
        }
    }

    private void confirmerModification(Objectif o) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Modifier cet objectif ?");
        alert.setContentText("Voulez-vous charger cet objectif dans le formulaire pour le modifier ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            chargerObjectifDansFormulaire(o);
            afficherGlobalMessage("Objectif prêt à être modifié.", false);
        }
    }

    private void confirmerSuppression(Objectif o) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cet objectif ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(o);
            refreshAll();
            afficherGlobalMessage("Objectif supprimé avec succès.", false);
        }
    }

    private void chargerObjectifDansFormulaire(Objectif o) {
        editingObjectifId = o.getId();

        titreField.setText(o.getTitre());
        descriptionArea.setText(o.getDescription());
        statutCombo.setValue(normalizeStatut(o.getStatutObjectif()));

        if (o.getDateDebut() != null) {
            dateDebutPicker.setValue(o.getDateDebut().toLocalDate());
        } else {
            dateDebutPicker.setValue(null);
        }

        if (o.getDateFin() != null) {
            dateFinPicker.setValue(o.getDateFin().toLocalDate());
        } else {
            dateFinPicker.setValue(null);
        }

        for (Activite a : activiteCombo.getItems()) {
            if (o.getIdActivite() != null && a.getIdActivite() == o.getIdActivite()) {
                activiteCombo.setValue(a);
                break;
            }
        }

        typeObjectifCombo.setValue(o.getTypeObjectif());
        valeurCibleField.setText(String.valueOf(o.getValeurCible()));

        saveButton.setText("Mettre à jour");
        setActiveTab("ajouter");
    }

    private void clearForm() {
        editingObjectifId = null;
        titreField.clear();
        descriptionArea.clear();
        statutCombo.setValue("en cours");
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        activiteCombo.setValue(null);
        typeObjectifCombo.setValue("stress");
        valeurCibleField.clear();
        saveButton.setText("💾 Ajouter");
    }

    private boolean isTermine(String statut) {
        return "terminé".equalsIgnoreCase(statut)
                || "termine".equalsIgnoreCase(statut)
                || "atteint".equalsIgnoreCase(statut);
    }

    private String normalizeStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            return "en cours";
        }
        if ("atteint".equalsIgnoreCase(statut) || "terminé".equalsIgnoreCase(statut) || "termine".equalsIgnoreCase(statut)) {
            return "terminé";
        }
        if ("annule".equalsIgnoreCase(statut)) {
            return "annulé";
        }
        return statut.toLowerCase();
    }

    private String getStatutBadgeStyle(String statut) {
        String normalized = normalizeStatut(statut);

        if ("terminé".equalsIgnoreCase(normalized)) {
            return "-fx-background-color: #eafaf1;" +
                    "-fx-text-fill: #1e8449;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 900;" +
                    "-fx-background-radius: 999;" +
                    "-fx-padding: 6 12;";
        }

        if ("annulé".equalsIgnoreCase(normalized)) {
            return "-fx-background-color: #fff1f1;" +
                    "-fx-text-fill: #d94b4b;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 900;" +
                    "-fx-background-radius: 999;" +
                    "-fx-padding: 6 12;";
        }

        return "-fx-background-color: #eaf3ff;" +
                "-fx-text-fill: #2b5b9d;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 900;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 6 12;";
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String safeDate(Date date) {
        return date == null ? "-" : date.toString();
    }

    private void afficherMessage(String msg, boolean erreur) {
        messageLabel.setText(msg == null ? "" : msg);
        if (erreur) {
            messageLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #1e8449; -fx-font-weight: bold;");
        }
    }

    private void afficherGlobalMessage(String msg, boolean erreur) {
        globalMessageLabel.setText(msg == null ? "" : msg);

        if (msg == null || msg.isEmpty()) {
            globalMessageLabel.setStyle(
                    "-fx-padding: 0;" +
                            "-fx-font-size: 0;" +
                            "-fx-background-color: transparent;"
            );
            return;
        }

        if (erreur) {
            globalMessageLabel.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #c0392b;" +
                            "-fx-background-color: #fff1f1;" +
                            "-fx-background-radius: 16;" +
                            "-fx-padding: 8 14;"
            );
        } else {
            globalMessageLabel.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #1e8449;" +
                            "-fx-background-color: #eafaf1;" +
                            "-fx-background-radius: 16;" +
                            "-fx-padding: 8 14;"
            );
        }
    }
}