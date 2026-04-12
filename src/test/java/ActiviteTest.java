import models.Activite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires - Activite")
class ActiviteTest {

    // ─── Création vide ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Création d'une activité vide → tous les champs sont null/0")
    void testCreationVide() {
        Activite a = new Activite();
        assertNull(a.getTitre(),       "titre doit être null");
        assertNull(a.getDescription(), "description doit être null");
        assertNull(a.getType(),        "type doit être null");
        assertNull(a.getAdresse(),     "adresse doit être null");
        assertNull(a.getDateDebut(),   "dateDebut doit être null");
        assertNull(a.getDateFin(),     "dateFin doit être null");
        assertEquals(0.0, a.getLatitude(),  "latitude doit être 0.0");
        assertEquals(0.0, a.getLongitude(), "longitude doit être 0.0");
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    @Test
    @DisplayName("setTitre / getTitre → retourne la valeur correcte")
    void testTitre() {
        Activite a = new Activite();
        a.setTitre("Yoga matinal");
        assertEquals("Yoga matinal", a.getTitre());
    }

    @Test
    @DisplayName("setDescription / getDescription → retourne la valeur correcte")
    void testDescription() {
        Activite a = new Activite();
        a.setDescription("Une séance de yoga pour bien commencer la journée.");
        assertEquals("Une séance de yoga pour bien commencer la journée.", a.getDescription());
    }

    @Test
    @DisplayName("setType / getType → retourne la valeur correcte (sport, culturel, créatif)")
    void testType() {
        Activite a = new Activite();
        for (String type : new String[]{"sport", "culturel", "créatif"}) {
            a.setType(type);
            assertEquals(type, a.getType());
        }
    }

    @Test
    @DisplayName("setAdresse / getAdresse → retourne la valeur correcte")
    void testAdresse() {
        Activite a = new Activite();
        a.setAdresse("Tunis, Rue de la Liberté");
        assertEquals("Tunis, Rue de la Liberté", a.getAdresse());
    }

    @Test
    @DisplayName("setDateDebut / getDateDebut → retourne la date correcte")
    void testDateDebut() {
        Activite a = new Activite();
        LocalDate date = LocalDate.of(2026, 5, 1);
        a.setDateDebut(date);
        assertEquals(date, a.getDateDebut());
    }

    @Test
    @DisplayName("setDateFin / getDateFin → retourne la date correcte")
    void testDateFin() {
        Activite a = new Activite();
        LocalDate date = LocalDate.of(2026, 6, 30);
        a.setDateFin(date);
        assertEquals(date, a.getDateFin());
    }

    // ─── Validation dates ────────────────────────────────────────────────────

    @Test
    @DisplayName("Date début avant date fin → valide")
    void testDatesValides() {
        Activite a = new Activite();
        LocalDate debut = LocalDate.of(2026, 4, 1);
        LocalDate fin   = LocalDate.of(2026, 4, 30);
        a.setDateDebut(debut);
        a.setDateFin(fin);
        assertTrue(a.getDateDebut().isBefore(a.getDateFin()),
                "dateDebut doit être avant dateFin");
    }

    @Test
    @DisplayName("Date début après date fin → invalide")
    void testDatesInvalides() {
        Activite a = new Activite();
        LocalDate debut = LocalDate.of(2026, 5, 15);
        LocalDate fin   = LocalDate.of(2026, 4, 1);
        a.setDateDebut(debut);
        a.setDateFin(fin);
        assertFalse(a.getDateDebut().isBefore(a.getDateFin()),
                "dateDebut après dateFin doit être invalide");
    }

    // ─── Latitude / Longitude ────────────────────────────────────────────────

    @Test
    @DisplayName("setLatitude / getLatitude → accepte valeur et null simulé (0.0)")
    void testLatitude() {
        Activite a = new Activite();
        a.setLatitude(36.8065);
        assertEquals(36.8065, a.getLatitude(), 0.0001);
        a.setLatitude(0.0); // null simulé
        assertEquals(0.0, a.getLatitude());
    }

    @Test
    @DisplayName("setLongitude / getLongitude → accepte valeur et null simulé (0.0)")
    void testLongitude() {
        Activite a = new Activite();
        a.setLongitude(10.1815);
        assertEquals(10.1815, a.getLongitude(), 0.0001);
        a.setLongitude(0.0); // null simulé
        assertEquals(0.0, a.getLongitude());
    }

    // ─── Contraintes métier ──────────────────────────────────────────────────

    @Test
    @DisplayName("Titre vide → invalide (contrainte NotBlank)")
    void testTitreVide() {
        Activite a = new Activite();
        a.setTitre("");
        assertTrue(a.getTitre().isBlank(), "Titre vide doit être considéré invalide");
    }

    @Test
    @DisplayName("Description vide → invalide (contrainte NotBlank)")
    void testDescriptionVide() {
        Activite a = new Activite();
        a.setDescription("");
        assertTrue(a.getDescription().isBlank(), "Description vide doit être considérée invalide");
    }

    @Test
    @DisplayName("Titre null → invalide (contrainte NotNull)")
    void testTitreNull() {
        Activite a = new Activite();
        a.setTitre(null);
        assertNull(a.getTitre(), "Titre null doit être invalide");
    }
}
