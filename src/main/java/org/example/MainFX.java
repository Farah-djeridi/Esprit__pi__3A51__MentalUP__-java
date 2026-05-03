package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {
        utils.SceneManager.setPrimaryStage(stage);
        try {
            FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/Login.fxml")
            );


            Scene scene = new Scene(loader.load());

            stage.setTitle("MentalUp - Dashboard Psychologue");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static void styleAlert(Dialog<?> alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; " +
                           "-fx-border-width: 1; -fx-border-radius: 12; " +
                           "-fx-background-radius: 12; -fx-padding: 5;");

        Node content = dialogPane.lookup(".content");
        if (content != null) {
            content.setStyle("-fx-font-size: 14px; -fx-text-fill: #2C3E50; " +
                            "-fx-padding: 10 20 20 20; -fx-line-spacing: 1.2;");
        }

      
        Node header = dialogPane.lookup(".header-panel");
        if (header != null) {
            header.setStyle("-fx-background-color: #F8FBFF; -fx-padding: 15 20; " +
                           "-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
        }

      
        alert.getDialogPane().getButtonTypes().forEach(type -> {
            Button btn = (Button) dialogPane.lookupButton(type);
            if (btn != null) {
                if (type.getButtonData() == ButtonBar.ButtonData.OK_DONE || 
                    type.getButtonData() == ButtonBar.ButtonData.YES) {
                    btn.setStyle("-fx-background-color: #1A73E8; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-background-radius: 8; " +
                                "-fx-padding: 8 22; -fx-cursor: hand;");
                } else {
                    btn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; " +
                                "-fx-font-weight: bold; -fx-background-radius: 8; " +
                                "-fx-padding: 8 22; -fx-cursor: hand;");
                }
                btn.setPrefHeight(38);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}