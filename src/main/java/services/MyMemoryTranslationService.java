package services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MyMemoryTranslationService {

    private static final String BASE_URL = "https://api.mymemory.translated.net/get";

    public String traduireAnglaisVersFrancais(String texte) {
        HttpURLConnection conn = null;

        try {
            if (texte == null || texte.isBlank()) {
                return "";
            }

            String texteEncode = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            String urlStr = BASE_URL + "?q=" + texteEncode + "&langpair=en|fr";

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(response.toString());

                if (json.has("responseData")) {
                    JSONObject responseData = json.getJSONObject("responseData");
                    String traduction = responseData.optString("translatedText", "").trim();

                    if (!traduction.isBlank()) {
                        return traduction;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        // fallback : si la traduction échoue, on retourne le texte original
        return texte;
    }
}
