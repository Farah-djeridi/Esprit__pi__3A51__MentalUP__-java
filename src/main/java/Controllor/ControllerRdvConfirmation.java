package Controllor;

import models.RendezVous;
import services.ServiceRendezVous;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ControllerRdvConfirmation {


    @FXML private Label  labelPsyNom;
    @FXML private Label  labelDate;
    @FXML private Label  labelHeure;
    @FXML private Label  labelType;
    @FXML private ComboBox<String> comboMode;
    @FXML private TextField txtTelephone;
    @FXML private Button btnConfirmer;


    private final ServiceRendezVous serviceRdv = new ServiceRendezVous();
    private RendezVous rdv;
    private int        etudiantId;
    private Runnable   onSuccess;


    public void initData(RendezVous rdv, String psyNom, int etudiantId, Runnable onSuccess) {
        this.rdv        = rdv;
        this.etudiantId = etudiantId;
        this.onSuccess  = onSuccess;


        labelPsyNom.setText(psyNom);


        if (rdv.getDate() != null) {
            java.time.LocalDate ld = rdv.getDate().toLocalDate();
            String[] joursFr = {"Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche"};
            String[] moisFr  = {"Janvier","Février","Mars","Avril","Mai","Juin",
                    "Juillet","Août","Septembre","Octobre","Novembre","Décembre"};
            String nomJour = joursFr[ld.getDayOfWeek().getValue() - 1];
            String nomMois = moisFr[ld.getMonthValue() - 1];
            labelDate.setText(nomJour + " " + ld.getDayOfMonth() + " " + nomMois + " " + ld.getYear());
        } else {
            labelDate.setText("Date non définie");
        }

        
        String heureDebut = rdv.getHeureDebut() != null
                ? rdv.getHeureDebut().toString().substring(0, 5) : "--:--";
        String heureFin   = rdv.getHeureFin() != null
                ? rdv.getHeureFin().toString().substring(0, 5) : "--:--";
        labelHeure.setText(heureDebut + " – " + heureFin);


        labelType.setText(rdv.getTypeRdv() != null ? rdv.getTypeRdv() : "Consultation individuelle");

    
        comboMode.getItems().addAll("Présentiel", "En ligne");
        comboMode.setValue("Présentiel");
    }


    @FXML
    private void onConfirmer(ActionEvent event) {
       
        ScaleTransition st = new ScaleTransition(Duration.millis(100), btnConfirmer);
        st.setToX(0.95); st.setToY(0.95);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        String selectedMode = comboMode.getValue();
        String telephone = txtTelephone.getText().trim();
        
        // Validation des champs
        boolean hasError = false;
        if (selectedMode == null || selectedMode.isEmpty()) {
            comboMode.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            hasError = true;
        } else {
            comboMode.setStyle("");
        }

        if (telephone.isEmpty()) {
            txtTelephone.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 10;");
            hasError = true;
        } else {
            txtTelephone.setStyle("");
        }

        if (hasError) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs obligatoires.");
            alert.showAndWait();
            return;
        }

        boolean ok = serviceRdv.reserverCreneau(rdv, etudiantId, selectedMode, telephone);

        if (ok) {

            btnConfirmer.setText("Réservé !");
            btnConfirmer.setStyle(
                    "-fx-background-color: linear-gradient(to right, #1E8449, #145A32);" +
                            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;" +
                            "-fx-background-radius: 10; -fx-padding: 10 28;"
            );


            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(Duration.millis(800));
            pause.setOnFinished(e -> {
                fermerFenetre();
                if (onSuccess != null) onSuccess.run();
            });
            pause.play();

        } else {

            btnConfirmer.setText(" Créneau indisponible");
            btnConfirmer.setStyle(
                    "-fx-background-color: linear-gradient(to right, #C0392B, #922B21);" +
                            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;" +
                            "-fx-background-radius: 10; -fx-padding: 10 28;"
            );
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(Duration.millis(1500));
            pause.setOnFinished(e -> fermerFenetre());
            pause.play();
        }
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
        fermerFenetre();
    }


    private void fermerFenetre() {
        Stage stage = (Stage) btnConfirmer.getScene().getWindow();
        FadeTransition ft = new FadeTransition(Duration.millis(150), stage.getScene().getRoot());
        ft.setToValue(0);
        ft.setOnFinished(e -> stage.close());
        ft.play();
    }
}