package utils;

import models.User;
import java.util.function.Consumer;

/**
 * Service Google OAuth2.
 * Credentials: client_id=824689937775-aheekj93ul2m1l4cd1vj7ap4dnpi4fgk.apps.googleusercontent.com
 */
public class GoogleAuthService {

    private static final String CLIENT_ID     = "824689937775-aheekj93ul2m1l4cd1vj7ap4dnpi4fgk.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI  = "http://localhost:8989/callback";

    /**
     * Lance le flux OAuth2 Google dans le navigateur,
     * attend le callback et retourne l'utilisateur Google.
     */
    public static void authenticate(Consumer<User> onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                // 1. Construire l'URL d'autorisation
                String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                    + "?client_id=" + CLIENT_ID
                    + "&redirect_uri=" + java.net.URLEncoder.encode(REDIRECT_URI, java.nio.charset.StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope=" + java.net.URLEncoder.encode("openid email profile", java.nio.charset.StandardCharsets.UTF_8)
                    + "&access_type=offline";

                // 2. Ouvrir le navigateur
                java.awt.Desktop.getDesktop().browse(new java.net.URI(authUrl));

                // 3. Démarrer un serveur HTTP local pour capturer le code
                com.sun.net.httpserver.HttpServer server =
                    com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8989), 0);
                String[] codeHolder = {null};
                Object lock = new Object();

                server.createContext("/callback", exchange -> {
                    String query = exchange.getRequestURI().getQuery();
                    String response = "<html><body><h2>Connexion réussie ! Vous pouvez fermer cet onglet.</h2></body></html>";
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    if (query != null && query.contains("code=")) {
                        for (String param : query.split("&")) {
                            if (param.startsWith("code=")) {
                                codeHolder[0] = param.substring(5);
                                break;
                            }
                        }
                    }
                    synchronized (lock) { lock.notifyAll(); }
                });
                server.start();

                // 4. Attendre le code (timeout 2 minutes)
                synchronized (lock) { lock.wait(120000); }
                server.stop(0);

                if (codeHolder[0] == null) {
                    javafx.application.Platform.runLater(() -> onError.accept("Timeout ou annulation."));
                    return;
                }

                // 5. Échanger le code contre un token
                String tokenJson = exchangeCodeForToken(codeHolder[0]);
                String accessToken = extractField(tokenJson, "access_token");

                // 6. Récupérer les infos utilisateur
                String userInfoJson = getUserInfo(accessToken);
                String email  = extractField(userInfoJson, "email");
                String prenom = extractField(userInfoJson, "given_name");
                String nom    = extractField(userInfoJson, "family_name");

                User googleUser = new User();
                googleUser.setEmail(email);
                googleUser.setPrenom(prenom != null ? prenom : "");
                googleUser.setNom(nom != null ? nom : "");
                googleUser.setMotDePasse(PasswordUtils.hashPassword(java.util.UUID.randomUUID().toString()));
                googleUser.setRole("etudiant");
                googleUser.setRoles("[\"ROLE_ETUDIANT\"]");

                javafx.application.Platform.runLater(() -> onSuccess.accept(googleUser));

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> onError.accept(e.getMessage()));
            }
        }).start();
    }

    private static String exchangeCodeForToken(String code) throws Exception {
        String params = "code=" + java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8)
            + "&client_id=" + CLIENT_ID
            + "&client_secret=" + CLIENT_SECRET
            + "&redirect_uri=" + java.net.URLEncoder.encode(REDIRECT_URI, java.nio.charset.StandardCharsets.UTF_8)
            + "&grant_type=authorization_code";
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
            new java.net.URL("https://oauth2.googleapis.com/token").openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.getOutputStream().write(params.getBytes());
        return new String(conn.getInputStream().readAllBytes());
    }

    private static String getUserInfo(String accessToken) throws Exception {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
            new java.net.URL("https://www.googleapis.com/oauth2/v3/userinfo").openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        return new String(conn.getInputStream().readAllBytes());
    }

    private static String extractField(String json, String field) {
        if (json == null) return null;
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int start = json.indexOf("\"", idx + key.length() + 1);
        if (start < 0) return null;
        int end = json.indexOf("\"", start + 1);
        return end > start ? json.substring(start + 1, end) : null;
    }
}
