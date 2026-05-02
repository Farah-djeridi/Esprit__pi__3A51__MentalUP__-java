package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainObjectifFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/objvue.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 850);
        scene.getStylesheets().add(getClass().getResource("/objectif.css").toExternalForm());

        stage.setTitle("Mes Objectifs");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}