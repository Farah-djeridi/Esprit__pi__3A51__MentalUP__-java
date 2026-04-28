package utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

/**
 * Jeu du serpent (Snake) classique
 */
public class JeuSnake {
    
    private static final int TAILLE_CASE = 20;
    private static final int LARGEUR = 25;
    private static final int HAUTEUR = 20;
    private static final Random random = new Random();
    
    // Directions
    private static final int HAUT = 0;
    private static final int BAS = 1;
    private static final int GAUCHE = 2;
    private static final int DROITE = 3;
    
    public static void lancerJeuSnake(Stage parentStage, String nomActivite) {
        Stage jeuStage = new Stage();
        jeuStage.initModality(Modality.APPLICATION_MODAL);
        jeuStage.initOwner(parentStage);
        jeuStage.setTitle("🐍 " + nomActivite + " - Snake");
        jeuStage.setResizable(false);
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #27ae60 0%, #2ecc71 100%);");
        
        // Variables du jeu
        final List<int[]> serpent = new ArrayList<>();
        final int[] direction = {DROITE};
        final int[] prochainDirection = {DROITE};
        final int[] pommeX = {0};
        final int[] pommeY = {0};
        final int[] score = {0};
        final boolean[] jeuActif = {false};
        final boolean[] jeuPause = {false};
        
        // Initialiser le serpent
        serpent.add(new int[]{5, 5});
        serpent.add(new int[]{4, 5});
        serpent.add(new int[]{3, 5});
        
        // Header
        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER);
        
        Label lblScore = new Label("Score: 0");
        lblScore.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label lblLongueur = new Label("Longueur: 3");
        lblLongueur.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        header.getChildren().addAll(lblScore, lblLongueur);
        
        // Grille de jeu
        GridPane grille = new GridPane();
        grille.setAlignment(Pos.CENTER);
        grille.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10; -fx-background-radius: 10;");
        
        Rectangle[][] cases = new Rectangle[HAUTEUR][LARGEUR];
        
        // Créer la grille
        for (int y = 0; y < HAUTEUR; y++) {
            for (int x = 0; x < LARGEUR; x++) {
                Rectangle rect = new Rectangle(TAILLE_CASE, TAILLE_CASE);
                rect.setFill(Color.web("#34495e"));
                rect.setStroke(Color.web("#2c3e50"));
                rect.setStrokeWidth(1);
                cases[y][x] = rect;
                grille.add(rect, x, y);
            }
        }
        
        // Placer la première pomme
        placerPomme(pommeX, pommeY, serpent);
        
        // Instructions
        Label instructions = new Label("Utilisez les flèches ↑↓←→ pour diriger le serpent\nMangez les pommes 🍎 sans vous mordre la queue !");
        instructions.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-text-alignment: center;");
        instructions.setAlignment(Pos.CENTER);
        
