package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainHomeFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
        Scene scene = new Scene(loader.load(), 1500, 920);

        stage.setTitle("MentalUp");
        stage.setScene(scene);

        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setMinWidth(1200);
        stage.setMinHeight(750);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}