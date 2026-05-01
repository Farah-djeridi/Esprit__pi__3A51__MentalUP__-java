package esprit.tn;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EtudiantFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/EtudiantActivites.fxml"));
        primaryStage.setTitle("MentalUp - Espace Étudiant");
        primaryStage.setScene(new Scene(root, 1280, 850));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
