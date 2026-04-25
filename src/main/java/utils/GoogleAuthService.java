package utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * Service d'authentification Google OAuth2 (flux Authorization Code + PKCE).
 * Les cles sont lues depuis config.properties.
 */
public class GoogleAuthService {

    private static final String CLIENT_ID     = AppConfig.get("google.client.id");
    private static final String CLIENT_SECRET = AppConfig.get("google.client.secret");
    private static final String REDIRECT_URI  = "http://localhost:8989/callback";
    private static final String SCOPE         = "openid email profile";
    private static final String AUTH_ENDPOINT  = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL   = "https://www.googleapis.com/oauth2/v3/userinfo";

    public static void authenticate(Consumer<User> onSuccess, Consumer<String> onError) {
        if (CLIENT_ID.isEmpty()) {
            onError.accept("google.client.id manquant dans config.properties");
            return;
        }
        try {
            String codeVerifier  = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String state         = generateState();

            String authUrl = AUTH_ENDPOINT
                    + "?client_id="           + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                    + "&redirect_uri="        + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope="               + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
                    + "&state="               + state
                    + "&code_challenge="      + codeChallenge
                    + "&code_challenge_method=S256"
                    + "&access_type=offline"
                    + "&prompt=select_account";

            Platform.runLater(() -> openGoogleWindow(authUrl, state, codeVerifier, onSuccess, onError));
        } catch (Exception e) {
            onError.accept("Erreur initialisation OAuth : " + e.getMessage());
        }
    }

    private static void openGoogleWindow(String authUrl, String state, String codeVerifier,
                                          Consumer<User> onSuccess, Consumer<String> onError) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Connexion avec Google");
        stage.setWidth(500); stage.setHeight(620);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        Label lbl = new Label("Chargement...");
        lbl.setStyle("-fx-text-fill: #7A9CB8; -fx-font-size: 11px; -fx-padding: 4 8;");

        VBox root = new VBox(lbl, webView);
        VBox.setVgrow(webView, javafx.scene.layout.Priority.ALWAYS);
        root.setPadding(new Insets(0));
        stage.setScene(new Scene(root));

        engine.locationProperty().addListener((obs, old, newUrl) -> {
            if (newUrl != null && newUrl.startsWith(REDIRECT_URI)) {
                stage.close();
                String code  = extractParam(newUrl, "code");
                String stRet = extractParam(newUrl, "state");
                String error = extractParam(newUrl, "error");
                if (error != null) { onError.accept("Google a refuse : " + error); return; }
                if (code == null || !state.equals(stRet)) { onError.accept("Reponse Google invalide."); return; }
                new Thread(() -> exchangeCodeForUser(code, codeVerifier, onSuccess, onError)).start();
            }
        });
        engine.load(authUrl);
        stage.show();
    }

    private static void exchangeCodeForUser(String code, String codeVerifier,
                                             Consumer<User> onSuccess, Consumer<String> onError) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String tokenBody = "grant_type=authorization_code"
                    + "&code="           + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&redirect_uri="   + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                    + "&client_id="      + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                    + "&client_secret="  + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8)
                    + "&code_verifier="  + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8);

            HttpRequest tokenReq = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_ENDPOINT))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenBody)).build();

            HttpResponse<String> tokenResp = client.send(tokenReq, HttpResponse.BodyHandlers.ofString());
            if (tokenResp.statusCode() != 200) {
                Platform.runLater(() -> onError.accept("Erreur token (" + tokenResp.statusCode() + ")"));
                return;
            }
            String accessToken = parseJsonField(tokenResp.body(), "access_token");
            if (accessToken == null) { Platform.runLater(() -> onError.accept("Token introuvable.")); return; }

            HttpRequest userReq = HttpRequest.newBuilder()
                    .uri(URI.create(USERINFO_URL))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET().build();

            HttpResponse<String> userResp = client.send(userReq, HttpResponse.BodyHandlers.ofString());
            if (userResp.statusCode() != 200) {
                Platform.runLater(() -> onError.accept("Erreur profil (" + userResp.statusCode() + ")"));
                return;
            }

            String body    = userResp.body();
            String email   = parseJsonField(body, "email");
            String prenom  = parseJsonField(body, "given_name");
            String nom     = parseJsonField(body, "family_name");
            String picture = parseJsonField(body, "picture");

            if (email == null) { Platform.runLater(() -> onError.accept("Email Google introuvable.")); return; }

            User u = new User();
            u.setEmail(email);
            u.setPrenom(prenom != null ? prenom : "");
            u.setNom(nom != null ? nom : "");
            u.setMotDePasse(PasswordUtils.hashPassword(generateState()));
            u.setRole("etudiant");
            u.setRoles("[\"ROLE_ETUDIANT\"]");
            if (picture != null) u.setAvatarFilename(picture);

            Platform.runLater(() -> onSuccess.accept(u));

        } catch (Exception e) {
            Platform.runLater(() -> onError.accept("Erreur OAuth : " + e.getMessage()));
        }
    }

    private static String generateCodeVerifier() {
        byte[] b = new byte[32]; new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static String generateCodeChallenge(String v) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(d);
    }

    private static String generateState() {
        byte[] b = new byte[16]; new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static String extractParam(String url, String param) {
        try {
            String q = url.contains("?") ? url.split("\\?", 2)[1] : url;
            for (String p : q.split("&")) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2 && kv[0].equals(param))
                    return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String parseJsonField(String json, String field) {
        try {
            String key = "\"" + field + "\"";
            int idx = json.indexOf(key); if (idx < 0) return null;
            int col = json.indexOf(':', idx + key.length()); if (col < 0) return null;
            int s   = json.indexOf('"', col + 1); if (s < 0) return null;
            int e   = json.indexOf('"', s + 1); if (e < 0) return null;
            return json.substring(s + 1, e);
        } catch (Exception e) { return null; }
    }
}
