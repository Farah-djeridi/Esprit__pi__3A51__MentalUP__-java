package utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Random;

/**
 * Mini-jeu de réaction pour les activités de type "Jeux"
 */
public class MiniJeu {
    
    private static final Random random = new Random();
    
    /**
     * Lance le mini-jeu de réaction
     */
    public static void lancerJeuReaction(Stage parentStage, String nomActivite) {
        Stage jeuStage = new Stage();
        jeuStage.initModality(Modality.APPLICATION_MODAL);
        jeuStage.initOwner(parentStage);
        jeuStage.setTitle("🎮 " + nomActivite + " - Jeu de Réaction");
        jeuStage.setResizable(false);
        
        // Variables du jeu
        final int[] score = {0};
        final int[] vies = {3};
        final int[] niveau = {1};
        final boolean[] jeuActif = {false};
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");
        root.setPrefSize(600, 500);
        
        // Header du jeu
        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER);
        
        Label lblScore = new Label("Score: 0");
        lblScore.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label lblVies = new Label("❤️❤️❤️");
        lblVies.setStyle("-fx-font-size: 18px;");
        
        Label lblNiveau = new Label("Niveau: 1");
        lblNiveau.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        header.getChildren().addAll(lblScore, lblVies, lblNiveau);
        
        // Zone de jeu
        Pane zoneJeu = new Pane();
        zoneJeu.setPrefSize(500, 300);
        zoneJeu.setPickOnBounds(true);
        zoneJeu.setBackground(new Background(new BackgroundFill(
                Color.web("#ffffff", 0.1), new CornerRadii(15), Insets.EMPTY)));
        zoneJeu.setStyle("-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; -fx-border-radius: 15;");
        
        // Instructions
        Label instructions = new Label("Cliquez sur les cibles rouges qui apparaissent !\nÉvitez les cibles bleues !");
        instructions.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-text-alignment: center;");
        instructions.setAlignment(Pos.CENTER);
        
        // Boutons
        Button btnCommencer = new Button("🎮 Commencer");
        btnCommencer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; " +
                             "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        
        Button btnQuitter = new Button("❌ Quitter");
        btnQuitter.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; " +
                           "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        btnQuitter.setOnAction(e -> jeuStage.close());
        
        HBox boutons = new HBox(20, btnCommencer, btnQuitter);
        boutons.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(header, zoneJeu, instructions, boutons);
        
        // Timeline pour faire apparaître les cibles
        Timeline timelineCibles = new Timeline();
        
        btnCommencer.setOnAction(e -> {
            if (!jeuActif[0]) {
                // Démarrer le jeu
                jeuActif[0] = true;
                score[0] = 0;
                vies[0] = 3;
                niveau[0] = 1;
                
                btnCommencer.setText("⏸️ Pause");
                btnCommencer.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 16px; " +
                                     "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
                
                instructions.setText("Jeu en cours... Cliquez sur les cibles rouges !");
                
                // Démarrer la timeline
                demarrerJeu(timelineCibles, zoneJeu, score, vies, niveau, lblScore, lblVies, lblNiveau, 
                           jeuActif, btnCommencer, instructions);
                
            } else {
                // Mettre en pause
                jeuActif[0] = false;
                timelineCibles.stop();
                zoneJeu.getChildren().clear();
                
                btnCommencer.setText("▶️ Reprendre");
                btnCommencer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; " +
                                     "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
                
                instructions.setText("Jeu en pause. Cliquez sur Reprendre pour continuer.");
            }
        });
        
        Scene scene = new Scene(root);
        jeuStage.setScene(scene);
        jeuStage.show();
        
