package utils;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Classe utilitaire pour la vérification anti-robot (CAPTCHA)
 */
public class CaptchaVerification {
    
    private static final Random random = new Random();
    
    /**
     * Interface pour le callback de résultat de vérification
     */
    public interface CaptchaCallback {
        void onResult(boolean success);
    }
    
    /**
     * Affiche une popup de vérification CAPTCHA avec images et glissement
     * @param parentStage La fenêtre parente
     * @param action L'action à effectuer (ex: "noter cette activité", "réserver cette place")
     * @param callback Le callback à appeler avec le résultat
     */
    public static void afficherVerification(Stage parentStage, String action, CaptchaCallback callback) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(parentStage);
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setTitle("Vérification de sécurité");
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 5);");
        root.setPrefWidth(450);
        
        // Titre
        Label titre = new Label("🤖 Vérification de sécurité");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        Label description = new Label("Pour " + action + ", veuillez confirmer que vous n'êtes pas un robot");
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; -fx-wrap-text: true; -fx-text-alignment: center;");
        description.setMaxWidth(390);
        
        // Choisir le type de CAPTCHA aléatoirement
        int typeCaptcha = random.nextInt(3);
        
        VBox captchaContainer;
        boolean[] captchaReussi = {false};
        
        switch (typeCaptcha) {
            case 0:
                captchaContainer = creerCaptchaDragDrop(captchaReussi);
                break;
            case 1:
                captchaContainer = creerCaptchaSlider(captchaReussi);
                break;
            case 2:
                captchaContainer = creerCaptchaSelection(captchaReussi);
                break;
            default:
                captchaContainer = creerCaptchaDragDrop(captchaReussi);
        }
        
        // Message d'erreur
        Label erreurLabel = new Label("");
        erreurLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 12px;");
        
        // Boutons
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-size: 13px; " +
                           "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        btnAnnuler.setOnAction(e -> {
            popup.close();
            callback.onResult(false);
        });
        
        Button btnVerifier = new Button("✅ Vérifier");
        btnVerifier.setStyle("-fx-background-color: #4299e1; -fx-text-fill: white; -fx-font-size: 13px; " +
                            "-fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 8;");
        
        btnVerifier.setOnAction(e -> {
            if (captchaReussi[0]) {
                // Animation de succès
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), btnVerifier);
                scale.setToX(1.1);
                scale.setToY(1.1);
                scale.setAutoReverse(true);
                scale.setCycleCount(2);
                scale.setOnFinished(ev -> {
                    popup.close();
                    callback.onResult(true);
                });
                scale.play();
            } else {
                erreurLabel.setText("❌ Veuillez compléter la vérification");
                // Animation d'erreur
                ScaleTransition shake = new ScaleTransition(Duration.millis(100), captchaContainer);
                shake.setToX(0.95);
                shake.setAutoReverse(true);
                shake.setCycleCount(4);
                shake.play();
            }
        });
        
        HBox boutons = new HBox(15, btnAnnuler, btnVerifier);
        boutons.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(titre, description, captchaContainer, erreurLabel, boutons);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        
        // Centrer la popup
        popup.setOnShown(e -> {
            if (parentStage != null) {
                popup.setX(parentStage.getX() + (parentStage.getWidth() - popup.getWidth()) / 2);
                popup.setY(parentStage.getY() + (parentStage.getHeight() - popup.getHeight()) / 2);
            }
        });
        
        popup.show();
    }
    
    
    /**
     * Crée un CAPTCHA avec drag & drop d'images
     */
    private static VBox creerCaptchaDragDrop(boolean[] reussi) {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #f7fafc; -fx-padding: 20; -fx-background-radius: 10; " +
                          "-fx-border-color: #e2e8f0; -fx-border-width: 2; -fx-border-radius: 10;");

        Label instruction = new Label("Glissez les ⭐ dans la zone de dépôt");
        instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // Éléments mélangés : 4 étoiles + 3 distracteurs
        List<String> elementsList = new ArrayList<>(Arrays.asList(
                "⭐", "🎪", "⭐", "🎨", "⭐", "🎭", "⭐"));
        Collections.shuffle(elementsList);

        final int totalCibles = 4; // toujours 4 étoiles

        // Zone source
        HBox sourceZone = new HBox(8);
        sourceZone.setAlignment(Pos.CENTER);
        sourceZone.setPadding(new Insets(10));
        sourceZone.setStyle("-fx-background-color: #edf2f7; -fx-background-radius: 8;");

        for (String element : elementsList) {
            Label elementLabel = new Label(element);
            elementLabel.setMinSize(44, 44);
            elementLabel.setAlignment(Pos.CENTER);
            elementLabel.setStyle("-fx-font-size: 22px; -fx-background-color: white; " +
                                 "-fx-background-radius: 6; -fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

            final String val = element;
            elementLabel.setOnDragDetected(event -> {
                Dragboard db = elementLabel.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(val);
                db.setContent(content);
                event.consume();
            });

            sourceZone.getChildren().add(elementLabel);
        }

        // Zone de dépôt
        VBox dropZone = new VBox(5);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPrefHeight(80);
        dropZone.setMinHeight(80);
        dropZone.setStyle("-fx-background-color: #e6fffa; -fx-border-color: #38b2ac; " +
                         "-fx-border-width: 2; -fx-border-style: dashed; -fx-background-radius: 8;");

        Label dropLabel = new Label("Déposez les ⭐ ici");
        dropLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #38b2ac;");

        HBox droppedItems = new HBox(5);
        droppedItems.setAlignment(Pos.CENTER);
        dropZone.getChildren().addAll(dropLabel, droppedItems);

        final int[] ciblesDeposees = {0};

        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString() && "⭐".equals(db.getString())) {
                // Retirer UNE étoile de la source
                boolean removed = false;
                for (javafx.scene.Node node : new ArrayList<>(sourceZone.getChildren())) {
                    if (!removed && node instanceof Label lbl && "⭐".equals(lbl.getText())) {
                        sourceZone.getChildren().remove(lbl);
                        removed = true;
                    }
                }
                Label dropped = new Label("⭐");
                dropped.setStyle("-fx-font-size: 22px; -fx-padding: 4;");
                droppedItems.getChildren().add(dropped);
                ciblesDeposees[0]++;

                if (ciblesDeposees[0] >= totalCibles) {
                    reussi[0] = true;
                    dropZone.setStyle("-fx-background-color: #f0fff4; -fx-border-color: #38a169; " +
                                     "-fx-border-width: 2; -fx-border-style: solid; -fx-background-radius: 8;");
                    dropLabel.setText("✅ Parfait !");
                    dropLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #38a169; -fx-font-weight: bold;");
                }
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        container.getChildren().addAll(instruction, sourceZone, dropZone);
        return container;
    }
    
    /**
     * Crée un CAPTCHA avec slider
     */
    private static VBox creerCaptchaSlider(boolean[] reussi) {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #f7fafc; -fx-padding: 20; -fx-background-radius: 10; " +
                          "-fx-border-color: #e2e8f0; -fx-border-width: 2; -fx-border-radius: 10;");
        
        Label instruction = new Label("Faites glisser le curseur pour compléter l'image");
        instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        // Puzzle visuel simple
        HBox puzzleBox = new HBox(0);
        puzzleBox.setAlignment(Pos.CENTER);
        puzzleBox.setStyle("-fx-background-color: #edf2f7; -fx-padding: 15; -fx-background-radius: 8;");
        
        // Partie fixe du puzzle
        Label partie1 = new Label("🧩");
        partie1.setStyle("-fx-font-size: 40px; -fx-background-color: #e2e8f0; -fx-padding: 10; " +
                        "-fx-background-radius: 6 0 0 6;");
        
        // Partie mobile (initialement vide)
        Label partieMobile = new Label("  ");
        partieMobile.setStyle("-fx-font-size: 40px; -fx-background-color: white; -fx-padding: 10; " +
                             "-fx-background-radius: 0 6 6 0; -fx-border-color: #cbd5e0; -fx-border-width: 2;");
        
        puzzleBox.getChildren().addAll(partie1, partieMobile);
        
        // Slider
        Slider slider = new Slider(0, 100, 0);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);
        slider.setPrefWidth(200);
        slider.setStyle("-fx-background-color: #e2e8f0;");
        
        Label sliderLabel = new Label("Glissez vers la droite →");
        sliderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
        
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double progress = newVal.doubleValue();
            if (progress > 90) {
                partieMobile.setText("🧩");
                partieMobile.setStyle("-fx-font-size: 40px; -fx-background-color: #f0fff4; -fx-padding: 10; " +
                                     "-fx-background-radius: 0 6 6 0; -fx-border-color: #38a169; -fx-border-width: 2;");
                sliderLabel.setText("✅ Puzzle complété !");
                sliderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #38a169; -fx-font-weight: bold;");
                reussi[0] = true;
            } else {
                partieMobile.setText("  ");
                partieMobile.setStyle("-fx-font-size: 40px; -fx-background-color: white; -fx-padding: 10; " +
                                     "-fx-background-radius: 0 6 6 0; -fx-border-color: #cbd5e0; -fx-border-width: 2;");
                sliderLabel.setText("Glissez vers la droite →");
                sliderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
                reussi[0] = false;
            }
        });
        
        container.getChildren().addAll(instruction, puzzleBox, slider, sliderLabel);
        return container;
    }
    
    /**
     * Crée un CAPTCHA de sélection d'images
     */
    private static VBox creerCaptchaSelection(boolean[] reussi) {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #f7fafc; -fx-padding: 20; -fx-background-radius: 10; " +
                          "-fx-border-color: #e2e8f0; -fx-border-width: 2; -fx-border-radius: 10;");
        
        String[] categories = {"animaux", "véhicules", "nourriture"};
        String categorieChoisie = categories[random.nextInt(categories.length)];
        
        Label instruction = new Label("Sélectionnez tous les " + categorieChoisie);
        instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        // Grille d'images (emojis)
        GridPane grille = new GridPane();
        grille.setHgap(10);
        grille.setVgap(10);
        grille.setAlignment(Pos.CENTER);
        
        Map<String, String[]> emojiCategories = Map.of(
            "animaux", new String[]{"🐶", "🚗", "🐱", "🍕", "🦁", "✈️", "🐸", "🍎", "🐧"},
            "véhicules", new String[]{"🚗", "🐶", "✈️", "🍕", "🚲", "🐱", "🚁", "🍎", "🚢"},
            "nourriture", new String[]{"🍕", "🐶", "🍎", "🚗", "🍔", "🐱", "🍰", "✈️", "🥕"}
        );
        
        String[] emojis = emojiCategories.get(categorieChoisie);
        Set<Integer> bonnesReponses = new HashSet<>();
        
        // Identifier les bonnes réponses
        for (int i = 0; i < emojis.length; i++) {
            String emoji = emojis[i];
            boolean estBonneReponse = false;
            switch (categorieChoisie) {
                case "animaux" -> estBonneReponse = "🐶🐱🦁🐸🐧".contains(emoji);
                case "véhicules" -> estBonneReponse = "🚗✈️🚲🚁🚢".contains(emoji);
                case "nourriture" -> estBonneReponse = "🍕🍎🍔🍰🥕".contains(emoji);
            }
            if (estBonneReponse) {
                bonnesReponses.add(i);
            }
        }
        
        Set<Integer> selections = new HashSet<>();
        
        for (int i = 0; i < emojis.length; i++) {
            final int index = i;
            Button emojiBtn = new Button(emojis[i]);
            emojiBtn.setStyle("-fx-font-size: 24px; -fx-padding: 15; -fx-background-color: white; " +
                             "-fx-background-radius: 8; -fx-cursor: hand; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
            
            emojiBtn.setOnAction(e -> {
                if (selections.contains(index)) {
                    // Désélectionner
                    selections.remove(index);
                    emojiBtn.setStyle("-fx-font-size: 24px; -fx-padding: 15; -fx-background-color: white; " +
                                     "-fx-background-radius: 8; -fx-cursor: hand; " +
                                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
                } else {
                    // Sélectionner
                    selections.add(index);
                    emojiBtn.setStyle("-fx-font-size: 24px; -fx-padding: 15; -fx-background-color: #bee3f8; " +
                                     "-fx-background-radius: 8; -fx-cursor: hand; " +
                                     "-fx-border-color: #3182ce; -fx-border-width: 2;");
                }
                
                // Vérifier si toutes les bonnes réponses sont sélectionnées
                reussi[0] = selections.equals(bonnesReponses);
            });
            
            grille.add(emojiBtn, i % 3, i / 3);
        }
        
        container.getChildren().addAll(instruction, grille);
        return container;
    }
}