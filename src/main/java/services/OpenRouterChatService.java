package services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class OpenRouterChatService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "openrouter/auto";

    private String getApiKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("config.properties introuvable.");
                return null;
            }

            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("OPENROUTER_API_KEY");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String envoyerMessage(String userMessage) {
        HttpURLConnection conn = null;

        try {
            String apiKey = getApiKey();
            System.out.println("API KEY = " + apiKey);

            if (apiKey == null || apiKey.isBlank()) {
                return fallbackConseil(userMessage);
            }

            if (userMessage == null || userMessage.isBlank()) {
                return "Je suis là pour t’aider. Tu peux m’écrire ce que tu ressens.";
            }

            // Détection locale rapide avant appel API pour certains cas simples
            String quickLocal = reponseLocalePrioritaire(userMessage);
            if (quickLocal != null) {
                return quickLocal;
            }

            URL url = new URL(API_URL);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("HTTP-Referer", "http://localhost");
            conn.setRequestProperty("X-Title", "MentalUp");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            String systemPrompt =
                    "Tu es un coach bienveillant dans une application de suivi mental pour étudiants. " +
                            "Réponds toujours en français correct, simple, naturel et chaleureux. " +
                            "Ne parle jamais en anglais. " +
                            "Évite les phrases maladroites, les répétitions et les longs paragraphes. " +
                            "Ta réponse doit contenir 3 à 5 lignes maximum. " +
                            "Donne toujours une réponse utile, concrète, humaine et courte. " +
                            "Si l'utilisateur demande des conseils, donne exactement 3 conseils simples. " +
                            "Si l'utilisateur dit merci, réponds brièvement avec chaleur. " +
                            "Si l'utilisateur exprime une émotion négative, commence par une phrase empathique. " +
                            "Si l'utilisateur exprime quelque chose de positif, félicite-le. " +
                            "Tu ne dois pas donner de diagnostic médical. " +
                            "Tu peux proposer un petit exercice de respiration ou une petite action simple. " +
                            "Le ton doit être rassurant et naturel, comme un vrai coach calme.";

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", systemPrompt));
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", userMessage));

            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("messages", messages);
            body.put("temperature", 0.6);
            body.put("max_tokens", 180);

            String requestBody = body.toString();
            System.out.println("MODELE ENVOYE = " + MODEL);
            System.out.println("OPENROUTER REQUEST = " + requestBody);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int status = conn.getResponseCode();
            System.out.println("OPENROUTER STATUS = " + status);

            InputStream responseStream =
                    (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            if (responseStream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }

            String responseText = response.toString().trim();
            System.out.println("OPENROUTER RESPONSE = " + responseText);

            if (status < 200 || status >= 300) {
                return fallbackConseil(userMessage);
            }

            if (responseText.isEmpty()) {
                return fallbackConseil(userMessage);
            }

            JSONObject json = new JSONObject(responseText);
            JSONArray choices = json.optJSONArray("choices");

            if (choices == null || choices.isEmpty()) {
                return fallbackConseil(userMessage);
            }

            JSONObject firstChoice = choices.optJSONObject(0);
            if (firstChoice == null) {
                return fallbackConseil(userMessage);
            }

            JSONObject message = firstChoice.optJSONObject("message");
            if (message == null) {
                return fallbackConseil(userMessage);
            }

            String content = message.optString("content", "").trim();

            if (content.isEmpty()) {
                return fallbackConseil(userMessage);
            }

            content = nettoyerReponse(content);

            if (seemsBad(content)) {
                return fallbackConseil(userMessage);
            }

            return content;

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackConseil(userMessage);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String nettoyerReponse(String content) {
        if (content == null) return "";

        content = content.replaceAll("\\*+", "");
        content = content.replaceAll("\\n{3,}", "\n\n").trim();

        // Supprimer quelques formulations trop robotiques
        content = content.replace("Je suis désolé de vous entendre parler ainsi.", "Je suis désolé que tu traverses cela.");
        content = content.replace("vous êtes important et valeureux", "tu as de la valeur");
        content = content.replace("ne vous préoccupez pas de ces émotionnels", "tes émotions sont normales");
        content = content.replace("Pensez à quel chose vous êtes gratte", "Pense à une chose pour laquelle tu es reconnaissant(e)");

        return content.trim();
    }

    private boolean seemsBad(String text) {
        if (text == null || text.isBlank()) {
            return true;
        }

        String t = text.toLowerCase().trim();

        return t.length() < 10
                || t.contains("ces émotionnels")
                || t.contains("quel chose vous êtes gratte")
                || t.contains("vous êtes capable de vous rétablir, je suis sûr de cela")
                || t.endsWith(":")
                || t.endsWith("1)")
                || t.endsWith("2)")
                || t.endsWith("3)");
    }

    private String reponseLocalePrioritaire(String userMessage) {
        String msg = normalize(userMessage);

        if (msg.isBlank()) {
            return "Je suis là pour t’aider. Tu peux m’écrire ce que tu ressens.";
        }

        if (containsAny(msg, "merci", "mercii", "mercie", "thanks")) {
            return "Avec plaisir 🌿\nPrends soin de toi, et écris-moi quand tu veux.";
        }

        if (containsAny(msg, "autre conseil", "autres conseils", "dautre conseil", "donner moi dautre", "encore des conseils")) {
            return "Bien sûr 🌿\n1. Bois un verre d’eau et fais une petite pause.\n2. Note ce que tu ressens en une phrase.\n3. Fais une seule petite tâche sans te presser.";
        }

        if (containsAny(msg, "je vais bien", "je me sens bien", "je suis bien", "je suis heureuse", "je suis content", "je suis motivé", "je suis calme")) {
            return "C’est une très bonne chose 🌿\nEssaie de garder ce bon moment en tête.\nTu peux aussi noter ce qui t’a aidé à te sentir ainsi aujourd’hui.";
        }

        return null;
    }

    private String fallbackConseil(String userMessage) {
        String msg = normalize(userMessage);

        if (containsAny(msg, "mauvais humeur", "mauvaise humeur", "je suis triste", "triste", "mal")) {
            return "Je suis désolé(e) que tu te sentes comme ça 🌿\n1. Fais une pause de 2 minutes.\n2. Respire lentement 5 fois.\n3. Essaie de parler à quelqu’un de confiance ou d’écrire ce que tu ressens.";
        }

        if (containsAny(msg, "stress", "stresse", "stressé", "stressée", "angoisse")) {
            return "Je comprends que tu te sentes stressé(e) 🌿\n1. Concentre-toi sur une seule petite tâche.\n2. Éloigne-toi de l’écran pendant 2 minutes.\n3. Inspire 4 secondes et expire 6 secondes, 5 fois.";
        }

        if (containsAny(msg, "maladie", "malade", "fatigue", "fatigué", "fatiguée")) {
            return "Je suis désolé(e) que tu passes par cette période 🌿\n1. Priorise le repos aujourd’hui.\n2. Bois de l’eau et mange léger si possible.\n3. Fais seulement le minimum nécessaire et sois doux/douce avec toi-même.";
        }

        if (containsAny(msg, "conseil", "aider", "aide-moi", "aide moi")) {
            return "Bien sûr 🌿\n1. Commence par une petite action simple.\n2. Respire lentement pendant une minute.\n3. Évite de tout porter seul(e) et parle si tu en ressens le besoin.";
        }

        if (containsAny(msg, "merci", "mercii", "mercie")) {
            return "Avec plaisir 🌿\nJe suis là pour toi.";
        }

        if (containsAny(msg, "heureux", "bien", "content", "motivé", "positive", "positif")) {
            return "C’est encourageant de lire ça 🌿\nGarde cette énergie et essaie de noter ce qui t’a fait du bien aujourd’hui.";
        }

        return "Je suis là pour t’aider 🌿\nPrends une minute pour respirer lentement.\nEnsuite, dis-moi ce que tu ressens le plus en ce moment : stress, tristesse, fatigue ou autre.";
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) return false;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("â", "a")
                .replace("ù", "u")
                .replace("û", "u")
                .replace("ô", "o")
                .replace("î", "i")
                .replace("ï", "i")
                .trim();
    }
}