        // Centrer la fenêtre
        if (parentStage != null) {
            jeuStage.setX(parentStage.getX() + (parentStage.getWidth() - jeuStage.getWidth()) / 2);
            jeuStage.setY(parentStage.getY() + (parentStage.getHeight() - jeuStage.getHeight()) / 2);
        }
    }
    
    private static void demarrerJeu(Timeline timeline, Pane zoneJeu, int[] score, int[] vies, int[] niveau,
                                   Label lblScore, Label lblVies, Label lblNiveau, boolean[] jeuActif,
                                   Button btnCommencer, Label instructions) {
        
        timeline.getKeyFrames().clear();
        
        // Intervalle entre les cibles (diminue avec le niveau)
        double intervalle = Math.max(0.5, 2.0 - (niveau[0] * 0.2));
        
        KeyFrame frame = new KeyFrame(Duration.seconds(intervalle), e -> {
            if (jeuActif[0] && vies[0] > 0) {
                creerCible(zoneJeu, score, vies, niveau, lblScore, lblVies, lblNiveau, 
                          jeuActif, btnCommencer, instructions, timeline);
            }
        });
        
        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private static void creerCible(Pane zoneJeu, int[] score, int[] vies, int[] niveau,
                                  Label lblScore, Label lblVies, Label lblNiveau, boolean[] jeuActif,
                                  Button btnCommencer, Label instructions, Timeline timeline) {
        
        // Type de cible : 70% rouge (bonne), 30% bleue (mauvaise)
        boolean cibleBonne = random.nextDouble() < 0.7;
        
        Circle cible = new Circle();
        cible.setRadius(20 + random.nextInt(15)); // Taille variable
        cible.setFill(cibleBonne ? Color.web("#e74c3c") : Color.web("#3498db"));
        cible.setStroke(Color.WHITE);
        cible.setStrokeWidth(3);
        cible.setEffect(new DropShadow(5, Color.BLACK));
        
        // Position aléatoire
        double x = cible.getRadius() + random.nextDouble() * (zoneJeu.getPrefWidth() - 2 * cible.getRadius());
        double y = cible.getRadius() + random.nextDouble() * (zoneJeu.getPrefHeight() - 2 * cible.getRadius());
        cible.setCenterX(x);
        cible.setCenterY(y);
        
        // Animation d'apparition
        cible.setScaleX(0);
        cible.setScaleY(0);
        ScaleTransition apparition = new ScaleTransition(Duration.millis(200), cible);
        apparition.setToX(1);
        apparition.setToY(1);
        
        // Durée de vie de la cible
        double dureeVie = Math.max(0.8, 2.5 - (niveau[0] * 0.1));
        
        // Animation de pulsation
        ScaleTransition pulsation = new ScaleTransition(Duration.millis(300), cible);
        pulsation.setFromX(1);
        pulsation.setFromY(1);
        pulsation.setToX(1.2);
        pulsation.setToY(1.2);
        pulsation.setAutoReverse(true);
        pulsation.setCycleCount(Timeline.INDEFINITE);
        
        // Timeline pour faire disparaître la cible
        Timeline disparition = new Timeline(new KeyFrame(Duration.seconds(dureeVie), ev -> {
            if (zoneJeu.getChildren().contains(cible)) {
                zoneJeu.getChildren().remove(cible);
                if (cibleBonne) {
                    // Pénalité pour avoir raté une bonne cible
                    vies[0]--;
                    mettreAJourAffichage(score, vies, niveau, lblScore, lblVies, lblNiveau);
                    
                    if (vies[0] <= 0) {
                        finirJeu(zoneJeu, jeuActif, btnCommencer, instructions, timeline, score[0]);
                    }
                }
            }
        }));
        
        // Gestion du clic
        cible.setOnMouseClicked(event -> {
            if (jeuActif[0]) {
                zoneJeu.getChildren().remove(cible);
                disparition.stop();
                pulsation.stop();
                
                if (cibleBonne) {
                    // Bonne cible cliquée
                    score[0] += 10 * niveau[0];
                    
                    // Animation de score
                    Label scorePopup = new Label("+" + (10 * niveau[0]));
                    scorePopup.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
                    scorePopup.setLayoutX(event.getX());
                    scorePopup.setLayoutY(event.getY() - 20);
                    zoneJeu.getChildren().add(scorePopup);
                    
                    TranslateTransition scoreAnim = new TranslateTransition(Duration.millis(800), scorePopup);
                    scoreAnim.setByY(-30);
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(800), scorePopup);
                    fadeOut.setToValue(0);
                    
                    ParallelTransition scoreEffect = new ParallelTransition(scoreAnim, fadeOut);
                    scoreEffect.setOnFinished(e -> zoneJeu.getChildren().remove(scorePopup));
                    scoreEffect.play();
                    
                    // Augmenter le niveau tous les 100 points
                    if (score[0] % 100 == 0 && score[0] > 0) {
                        niveau[0]++;
                        timeline.stop();
                        demarrerJeu(timeline, zoneJeu, score, vies, niveau, lblScore, lblVies, lblNiveau,
                                   jeuActif, btnCommencer, instructions);
                    }
                    
                } else {
                    // Mauvaise cible cliquée
                    vies[0]--;
                    
                    // Animation d'erreur
                    Label erreurPopup = new Label("-1 ❤️");
                    erreurPopup.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                    erreurPopup.setLayoutX(event.getX());
                    erreurPopup.setLayoutY(event.getY() - 20);
                    zoneJeu.getChildren().add(erreurPopup);
                    
                    TranslateTransition erreurAnim = new TranslateTransition(Duration.millis(800), erreurPopup);
                    erreurAnim.setByY(-30);
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(800), erreurPopup);
                    fadeOut.setToValue(0);
                    
                    ParallelTransition erreurEffect = new ParallelTransition(erreurAnim, fadeOut);
                    erreurEffect.setOnFinished(e -> zoneJeu.getChildren().remove(erreurPopup));
                    erreurEffect.play();
                    
                    if (vies[0] <= 0) {
                        finirJeu(zoneJeu, jeuActif, btnCommencer, instructions, timeline, score[0]);
                    }
                }
                
                mettreAJourAffichage(score, vies, niveau, lblScore, lblVies, lblNiveau);
            }
        });
        
        // Effet de survol
        cible.setOnMouseEntered(e -> cible.setScaleX(1.1));
        cible.setOnMouseEntered(e -> cible.setScaleY(1.1));
        cible.setOnMouseExited(e -> cible.setScaleX(1.0));
        cible.setOnMouseExited(e -> cible.setScaleY(1.0));
        
        zoneJeu.getChildren().add(cible);
        apparition.play();
        pulsation.play();
        disparition.play();
    }
    
    private static void mettreAJourAffichage(int[] score, int[] vies, int[] niveau,
                                           Label lblScore, Label lblVies, Label lblNiveau) {
        lblScore.setText("Score: " + score[0]);
        lblNiveau.setText("Niveau: " + niveau[0]);
        
        String coeurs = "";
        for (int i = 0; i < vies[0]; i++) {
            coeurs += "❤️";
        }
        for (int i = vies[0]; i < 3; i++) {
            coeurs += "🖤";
        }
        lblVies.setText(coeurs);
    }
    
    private static void finirJeu(Pane zoneJeu, boolean[] jeuActif, Button btnCommencer,
                               Label instructions, Timeline timeline, int scoreTotal) {
        jeuActif[0] = false;
        timeline.stop();
        zoneJeu.getChildren().clear();
        
        btnCommencer.setText("🔄 Rejouer");
        btnCommencer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; " +
                             "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        
        String message = scoreTotal >= 200 ? "🏆 Excellent ! Score final: " + scoreTotal :
                        scoreTotal >= 100 ? "👍 Bien joué ! Score final: " + scoreTotal :
                        "💪 Continuez à vous entraîner ! Score: " + scoreTotal;
        
        instructions.setText("🎮 Jeu terminé !\n" + message);
        
        // Animation de fin
        Label gameOver = new Label("GAME OVER");
        gameOver.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
        gameOver.setLayoutX(zoneJeu.getPrefWidth() / 2 - 120);
        gameOver.setLayoutY(zoneJeu.getPrefHeight() / 2 - 24);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), gameOver);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        zoneJeu.getChildren().add(gameOver);
        fadeIn.play();
        
        // Supprimer le texte après 3 secondes
        Timeline cleanup = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            if (zoneJeu.getChildren().contains(gameOver)) {
                zoneJeu.getChildren().remove(gameOver);
            }
        }));
        cleanup.play();
    }
}