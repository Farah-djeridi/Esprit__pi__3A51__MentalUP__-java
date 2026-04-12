import models.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires - Reservation")
class ReservationTest {

    // ─── Création vide ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Création d'une réservation vide → tous les champs sont null/0")
    void testCreationVide() {
        Reservation r = new Reservation();
        assertEquals(0,   r.getIdReservation(),  "idReservation doit être 0");
        assertEquals(0,   r.getIdActivite(),      "idActivite doit être 0");
        assertNull(r.getNomEtudiant(),            "nomEtudiant doit être null");
        assertNull(r.getPlace(),                  "place doit être null");
        assertNull(r.getDateReservation(),        "dateReservation doit être null");
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    @Test
    @DisplayName("setIdActivite / getIdActivite → retourne la valeur correcte")
    void testIdActivite() {
        Reservation r = new Reservation();
        r.setIdActivite(5);
        assertEquals(5, r.getIdActivite());
    }

    @Test
    @DisplayName("idActivite négatif → invalide (contrainte Positive)")
    void testIdActiviteNegatif() {
        Reservation r = new Reservation();
        r.setIdActivite(-1);
        assertTrue(r.getIdActivite() <= 0, "idActivite négatif doit être invalide");
    }

    @Test
    @DisplayName("idActivite zéro → invalide (contrainte Positive)")
    void testIdActiviteZero() {
        Reservation r = new Reservation();
        r.setIdActivite(0);
        assertTrue(r.getIdActivite() <= 0, "idActivite zéro doit être invalide");
    }

    @Test
    @DisplayName("setNomEtudiant / getNomEtudiant → retourne la valeur correcte")
    void testNomEtudiant() {
        Reservation r = new Reservation();
        r.setNomEtudiant("Sophie Am.");
        assertEquals("Sophie Am.", r.getNomEtudiant());
    }

    @Test
    @DisplayName("nomEtudiant null → valide (champ optionnel)")
    void testNomEtudiantNull() {
        Reservation r = new Reservation();
        r.setNomEtudiant(null);
        assertNull(r.getNomEtudiant(), "nomEtudiant null doit être accepté (optionnel)");
    }

    @Test
    @DisplayName("setPlace / getPlace → retourne la valeur correcte")
    void testPlace() {
        Reservation r = new Reservation();
        r.setPlace("G4");
        assertEquals("G4", r.getPlace());
    }

    @Test
    @DisplayName("place null → valide (champ optionnel)")
    void testPlaceNull() {
        Reservation r = new Reservation();
        r.setPlace(null);
        assertNull(r.getPlace(), "place null doit être accepté (optionnel)");
    }

    @Test
    @DisplayName("setDateReservation / getDateReservation → retourne la date correcte")
    void testDateReservation() {
        Reservation r = new Reservation();
        LocalDate date = LocalDate.of(2026, 4, 12);
        r.setDateReservation(date);
        assertEquals(date, r.getDateReservation());
    }

    @Test
    @DisplayName("dateReservation null → invalide (contrainte NotNull)")
    void testDateReservationNull() {
        Reservation r = new Reservation();
        r.setDateReservation(null);
        assertNull(r.getDateReservation(), "dateReservation null doit être invalide");
    }

    // ─── Constructeur avec paramètres ────────────────────────────────────────

    @Test
    @DisplayName("Constructeur avec paramètres → tous les champs correctement initialisés")
    void testConstructeurParametres() {
        LocalDate date = LocalDate.of(2026, 4, 12);
        Reservation r = new Reservation(3, "Sophie Am.", "H8", date);
        assertEquals(3,            r.getIdActivite());
        assertEquals("Sophie Am.", r.getNomEtudiant());
        assertEquals("H8",         r.getPlace());
        assertEquals(date,         r.getDateReservation());
    }

    @Test
    @DisplayName("titreActivite → getter/setter fonctionne correctement")
    void testTitreActivite() {
        Reservation r = new Reservation();
        r.setTitreActivite("Yoga matinal");
        assertEquals("Yoga matinal", r.getTitreActivite());
    }
}
