package services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ZenQuotesService {

    private static final String TODAY_API_URL = "https://zenquotes.io/api/today";
    private static final String RANDOM_API_URL = "https://zenquotes.io/api/random";

    public String[] getTodayQuote() {
        return fetchQuoteFromUrl(TODAY_API_URL);
    }

    public String[] getRandomQuote() {
        return fetchQuoteFromUrl(RANDOM_API_URL);
    }

    private String[] fetchQuoteFromUrl(String apiUrl) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONArray jsonArray = new JSONArray(response.toString());

                if (jsonArray.length() > 0) {
                    JSONObject obj = jsonArray.getJSONObject(0);

                    String quote = obj.optString("q", "").trim();
                    String author = obj.optString("a", "").trim();

                    return new String[]{quote, author};
                }

            } else if (responseCode == 429) {
                System.out.println("ZenQuotes bloquée temporairement : trop de requêtes (HTTP 429).");
                return new String[]{"__RATE_LIMIT__", ""};
            } else {
                System.out.println("Erreur API ZenQuotes - code HTTP : " + responseCode);
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération de la citation : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (conn != null) {
                conn.disconnect();
            }
        }

        return new String[]{"", ""};
    }
}