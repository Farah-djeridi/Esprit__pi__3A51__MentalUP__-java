package utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.*;

/**
 * CAPTCHA avancé avec interactions visuelles modernes
 */
public class CaptchaAvance {
    
    private static final Random random = new Random();
    
    public interface CaptchaCallback {
        void onResult(boolean success);
    }
    
    /**
     * Affiche un CAPTCHA avancé avec différents types d'interactions
     */
    public static void afficherCaptchaAvance(Stage parentStage, String action, CaptchaCallback callback) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(parentStage);
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setTitle("Vérification avancée");
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%); " +
                      "-fx-background-radius: 16; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 25, 0, 0, 8);");
        root.setPrefWidth(500);
        
        // Titre avec animation
        Label titre = new Label("🛡️ Vérification Sécurisée");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label description = new Label("Pour " + action + ", complétez la vérification ci-dessous");
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9); " +
                           "-fx-wrap-text: true; -fx-text-alignment: center;");
        description.setMaxWidth(440);
        
        // Choisir le type de CAPTCHA
        int typeCaptcha = random.nextInt(3);
        
        VBox captchaContainer;
        boolean[] captchaReussi = {false};
        
        switch (typeCaptcha) {
            case 0:
                captchaContainer = creerCaptchaSequence(captchaReussi);
                break;
            case 1:
                captchaContainer = creerCaptchaRotation(captchaReussi);
                break;
            case 2:
                captchaContainer = creerCaptchaPattern(captchaReussi);
                break;
            default:
                captchaContainer = creerCaptchaSequence(captchaReussi);
        }
        
        // Animation d'entrée pour le container
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), captchaContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), captchaContainer);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        
        ParallelTransition entree = new ParallelTransition(fadeIn, scaleIn);
        
        // Boutons
        Button btnAnnuler = new Button("✕ Annuler");
        btnAnnuler.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                           "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 25; " +
                           "-fx-cursor: hand; -fx-background-radius: 25; -fx-border-color: white; " +
                           "-fx-border-width: 1; -fx-border-radius: 25;");
        btnAnnuler.setOnAction(e -> {
            popup.close();
            callback.onResult(false);
        });
        
        Button btnValider = new Button("✓ Valider");
        btnValider.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-text-fill: #667eea; " +
                           "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 25; " +
                           "-fx-cursor: hand; -fx-background-radius: 25;");
        
        btnValider.setOnAction(e -> {
            if (captchaReussi[0]) {
                // Animation de succès
                RotateTransition rotation = new RotateTransition(Duration.millis(300), btnValider);
                rotation.setByAngle(360);
                
                ScaleTransition scale = new ScaleTransition(Duration.millis(300), btnValider);
                scale.setToX(1.2);
                scale.setToY(1.2);
                scale.setAutoReverse(true);
                scale.setCycleCount(2);
                
                ParallelTransition succes = new ParallelTransition(rotation, scale);
                succes.setOnFinished(ev -> {
                    popup.close();
                    callback.onResult(true);
                });
                succes.play();
            } else {
                // Animation d'erreur
                TranslateTransition shake = new TranslateTransition(Duration.millis(50), captchaContainer);
                shake.setFromX(0);
                shake.setToX(10);
                shake.setAutoReverse(true);
                shake.setCycleCount(6);
                shake.play();
            }
        });
        
        HBox boutons = new HBox(20, btnAnnuler, btnValider);
        boutons.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(titre, description, captchaContainer, boutons);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        
        // Centrer et afficher
        popup.setOnShown(e -> {
            if (parentStage != null) {
                popup.setX(parentStage.getX() + (parentStage.getWidth() - popup.getWidth()) / 2);
                popup.setY(parentStage.getY() + (parentStage.getHeight() - popup.getHeight()) / 2);
            }
            entree.play();
        });
        
        popup.show();
    }
    
    /**
     * CAPTCHA avec séquence de couleurs à reproduire
     */
    private static VBox creerCaptchaSequence(boolean[] reussi) {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-padding: 25; " +
                          "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        
        Label instruction = new Label("Mémorisez la séquence, puis reproduisez-la");
        instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        // Couleurs disponibles
        Color[] couleurs = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE};
        String[] nomsCouleurs = {"Rouge", "Bleu", "Vert", "Jaune", "Violet"};
        
        // Générer séquence aléatoire
        List<Integer> sequence = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            sequence.add(random.nextInt(couleurs.length));
        }
        
        // Zone d'affichage de la séquence
        HBox sequenceDisplay = new HBox(10);
        sequenceDisplay.setAlignment(Pos.CENTER);
        
        Circle[] cercles = new Circle[4];
        for (int i = 0; i < 4; i++) {
            cercles[i] = new Circle(20);
            cercles[i].setFill(Color.LIGHTGRAY);
            cercles[i].setStroke(Color.DARKGRAY);
            cercles[i].setStrokeWidth(2);
            sequenceDisplay.getChildren().add(cercles[i]);
        }
        
        // Boutons de couleurs pour la reproduction
        HBox boutonsContainer = new HBox(10);
        boutonsContainer.setAlignment(Pos.CENTER);
        
        List<Integer> reponseUtilisateur = new ArrayList<>();
        
        for (int i = 0; i < couleurs.length; i++) {
            final int index = i;
            Button btnCouleur = new Button(nomsCouleurs[i]);
            btnCouleur.setStyle("-fx-background-color: " + toHexString(couleurs[i]) + "; " +
                               "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; " +
                               "-fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
            
            btnCouleur.setOnAction(e -> {
                if (reponseUtilisateur.size() < 4) {
                    reponseUtilisateur.add(index);
                    int pos = reponseUtilisateur.size() - 1;
                    cercles[pos].setFill(couleurs[index]);
                    
                    // Animation du cercle
                    ScaleTransition scale = new ScaleTransition(Duration.millis(200), cercles[pos]);
                    scale.setFromX(0.8);
                    scale.setFromY(0.8);
                    scale.setToX(1.0);
                    scale.setToY(1.0);
                    scale.play();
                    
                    // Vérifier si séquence complète
                    if (reponseUtilisateur.size() == 4) {
                        reussi[0] = reponseUtilisateur.equals(sequence);
                        if (reussi[0]) {
                            instruction.setText("✅ Séquence correcte !");
                            instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #38a169;");
                        } else {
                            instruction.setText("❌ Séquence incorrecte, réessayez");
                            instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e53e3e;");
                            // Reset après 1 seconde
                            Timeline reset = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                                reponseUtilisateur.clear();
                                for (Circle c : cercles) {
                                    c.setFill(Color.LIGHTGRAY);
                                }
                                instruction.setText("Mémorisez la séquence, puis reproduisez-la");
                                instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
                            }));
                            reset.play();
                        }
                    }
                }
            });
            
            boutonsContainer.getChildren().add(btnCouleur);
        }
        
        Button btnVoirSequence = new Button("👁 Voir la séquence");
        btnVoirSequence.setStyle("-fx-background-color: #4299e1; -fx-text-fill: white; -fx-font-size: 12px; " +
                                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-background-radius: 6;");
        
        btnVoirSequence.setOnAction(e -> {
            btnVoirSequence.setDisable(true);
            Timeline animation = new Timeline();
            
            for (int i = 0; i < sequence.size(); i++) {
                final int index = i;
                KeyFrame frame1 = new KeyFrame(Duration.millis(i * 800), ev -> {
                    cercles[index].setFill(couleurs[sequence.get(index)]);
                    ScaleTransition pulse = new ScaleTransition(Duration.millis(300), cercles[index]);
                    pulse.setToX(1.3);
                    pulse.setToY(1.3);
                    pulse.setAutoReverse(true);
                    pulse.setCycleCount(2);
                    pulse.play();
                });
                
                KeyFrame frame2 = new KeyFrame(Duration.millis(i * 800 + 600), ev -> {
                    cercles[index].setFill(Color.LIGHTGRAY);
                });
                
                animation.getKeyFrames().addAll(frame1, frame2);
            }
            
            animation.setOnFinished(ev -> {
                instruction.setText("Maintenant, reproduisez la séquence");
                instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4299e1;");
            });
            
            animation.play();
        });
        
        container.getChildren().addAll(instruction, sequenceDisplay, btnVoirSequence, boutonsContainer);
        return container;
    }
    
    /**
     * CAPTCHA avec rotation d'image
     */
    private static VBox creerCaptchaRotation(boolean[] reussi) {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-padding: 25; " +
                          "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        Label instruction = new Label("Faites tourner l'image pour la remettre droite");
        instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // Image à faire tourner
        Label imageLabel = new Label("🏠");
        imageLabel.setStyle("-fx-font-size: 60px; -fx-background-color: white; -fx-padding: 20; " +
                           "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        // Rotation initiale aléatoire : 90, 180 ou 270 (jamais 0 ni 360)
        int[] multiples = {90, 180, 270};
        double rotationInitiale = multiples[random.nextInt(multiples.length)];
        imageLabel.setRotate(rotationInitiale);

        // Slider : l'utilisateur doit amener l'image à 0° (= 360°)
        // La correction nécessaire est (360 - rotationInitiale)
        Slider rotationSlider = new Slider(0, 360, 0);
        rotationSlider.setShowTickLabels(true);
        rotationSlider.setShowTickMarks(true);
        rotationSlider.setMajorTickUnit(90);
        rotationSlider.setPrefWidth(300);

        Label angleLabel = new Label("Angle: 0°");
        angleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");

        rotationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double correction = newVal.doubleValue();
            // Rotation affichée = initiale - correction (on tourne dans le sens inverse)
            double rotationAffichee = (rotationInitiale - correction + 360) % 360;
            imageLabel.setRotate(rotationAffichee);
            angleLabel.setText("Angle image: " + Math.round(rotationAffichee) + "°");

            // Succès si l'image est droite (rotationAffichee proche de 0° ou 360°)
            boolean droite = rotationAffichee < 10 || rotationAffichee > 350;
            if (droite) {
                reussi[0] = true;
                instruction.setText("✅ Parfait ! L'image est droite");
                instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #38a169;");
            } else {
                reussi[0] = false;
                instruction.setText("Faites tourner l'image pour la remettre droite");
                instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
            }
        });

        container.getChildren().addAll(instruction, imageLabel, rotationSlider, angleLabel);
        return container;
    }
    
    /**
     * CAPTCHA avec pattern à compléter
     */
    private static VBox creerCaptchaPattern(boolean[] reussi) {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-padding: 25; " +
                          "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        
        Label instruction = new Label("Complétez le motif en cliquant sur les cases manquantes");
        instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        // Grille 4x4 avec pattern à compléter
        GridPane grille = new GridPane();
        grille.setHgap(5);
        grille.setVgap(5);
        grille.setAlignment(Pos.CENTER);
        
        // Pattern en damier avec quelques cases manquantes
        boolean[][] pattern = {
            {true, false, true, false},
            {false, true, false, true},
            {true, false, true, false},
            {false, true, false, true}
        };
        
        // Cases à faire deviner (on en cache quelques-unes)
        Set<String> casesManquantes = new HashSet<>();
        casesManquantes.add("0,2");
        casesManquantes.add("1,1");
        casesManquantes.add("2,0");
        casesManquantes.add("3,3");
        
        Set<String> casesCliquees = new HashSet<>();
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                final int row = i;
                final int col = j;
                String cle = row + "," + col;
                
                Rectangle rect = new Rectangle(40, 40);
                
                if (casesManquantes.contains(cle)) {
                    // Case à deviner
                    rect.setFill(Color.LIGHTGRAY);
                    rect.setStroke(Color.DARKGRAY);
                    rect.setStrokeWidth(2);
                    rect.setStyle("-fx-cursor: hand;");
                    
                    rect.setOnMouseClicked(e -> {
                        boolean shouldBeFilled = pattern[row][col];
                        
                        if (casesCliquees.contains(cle)) {
                            // Déjà cliqué, toggle
                            casesCliquees.remove(cle);
                            rect.setFill(Color.LIGHTGRAY);
                        } else {
                            // Nouveau clic
                            casesCliquees.add(cle);
                            rect.setFill(shouldBeFilled ? Color.DARKBLUE : Color.WHITE);
                        }
                        
                        // Vérifier si toutes les cases manquantes sont correctement remplies
                        boolean toutCorrect = true;
                        for (String caseMissing : casesManquantes) {
                            String[] coords = caseMissing.split(",");
                            int r = Integer.parseInt(coords[0]);
                            int c = Integer.parseInt(coords[1]);
                            boolean shouldFill = pattern[r][c];
                            boolean isClicked = casesCliquees.contains(caseMissing);
                            
                            if (shouldFill != isClicked) {
                                toutCorrect = false;
                                break;
                            }
                        }
                        
                        if (toutCorrect && casesCliquees.size() == casesManquantes.size()) {
                            reussi[0] = true;
                            instruction.setText("✅ Motif complété correctement !");
                            instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #38a169;");
                        } else {
                            reussi[0] = false;
                        }
                    });
                } else {
                    // Case déjà visible
                    rect.setFill(pattern[i][j] ? Color.DARKBLUE : Color.WHITE);
                    rect.setStroke(Color.GRAY);
                    rect.setStrokeWidth(1);
                }
                
                grille.add(rect, j, i);
            }
        }
        
        container.getChildren().addAll(instruction, grille);
        return container;
    }
    
    /**
     * Convertit une couleur JavaFX en string hexadécimale
     */
    private static String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
}