        // Timeline du jeu
        Timeline gameLoop = new Timeline(new KeyFrame(Duration.millis(150), e -> {
            if (jeuActif[0] && !jeuPause[0]) {
                direction[0] = prochainDirection[0];
                
                // Calculer la nouvelle position de la tête
                int[] tete = serpent.get(0);
                int nouveauX = tete[0];
                int nouveauY = tete[1];
                
                switch (direction[0]) {
                    case HAUT -> nouveauY--;
                    case BAS -> nouveauY++;
                    case GAUCHE -> nouveauX--;
                    case DROITE -> nouveauX++;
                }
                
                // Vérifier les collisions avec les murs
                if (nouveauX < 0 || nouveauX >= LARGEUR || nouveauY < 0 || nouveauY >= HAUTEUR) {
                    finirJeu(jeuStage, score[0], "Collision avec le mur !");
                    jeuActif[0] = false;
                    return;
                }
                
                // Vérifier les collisions avec le corps
                for (int[] segment : serpent) {
                    if (segment[0] == nouveauX && segment[1] == nouveauY) {
                        finirJeu(jeuStage, score[0], "Le serpent s'est mordu la queue !");
                        jeuActif[0] = false;
                        return;
                    }
                }
                
                // Ajouter la nouvelle tête
                serpent.add(0, new int[]{nouveauX, nouveauY});
                
                // Vérifier si on mange une pomme
                if (nouveauX == pommeX[0] && nouveauY == pommeY[0]) {
                    score[0] += 10;
                    lblScore.setText("Score: " + score[0]);
                    lblLongueur.setText("Longueur: " + serpent.size());
                    placerPomme(pommeX, pommeY, serpent);
                    
                    // Animation de score
                    ScaleTransition scoreAnim = new ScaleTransition(Duration.millis(200), lblScore);
                    scoreAnim.setToX(1.2);
                    scoreAnim.setToY(1.2);
                    scoreAnim.setAutoReverse(true);
                    scoreAnim.setCycleCount(2);
                    scoreAnim.play();
                } else {
                    // Supprimer la queue si pas de pomme mangée
                    serpent.remove(serpent.size() - 1);
                }
                
                // Mettre à jour l'affichage
                mettreAJourAffichage(cases, serpent, pommeX[0], pommeY[0]);
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        
        // Boutons
        Button btnCommencer = new Button("🐍 Commencer");
        btnCommencer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; " +
                             "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        
        Button btnPause = new Button("⏸️ Pause");
        btnPause.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 16px; " +
                         "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        btnPause.setDisable(true);
        
        Button btnQuitter = new Button("❌ Quitter");
        btnQuitter.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; " +
                           "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        btnQuitter.setOnAction(e -> {
            gameLoop.stop();
            jeuStage.close();
        });
        
        btnCommencer.setOnAction(e -> {
            if (!jeuActif[0]) {
                // Démarrer le jeu
                jeuActif[0] = true;
                jeuPause[0] = false;
                gameLoop.play();
                btnCommencer.setText("🔄 Recommencer");
                btnCommencer.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 16px; " +
                                     "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
                btnPause.setDisable(false);
                instructions.setText("Jeu en cours... Dirigez le serpent avec les flèches !");
            } else {
                // Recommencer
                gameLoop.stop();
                jeuActif[0] = false;
                jeuPause[0] = false;
                score[0] = 0;
                direction[0] = DROITE;
                prochainDirection[0] = DROITE;
                
                // Réinitialiser le serpent
                serpent.clear();
                serpent.add(new int[]{5, 5});
                serpent.add(new int[]{4, 5});
                serpent.add(new int[]{3, 5});
                
                placerPomme(pommeX, pommeY, serpent);
                mettreAJourAffichage(cases, serpent, pommeX[0], pommeY[0]);
                
                lblScore.setText("Score: 0");
                lblLongueur.setText("Longueur: 3");
                btnCommencer.setText("🐍 Commencer");
                btnCommencer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; " +
                                     "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
                btnPause.setDisable(true);
                instructions.setText("Utilisez les flèches ↑↓←→ pour diriger le serpent\nMangez les pommes 🍎 sans vous mordre la queue !");
            }
        });
        
        btnPause.setOnAction(e -> {
            if (jeuActif[0]) {
                jeuPause[0] = !jeuPause[0];
                if (jeuPause[0]) {
                    btnPause.setText("▶️ Reprendre");
                    instructions.setText("Jeu en pause. Cliquez sur Reprendre pour continuer.");
                } else {
                    btnPause.setText("⏸️ Pause");
                    instructions.setText("Jeu en cours... Dirigez le serpent avec les flèches !");
                }
            }
        });
        
        HBox boutons = new HBox(20, btnCommencer, btnPause, btnQuitter);
        boutons.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(header, grille, instructions, boutons);
        
        Scene scene = new Scene(root);
        jeuStage.setScene(scene);

        // Gestion des touches via addEventFilter sur le STAGE :
        // capture les événements avant qu'ils atteignent les boutons focusés
        jeuStage.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();

            // Consommer les flèches et espace pour éviter la navigation entre boutons
            if (code == KeyCode.UP || code == KeyCode.DOWN
                    || code == KeyCode.LEFT || code == KeyCode.RIGHT
                    || code == KeyCode.SPACE) {
                e.consume();
            }

            if (!jeuActif[0] || jeuPause[0]) {
                if (code == KeyCode.SPACE) btnPause.fire();
                return;
            }

            int nouvelleDirection = direction[0];

            switch (code) {
                case UP    -> { if (direction[0] != BAS)    nouvelleDirection = HAUT;   }
                case DOWN  -> { if (direction[0] != HAUT)   nouvelleDirection = BAS;    }
                case LEFT  -> { if (direction[0] != DROITE) nouvelleDirection = GAUCHE; }
                case RIGHT -> { if (direction[0] != GAUCHE) nouvelleDirection = DROITE; }
                case SPACE -> btnPause.fire();
                default    -> { return; }
            }

            prochainDirection[0] = nouvelleDirection;
        });

