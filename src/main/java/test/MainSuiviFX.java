package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainSuiviFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/suivi_mentale.fxml"));
        Scene scene = new Scene(loader.load(), 1500, 920);

        stage.setTitle("Suivi Mental");
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