package services;

import models.Ressource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AIService {

    // IMPORTANT: Remplacez par votre clé API Gemini (Google)
    // Obtenez-la gratuitement sur :
    private static final String API_KEY = "AIzaSyDwUX-w80i2XT1z8a57AgPtnxKyfS6sfJQ";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
            + API_KEY;

    private final HttpClient httpClient;
    private final ServiceRessource serviceRessource;

    public AIService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.serviceRessource = new ServiceRessource();
    }

    /**
     * Envoie un message à l'IA et retourne la réponse de manière asynchrone.
     */
    public CompletableFuture<String> sendMessage(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Analyse locale pour le "Bonus intelligent" (recommandation de ressources)
                String recommendations = generateLocalRecommendations(userMessage);

                // 2. Préparation du prompt pour l'IA
                String systemPrompt = "Tu es un assistant IA intégré dans l'interface de consultation des ressources pour les étudiants de MentalUP. "
                        +
                        "Ton rôle est :\n" +
                        "- Aider l'étudiant à comprendre les ressources affichées.\n" +
                        "- Expliquer ou simplifier un article, une vidéo ou un cours.\n" +
                        "- Répondre aux questions liées au contenu pédagogique.\n" +
                        "- Donner des exemples pour mieux comprendre les notions.\n" +
                        "- Orienter vers des ressources similaires disponibles dans la plateforme.\n" +
                        "Règles :\n" +
                        "- Toujours rester dans un contexte éducatif.\n" +
                        "- Répondre de façon simple, claire et structurée.\n" +
                        "- Être bref mais utile.\n" +
                        "- Utiliser un langage adapté aux étudiants.\n" +
                        "- Si la question n'est pas liée aux ressources, rediriger vers le contenu pédagogique.\n" +
                        "Si des ressources locales pertinentes te sont fournies dans ce contexte caché, utilise-les pour tes recommandations.";

                String fullUserMessage = userMessage;
                if (!recommendations.isEmpty()) {
                    fullUserMessage += "\n\n[Contexte système caché à l'utilisateur : voici des ressources locales pertinentes que tu peux recommander si ça a du sens : "
                            + recommendations + "]";
                }

                // 3. Construction de la requête JSON (format Gemini)
                JSONObject requestBody = new JSONObject();

                // Instructions système
                JSONObject systemInstruction = new JSONObject();
                JSONArray systemParts = new JSONArray();
                JSONObject systemPart = new JSONObject();
                systemPart.put("text", systemPrompt);
                systemParts.put(systemPart);
                systemInstruction.put("parts", systemParts);
                requestBody.put("systemInstruction", systemInstruction);

                // Message utilisateur
                JSONArray contents = new JSONArray();
                JSONObject contentObj = new JSONObject();
                contentObj.put("role", "user");
                JSONArray contentParts = new JSONArray();
                JSONObject contentPart = new JSONObject();
                contentPart.put("text", fullUserMessage);
                contentParts.put(contentPart);
                contentObj.put("parts", contentParts);
                contents.put(contentObj);
                requestBody.put("contents", contents);

                // 4. Appel HTTP
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // 5. Gestion de la réponse
                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject firstCandidate = candidates.getJSONObject(0);
                        JSONObject content = firstCandidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            return parts.getJSONObject(0).getString("text");
                        }
                    }
                } else {
                    System.err.println("Erreur API IA : " + response.statusCode() + " - " + response.body());
                    return "Désolé, je rencontre des difficultés techniques pour vous répondre (Erreur "
                            + response.statusCode() + ").\n\n" +
                            (!recommendations.isEmpty()
                                    ? "En attendant, voici quelques ressources qui pourraient vous aider :\n"
                                            + recommendations
                                    : "");
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Désolé, une erreur de connexion est survenue. Veuillez vérifier votre connexion internet ou réessayer plus tard.";
            }

            return "Je n'ai pas pu générer de réponse.";
        });
    }

    /**
     * Recherche des ressources pertinentes dans la BD basées sur les mots-clés du
     * message.
     */
    private String generateLocalRecommendations(String userMessage) {
        String msgLower = userMessage.toLowerCase();

        // Définir quelques mots-clés cibles
        boolean hasStress = msgLower.contains("stress") || msgLower.contains("anxiété") || msgLower.contains("panique");
        boolean hasExamen = msgLower.contains("examen") || msgLower.contains("révision") || msgLower.contains("test");
        boolean hasDepression = msgLower.contains("dépression") || msgLower.contains("triste")
                || msgLower.contains("seul");

        if (!hasStress && !hasExamen && !hasDepression) {
            return ""; // Pas de recommandation spécifique
        }

        List<Ressource> toutesLesRessources = serviceRessource.getAll();

        // Filtrer les ressources basées sur les mots clés trouvés
        List<Ressource> pertinentes = toutesLesRessources.stream()
                .filter(r -> {
                    String titreDesc = (r.getTitre() + " " + r.getDescription()).toLowerCase();
                    if (hasStress && (titreDesc.contains("stress") || titreDesc.contains("bien-être")
                            || titreDesc.contains("respiration")))
                        return true;
                    if (hasExamen && (titreDesc.contains("examen") || titreDesc.contains("organisation")
                            || titreDesc.contains("concentration")))
                        return true;
                    if (hasDepression && (titreDesc.contains("dépression") || titreDesc.contains("aide")
                            || titreDesc.contains("psychologue")))
                        return true;
                    return false;
                })
                .limit(3) // Limiter à 3 recommandations pour ne pas surcharger le prompt
                .collect(Collectors.toList());

        if (pertinentes.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Ressource r : pertinentes) {
            sb.append("- ").append(r.getTitre());
            if (r.getLien() != null && !r.getLien().isEmpty()) {
                sb.append(" (Lien: ").append(r.getLien()).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
