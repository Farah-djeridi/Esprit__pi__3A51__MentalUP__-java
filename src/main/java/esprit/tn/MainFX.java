package esprit.tn;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.SceneManager;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("MentalUp");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        SceneManager.goToLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
