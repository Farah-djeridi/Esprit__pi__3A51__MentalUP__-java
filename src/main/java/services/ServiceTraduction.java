package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

public class ServiceTraduction {

    private static final String API_URL = "https://api.mymemory.translated.net/get";

    public ServiceTraduction() {}

    /**
     * Traduire un texte
     * @param texte texte à traduire
     * @param targetLang langue cible (ex: fr)
     * @param sourceLang langue source (auto, en, fr, ar...)
     */
    public String traduire(String texte, String targetLang, String sourceLang) {

        try {
            // Vérifier texte vide
            if (texte == null || texte.trim().isEmpty()) {
                return texte;
            }

            // Détection automatique
            if (sourceLang.equals("auto")) {
                sourceLang = detectLanguage(texte);
            }

            String langPair = sourceLang + "|" + targetLang;

            String query = String.format("q=%s&langpair=%s",
                    URLEncoder.encode(texte, "UTF-8"),
                    URLEncoder.encode(langPair, "UTF-8"));

            URL url = new URL(API_URL + "?" + query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            // Convertir JSON
            JSONObject json = new JSONObject(response.toString());

            return json
                    .getJSONObject("responseData")
                    .getString("translatedText");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Détection simple de langue
     */
    private String detectLanguage(String texte) {

        // Arabe
        if (texte.matches(".*\\p{InArabic}.*")) {
            return "ar";
        }

        // Français
        if (texte.matches(".*[éèêàùçôîï].*")) {
            return "fr";
        }

        // Par défaut
        return "en";
    }
}