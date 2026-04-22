package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class ToxicityAnalysisService {

    private static final String API_URL =
            "https://router.huggingface.co/hf-inference/models/unitary/toxic-bert";

    private String apiKey;
    private ServiceTraduction traductionService;
    private boolean apiConfigured = false;

    public ToxicityAnalysisService() {
        traductionService = new ServiceTraduction();
        loadApiKey();
    }

    private void loadApiKey() {
        try {
            Properties props = new Properties();
            InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("config/config.properties");

            if (input != null) {
                props.load(input);
                String key = props.getProperty("HUGGING_FACE_TOKEN");

                if (key != null && !key.isEmpty()) {
                    apiKey = "Bearer " + key;
                    this.apiConfigured = true;
                    System.out.println("✅ API configurée avec succès");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur config API: " + e.getMessage());
        }
    }

    public double analyze(String text) {
        System.out.println("=== DÉBUT ANALYSE TOXICITÉ ===");
        System.out.println("Texte original: " + text);

        try {
            if (text == null || text.trim().isEmpty() || apiKey == null) {
                return 0.0;
            }

            // 🔥 DÉTECTER LA LANGUE ET TRADUIRE UNIQUEMENT SI NÉCESSAIRE
            String detectedLang = detectLanguage(text);
            System.out.println("Langue détectée: " + detectedLang);

            String textToAnalyze = text;

            // Traduire seulement si ce n'est pas déjà de l'anglais
            if (!"en".equals(detectedLang)) {
                System.out.println("🔄 Traduction nécessaire (" + detectedLang + " → en)");
                String translated = traductionService.traduire(text, "en", detectedLang);

                // Vérifier si la traduction est valide
                if (translated != null &&
                        !translated.equals("PLEASE SELECT TWO DISTINCT LANGUAGES") &&
                        !translated.contains("SELECT") &&
                        !translated.isEmpty()) {
                    textToAnalyze = translated;
                    System.out.println("✅ Traduction réussie: " + textToAnalyze);
                } else {
                    System.out.println("⚠️ Traduction invalide, utilisation du texte original");
                    textToAnalyze = text;
                }
            } else {
                System.out.println("✅ Texte déjà en anglais, pas de traduction nécessaire");
                textToAnalyze = text;
            }

            System.out.println("Texte final analysé: " + textToAnalyze);

            // Appel à l'API Hugging Face
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonInput = "{\"inputs\": \"" + escapeJson(textToAnalyze) + "\"}";
            System.out.println("Requête JSON: " + jsonInput);

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Code réponse HTTP: " + responseCode);

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseStr = response.toString();
            System.out.println("Réponse API: " + responseStr);

            if (responseCode >= 400) {
                System.err.println("❌ Erreur API: " + responseStr);
                return 0.0;
            }

            JSONArray array = new JSONArray(responseStr);
            JSONArray labels = array.getJSONArray(0);

            System.out.println("Labels reçus: " + labels.length());

            double maxScore = 0.0;

            for (int i = 0; i < labels.length(); i++) {
                JSONObject obj = labels.getJSONObject(i);
                String label = obj.getString("label").toLowerCase();
                double score = obj.getDouble("score");

                System.out.println("  - " + label + ": " + score);

                if (label.equals("toxic") ||
                        label.equals("severe_toxic") ||
                        label.equals("insult") ||
                        label.equals("threat") ||
                        label.equals("identity_hate")) {

                    maxScore = Math.max(maxScore, score);
                    System.out.println("    → Catégorie toxique, nouveau max: " + maxScore);
                }
            }

            System.out.println("=== SCORE FINAL: " + maxScore + " ===");
            return maxScore;

        } catch (Exception e) {
            System.err.println("❌ Exception lors de l'analyse: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    // 🔥 VOTRE FONCTION DE DÉTECTION DE LANGUE
    private String detectLanguage(String texte) {
        if (texte == null || texte.isEmpty()) {
            return "en";
        }

        // Arabe
        if (texte.matches(".*\\p{InArabic}.*")) {
            return "ar";
        }

        // Français (caractères accentués)
        if (texte.matches(".*[éèêàùçôîï].*")) {
            return "fr";
        }

        // Par défaut
        return "en";
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    public boolean isToxic(String text) {
        return analyze(text) >= 0.7;
    }

    public boolean isConfigured() {
        return apiKey != null;
    }
}