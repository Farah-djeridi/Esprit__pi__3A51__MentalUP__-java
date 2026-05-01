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

import java.util.Random;

/**
 * Jeu de labyrinthe avec contrôles au clavier
 */
public class JeuLabyrinthe {
    
    private static final int TAILLE_CASE = 25;
    private static final int LARGEUR = 21; // Impair pour avoir des murs
    private static final int HAUTEUR = 15;
    private static final Random random = new Random();
    
    // Types de cases
    private static final int MUR = 1;
    private static final int CHEMIN = 0;
    private static final int JOUEUR = 2;
    private static final int SORTIE = 3;
    private static final int TRESOR = 4;
    
    public static void lancerJeuLabyrinthe(Stage parentStage, String nomActivite) {
        Stage jeuStage = new Stage();
        jeuStage.initModality(Modality.APPLICATION_MODAL);
        jeuStage.initOwner(parentStage);
        jeuStage.setTitle("🏃 " + nomActivite + " - Labyrinthe");
        jeuStage.setResizable(false);
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1e3c72 0%, #2a5298 100%);");
        
        // Variables du jeu
        final int[][] labyrinthe = new int[HAUTEUR][LARGEUR];
        final int[] joueurX = {1};
        final int[] joueurY = {1};
        final int[] score = {0};
        final int[] tresorsRestants = {5};
        final long[] tempsDebut = {System.currentTimeMillis()};
        
        // Header
        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER);
        
        Label lblScore = new Label("Trésors: 5");
        lblScore.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label lblTemps = new Label("Temps: 0s");
        lblTemps.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        header.getChildren().addAll(lblScore, lblTemps);
        
        // Grille du labyrinthe
        GridPane grille = new GridPane();
        grille.setAlignment(Pos.CENTER);
        grille.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10; -fx-background-radius: 10;");
        
        Rectangle[][] cases = new Rectangle[HAUTEUR][LARGEUR];
        
        // Générer le labyrinthe
        genererLabyrinthe(labyrinthe);
        placerTresors(labyrinthe, tresorsRestants[0]);
        
        // Créer l'affichage
        for (int y = 0; y < HAUTEUR; y++) {
            for (int x = 0; x < LARGEUR; x++) {
                Rectangle rect = new Rectangle(TAILLE_CASE, TAILLE_CASE);
                cases[y][x] = rect;
                mettreAJourCase(rect, labyrinthe[y][x], x == joueurX[0] && y == joueurY[0]);
                grille.add(rect, x, y);
            }
        }
        
        // Instructions
        Label instructions = new Label("Utilisez les flèches ↑↓←→ pour vous déplacer\nCollectez tous les trésors 💎 puis atteignez la sortie 🚪");
        instructions.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-text-alignment: center;");
        instructions.setAlignment(Pos.CENTER);
        
        // Timer
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            long tempsEcoule = (System.currentTimeMillis() - tempsDebut[0]) / 1000;
            lblTemps.setText("Temps: " + tempsEcoule + "s");
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
        
