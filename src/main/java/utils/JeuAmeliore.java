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

import java.util.*;

/**
 * Jeux améliorés avec design unifié
 */
public class JeuAmeliore {
    
    private static final Random random = new Random();
    
    /**
     * Crée l'interface de base commune aux deux jeux
     */
    private static VBox creerInterfaceBase(String titre, String couleurPrimaire, String couleurSecondaire) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, " + couleurPrimaire + " 0%, " + couleurSecondaire + " 100%);");
        root.setPrefSize(650, 550);
        
        // Titre avec style unifié
        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0, 0, 2);");
        
        root.getChildren().add(lblTitre);
        return root;
    }
    
    /**
     * Crée un header de score unifié
     */
    private static HBox creerHeaderScore(Label... labels) {
        HBox header = new HBox(40);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-padding: 15 30; " +
                       "-fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        
        for (Label label : labels) {
            label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        }
        
        header.getChildren().addAll(labels);
        return header;
    }
    
    /**
     * Crée des boutons avec style unifié
     */
    private static Button creerBouton(String texte, String couleur, String couleurHover) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-font-size: 16px; " +
                    "-fx-font-weight: bold; -fx-padding: 15 30; -fx-cursor: hand; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 3);");
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + couleurHover + "; -fx-text-fill: white; -fx-font-size: 16px; " +
                    "-fx-font-weight: bold; -fx-padding: 15 30; -fx-cursor: hand; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 4);"));
        
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-font-size: 16px; " +
                    "-fx-font-weight: bold; -fx-padding: 15 30; -fx-cursor: hand; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 3);"));
        
        return btn;
    }
    
    /**
     * Jeu de réaction amélioré avec design unifié
     */
    public static void lancerJeuReactionAmeliore(Stage parentStage, String nomActivite) {
        Stage jeuStage = new Stage();
        jeuStage.initModality(Modality.APPLICATION_MODAL);
        jeuStage.initOwner(parentStage);
        jeuStage.setTitle("🎯 " + nomActivite + " - Jeu de Réaction Pro");
        jeuStage.setResizable(false);
        
        VBox root = creerInterfaceBase("🎯 JEU DE RÉACTION PRO", "#e74c3c", "#c0392b");
        
        // Variables du jeu
        final int[] score = {0};
        final int[] vies = {3};
        final int[] niveau = {1};
        final int[] combo = {0};
        final boolean[] jeuActif = {false};
        
        // Header avec stats
        Label lblScore = new Label("Score: 0");
        Label lblVies = new Label("❤️❤️❤️");
        Label lblNiveau = new Label("Niveau: 1");
        Label lblCombo = new Label("Combo: 0");
        
        HBox header = creerHeaderScore(lblScore, lblVies, lblNiveau, lblCombo);
        
        // Zone de jeu améliorée
        Pane zoneJeu = new Pane();
        zoneJeu.setPrefSize(550, 350);
        zoneJeu.setPickOnBounds(true); // capture les clics même sur zone vide
        zoneJeu.setBackground(new Background(new BackgroundFill(
                Color.web("#ffffff", 0.1), new CornerRadii(20), Insets.EMPTY)));
        zoneJeu.setStyle("-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 3; -fx-border-radius: 20;");
        
        // Instructions stylées
        Label instructions = new Label("🎯 Cliquez sur les cibles ROUGES\n❌ Évitez les cibles BLEUES\n⚡ Enchaînez pour des combos !");
        instructions.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-text-alignment: center; " +
                             "-fx-background-color: rgba(0,0,0,0.3); -fx-padding: 10; -fx-background-radius: 10;");
        instructions.setAlignment(Pos.CENTER);
        
        // Boutons améliorés
        Button btnCommencer = creerBouton("🚀 COMMENCER", "#27ae60", "#229954");
        Button btnQuitter = creerBouton("❌ QUITTER", "#e74c3c", "#c0392b");
        
        btnQuitter.setOnAction(e -> jeuStage.close());
        
        HBox boutons = new HBox(20, btnCommencer, btnQuitter);
        boutons.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(header, zoneJeu, instructions, boutons);
        
        // Timeline pour les cibles
        Timeline timelineCibles = new Timeline();
        
        btnCommencer.setOnAction(e -> {
            if (!jeuActif[0]) {
                // Démarrer
                jeuActif[0] = true;
                score[0] = 0;
                vies[0] = 3;
                niveau[0] = 1;
                combo[0] = 0;
                
                btnCommencer.setText("⏸️ PAUSE");
                btnCommencer.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 16px; " +
                                     "-fx-font-weight: bold; -fx-padding: 15 30; -fx-cursor: hand; -fx-background-radius: 12;");
                
                instructions.setText("🔥 JEU EN COURS ! Visez les cibles rouges !");
                demarrerJeuReactionAmeliore(timelineCibles, zoneJeu, score, vies, niveau, combo,
                                          lblScore, lblVies, lblNiveau, lblCombo, jeuActif, btnCommencer, instructions);
            } else {
                // Pause
                jeuActif[0] = false;
                timelineCibles.stop();
                zoneJeu.getChildren().clear();
                btnCommencer.setText("▶️ REPRENDRE");
                btnCommencer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; " +
                                     "-fx-font-weight: bold; -fx-padding: 15 30; -fx-cursor: hand; -fx-background-radius: 12;");
                instructions.setText("⏸️ Jeu en pause. Cliquez pour reprendre !");
            }
        });
        
        Scene scene = new Scene(root);
        jeuStage.setScene(scene);
        jeuStage.show();
        
        // Centrer
        if (parentStage != null) {
            jeuStage.setX(parentStage.getX() + (parentStage.getWidth() - jeuStage.getWidth()) / 2);
            jeuStage.setY(parentStage.getY() + (parentStage.getHeight() - jeuStage.getHeight()) / 2);
        }
    }
    
    private static void demarrerJeuReactionAmeliore(Timeline timeline, Pane zoneJeu, int[] score, int[] vies, 
                                                   int[] niveau, int[] combo, Label lblScore, Label lblVies, 
                                                   Label lblNiveau, Label lblCombo, boolean[] jeuActif,
                                                   Button btnCommencer, Label instructions) {
        timeline.getKeyFrames().clear();
        
        double intervalle = Math.max(0.3, 1.5 - (niveau[0] * 0.15));
        
        KeyFrame frame = new KeyFrame(Duration.seconds(intervalle), e -> {
            if (jeuActif[0] && vies[0] > 0) {
                creerCibleAmelioree(zoneJeu, score, vies, niveau, combo, lblScore, lblVies, 
                                  lblNiveau, lblCombo, jeuActif, btnCommencer, instructions, timeline);
            }
        });
        
        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private static void creerCibleAmelioree(Pane zoneJeu, int[] score, int[] vies, int[] niveau, int[] combo,
                                          Label lblScore, Label lblVies, Label lblNiveau, Label lblCombo,
                                          boolean[] jeuActif, Button btnCommencer, Label instructions, Timeline timeline) {
        
        boolean cibleBonne = random.nextDouble() < 0.75;
        
        Circle cible = new Circle();
        cible.setRadius(15 + random.nextInt(20));
        cible.setFill(cibleBonne ? Color.web("#e74c3c") : Color.web("#3498db"));
        cible.setStroke(Color.WHITE);
        cible.setStrokeWidth(4);
        cible.setEffect(new DropShadow(8, Color.BLACK));
        
        // Position aléatoire
        double x = cible.getRadius() + random.nextDouble() * (zoneJeu.getPrefWidth() - 2 * cible.getRadius());
        double y = cible.getRadius() + random.nextDouble() * (zoneJeu.getPrefHeight() - 2 * cible.getRadius());
        cible.setCenterX(x);
        cible.setCenterY(y);
        
        // Animation d'apparition plus spectaculaire
        cible.setScaleX(0);
        cible.setScaleY(0);
        cible.setOpacity(0);
        
        ScaleTransition apparition = new ScaleTransition(Duration.millis(300), cible);
        apparition.setToX(1);
        apparition.setToY(1);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cible);
        fadeIn.setToValue(1);
        
        ParallelTransition entree = new ParallelTransition(apparition, fadeIn);
        
        // Pulsation plus dynamique
        ScaleTransition pulsation = new ScaleTransition(Duration.millis(400), cible);
        pulsation.setFromX(1);
        pulsation.setFromY(1);
        pulsation.setToX(1.3);
        pulsation.setToY(1.3);
        pulsation.setAutoReverse(true);
        pulsation.setCycleCount(Timeline.INDEFINITE);
        
        // Durée de vie
        double dureeVie = Math.max(0.6, 2.0 - (niveau[0] * 0.1));
        
        Timeline disparition = new Timeline(new KeyFrame(Duration.seconds(dureeVie), ev -> {
            if (zoneJeu.getChildren().contains(cible)) {
                zoneJeu.getChildren().remove(cible);
                if (cibleBonne) {
                    vies[0]--;
                    combo[0] = 0; // Reset combo
                    mettreAJourAffichageAmeliore(score, vies, niveau, combo, lblScore, lblVies, lblNiveau, lblCombo);
                    
                    if (vies[0] <= 0) {
                        finirJeuAmeliore(zoneJeu, jeuActif, btnCommencer, instructions, timeline, score[0], "RÉACTION");
                    }
                }
            }
        }));
        
        // Gestion du clic améliorée
        cible.setOnMouseClicked(event -> {
            if (jeuActif[0]) {
                zoneJeu.getChildren().remove(cible);
                disparition.stop();
                pulsation.stop();
                
                if (cibleBonne) {
                    // Bonne cible
                    combo[0]++;
                    int points = (10 * niveau[0]) + (combo[0] * 5); // Bonus combo
                    score[0] += points;
                    
                    // Animation de score avec combo
                    Label scorePopup = new Label("+" + points + (combo[0] > 1 ? " (x" + combo[0] + ")" : ""));
                    scorePopup.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");
                    scorePopup.setLayoutX(event.getX());
                    scorePopup.setLayoutY(event.getY() - 30);
                    zoneJeu.getChildren().add(scorePopup);
                    
                    // Animation plus spectaculaire
                    ScaleTransition scaleScore = new ScaleTransition(Duration.millis(200), scorePopup);
                    scaleScore.setFromX(0.5);
                    scaleScore.setFromY(0.5);
                    scaleScore.setToX(1.5);
                    scaleScore.setToY(1.5);
                    
                    TranslateTransition moveScore = new TranslateTransition(Duration.millis(1000), scorePopup);
                    moveScore.setByY(-50);
                    
                    FadeTransition fadeScore = new FadeTransition(Duration.millis(1000), scorePopup);
                    fadeScore.setFromValue(1);
                    fadeScore.setToValue(0);
                    
                    ParallelTransition scoreEffect = new ParallelTransition(scaleScore, moveScore, fadeScore);
                    scoreEffect.setOnFinished(e -> zoneJeu.getChildren().remove(scorePopup));
                    scoreEffect.play();
                    
                    // Niveau up
                    if (score[0] % 150 == 0 && score[0] > 0) {
                        niveau[0]++;
                        timeline.stop();
                        demarrerJeuReactionAmeliore(timeline, zoneJeu, score, vies, niveau, combo,
                                                   lblScore, lblVies, lblNiveau, lblCombo, jeuActif, btnCommencer, instructions);
                    }
                    
                } else {
                    // Mauvaise cible
                    vies[0]--;
                    combo[0] = 0; // Reset combo
                    
                    Label erreurPopup = new Label("-1 ❤️");
                    erreurPopup.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                    erreurPopup.setLayoutX(event.getX());
                    erreurPopup.setLayoutY(event.getY() - 30);
                    zoneJeu.getChildren().add(erreurPopup);
                    
                    // Animation d'erreur
                    TranslateTransition erreurMove = new TranslateTransition(Duration.millis(800), erreurPopup);
                    erreurMove.setByY(-40);
                    FadeTransition erreurFade = new FadeTransition(Duration.millis(800), erreurPopup);
                    erreurFade.setToValue(0);
                    
                    ParallelTransition erreurEffect = new ParallelTransition(erreurMove, erreurFade);
                    erreurEffect.setOnFinished(e -> zoneJeu.getChildren().remove(erreurPopup));
                    erreurEffect.play();
                    
                    if (vies[0] <= 0) {
                        finirJeuAmeliore(zoneJeu, jeuActif, btnCommencer, instructions, timeline, score[0], "RÉACTION");
                    }
                }
                
                mettreAJourAffichageAmeliore(score, vies, niveau, combo, lblScore, lblVies, lblNiveau, lblCombo);
            }
        });
        
        // Effet de survol amélioré
        cible.setOnMouseEntered(e -> {
            cible.setScaleX(1.2);
            cible.setScaleY(1.2);
            cible.setEffect(new DropShadow(12, cibleBonne ? Color.web("#e74c3c") : Color.web("#3498db")));
        });
        cible.setOnMouseExited(e -> {
            cible.setScaleX(1.0);
            cible.setScaleY(1.0);
            cible.setEffect(new DropShadow(8, Color.BLACK));
        });
        
        zoneJeu.getChildren().add(cible);
        entree.play();
        pulsation.play();
        disparition.play();
    }
    
    private static void mettreAJourAffichageAmeliore(int[] score, int[] vies, int[] niveau, int[] combo,
                                                   Label lblScore, Label lblVies, Label lblNiveau, Label lblCombo) {
        lblScore.setText("Score: " + score[0]);
        lblNiveau.setText("Niveau: " + niveau[0]);
        lblCombo.setText("Combo: " + combo[0]);
        
        String coeurs = "";
        for (int i = 0; i < vies[0]; i++) {
            coeurs += "❤️";
        }
        for (int i = vies[0]; i < 3; i++) {
            coeurs += "🖤";
        }
        lblVies.setText(coeurs);
    }
    
    private static void finirJeuAmeliore(Pane zoneJeu, boolean[] jeuActif, Button btnCommencer,
                                       Label instructions, Timeline timeline, int scoreTotal, String typeJeu) {
        jeuActif[0] = false;
        timeline.stop();
        zoneJeu.getChildren().clear();
        
        btnCommencer.setText("🔄 REJOUER");
        btnCommencer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; " +
                             "-fx-font-weight: bold; -fx-padding: 15 30; -fx-cursor: hand; -fx-background-radius: 12;");
        
        String message;
        if (typeJeu.equals("RÉACTION")) {
            message = scoreTotal >= 300 ? "🏆 LÉGENDAIRE ! Score: " + scoreTotal :
                     scoreTotal >= 200 ? "🥇 EXCELLENT ! Score: " + scoreTotal :
                     scoreTotal >= 100 ? "🥈 TRÈS BIEN ! Score: " + scoreTotal :
                     "🥉 BON EFFORT ! Score: " + scoreTotal;
        } else {
            message = scoreTotal >= 200 ? "🧠 GÉNIE ! Score: " + scoreTotal :
                     scoreTotal >= 150 ? "🎯 EXPERT ! Score: " + scoreTotal :
                     scoreTotal >= 100 ? "👍 BIEN ! Score: " + scoreTotal :
                     "💪 CONTINUEZ ! Score: " + scoreTotal;
        }
        
        instructions.setText("🎮 GAME OVER !\n" + message);
        
        // Animation de fin spectaculaire
        Label gameOver = new Label("GAME OVER");
        gameOver.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-text-fill: white; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 5);");
        gameOver.setLayoutX(zoneJeu.getPrefWidth() / 2 - 150);
        gameOver.setLayoutY(zoneJeu.getPrefHeight() / 2 - 30);
        
        ScaleTransition scaleGameOver = new ScaleTransition(Duration.millis(1000), gameOver);
        scaleGameOver.setFromX(0);
        scaleGameOver.setFromY(0);
        scaleGameOver.setToX(1);
        scaleGameOver.setToY(1);
        
        FadeTransition fadeGameOver = new FadeTransition(Duration.millis(1000), gameOver);
        fadeGameOver.setFromValue(0);
        fadeGameOver.setToValue(1);
        
        ParallelTransition finAnimation = new ParallelTransition(scaleGameOver, fadeGameOver);
        
        zoneJeu.getChildren().add(gameOver);
        finAnimation.play();
        
        Timeline cleanup = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            if (zoneJeu.getChildren().contains(gameOver)) {
                zoneJeu.getChildren().remove(gameOver);
            }
        }));
        cleanup.play();
    }
}