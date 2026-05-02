package Controllor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Ressource;
import services.ServiceRessource;

import java.awt.Desktop;
import java.net.URI;
import java.text.SimpleDateFormat;

public class ControllerRessourceDetail {

    @FXML
    private Label detailCategory;
    @FXML
    private ImageView detailImage;
    @FXML
    private Label detailTitle;
    @FXML
    private Label detailDate;
    @FXML
    private Label detailViews;
    @FXML
    private Label detailDesc;
    @FXML
    private Button openLinkBtn;

    private Ressource ressource;
    private ServiceRessource serviceRessource = new ServiceRessource();

    public void initData(Ressource r) {
        this.ressource = r;

        detailTitle.setText(r.getTitre());
        detailDesc.setText(r.getDescription());
        detailCategory.setText(r.getCategorieNom() != null ? r.getCategorieNom() : "Non classé");
        detailViews.setText("👀 " + r.getNbVues() + " vues");

        if (r.getDatePublication() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            detailDate.setText("📅 " + sdf.format(r.getDatePublication()));
        } else {
            detailDate.setText("📅 Date inconnue");
        }

        try {
            String imgUrl = r.getImage() != null && !r.getImage().isEmpty() ? r.getImage() : "/Images/default_res.png";
            Image image = new Image(imgUrl, 740, 300, false, true);
            detailImage.setImage(image);
        } catch (Exception e) {
            detailImage.setImage(null);
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/StudentRessources.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openLink(ActionEvent event) {
        if (ressource != null && ressource.getLien() != null && !ressource.getLien().isEmpty()) {
            try {
                Desktop.getDesktop().browse(new URI(ressource.getLien()));
                // Increment views
                serviceRessource.incrementViews(ressource.getId());
                ressource.setNbVues(ressource.getNbVues() + 1);
                detailViews.setText("👀 " + ressource.getNbVues() + " vues");
            } catch (Exception ex) {
                System.out.println("Impossible d'ouvrir le lien: " + ex.getMessage());
            }
        }
    }
}
