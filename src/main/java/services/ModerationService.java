package services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ModerationService {

    private static final String API_URL = "https://api.openai.com/v1/moderations";
    private String apiKey;
    private final OkHttpClient client;

    public ModerationService() {
        this.client = new OkHttpClient();
        loadApiKey();
    }

    private void loadApiKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("Désolé, impossible de trouver config.properties");
                return;
            }
            prop.load(input);
            this.apiKey = prop.getProperty("OPENAI_API_KEY");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public ModerationResult analyzeContent(String title, String description) {
        if (apiKey == null || apiKey.isEmpty() || "YOUR_OPENAI_API_KEY_HERE".equals(apiKey)) {
            // Pour les tests sans clé API, on renvoie toujours SAFE
            return new ModerationResult("SAFE", 0.0, "API Key non configurée.");
        }

        String contentToAnalyze = title + "\n" + description;
        
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("input", contentToAnalyze);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Erreur API OpenAI: " + response.code());
                return new ModerationResult("SAFE", 0.0, "Erreur API");
            }

            String responseData = response.body().string();
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray results = jsonObject.getJSONArray("results");
            JSONObject firstResult = results.getJSONObject(0);

            boolean flagged = firstResult.getBoolean("flagged");
            JSONObject categoryScores = firstResult.getJSONObject("category_scores");
            
            double maxScore = 0.0;
            for (String key : categoryScores.keySet()) {
                double score = categoryScores.getDouble(key);
                if (score > maxScore) {
                    maxScore = score;
                }
            }

            String status;
            String suggestion = "";

            if (flagged || maxScore > 0.5) {
                status = "TOXIC";
                suggestion = "Cette ressource contient un contenu potentiellement toxique.";
            } else if (maxScore > 0.1) {
                status = "WARNING";
                suggestion = "Essayez une reformulation plus positive pour cette ressource.";
            } else {
                status = "SAFE";
                suggestion = "Contenu approprié.";
            }

            return new ModerationResult(status, maxScore, suggestion);

        } catch (IOException e) {
            e.printStackTrace();
            return new ModerationResult("SAFE", 0.0, "Erreur réseau");
        }
    }

    public static class ModerationResult {
        private String status; // SAFE, WARNING, TOXIC
        private double score;
        private String suggestion;

        public ModerationResult(String status, double score, String suggestion) {
            this.status = status;
            this.score = score;
            this.suggestion = suggestion;
        }

        public String getStatus() { return status; }
        public double getScore() { return score; }
        public String getSuggestion() { return suggestion; }
    }
}
