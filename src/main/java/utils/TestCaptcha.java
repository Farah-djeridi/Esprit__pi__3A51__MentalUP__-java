package utils;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Classe de test pour démontrer les différents types de CAPTCHA
 */
public class TestCaptcha extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f7fafc;");
        
        Label titre = new Label("🧪 Test des CAPTCHA");
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        Label description = new Label("Testez les différents types de vérification anti-robot");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096;");
        
        // Bouton pour CAPTCHA simple
        Button btnCaptchaSimple = new Button("🎯 CAPTCHA Simple");
        btnCaptchaSimple.setStyle("-fx-background-color: #4299e1; -fx-text-fill: white; " +
                                 "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 15 30; " +
                                 "-fx-cursor: hand; -fx-background-radius: 8;");
        btnCaptchaSimple.setPrefWidth(250);
        
        btnCaptchaSimple.setOnAction(e -> {
            CaptchaVerification.afficherVerification(
                primaryStage,
                "tester le CAPTCHA simple",
                success -> {
                    if (success) {
                        System.out.println("✅ CAPTCHA simple réussi !");
                    } else {
                        System.out.println("❌ CAPTCHA simple échoué");
                    }
                }
            );
        });
        
        // Bouton pour CAPTCHA avancé
        Button btnCaptchaAvance = new Button("🚀 CAPTCHA Avancé");
        btnCaptchaAvance.setStyle("-fx-background-color: #9f7aea; -fx-text-fill: white; " +
                                 "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 15 30; " +
                                 "-fx-cursor: hand; -fx-background-radius: 8;");
        btnCaptchaAvance.setPrefWidth(250);
        
        btnCaptchaAvance.setOnAction(e -> {
            CaptchaAvance.afficherCaptchaAvance(
                primaryStage,
                "tester le CAPTCHA avancé",
                success -> {
                    if (success) {
                        System.out.println("✅ CAPTCHA avancé réussi !");
                    } else {
                        System.out.println("❌ CAPTCHA avancé échoué");
                    }
                }
            );
        });
        
        // Bouton pour test aléatoire
        Button btnTestAleatoire = new Button("🎲 Test Aléatoire");
        btnTestAleatoire.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; " +
                                 "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 15 30; " +
                                 "-fx-cursor: hand; -fx-background-radius: 8;");
        btnTestAleatoire.setPrefWidth(250);
        
        btnTestAleatoire.setOnAction(e -> {
            boolean utiliserAvance = Math.random() > 0.5;
            
            if (utiliserAvance) {
                System.out.println("🎯 Test avec CAPTCHA avancé...");
                CaptchaAvance.afficherCaptchaAvance(
                    primaryStage,
                    "effectuer un test aléatoire",
                    success -> {
                        System.out.println(success ? "✅ Test réussi !" : "❌ Test échoué");
                    }
                );
            } else {
                System.out.println("🎯 Test avec CAPTCHA simple...");
                CaptchaVerification.afficherVerification(
                    primaryStage,
                    "effectuer un test aléatoire",
                    success -> {
                        System.out.println(success ? "✅ Test réussi !" : "❌ Test échoué");
                    }
                );
            }
        });
        
        root.getChildren().addAll(titre, description, btnCaptchaSimple, btnCaptchaAvance, btnTestAleatoire);
        
        Scene scene = new Scene(root, 400, 350);
        primaryStage.setTitle("Test CAPTCHA");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}