package controllers;

import Models.RendezVous;
import Services.ServiceRendezVous;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class ControllerMeet {

    @FXML private Label labelMeetingTitle;
    @FXML private Button btnLaunchMeet;
    @FXML private Button btnTerminer;
    @FXML private VBox waitingArea;
    @FXML private SplitPane splitPane;
    @FXML private VBox dossierPanel;
    @FXML private StackPane dossierContent;
    @FXML private HBox statusBadge;

    private RendezVous rdv;
    private int userId;
    private String userType; // "patient" or "psychologue"
    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private Timer checkTimer;

    public void initData(RendezVous rdv, int userId, String userType) {
        this.rdv = rdv;
        this.userId = userId;
        this.userType = userType;

        labelMeetingTitle.setText("Session : " + rdv.getTypeRdv());

        if ("psychologue".equalsIgnoreCase(userType)) {
            serviceRdv.setPsyJoined(rdv.getId(), true);
            showDossierPanel();
            btnLaunchMeet.setText("Lancer l'appel vidéo");
            btnTerminer.setVisible(true);
            btnTerminer.setManaged(true);
        } else {
            // Patient: wait for psy
            btnTerminer.setVisible(false);
            btnTerminer.setManaged(false);
            btnLaunchMeet.setVisible(false);
            btnLaunchMeet.setManaged(false);
            waitingArea.setVisible(true);
            waitingArea.setManaged(true);
            
            dossierPanel.setManaged(false);
            dossierPanel.setVisible(false);
            splitPane.setDividerPositions(1.0);
            
            startCheckingPsyPresence();
        }
    }

    @FXML
    private void onLaunchMeet(ActionEvent event) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(rdv.getLienMeet()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCheckingPsyPresence() {
        checkTimer = new Timer(true);
        checkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                RendezVous updatedRdv = serviceRdv.getById(rdv.getId());
                if (updatedRdv != null && updatedRdv.isPsyJoined()) {
                    Platform.runLater(() -> {
                        waitingArea.setVisible(false);
                        waitingArea.setManaged(false);
                        btnLaunchMeet.setVisible(true);
                        btnLaunchMeet.setManaged(true);
                        btnLaunchMeet.setText("Rejoindre le Psychologue");
                        checkTimer.cancel();
                    });
                }
            }
        }, 0, 3000);
    }

    private void showDossierPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/AdminDossiers.fxml"));
            Parent root = loader.load();
            if (root instanceof javafx.scene.layout.BorderPane bp) {
                dossierContent.getChildren().setAll(bp.getCenter());
            } else {
                dossierContent.getChildren().setAll(root);
            }
            splitPane.setDividerPositions(0.4); // Dossier on the right
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void closeDossierPanel(ActionEvent event) {
        splitPane.setDividerPositions(1.0);
    }

    @FXML
    private void onTerminerSession(ActionEvent event) {
        serviceRdv.terminerRdv(rdv.getId());
        onRetour(event);
    }

    @FXML
    private void onRetour(ActionEvent event) {
        if (checkTimer != null) checkTimer.cancel();
        if ("psychologue".equalsIgnoreCase(userType)) {
            serviceRdv.setPsyJoined(rdv.getId(), false);
        }
        
        try {
            // Redirect back to the psy dashboard list
            String fxml = "psychologue".equalsIgnoreCase(userType) ? "/gui/VoirRendezVous.fxml" : "/gui/Home.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
