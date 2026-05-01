package utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.*;

/**
 * Jeu de mémoire avec des cartes à retourner
 */
public class JeuMemoire {
    
    private static final String[] SYMBOLES = {"🎯", "🎪", "🎨", "🎵", "🌟", "🎈", "🎭", "🎮"};
    private static final Random random = new Random();
    
    public static void lancerJeuMemoire(Stage parentStage, String nomActivite) {
        Stage jeuStage = new Stage();
        jeuStage.initModality(Modality.APPLICATION_MODAL);
        jeuStage.initOwner(parentStage);
        jeuStage.setTitle("🧠 " + nomActivite + " - Jeu de Mémoire");
        jeuStage.setResizable(false);
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50 0%, #34495e 100%);");
        root.setPrefSize(600, 550);
        
        // Variables du jeu
        final int[] score = {0};
        final int[] coups = {0};
        final int[] pairesRestantes = {8};
        final Button[] carteSelectionnee = {null};
        final boolean[] jeuActif = {false};
        final boolean[] enAttente = {false}; // bloque les clics pendant le retournement
        
        // Header
        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER);
        
        Label lblScore = new Label("Score: 0");
        lblScore.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label lblCoups = new Label("Coups: 0");
        lblCoups.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label lblPaires = new Label("Paires: 8");
        lblPaires.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        header.getChildren().addAll(lblScore, lblCoups, lblPaires);
        
        // Grille de cartes 4x4
        GridPane grille = new GridPane();
        grille.setHgap(10);
        grille.setVgap(10);
        grille.setAlignment(Pos.CENTER);
        
