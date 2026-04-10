package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainAdminFx extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeAdmin.fxml"));

            if (loader.getLocation() == null) {
                throw new RuntimeException("Impossible de trouver /HomeAdmin.fxml dans resources");
            }

            Scene scene = new Scene(loader.load(), 1024, 700);
            primaryStage.setTitle("MentalUp Admin Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}