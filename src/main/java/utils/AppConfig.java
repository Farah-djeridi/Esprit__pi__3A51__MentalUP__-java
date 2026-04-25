package utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Charge les cles API et parametres depuis src/main/resources/config.properties.
 *
 * Le fichier config.properties est dans .gitignore — il ne sera jamais pousse sur Git.
 * Partager config.properties.example avec l'equipe (sans les vraies valeurs).
 *
 * Usage :
 *   String token = AppConfig.get("huggingface.token");
 *   String dbUrl = AppConfig.get("db.url");
 */
public class AppConfig {

    private static final Properties props = new Properties();
    private static boolean loaded = false;

    static {
        load();
    }

    private static void load() {
        try (InputStream is = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
                loaded = true;
                System.out.println("Configuration chargee depuis config.properties");
            } else {
                System.err.println("ATTENTION : config.properties introuvable !");
                System.err.println("  Copiez config.properties.example en config.properties");
                System.err.println("  et remplissez vos valeurs.");
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement config : " + e.getMessage());
        }
    }

    /** Retourne la valeur de la propriete, ou defaultValue si absente */
    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    /** Retourne la valeur de la propriete, ou "" si absente */
    public static String get(String key) {
        return props.getProperty(key, "");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
