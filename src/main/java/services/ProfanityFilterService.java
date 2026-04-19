package services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Service de détection des mots inappropriés via l'API Ninjas
 */
public class ProfanityFilterService {

    private static final String API_URL = "https://api.api-ninjas.com/v1/profanityfilter";
    private static final int MAX_TEXT_LENGTH = 1000;

    private static final List<String> FRENCH_BAD_WORDS = Arrays.asList(
            "merde", "connard", "salope", "putain", "enculé", "connasse",
            "batard", "bâtard", "foutre", "bordel", "gogol", "debile",
            "débile", "couille", "bite", "chatte", "nique", "fdp", "tg",
            "gueule", "pétasse","test"
    );

    private String apiKey;
    private boolean apiConfigured = false;

    public ProfanityFilterService() {
        loadFromConfigFile();
    }

    private void loadFromConfigFile() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config/config.properties")) {

            if (input != null) {
                Properties props = new Properties();
                props.load(input);

                String key = props.getProperty("api.ninjas.key");

                if (key != null && !key.trim().isEmpty()
                        && !key.contains("VOTRE")) {

                    this.apiKey = key.trim();
                    this.apiConfigured = true;
                    System.out.println("✅ API prête !");
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur config API: " + e.getMessage());
        }

        if (!apiConfigured) {
            System.err.println("⚠️ API non configurée → filtre local seulement");
        }
    }

    public boolean isApiConfigured() {
        return apiConfigured;
    }

    public boolean containsProfanity(String text) {
        if (text == null || text.trim().isEmpty()) return false;

        String clean = text.trim();

        // 1. Filtre FR
        if (containsFrenchProfanity(clean)) return true;

        // 2. API
        if (!apiConfigured) return false;

        for (String chunk : splitIntoChunks(clean)) {
            ProfanityResponse res = callApi(chunk);

            if (res != null && res.isSuccess() && res.hasProfanity()) {
                return true;
            }
        }

        return false;
    }


    public String getCensoredText(String text) {
        if (text == null || text.trim().isEmpty()) return "";

        String result = censorFrenchProfanity(text);

        if (!apiConfigured) return result;

        StringBuilder finalText = new StringBuilder();

        for (String chunk : splitIntoChunks(result)) {
            ProfanityResponse res = callApi(chunk);

            if (res != null && res.isSuccess() && res.getCensored() != null) {
                finalText.append(res.getCensored());
            } else {
                finalText.append(chunk);
            }
        }

        return finalText.toString();
    }

    public void validateText(String text, String field) {
        if (containsProfanity(text)) {
            String censored = getCensoredText(text);
            throw new IllegalArgumentException(
                    "❌ Le " + field + " contient des mots inappropriés.\n"
            );
        }
    }

    private ProfanityResponse callApi(String text) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            URL url = new URL(API_URL + "?text=" + encoded);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Api-Key", apiKey);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() != 200) {
                return new ProfanityResponse(false, null, false);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String json = response.toString();

            boolean hasProfanity = json.contains("\"has_profanity\":true")
                    || json.contains("\"has_profanity\": true");

            String censored = null;

            int i = json.indexOf("\"censored\":\"");
            if (i != -1) {
                int start = i + 12;
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    censored = json.substring(start, end)
                            .replace("\\\"", "\"")
                            .replace("\\n", "\n");
                }
            }

            return new ProfanityResponse(true, censored, hasProfanity);

        } catch (Exception e) {
            System.err.println("Erreur API: " + e.getMessage());
            return null;
        }
    }


    private List<String> splitIntoChunks(String text) {
        List<String> list = new java.util.ArrayList<>();
        for (int i = 0; i < text.length(); i += MAX_TEXT_LENGTH) {
            list.add(text.substring(i, Math.min(text.length(), i + MAX_TEXT_LENGTH)));
        }
        return list;
    }

    private boolean containsFrenchProfanity(String text) {
        String lower = text.toLowerCase();
        for (String word : FRENCH_BAD_WORDS) {
            if (Pattern.compile("\\b" + Pattern.quote(word) + "\\b")
                    .matcher(lower).find()) {
                return true;
            }
        }
        return false;
    }

    private String censorFrenchProfanity(String text) {
        String result = text;
        for (String word : FRENCH_BAD_WORDS) {
            result = result.replaceAll("(?i)\\b" + Pattern.quote(word) + "\\b",
                    "*".repeat(word.length()));
        }
        return result;
    }

    private static class ProfanityResponse {
        private final boolean success;
        private final String censored;
        private final boolean hasProfanity;

        public ProfanityResponse(boolean success, String censored, boolean hasProfanity) {
            this.success = success;
            this.censored = censored;
            this.hasProfanity = hasProfanity;
        }

        public boolean isSuccess() { return success; }
        public String getCensored() { return censored; }
        public boolean hasProfanity() { return hasProfanity; }
    }
}