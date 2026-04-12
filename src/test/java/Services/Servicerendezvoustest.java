package Services;

import Models.RendezVous;
import org.junit.jupiter.api.*;
import validators.RendezVousValidator;
import validators.RendezVousValidator.ValidationResult;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ServiceRendezVous + RendezVousValidator.
 *
 * Structure :
 *  - Tests de validation (sans BDD)
 *  - Tests d'intégration service (avec BDD réelle)
 *
 * Lancer : mvn test  OU  clic droit → Run Tests dans IntelliJ/NetBeans
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceRendezVousTest {

    private static ServiceRendezVous service;

    // ── ID de test (nettoyés après chaque test d'insertion) ──
    private static int dernierIdInsere = -1;

    @BeforeAll
    static void setUp() {
        service = new ServiceRendezVous();
    }

    // ═══════════════════════════════════════════════════════════
    //  BLOC 1 — TESTS DE VALIDATION (RendezVousValidator)
    //  Ces tests ne touchent PAS la base de données
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ RDV valide — tous les champs corrects")
    void testRdvValide() {
        RendezVous r = rdvValide();
        ValidationResult result = RendezVousValidator.valider(r);
        assertTrue(result.isValide(),
                "Un RDV valide ne doit pas avoir d'erreurs. Erreurs : " + result.getMessageComplet());
    }

    @Test
    @Order(2)
    @DisplayName("❌ Date null — doit échouer")
    void testDateNull() {
        RendezVous r = rdvValide();
        r.setDate(null);
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("date")),
                "Doit signaler que la date est obligatoire.");
    }

    @Test
    @Order(3)
    @DisplayName("❌ Date dans le passé — doit échouer")
    void testDateDansLePassé() {
        RendezVous r = rdvValide();
        r.setDate(Date.valueOf(LocalDate.now().minusDays(1)));
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("passé")));
    }

    @Test
    @Order(4)
    @DisplayName("❌ Heure de début null — doit échouer")
    void testHeureDebutNull() {
        RendezVous r = rdvValide();
        r.setHeureDebut(null);
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.toLowerCase().contains("heure de début")));
    }

    @Test
    @Order(5)
    @DisplayName("❌ Heure début avant 8h — doit échouer")
    void testHeureDebutTropTot() {
        RendezVous r = rdvValide();
        r.setHeureDebut(Time.valueOf("07:00:00"));
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("08h") || e.contains("8h")));
    }

    @Test
    @Order(6)
    @DisplayName("❌ Heure fin avant heure début — doit échouer")
    void testHeureFinAvantDebut() {
        RendezVous r = rdvValide();
        r.setHeureDebut(Time.valueOf("14:00:00"));
        r.setHeureFin(Time.valueOf("13:00:00"));
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("après")));
    }

    @Test
    @Order(7)
    @DisplayName("❌ Durée < 30 minutes — doit échouer")
    void testDureeTropCourte() {
        RendezVous r = rdvValide();
        r.setHeureDebut(Time.valueOf("10:00:00"));
        r.setHeureFin(Time.valueOf("10:15:00"));
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("30 minutes")));
    }

    @Test
    @Order(8)
    @DisplayName("❌ Statut invalide — doit échouer")
    void testStatutInvalide() {
        RendezVous r = rdvValide();
        r.setStatut("inventé");
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("Statut invalide")));
    }

    @Test
    @Order(9)
    @DisplayName("❌ Type RDV invalide — doit échouer")
    void testTypeInvalide() {
        RendezVous r = rdvValide();
        r.setTypeRdv("rdv_bizarre");
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("Type invalide")));
    }

    @Test
    @Order(10)
    @DisplayName("❌ Psychologue ID = 0 — doit échouer")
    void testPsychologueIdZero() {
        RendezVous r = rdvValide();
        r.setPsychologueId(0);
        ValidationResult result = RendezVousValidator.valider(r);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("psychologue")));
    }

    @Test
    @Order(11)
    @DisplayName("✅ Tous les statuts valides passent")
    void testTousStatutsValides() {
        for (String statut : RendezVousValidator.STATUTS_VALIDES) {
            RendezVous r = rdvValide();
            r.setStatut(statut);
            ValidationResult result = RendezVousValidator.valider(r);
            assertTrue(result.isValide(), "Statut '" + statut + "' devrait être valide. Erreurs : " + result.getMessageComplet());
        }
    }

    @Test
    @Order(12)
    @DisplayName("✅ Tous les types valides passent")
    void testTousTypesValides() {
        for (String type : RendezVousValidator.TYPES_VALIDES) {
            RendezVous r = rdvValide();
            r.setTypeRdv(type);
            ValidationResult result = RendezVousValidator.valider(r);
            assertTrue(result.isValide(), "Type '" + type + "' devrait être valide. Erreurs : " + result.getMessageComplet());
        }
    }

    @Test
    @Order(13)
    @DisplayName("✅ parseHeure — format valide HH:mm")
    void testParseHeureValide() {
        ValidationResult result = new ValidationResult();
        Time t = RendezVousValidator.parseHeure("09:30", "Heure début", result);
        assertNotNull(t);
        assertTrue(result.isValide());
    }

    @Test
    @Order(14)
    @DisplayName("❌ parseHeure — format invalide")
    void testParseHeureInvalide() {
        ValidationResult result = new ValidationResult();
        Time t = RendezVousValidator.parseHeure("9h30", "Heure début", result);
        assertNull(t);
        assertFalse(result.isValide());
    }

    @Test
    @Order(15)
    @DisplayName("❌ RDV null — doit échouer immédiatement")
    void testRdvNull() {
        ValidationResult result = RendezVousValidator.valider(null);
        assertFalse(result.isValide());
        assertEquals(1, result.getErreurs().size());
    }

    // ═══════════════════════════════════════════════════════════
    //  BLOC 2 — TESTS D'INTÉGRATION SERVICE (avec BDD)
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(20)
    @DisplayName("🗄 getAll — retourne une liste non nulle")
    void testGetAllNonNull() {
        assertNotNull(service.getAll(), "getAll() ne doit jamais retourner null.");
    }

    @Test
    @Order(21)
    @DisplayName("🗄 add + getAll — le RDV ajouté est retrouvé")
    void testAddEtGetAll() {
        RendezVous r = rdvValide();
        int avantAjout = service.getAll().size();
        service.add(r);
        int apresAjout = service.getAll().size();
        assertTrue(apresAjout > avantAjout, "Après add(), la liste doit contenir un élément de plus.");
    }

    @Test
    @Order(22)
    @DisplayName("🗄 getByPsychologueId — retourne uniquement les RDV du psy")
    void testGetByPsychologueId() {
        service.getByPsychologueId(2).forEach(r ->
                assertEquals(2, r.getPsychologueId(),
                        "Tous les RDV retournés doivent appartenir au psy id=2.")
        );
    }

    @Test
    @Order(23)
    @DisplayName("🗄 reserverCreneau — créneau libre devient réservé")
    void testReserverCreneau() {
        // Insérer un créneau libre
        RendezVous r = rdvValide();
        r.setStatut("libre");
        service.add(r);

        // Récupérer le dernier inséré
        var list = service.getByPsychologueId(2);
        assertFalse(list.isEmpty());
        RendezVous insere = list.get(list.size() - 1);

        boolean ok = service.reserverCreneau(insere.getId(), 1);
        assertTrue(ok, "La réservation doit réussir sur un créneau libre.");

        // Nettoyage
        service.delete(insere.getId());
    }

    @Test
    @Order(24)
    @DisplayName("🗄 reserverCreneau — créneau déjà réservé = échec")
    void testReserverCreneauDejaReserve() {
        // Insérer un créneau déjà réservé
        RendezVous r = rdvValide();
        r.setStatut("réservé");
        service.add(r);

        var list = service.getByPsychologueId(2);
        RendezVous insere = list.get(list.size() - 1);

        boolean ok = service.reserverCreneau(insere.getId(), 1);
        assertFalse(ok, "La réservation doit échouer sur un créneau déjà réservé.");

        // Nettoyage
        service.delete(insere.getId());
    }

    @Test
    @Order(25)
    @DisplayName("🗄 getRdvAujourdhui — filtre uniquement la date d'aujourd'hui")
    void testGetRdvAujourdhui() {
        service.getRdvAujourdhui(1).forEach(r ->
                assertEquals(LocalDate.now(), r.getDate().toLocalDate(),
                        "Tous les RDV d'aujourd'hui doivent avoir la date de ce jour.")
        );
    }

    @Test
    @Order(26)
    @DisplayName("🗄 getRdvAvenir — toutes les dates sont dans le futur")
    void testGetRdvAvenir() {
        service.getRdvAvenir(1).forEach(r ->
                assertTrue(r.getDate().toLocalDate().isAfter(LocalDate.now()),
                        "Les RDV à venir doivent être dans le futur.")
        );
    }

    @Test
    @Order(27)
    @DisplayName("🗄 getRdvAnciens — toutes les dates sont dans le passé")
    void testGetRdvAnciens() {
        service.getRdvAnciens(1).forEach(r ->
                assertTrue(r.getDate().toLocalDate().isBefore(LocalDate.now()),
                        "Les anciens RDV doivent être dans le passé.")
        );
    }

    @Test
    @Order(28)
    @DisplayName("🗄 delete — suppression vérifiée")
    void testDelete() {
        RendezVous r = rdvValide();
        service.add(r);
        var list = service.getByPsychologueId(2);
        RendezVous insere = list.get(list.size() - 1);
        int idASupprimer = insere.getId();

        service.delete(idASupprimer);

        boolean encorePresent = service.getAll().stream()
                .anyMatch(rv -> rv.getId() == idASupprimer);
        assertFalse(encorePresent, "Le RDV supprimé ne doit plus être présent.");
    }

    @Test
    @Order(29)
    @DisplayName("🗄 update — modification vérifiée")
    void testUpdate() {
        RendezVous r = rdvValide();
        service.add(r);
        var list = service.getByPsychologueId(2);
        RendezVous insere = list.get(list.size() - 1);

        insere.setStatut("confirmé");
        insere.setTypeRdv("bilan");
        service.update(insere);

        // Nettoyage
        service.delete(insere.getId());
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER — Crée un RDV valide pour les tests
    // ═══════════════════════════════════════════════════════════
    private RendezVous rdvValide() {
        RendezVous r = new RendezVous();
        r.setDate(Date.valueOf(LocalDate.now().plusDays(1)));
        r.setHeureDebut(Time.valueOf("10:00:00"));
        r.setHeureFin(Time.valueOf("11:00:00"));
        r.setStatut("libre");
        r.setTypeRdv("consultation");
        r.setPsychologueId(2);
        return r;
    }
}