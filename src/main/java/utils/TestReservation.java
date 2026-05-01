package utils;

import javafx.application.Platform;

/**
 * Utilitaire de test pour déboguer les problèmes de réservation
 */
public class TestReservation {
    
    /**
     * Test direct de la réservation sans CAPTCHA
     */
    public static void testerReservationDirecte(Runnable actionReservation) {
        System.out.println("🧪 Test direct de réservation...");
        
        // Exécuter sur le thread JavaFX
        Platform.runLater(() -> {
            try {
                System.out.println("🎯 Exécution de l'action de réservation...");
                actionReservation.run();
                System.out.println("✅ Action de réservation terminée");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la réservation: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Ajoute des logs détaillés pour le débogage
     */
    public static void logDebug(String message) {
        System.out.println("[DEBUG] " + java.time.LocalTime.now() + " - " + message);
    }
}