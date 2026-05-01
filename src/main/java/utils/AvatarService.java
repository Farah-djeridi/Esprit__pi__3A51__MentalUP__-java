package utils;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * Service de génération d'avatar IA via Hugging Face Inference API.
 * Token: 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
 */
public class AvatarService {

    private static final String HF_TOKEN = "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";
    private static final String MODEL_URL =
        "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0";

    /**
     * Génère un avatar à partir d'un prompt texte.
     * @return chemin du fichier image généré, ou null en cas d'erreur
     */
    public static String generateAvatarFromPrompt(String prompt) {
        try {
            // Construire la requête JSON
            String body = "{\"inputs\": \"" + prompt.replace("\"", "\\\"") +
                          ", portrait, high quality, detailed face\"}";

            HttpURLConnection conn = (HttpURLConnection)
                new URL(MODEL_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.getOutputStream().write(body.getBytes());

            int status = conn.getResponseCode();
            if (status == 200) {
                // Sauvegarder l'image dans un fichier temporaire
                byte[] imageBytes = conn.getInputStream().readAllBytes();
                File tmpFile = File.createTempFile("avatar_", ".png",
                    new File(System.getProperty("java.io.tmpdir")));
                Files.write(tmpFile.toPath(), imageBytes);
                System.out.println("✅ Avatar généré : " + tmpFile.getAbsolutePath());
                return tmpFile.getAbsolutePath();
            } else {
                String error = new String(conn.getErrorStream() != null ?
                    conn.getErrorStream().readAllBytes() : new byte[0]);
                System.err.println("❌ Erreur Hugging Face (" + status + "): " + error);
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur génération avatar: " + e.getMessage());
            return null;
        }
    }
}
