package services;

import models.SuiviMentale;
import models.TipData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WellbeingTipService {

    private static final String BASE_URL = "https://odphp.health.gov/myhealthfinder/api/v4";

    public TipData getTipForSuivi(SuiviMentale s) {
        if (s == null) {
            return buildFallback("general");
        }

        int stress = s.getTauxDeStress();
        int energie = s.getNiveauDenergie();
        String qualiteSommeil = s.getQualiteDuSommeil() == null
                ? ""
                : s.getQualiteDuSommeil().trim().toLowerCase();

        String type = "mindfulness";

        if (stress >= 7) {
            type = "stress";
        } else if (energie <= 3) {
            type = "energy";
        } else if (qualiteSommeil.equals("mauvais") || qualiteSommeil.equals("terrible")) {
            type = "sleep";
        }

        String[] keywords;

        switch (type) {
            case "stress":
                keywords = new String[]{"stress", "anxiety", "relaxation", "coping"};
                break;

            case "sleep":
                keywords = new String[]{"sleep", "insomnia", "sleep hygiene"};
                break;

            case "energy":
                // 🔥 MODIFICATION ICI (moins de fallback)
                keywords = new String[]{
                        "motivation",
                        "mental health",
                        "wellbeing",
                        "mindfulness"
                };
                break;

            default:
                keywords = new String[]{"mindfulness", "meditation", "breathing"};
        }

        for (String keyword : keywords) {
            try {
                TipData tip = searchApi(keyword, type);
                if (tip != null) {
                    return tip;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return buildFallback(type);
    }

    private TipData searchApi(String keyword, String type) throws Exception {

        String apiUrl = BASE_URL + "/topicsearch.json?keyword=" + keyword.replace(" ", "%20");

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        int status = conn.getResponseCode();

        if (status != 200) {
            return null;
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
        );

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        br.close();
        conn.disconnect();

        JSONObject root = new JSONObject(response.toString());
        JSONObject result = root.optJSONObject("Result");
        if (result == null) return null;

        JSONObject resourcesObj = result.optJSONObject("Resources");
        if (resourcesObj == null) return null;

        Object resourceRaw = resourcesObj.opt("Resource");
        if (resourceRaw == null) return null;

        JSONArray resources;

        if (resourceRaw instanceof JSONArray) {
            resources = (JSONArray) resourceRaw;
        } else {
            resources = new JSONArray();
            resources.put(resourceRaw);
        }

        if (resources.isEmpty()) return null;

        // 🔥 amélioration : chercher un bon résultat
        JSONObject r = null;

        for (int i = 0; i < resources.length(); i++) {
            JSONObject candidate = resources.optJSONObject(i);
            if (candidate == null) continue;

            String candidateTitle = candidate.optString("Title", "");
            String candidateUrl = candidate.optString("AccessibleVersion",
                    candidate.optString("Url", ""));

            if (!candidateTitle.isBlank() || !candidateUrl.isBlank()) {
                r = candidate;
                break;
            }
        }

        if (r == null) return null;

        String title = r.optString("Title", "Conseil bien-être");
        String url = r.optString("AccessibleVersion", r.optString("Url", ""));

        String text = extractShortText(r);

        if (text == null || text.isBlank()) {
            text = fallbackText(type);
        }

        return new TipData(
                title,
                limitText(text, 220),
                buildExercise(type),
                type,
                "ODPHP MyHealthfinder",
                url,
                keyword
        );
    }

    private String extractShortText(JSONObject resource) {
        try {
            JSONObject sectionsObj = resource.optJSONObject("Sections");
            if (sectionsObj == null) return "";

            Object rawSection = sectionsObj.opt("Section");
            if (rawSection == null) return "";

            JSONArray sections;

            if (rawSection instanceof JSONArray) {
                sections = (JSONArray) rawSection;
            } else {
                sections = new JSONArray();
                sections.put(rawSection);
            }

            for (int i = 0; i < sections.length(); i++) {
                JSONObject sec = sections.optJSONObject(i);
                if (sec == null) continue;

                String content = sec.optString("Content", "");

                if (!content.isBlank()) {
                    return content
                            .replaceAll("<[^>]*>", "")
                            .replaceAll("\\s+", " ")
                            .trim();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private TipData buildFallback(String type) {
        return new TipData(
                "Conseil bien-être",
                fallbackText(type),
                buildExercise(type),
                type,
                "Interne (fallback)",
                "",
                type
        );
    }

    private String fallbackText(String type) {
        switch (type) {
            case "stress":
                return "Réduire le stress : relâchez les épaules, ralentissez la respiration et recentrez-vous sur le moment présent.";

            case "sleep":
                return "Sommeil : adoptez une routine calme, réduisez les écrans avant de dormir et gardez une heure régulière.";

            case "energy":
                return "Énergie mentale : faites une pause courte, hydratez-vous et commencez une micro-action simple pour relancer votre élan.";

            default:
                return "Prenez deux minutes pour vous recentrer et relâcher les tensions.";
        }
    }

    private String buildExercise(String type) {
        switch (type) {
            case "stress":
                return "Respirez 4 secondes, expirez 6 secondes pendant 2 minutes.";

            case "sleep":
                return "Éteignez les écrans 30 minutes avant de dormir et faites 1 minute de respiration lente.";

            case "energy":
                return "Faites 5 respirations lentes puis choisissez une micro-action de 2 minutes.";

            default:
                return "Faites 5 respirations lentes et relâchez les épaules.";
        }
    }

    private String limitText(String text, int maxLen) {
        if (text == null) return "";

        text = text.trim();

        if (text.length() <= maxLen) return text;

        return text.substring(0, maxLen) + "…";
    }
}