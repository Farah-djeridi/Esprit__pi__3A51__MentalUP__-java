package utils;

import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Service de generation d'avatar via Hugging Face Inference Providers (2025).
 * La cle API est lue depuis config.properties (cle : huggingface.token).
 */
public class AvatarService {

    private static final String HF_TOKEN = AppConfig.get("huggingface.token");
    private static final String MODEL    = "black-forest-labs/FLUX.1-schnell";
    private static final String API_URL  = "https://router.huggingface.co/hf-inference/models/" + MODEL;
    private static final int    TIMEOUT  = 120;

    public static String generateAvatarFromPrompt(String prompt) {
        try {
            byte[] data = callApi(prompt);
            if (data == null) return null;
            String path = buildSavePath("avatar_" + System.currentTimeMillis() + ".png");
            Files.write(Paths.get(path), data);
            return path;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public static String generateAvatarUrl(String prompt) {
        try {
            byte[] data = callApi(prompt);
            if (data == null) return null;
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(data);
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public static String generateAvatarAndSave(String prompt, String filename) {
        try {
            byte[] data = callApi(prompt);
            if (data == null) return null;
            String path = buildSavePath(filename);
            Files.write(Paths.get(path), data);
            return path;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private static byte[] callApi(String prompt) throws Exception {
        if (HF_TOKEN.isEmpty()) {
            System.err.println("huggingface.token manquant dans config.properties");
            return null;
        }
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30)).build();

        String enriched = prompt.trim() + ", portrait avatar, digital art, high quality, detailed face";
        String body = "{\"inputs\": \"" + escapeJson(enriched) + "\","
                    + "\"parameters\": {},"
                    + "\"options\": {\"wait_for_model\": true}}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + HF_TOKEN)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(TIMEOUT))
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("HuggingFace status : " + resp.statusCode());

        if (resp.statusCode() == 200 && isImage(resp.body())) return resp.body();
        if (resp.statusCode() == 503) {
            System.out.println("Modele en chargement, attente 25s...");
            Thread.sleep(25_000);
            HttpResponse<byte[]> retry = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (retry.statusCode() == 200 && isImage(retry.body())) return retry.body();
        }
        System.err.println("Erreur API (" + resp.statusCode() + ") : " + new String(resp.body()));
        return null;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "'").replace("\n", " ").replace("\r", "");
    }

    private static boolean isImage(byte[] d) {
        if (d == null || d.length < 4) return false;
        boolean png  = (d[0]&0xFF)==0x89 && (d[1]&0xFF)==0x50 && (d[2]&0xFF)==0x4E && (d[3]&0xFF)==0x47;
        boolean jpeg = (d[0]&0xFF)==0xFF && (d[1]&0xFF)==0xD8;
        return png || jpeg;
    }

    private static String buildSavePath(String fileName) throws Exception {
        Path dir = Paths.get(System.getProperty("user.home"), "Pictures", "avatars");
        if (!Files.exists(dir)) Files.createDirectories(dir);
        return dir.resolve(fileName).toString();
    }
}