        // Initialiser l'affichage
        mettreAJourAffichage(cases, serpent, pommeX[0], pommeY[0]);

        jeuStage.show();
        jeuStage.requestFocus();
        
        // Centrer la fenêtre
        if (parentStage != null) {
            jeuStage.setX(parentStage.getX() + (parentStage.getWidth() - jeuStage.getWidth()) / 2);
            jeuStage.setY(parentStage.getY() + (parentStage.getHeight() - jeuStage.getHeight()) / 2);
        }
    }
    
    private static void placerPomme(int[] pommeX, int[] pommeY, List<int[]> serpent) {
        boolean positionValide;
        do {
            pommeX[0] = random.nextInt(LARGEUR);
            pommeY[0] = random.nextInt(HAUTEUR);
            
            positionValide = true;
            for (int[] segment : serpent) {
                if (segment[0] == pommeX[0] && segment[1] == pommeY[0]) {
                    positionValide = false;
                    break;
                }
            }
        } while (!positionValide);
    }
    
    private static void mettreAJourAffichage(Rectangle[][] cases, List<int[]> serpent, int pommeX, int pommeY) {
        // Effacer la grille
        for (int y = 0; y < HAUTEUR; y++) {
            for (int x = 0; x < LARGEUR; x++) {
                cases[y][x].setFill(Color.web("#34495e"));
                cases[y][x].setStroke(Color.web("#2c3e50"));
                cases[y][x].setStrokeWidth(1);
            }
        }
        
        // Dessiner le serpent
        for (int i = 0; i < serpent.size(); i++) {
            int[] segment = serpent.get(i);
            Rectangle rect = cases[segment[1]][segment[0]];
            
            if (i == 0) {
                // Tête du serpent
                rect.setFill(Color.web("#f1c40f"));
                rect.setStroke(Color.web("#f39c12"));
                rect.setStrokeWidth(2);
            } else {
                // Corps du serpent
                rect.setFill(Color.web("#2ecc71"));
                rect.setStroke(Color.web("#27ae60"));
                rect.setStrokeWidth(1);
            }
        }
        
        // Dessiner la pomme
        Rectangle pomme = cases[pommeY][pommeX];
        pomme.setFill(Color.web("#e74c3c"));
        pomme.setStroke(Color.web("#c0392b"));
        pomme.setStrokeWidth(2);
    }
    
    private static void finirJeu(Stage jeuStage, int scoreTotal, String raison) {
        String emoji, titre, sousTitre, couleur;
        if (scoreTotal >= 200) {
            emoji = "🏆"; titre = "LÉGENDAIRE !"; sousTitre = "Maître du serpent !"; couleur = "#f6c90e";
        } else if (scoreTotal >= 100) {
            emoji = "🥇"; titre = "EXCELLENT !"; sousTitre = "Très bon score !"; couleur = "#27ae60";
        } else if (scoreTotal >= 50) {
            emoji = "🥈"; titre = "PAS MAL !"; sousTitre = "Continuez comme ça !"; couleur = "#3498db";
        } else {
            emoji = "🥉"; titre = "BON DÉBUT !"; sousTitre = "L'entraînement paie !"; couleur = "#9b59b6";
        }

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(jeuStage);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(400);
        root.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 10);");

        // Bandeau coloré
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 25, 20));
        header.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 20 20 0 0;");
        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 52px;");
        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 0, 2);");
        Label lblSous = new Label(sousTitre);
        lblSous.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");
        // Raison de fin
        Label lblRaison = new Label("💀 " + raison);
        lblRaison.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.75); -fx-padding: 4 0 0 0;");
        header.getChildren().addAll(lblEmoji, lblTitre, lblSous, lblRaison);

        // Stats
        HBox stats = new HBox(0);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(25, 20, 20, 20));
        stats.setStyle("-fx-background-color: #16213e;");
        VBox s1 = creerStat(scoreTotal + " pts", "SCORE", couleur);
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: #2a2a4a;");
        VBox s2 = creerStat((scoreTotal / 10) + " 🍎", "POMMES", couleur);
        HBox.setHgrow(s1, Priority.ALWAYS);
        HBox.setHgrow(s2, Priority.ALWAYS);
        stats.getChildren().addAll(s1, sep, s2);

        // Boutons
        HBox boutons = new HBox(12);
        boutons.setAlignment(Pos.CENTER);
        boutons.setPadding(new Insets(0, 20, 25, 20));
        boutons.setStyle("-fx-background-color: #16213e; -fx-background-radius: 0 0 20 20;");

        Button btnRejouer = new Button("🔄  Rejouer");
        btnRejouer.setPrefWidth(160); btnRejouer.setPrefHeight(44);
        btnRejouer.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnRejouer.setOnMouseEntered(e -> btnRejouer.setOpacity(0.85));
        btnRejouer.setOnMouseExited(e  -> btnRejouer.setOpacity(1.0));
        btnRejouer.setOnAction(e -> popup.close());

        Button btnQuitter = new Button("✕  Quitter");
        btnQuitter.setPrefWidth(160); btnQuitter.setPrefHeight(44);
        btnQuitter.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnQuitter.setOnMouseEntered(e -> btnQuitter.setOpacity(0.85));
        btnQuitter.setOnMouseExited(e  -> btnQuitter.setOpacity(1.0));
        btnQuitter.setOnAction(e -> { popup.close(); jeuStage.close(); });
        boutons.getChildren().addAll(btnRejouer, btnQuitter);

        root.getChildren().addAll(header, stats, boutons);

        // Animation d'entrée
        root.setScaleX(0.5); root.setScaleY(0.5); root.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(350), root);
        st.setToX(1); st.setToY(1);
        st.setInterpolator(Interpolator.SPLINE(0.175, 0.885, 0.32, 1.275));
        FadeTransition ft = new FadeTransition(Duration.millis(250), root);
        ft.setToValue(1);
        new ParallelTransition(st, ft).play();

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.show();
        popup.setX(jeuStage.getX() + (jeuStage.getWidth()  - 400) / 2);
        popup.setY(jeuStage.getY() + (jeuStage.getHeight() - 300) / 2);
    }

    private static VBox creerStat(String valeur, String label, String couleur) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10, 20, 10, 20));
        Label lblVal = new Label(valeur);
        lblVal.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");
        Label lblLab = new Label(label);
        lblLab.setStyle("-fx-font-size: 11px; -fx-text-fill: #8892b0; -fx-font-weight: bold;");
        box.getChildren().addAll(lblVal, lblLab);
        return box;
    }
}