        // Boutons
        Button btnRecommencer = new Button("🔄 Nouveau Labyrinthe");
        btnRecommencer.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 14px; " +
                               "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 8;");
        
        Button btnQuitter = new Button("❌ Quitter");
        btnQuitter.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; " +
                           "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 8;");
        btnQuitter.setOnAction(e -> {
            timer.stop();
            jeuStage.close();
        });
        
        btnRecommencer.setOnAction(e -> {
            // Réinitialiser le jeu
            joueurX[0] = 1;
            joueurY[0] = 1;
            score[0] = 0;
            tresorsRestants[0] = 5;
            tempsDebut[0] = System.currentTimeMillis();
            
            genererLabyrinthe(labyrinthe);
            placerTresors(labyrinthe, tresorsRestants[0]);
            
            // Mettre à jour l'affichage
            for (int y = 0; y < HAUTEUR; y++) {
                for (int x = 0; x < LARGEUR; x++) {
                    mettreAJourCase(cases[y][x], labyrinthe[y][x], x == joueurX[0] && y == joueurY[0]);
                }
            }
            
            lblScore.setText("Trésors: " + tresorsRestants[0]);
            lblTemps.setText("Temps: 0s");
        });
        
        HBox boutons = new HBox(20, btnRecommencer, btnQuitter);
        boutons.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(header, grille, instructions, boutons);
        
        Scene scene = new Scene(root);
        jeuStage.setScene(scene);

        // Gestion des touches via addEventFilter sur le STAGE :
        // le stage reçoit les événements clavier même si un bouton a le focus
        jeuStage.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            if (code != KeyCode.UP && code != KeyCode.DOWN
                    && code != KeyCode.LEFT && code != KeyCode.RIGHT) return;

            // Consommer l'événement pour que les boutons ne le traitent pas
            e.consume();

            int nouveauX = joueurX[0];
            int nouveauY = joueurY[0];

            switch (code) {
                case UP    -> nouveauY--;
                case DOWN  -> nouveauY++;
                case LEFT  -> nouveauX--;
                case RIGHT -> nouveauX++;
                default    -> { return; }
            }

            // Vérifier les limites et les murs
            if (nouveauX >= 0 && nouveauX < LARGEUR && nouveauY >= 0 && nouveauY < HAUTEUR
                    && labyrinthe[nouveauY][nouveauX] != MUR) {

                // Effacer l'ancienne position
                mettreAJourCase(cases[joueurY[0]][joueurX[0]], labyrinthe[joueurY[0]][joueurX[0]], false);

                // Vérifier si c'est un trésor
                if (labyrinthe[nouveauY][nouveauX] == TRESOR) {
                    labyrinthe[nouveauY][nouveauX] = CHEMIN;
                    tresorsRestants[0]--;
                    score[0] += 10;
                    lblScore.setText("Trésors: " + tresorsRestants[0]);

                    // Animation de collecte
                    ScaleTransition collect = new ScaleTransition(Duration.millis(200), cases[nouveauY][nouveauX]);
                    collect.setFromX(1);
                    collect.setFromY(1);
                    collect.setToX(1.5);
                    collect.setToY(1.5);
                    collect.setAutoReverse(true);
                    collect.setCycleCount(2);
                    collect.play();
                }

                // Vérifier si c'est la sortie
                if (labyrinthe[nouveauY][nouveauX] == SORTIE && tresorsRestants[0] == 0) {
                    timer.stop();
                    long tempsTotal = (System.currentTimeMillis() - tempsDebut[0]) / 1000;
                    int scoreTotal = score[0] + (int) Math.max(0, 100 - tempsTotal);

                    String emoji, titre, sousTitre, couleur;
                    if (tempsTotal <= 30) {
                        emoji = "🏆"; titre = "INCROYABLE !"; sousTitre = "Vitesse fulgurante !"; couleur = "#f6c90e";
                    } else if (tempsTotal <= 60) {
                        emoji = "🥇"; titre = "EXCELLENT !"; sousTitre = "Très bon parcours !"; couleur = "#27ae60";
                    } else if (tempsTotal <= 90) {
                        emoji = "🥈"; titre = "BIEN JOUÉ !"; sousTitre = "Labyrinthe maîtrisé !"; couleur = "#3498db";
                    } else {
                        emoji = "🥉"; titre = "TERMINÉ !"; sousTitre = "Vous avez trouvé la sortie !"; couleur = "#9b59b6";
                    }
                    afficherPopupResultat(jeuStage, emoji, titre, sousTitre, couleur,
                            "Score", scoreTotal + " pts",
                            "Temps", tempsTotal + "s");
                    return;
                }

                // Déplacer le joueur
                joueurX[0] = nouveauX;
                joueurY[0] = nouveauY;

                // Mettre à jour l'affichage
                mettreAJourCase(cases[joueurY[0]][joueurX[0]], labyrinthe[joueurY[0]][joueurX[0]], true);
            }
        });

        jeuStage.show();
        jeuStage.requestFocus();
        
        // Centrer la fenêtre
        if (parentStage != null) {
            jeuStage.setX(parentStage.getX() + (parentStage.getWidth() - jeuStage.getWidth()) / 2);
            jeuStage.setY(parentStage.getY() + (parentStage.getHeight() - jeuStage.getHeight()) / 2);
        }
    }
    
    private static void genererLabyrinthe(int[][] labyrinthe) {
        // Initialiser avec des murs
        for (int y = 0; y < HAUTEUR; y++) {
            for (int x = 0; x < LARGEUR; x++) {
                labyrinthe[y][x] = MUR;
            }
        }
        
        // Créer des chemins avec l'algorithme de génération simple
        creerChemin(labyrinthe, 1, 1);
        
        // Placer la sortie
        labyrinthe[HAUTEUR-2][LARGEUR-2] = SORTIE;
    }
    
    private static void creerChemin(int[][] labyrinthe, int x, int y) {
        labyrinthe[y][x] = CHEMIN;
        
        // Directions possibles (haut, bas, gauche, droite)
        int[][] directions = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
        
        // Mélanger les directions
        for (int i = 0; i < directions.length; i++) {
            int j = random.nextInt(directions.length);
            int[] temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }
        
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            if (nx > 0 && nx < LARGEUR-1 && ny > 0 && ny < HAUTEUR-1 && labyrinthe[ny][nx] == MUR) {
                labyrinthe[y + dir[1]/2][x + dir[0]/2] = CHEMIN; // Casser le mur
                creerChemin(labyrinthe, nx, ny);
            }
        }
    }
    
    private static void placerTresors(int[][] labyrinthe, int nombreTresors) {
        int places = 0;
        int tentatives = 0;
        
        while (places < nombreTresors && tentatives < 100) {
            int x = random.nextInt(LARGEUR);
            int y = random.nextInt(HAUTEUR);
            
            if (labyrinthe[y][x] == CHEMIN && !(x == 1 && y == 1) && !(x == LARGEUR-2 && y == HAUTEUR-2)) {
                labyrinthe[y][x] = TRESOR;
                places++;
            }
            tentatives++;
        }
    }
    
    private static void mettreAJourCase(Rectangle rect, int type, boolean joueurPresent) {
        if (joueurPresent) {
            rect.setFill(Color.YELLOW);
            rect.setStroke(Color.ORANGE);
            rect.setStrokeWidth(2);
        } else {
            switch (type) {
                case MUR -> {
                    rect.setFill(Color.web("#34495e"));
                    rect.setStroke(Color.web("#2c3e50"));
                    rect.setStrokeWidth(1);
                }
                case CHEMIN -> {
                    rect.setFill(Color.web("#ecf0f1"));
                    rect.setStroke(Color.web("#bdc3c7"));
                    rect.setStrokeWidth(1);
                }
                case SORTIE -> {
                    rect.setFill(Color.web("#27ae60"));
                    rect.setStroke(Color.web("#229954"));
                    rect.setStrokeWidth(2);
                }
                case TRESOR -> {
                    rect.setFill(Color.web("#f1c40f"));
                    rect.setStroke(Color.web("#f39c12"));
                    rect.setStrokeWidth(2);
                }
            }
        }
    }

    // ── Popup résultat custom ─────────────────────────────────────────────────

    private static void afficherPopupResultat(Stage owner, String emoji, String titre, String sousTitre,
                                              String couleurAccent,
                                              String stat1Label, String stat1Val,
                                              String stat2Label, String stat2Val) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);
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
        header.setStyle("-fx-background-color: " + couleurAccent + "; -fx-background-radius: 20 20 0 0;");

        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 52px;");
        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 0, 2);");
        Label lblSous = new Label(sousTitre);
        lblSous.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");
        header.getChildren().addAll(lblEmoji, lblTitre, lblSous);

        // Stats
        HBox stats = new HBox(0);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(25, 20, 20, 20));
        stats.setStyle("-fx-background-color: #16213e;");
        VBox s1 = creerStat(stat1Val, stat1Label, couleurAccent);
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: #2a2a4a;");
        VBox s2 = creerStat(stat2Val, stat2Label, couleurAccent);
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
        btnRejouer.setStyle("-fx-background-color: " + couleurAccent + "; -fx-text-fill: white; " +
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
        btnQuitter.setOnAction(e -> { popup.close(); owner.close(); });
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
        popup.setX(owner.getX() + (owner.getWidth()  - 400) / 2);
        popup.setY(owner.getY() + (owner.getHeight() - 280) / 2);
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