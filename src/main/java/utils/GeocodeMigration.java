package utils;

import models.Activite;
import services.ServiceActivite;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Script de migration pour géocoder toutes les activités existantes
 */
public class GeocodeMigration {
    
    public static void main(String[] args) {
        System.out.println("=== Démarrage du géocodage des activités ===\n");
        
        ServiceActivite service = new ServiceActivite();
        
        try {
            List<Activite> activites = service.getAllActivites();
            System.out.println("Nombre d'activités à traiter : " + activites.size() + "\n");
            
            int success = 0, failed = 0, skipped = 0;
            
            for (Activite activite : activites) {
                // Vérifier si déjà géocodée
                if (activite.getLatitude() != 0.0 && activite.getLongitude() != 0.0) {
                    System.out.println("✓ SKIP: '" + activite.getTitre() + "' déjà géocodée");
                    skipped++;
                    continue;
                }
                
                System.out.print("⏳ Géocodage de '" + activite.getTitre() + "' (" + activite.getAdresse() + ")... ");
                
                try {
                    double[] coords = geocoderAdresse(activite.getAdresse());
                    
                    if (coords != null) {
                        activite.setLatitude(coords[0]);
                        activite.setLongitude(coords[1]);
                        service.modifierActivite(activite);
                        System.out.println("✓ OK [" + coords[0] + ", " + coords[1] + "]");
                        success++;
                        
                        // Pause pour respecter les limites de l'API Nominatim (1 req/sec)
                        Thread.sleep(1100);
                    } else {
                        System.out.println("✗ ÉCHEC (adresse introuvable)");
                        failed++;
                    }
                    
                } catch (Exception e) {
                    System.out.println("✗ ERREUR: " + e.getMessage());
                    failed++;
                }
            }
            
            System.out.println("\n=== Résumé ===");
            System.out.println("✓ Succès    : " + success);
            System.out.println("✗ Échecs    : " + failed);
            System.out.println("⊘ Ignorées  : " + skipped);
            System.out.println("━ Total     : " + activites.size());
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static double[] geocoderAdresse(String adresse) {
        try {
            String encoded = URLEncoder.encode(adresse, StandardCharsets.UTF_8);
            URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1");
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "MentalUpApp/1.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            if (conn.getResponseCode() != 200) {
                return null;
            }
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            String json = sb.toString();
            
            // Parse simple du JSON
            if (json.contains("\"lat\"") && json.contains("\"lon\"")) {
                int latIdx = json.indexOf("\"lat\":\"") + 7;
                int latEnd = json.indexOf("\"", latIdx);
                int lonIdx = json.indexOf("\"lon\":\"") + 7;
                int lonEnd = json.indexOf("\"", lonIdx);
                
                if (latIdx > 6 && latEnd > latIdx && lonIdx > 6 && lonEnd > lonIdx) {
                    String latStr = json.substring(latIdx, latEnd);
                    String lonStr = json.substring(lonIdx, lonEnd);
                    
                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);
                    
                    return new double[]{lat, lon};
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("\nErreur géocodage: " + e.getMessage());
            return null;
        }
    }
}
