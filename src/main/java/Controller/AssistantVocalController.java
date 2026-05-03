package Controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import models.Activite;

import java.util.List;
import java.util.function.Consumer;

public class AssistantVocalController {

    private Stage popupEcoute;
    private Consumer<String> onResultat;

    /**
     * Ouvre le popup "Écoute en cours..." et appelle onResultat avec le texte reconnu.
     */
    public void demarrerEcoute(Stage owner, Consumer<String> onResultat) {
        this.onResultat = onResultat;

        // ── Popup écoute ──────────────────────────────────────────────────────
        popupEcoute = new Stage();
        popupEcoute.initModality(Modality.APPLICATION_MODAL);
        popupEcoute.initOwner(owner);
        popupEcoute.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 50, 40, 50));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 5);");
        root.setPrefWidth(380);

        // Icône micro avec animation pulse
        StackPane microPane = new StackPane();
        Circle pulse = new Circle(40, Color.web("#e53e3e", 0.2));
        Circle microBg = new Circle(30, Color.web("#e53e3e"));
        Label microIcon = new Label("🎤");
        microIcon.setStyle("-fx-font-size: 26px;");
        microPane.getChildren().addAll(pulse, microBg, microIcon);

        // Animation pulse
        ScaleTransition pulseAnim = new ScaleTransition(Duration.millis(800), pulse);
        pulseAnim.setFromX(1.0); pulseAnim.setToX(1.6);
        pulseAnim.setFromY(1.0); pulseAnim.setToY(1.6);
        pulseAnim.setCycleCount(Animation.INDEFINITE);
        pulseAnim.setAutoReverse(true);
        pulseAnim.play();

        Label lblEcoute = new Label("Écoute en cours...");
        lblEcoute.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label lblConsigne = new Label("Dites le nom de l'activité que vous recherchez");
        lblConsigne.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        lblConsigne.setWrapText(true);
        lblConsigne.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        root.getChildren().addAll(microPane, lblEcoute, lblConsigne);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popupEcoute.setScene(scene);
        popupEcoute.show();

        // Centrer
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        popupEcoute.setX(screen.getWidth() / 2 - 190);
        popupEcoute.setY(screen.getHeight() / 2 - 150);

        // ── Reconnaissance vocale via WebView + Web Speech API ────────────────
        demarrerReconnaissanceVocale();
    }

    private void demarrerReconnaissanceVocale() {
        // WebView cachée pour utiliser l'API Web Speech
        WebView webView = new WebView();
        webView.setVisible(false);

        String html = "<!DOCTYPE html><html><body>" +
            "<script>" +
            "var recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();" +
            "recognition.lang = 'fr-FR';" +
            "recognition.interimResults = false;" +
            "recognition.maxAlternatives = 1;" +
            "recognition.onresult = function(event) {" +
            "  var texte = event.results[0][0].transcript;" +
            "  document.title = 'RESULT:' + texte;" +
            "};" +
            "recognition.onerror = function(event) {" +
            "  document.title = 'ERROR:' + event.error;" +
            "};" +
            "recognition.onend = function() {" +
            "  if (!document.title.startsWith('RESULT:') && !document.title.startsWith('ERROR:')) {" +
            "    document.title = 'ERROR:no-speech';" +
            "  }" +
            "};" +
            "recognition.start();" +
            "</script></body></html>";

        webView.getEngine().loadContent(html);

        // Écouter les changements de titre pour récupérer le résultat
        webView.getEngine().titleProperty().addListener((obs, ov, nv) -> {
            if (nv == null) return;
            if (nv.startsWith("RESULT:")) {
                String texte = nv.substring(7).trim();
                Platform.runLater(() -> {
                    fermerPopup();
                    if (onResultat != null) onResultat.accept(texte);
                });
            } else if (nv.startsWith("ERROR:")) {
                Platform.runLater(() -> {
                    fermerPopup();
                    // Fallback : simuler avec un message d'erreur
                    if (onResultat != null) onResultat.accept("");
                });
            }
        });

        // Timeout de 8 secondes
        PauseTransition timeout = new PauseTransition(Duration.seconds(8));
        timeout.setOnFinished(e -> Platform.runLater(() -> {
            fermerPopup();
            if (onResultat != null) onResultat.accept("");
        }));
        timeout.play();

        // Garder une référence pour éviter le GC
        popupEcoute.setUserData(webView);
    }

    private void fermerPopup() {
        if (popupEcoute != null && popupEcoute.isShowing()) {
            popupEcoute.close();
        }
    }

    /**
     * Fait clignoter une carte avec une bordure colorée.
     */
    public static void clignoterCarte(VBox card) {
        String styleOriginal = card.getStyle();
        String styleHighlight = styleOriginal +
                "-fx-border-color: #e53e3e; -fx-border-width: 3; -fx-border-radius: 15;";

        Timeline timeline = new Timeline();
        for (int i = 0; i < 6; i++) {
            final boolean highlight = (i % 2 == 0);
            timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(i * 350),
                e -> card.setStyle(highlight ? styleHighlight : styleOriginal)
            ));
        }
        // Remettre le style original à la fin
        timeline.getKeyFrames().add(new KeyFrame(
            Duration.millis(6 * 350),
            e -> card.setStyle(styleOriginal)
        ));

        // Scroll vers la carte
        card.setStyle(styleHighlight);
        timeline.play();
    }

    /**
     * Recherche une activité par nom (correspondance partielle).
     */
    public static Activite rechercherActivite(String texte, List<Activite> activites) {
        if (texte == null || texte.isEmpty()) return null;
        String lower = texte.toLowerCase().trim();
        // Correspondance exacte d'abord
        for (Activite a : activites) {
            if (a.getTitre().toLowerCase().equals(lower)) return a;
        }
        // Correspondance partielle
        for (Activite a : activites) {
            if (a.getTitre().toLowerCase().contains(lower)) return a;
        }
        // Correspondance par mots
        String[] mots = lower.split("\\s+");
        for (Activite a : activites) {
            String titre = a.getTitre().toLowerCase();
            for (String mot : mots) {
                if (mot.length() > 2 && titre.contains(mot)) return a;
            }
        }
        return null;
    }
}