        // Créer les paires de cartes
        List<String> cartes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            cartes.add(SYMBOLES[i]);
            cartes.add(SYMBOLES[i]); // Ajouter la paire
        }
        Collections.shuffle(cartes);
        
        // Créer les boutons de cartes
        Button[][] boutonsCartes = new Button[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                final int index = i * 4 + j;
                final String symbole = cartes.get(index);
                
                Button carte = new Button("?");
                carte.setPrefSize(80, 80);
                carte.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 24px; " +
                              "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
                
                carte.setUserData(symbole);
                boutonsCartes[i][j] = carte;
                
                carte.setOnAction(e -> {
                    if (!jeuActif[0] || carte.isDisabled() || enAttente[0]) return;
                    
                    // Retourner la carte
                    carte.setText(symbole);
                    carte.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 24px; " +
                                  "-fx-font-weight: bold; -fx-background-radius: 10;");
                    carte.setDisable(true);
                    
                    if (carteSelectionnee[0] == null) {
                        // Première carte sélectionnée
                        carteSelectionnee[0] = carte;
                    } else {
                        // Deuxième carte sélectionnée
                        coups[0]++;
                        lblCoups.setText("Coups: " + coups[0]);
                        
                        Button premiereCarte = carteSelectionnee[0];
                        String premiereValeur = (String) premiereCarte.getUserData();
                        String deuxiemeValeur = (String) carte.getUserData();
                        
                        carteSelectionnee[0] = null;
                        
                        if (premiereValeur.equals(deuxiemeValeur)) {
                            // Paire trouvée !
                            pairesRestantes[0]--;
                            score[0] += Math.max(10, 50 - coups[0]); // Bonus pour moins de coups
                            
                            lblScore.setText("Score: " + score[0]);
                            lblPaires.setText("Paires: " + pairesRestantes[0]);
                            
                            // Animation de succès
                            ScaleTransition scale1 = new ScaleTransition(Duration.millis(200), premiereCarte);
                            scale1.setToX(1.2);
                            scale1.setToY(1.2);
                            scale1.setAutoReverse(true);
                            scale1.setCycleCount(2);
                            
                            ScaleTransition scale2 = new ScaleTransition(Duration.millis(200), carte);
                            scale2.setToX(1.2);
                            scale2.setToY(1.2);
                            scale2.setAutoReverse(true);
                            scale2.setCycleCount(2);
                            
                            ParallelTransition celebration = new ParallelTransition(scale1, scale2);
                            celebration.play();
                            
                            // Marquer les cartes comme trouvées (vertes, définitivement désactivées)
                            premiereCarte.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 24px; " +
                                                  "-fx-font-weight: bold; -fx-background-radius: 10;");
                            carte.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 24px; " +
                                          "-fx-font-weight: bold; -fx-background-radius: 10;");
                            
                            // Vérifier si le jeu est terminé
                            if (pairesRestantes[0] == 0) {
                                finirJeuMemoire(jeuStage, score[0], coups[0]);
                            }
                            
                        } else {
                            // Pas une paire - bloquer les clics puis retourner les cartes après un délai
                            enAttente[0] = true;
                            Timeline retourner = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                                premiereCarte.setText("?");
                                premiereCarte.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 24px; " +
                                                      "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
                                premiereCarte.setDisable(false);
                                
                                carte.setText("?");
                                carte.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 24px; " +
                                              "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
                                carte.setDisable(false);
                                
                                enAttente[0] = false; // débloquer les clics
                            }));
                            retourner.play();
                        }
                    }
                });
                
                grille.add(carte, j, i);
            }
        }
        
        // Instructions
        Label instructions = new Label("Trouvez toutes les paires en retournant les cartes !\nMémorisez les positions pour un meilleur score.");
        instructions.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-text-alignment: center;");
        instructions.setAlignment(Pos.CENTER);
        
        // Boutons
        Button btnCommencer = new Button("🧠 Commencer");
        btnCommencer.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; " +
                             "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        
        Button btnQuitter = new Button("❌ Quitter");
        btnQuitter.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; " +
                           "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        btnQuitter.setOnAction(e -> jeuStage.close());
        
        Button btnRecommencer = new Button("🔄 Recommencer");
        btnRecommencer.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 16px; " +
                               "-fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand; -fx-background-radius: 10;");
        btnRecommencer.setVisible(false);
        
        HBox boutons = new HBox(20, btnCommencer, btnRecommencer, btnQuitter);
        boutons.setAlignment(Pos.CENTER);
        
        btnCommencer.setOnAction(e -> {
            jeuActif[0] = true;
            btnCommencer.setVisible(false);
            btnRecommencer.setVisible(true);
            instructions.setText("Jeu en cours... Trouvez toutes les paires !");
            
            // Montrer brièvement toutes les cartes
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Button carte = boutonsCartes[i][j];
                    carte.setText((String) carte.getUserData());
                    carte.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 24px; " +
                                  "-fx-font-weight: bold; -fx-background-radius: 10;");
                }
            }
            
            // Cacher les cartes après 2 secondes
            Timeline cacher = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        Button carte = boutonsCartes[i][j];
                        carte.setText("?");
                        carte.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 24px; " +
                                      "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
                        carte.setDisable(false);
                    }
                }
            }));
            cacher.play();
        });
        
        btnRecommencer.setOnAction(e -> {
            // Réinitialiser le jeu
            score[0] = 0;
            coups[0] = 0;
            pairesRestantes[0] = 8;
            carteSelectionnee[0] = null;
            jeuActif[0] = false;
            enAttente[0] = false;
            
            lblScore.setText("Score: 0");
            lblCoups.setText("Coups: 0");
            lblPaires.setText("Paires: 8");
            
            // Mélanger à nouveau les cartes
            Collections.shuffle(cartes);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Button carte = boutonsCartes[i][j];
                    carte.setUserData(cartes.get(i * 4 + j));
                    carte.setText("?");
                    carte.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 24px; " +
                                  "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
                    carte.setDisable(true);
                }
            }
            
            btnCommencer.setVisible(true);
            btnRecommencer.setVisible(false);
            instructions.setText("Trouvez toutes les paires en retournant les cartes !\nMémorisez les positions pour un meilleur score.");
        });
        
        root.getChildren().addAll(header, grille, instructions, boutons);
        
        Scene scene = new Scene(root);
        jeuStage.setScene(scene);
        jeuStage.show();
        
        // Centrer la fenêtre
        if (parentStage != null) {
            jeuStage.setX(parentStage.getX() + (parentStage.getWidth() - jeuStage.getWidth()) / 2);
            jeuStage.setY(parentStage.getY() + (parentStage.getHeight() - jeuStage.getHeight()) / 2);
        }
    }
    
    private static void finirJeuMemoire(Stage jeuStage, int scoreTotal, int coupsTotal) {
        String emoji, titre, sousTitre, couleurAccent;
        if (coupsTotal <= 20) {
            emoji = "🏆"; titre = "LÉGENDAIRE !"; sousTitre = "Mémoire exceptionnelle !";
            couleurAccent = "#f6c90e";
        } else if (coupsTotal <= 30) {
            emoji = "🥇"; titre = "EXCELLENT !"; sousTitre = "Très bonne mémoire !";
            couleurAccent = "#27ae60";
        } else if (coupsTotal <= 40) {
            emoji = "🥈"; titre = "BIEN JOUÉ !"; sousTitre = "Continuez à vous entraîner !";
            couleurAccent = "#3498db";
        } else {
            emoji = "🥉"; titre = "BON EFFORT !"; sousTitre = "La pratique améliore la mémoire !";
            couleurAccent = "#9b59b6";
        }
        afficherPopupResultat(jeuStage, emoji, titre, sousTitre, couleurAccent,
                "Score final", scoreTotal + " pts",
                "Coups joués", coupsTotal + " coups",
                true);
    }

    private static void afficherPopupResultat(Stage owner, String emoji, String titre, String sousTitre,
                                              String couleurAccent,
                                              String stat1Label, String stat1Val,
                                              String stat2Label, String stat2Val,
                                              boolean avecRejouer) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);

        // ── Fond principal ────────────────────────────────────────────────────
        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(400);
        root.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 10);");

        // ── Bandeau coloré du haut ────────────────────────────────────────────
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

        // ── Stats ─────────────────────────────────────────────────────────────
        HBox stats = new HBox(0);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(25, 20, 20, 20));
        stats.setStyle("-fx-background-color: #16213e;");

        VBox stat1 = creerStat(stat1Val, stat1Label, couleurAccent);
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: #2a2a4a;");
        VBox stat2 = creerStat(stat2Val, stat2Label, couleurAccent);

        HBox.setHgrow(stat1, Priority.ALWAYS);
        HBox.setHgrow(stat2, Priority.ALWAYS);
        stats.getChildren().addAll(stat1, sep, stat2);

        // ── Boutons ───────────────────────────────────────────────────────────
        HBox boutons = new HBox(12);
        boutons.setAlignment(Pos.CENTER);
        boutons.setPadding(new Insets(0, 20, 25, 20));
        boutons.setStyle("-fx-background-color: #16213e; -fx-background-radius: 0 0 20 20;");

        if (avecRejouer) {
            Button btnRejouer = new Button("🔄  Rejouer");
            btnRejouer.setPrefWidth(160);
            btnRejouer.setPrefHeight(44);
            btnRejouer.setStyle("-fx-background-color: " + couleurAccent + "; -fx-text-fill: white; " +
                                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
            btnRejouer.setOnMouseEntered(e -> btnRejouer.setOpacity(0.85));
            btnRejouer.setOnMouseExited(e  -> btnRejouer.setOpacity(1.0));
            btnRejouer.setOnAction(e -> popup.close());
            boutons.getChildren().add(btnRejouer);
        }

        Button btnQuitter = new Button("✕  Quitter");
        btnQuitter.setPrefWidth(160);
        btnQuitter.setPrefHeight(44);
        btnQuitter.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnQuitter.setOnMouseEntered(e -> btnQuitter.setOpacity(0.85));
        btnQuitter.setOnMouseExited(e  -> btnQuitter.setOpacity(1.0));
        btnQuitter.setOnAction(e -> { popup.close(); owner.close(); });
        boutons.getChildren().add(btnQuitter);

        root.getChildren().addAll(header, stats, boutons);

        // ── Animation d'entrée ────────────────────────────────────────────────
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

        // Centrer sur le owner
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