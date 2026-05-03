package Controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import models.Activite;
import services.ServiceActivite;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AssistantController {

    private VBox messagesBox;
    private ScrollPane scrollPane;
    private TextField inputField;
    private Stage stage;
    private ServiceActivite serviceActivite;

    public void ouvrirAssistant(Stage owner) {
        serviceActivite = new ServiceActivite();
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Assistant Mental Up");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        root.setPrefWidth(340);
        root.setMaxHeight(520);

        // ── Header ────────────────────────────────────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setStyle("-fx-background-color: #5a8a9f; -fx-background-radius: 16 16 0 0;");

        Label icon  = new Label("🤖");
        icon.setStyle("-fx-font-size: 18px;");
        Label title = new Label("Assistant Mental Up");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 4;");
        btnClose.setOnAction(e -> stage.close());

        header.getChildren().addAll(icon, title, spacer, btnClose);

        // ── Zone messages ─────────────────────────────────────────────────────
        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(14));
        messagesBox.setFillWidth(true);

        scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(380);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // ── Footer saisie ─────────────────────────────────────────────────────
        HBox footer = new HBox(8);
        footer.setPadding(new Insets(10, 12, 12, 12));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 0 16 16; " +
                        "-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Button btnAnnuler = new Button("↩ Annuler");
        btnAnnuler.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; " +
                            "-fx-font-size: 12px; -fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> stage.close());

        inputField = new TextField();
        inputField.setPromptText("Écrivez votre message...");
        inputField.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                            "-fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20; " +
                            "-fx-padding: 8 14; -fx-font-size: 13px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> envoyerMessage());

        Button btnEnvoyer = new Button("➤");
        btnEnvoyer.setStyle("-fx-background-color: #5a8a9f; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-pref-width: 36; -fx-pref-height: 36; " +
                            "-fx-background-radius: 50%; -fx-cursor: hand;");
        btnEnvoyer.setOnAction(e -> envoyerMessage());

        footer.getChildren().addAll(btnAnnuler, inputField, btnEnvoyer);

        root.getChildren().addAll(header, scrollPane, footer);

        // Message de bienvenue
        afficherMessageBot(
            "Bonjour! Je suis votre Assistant Mental Up 🤖\n\n" +
            "Comment vous sentez-vous aujourd'hui?\n\n" +
            "Dites-moi votre état et je vous recommande des activités adaptées."
        );

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        // Centrer sur l'écran
        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(screen.getMaxX() - 370);
        stage.setY(screen.getMaxY() - 560);
    }

    private void envoyerMessage() {
        String texte = inputField.getText().trim();
        if (texte.isEmpty()) return;
        inputField.clear();

        // Message utilisateur
        afficherMessageUser(texte);

        // Réponse de l'assistant après un délai
        Platform.runLater(() -> {
            try { Thread.sleep(500); } catch (Exception ignored) {}
            String reponse = genererReponse(texte.toLowerCase());
            afficherMessageBot(reponse);
        });
    }

    private String genererReponse(String msg) {
        // Trier les activités
        if (msg.contains("trier") || msg.contains("liste") || msg.contains("activit")) {
            try {
                List<Activite> activites = serviceActivite.getAllActivites();
                if (activites.isEmpty()) return "Aucune activité disponible pour le moment.";
                StringBuilder sb = new StringBuilder("📋 Activités disponibles:\n\n");
                activites.stream().limit(5).forEach(a ->
                    sb.append("• ").append(a.getTitre()).append("\n")
                );
                return sb.toString();
            } catch (SQLException e) {
                return "Erreur lors du chargement des activités.";
            }
        }

        // États émotionnels
        if (msg.contains("triste") || msg.contains("déprimé") || msg.contains("deprime") || msg.contains("seul")) {
            return recommanderActivites("social", "sport", "créatif",
                "😢 Je comprends que vous vous sentez triste.\n\n" +
                "Je vous recommande des activités pour vous remonter le moral:");
        }
        if (msg.contains("stress") || msg.contains("anxieux") || msg.contains("anxieus") || msg.contains("angoiss")) {
            return recommanderActivites("nature", "culturel", "bien-être",
                "😰 Le stress peut être difficile à gérer.\n\n" +
                "Voici des activités apaisantes pour vous:");
        }
        if (msg.contains("fatigu") || msg.contains("épuisé") || msg.contains("epuise")) {
            return recommanderActivites("culturel", "nature",
                "😴 Vous semblez fatigué.\n\n" +
                "Des activités douces pour vous ressourcer:");
        }
        if (msg.contains("joyeux") || msg.contains("heureux") || msg.contains("bien") || msg.contains("super")) {
            return recommanderActivites("sport", "social", "créatif",
                "😊 Super! Profitez de cette énergie!\n\n" +
                "Des activités pour maintenir cette bonne humeur:");
        }
        if (msg.contains("colère") || msg.contains("colere") || msg.contains("énervé") || msg.contains("enerve")) {
            return recommanderActivites("sport", "nature",
                "😡 Canalisons cette énergie positivement!\n\n" +
                "Des activités pour vous défouler:");
        }
        if (msg.contains("bonjour") || msg.contains("salut") || msg.contains("hello")) {
            return "👋 Bonjour! Comment vous sentez-vous aujourd'hui?\n\n" +
                   "Dites-moi votre état émotionnel et je vous recommanderai des activités adaptées.";
        }
        if (msg.contains("merci")) {
            return "😊 Avec plaisir! N'hésitez pas si vous avez d'autres questions.\n\nPrenez soin de vous! 💙";
        }
        if (msg.contains("aide") || msg.contains("help")) {
            return "🤖 Je peux vous aider à:\n\n" +
                   "• Taper \"trier\" pour voir les activités\n" +
                   "• Décrire votre état (triste, stressé, fatigué, joyeux...)\n" +
                   "• Je vous recommanderai des activités adaptées!";
        }

        return "🤔 Je n'ai pas bien compris.\n\n" +
               "Essayez de me dire comment vous vous sentez:\n" +
               "\"Je suis stressé\", \"Je suis triste\", \"Je suis joyeux\"...\n\n" +
               "Ou tapez \"trier\" pour voir les activités.";
    }

    private String recommanderActivites(String... types) {
        String intro = types[types.length - 1];
        String[] typesFiltres = java.util.Arrays.copyOf(types, types.length - 1);

        StringBuilder sb = new StringBuilder(intro + "\n\n");
        try {
            List<Activite> activites = serviceActivite.getAllActivites();
            List<Activite> filtrees = activites.stream()
                .filter(a -> {
                    String t = a.getType() != null ? a.getType().toLowerCase() : "";
                    for (String type : typesFiltres) {
                        if (t.contains(type.toLowerCase())) return true;
                    }
                    return false;
                })
                .limit(4)
                .collect(Collectors.toList());

            if (filtrees.isEmpty()) {
                activites.stream().limit(3).forEach(a ->
                    sb.append("• ").append(a.getTitre()).append("\n"));
            } else {
                filtrees.forEach(a ->
                    sb.append("• ").append(a.getTitre()).append("\n"));
            }
        } catch (SQLException e) {
            sb.append("Erreur lors du chargement.");
        }
        sb.append("\n💙 Prenez soin de vous!");
        return sb.toString();
    }

    private void afficherMessageBot(String texte) {
        Platform.runLater(() -> {
            HBox row = new HBox(8);
            row.setAlignment(Pos.TOP_LEFT);

            // Avatar bot
            Circle avatar = new Circle(16, Color.web("#5a8a9f"));
            Label avatarIcon = new Label("🤖");
            avatarIcon.setStyle("-fx-font-size: 14px;");
            StackPane avatarPane = new StackPane(avatar, avatarIcon);

            // Bulle
            Label msg = new Label(texte);
            msg.setWrapText(true);
            msg.setMaxWidth(230);
            msg.setStyle("-fx-background-color: #f0f7ff; -fx-text-fill: #2d3748; " +
                         "-fx-font-size: 12.5px; -fx-padding: 10 14; " +
                         "-fx-background-radius: 0 14 14 14; -fx-line-spacing: 2;");

            row.getChildren().addAll(avatarPane, msg);

            FadeTransition ft = new FadeTransition(Duration.millis(300), row);
            ft.setFromValue(0); ft.setToValue(1); ft.play();

            messagesBox.getChildren().add(row);
            scrollToBottom();
        });
    }

    private void afficherMessageUser(String texte) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);

        Label msg = new Label(texte);
        msg.setWrapText(true);
        msg.setMaxWidth(220);
        msg.setStyle("-fx-background-color: #5a8a9f; -fx-text-fill: white; " +
                     "-fx-font-size: 12.5px; -fx-padding: 10 14; " +
                     "-fx-background-radius: 14 0 14 14;");

        row.getChildren().add(msg);

        FadeTransition ft = new FadeTransition(Duration.millis(200), row);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        messagesBox.getChildren().add(row);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}
