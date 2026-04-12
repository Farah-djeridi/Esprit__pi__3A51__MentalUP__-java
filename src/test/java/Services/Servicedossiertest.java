package Services;

import Models.Dossier;
import org.junit.jupiter.api.*;
import validators.DossierValidator;
import validators.DossierValidator.ValidationResult;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceDossierTest {

    private static ServiceDossier service;

    @BeforeAll
    static void setUp() {
        service = new ServiceDossier();
    }

    // TESTS DE VALIDATION (DossierValidator)


    @Test
    @Order(1)
    @DisplayName("Dossier valide — tous les champs corrects")
    void testDossierValide() {
        Dossier d = dossierValide();
        ValidationResult result = DossierValidator.valider(d);
        assertTrue(result.isValide(),
                "Un dossier valide ne doit pas avoir d'erreurs. Erreurs : " + result.getMessageComplet());
    }

    @Test
    @Order(2)
    @DisplayName("Date création null — doit échouer")
    void testDateCreationNull() {
        Dossier d = dossierValide();
        d.setDateCreation(null);
        ValidationResult result = DossierValidator.valider(d);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("date")));
    }

    @Test
    @Order(3)
    @DisplayName(" Date création dans le futur — doit échouer")
    void testDateCreationFutur() {
        Dossier d = dossierValide();
        d.setDateCreation(Date.valueOf(LocalDate.now().plusDays(1)));
        ValidationResult result = DossierValidator.valider(d);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("futur")));
    }

    @Test
    @Order(4)
    @DisplayName("Niveau de risque null — doit échouer")
    void testNiveauRisqueNull() {
        Dossier d = dossierValide();
        d.setNiveauRisque(null);
        ValidationResult result = DossierValidator.valider(d);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("risque")));
    }

    @Test
    @Order(5)
    @DisplayName("Niveau de risque invalide — doit échouer")
    void testNiveauRisqueInvalide() {
        Dossier d = dossierValide();
        d.setNiveauRisque("extrême");
        ValidationResult result = DossierValidator.valider(d);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("invalide")));
    }

    @Test
    @Order(6)
    @DisplayName(" Tous les niveaux de risque valides passent")
    void testTousNiveauxRisqueValides() {
        for (String niveau : DossierValidator.NIVEAUX_RISQUE_VALIDES) {
            Dossier d = dossierValide();
            d.setNiveauRisque(niveau);
            ValidationResult result = DossierValidator.valider(d);
            assertTrue(result.isValide(),
                    "Niveau '" + niveau + "' devrait être valide. Erreurs : " + result.getMessageComplet());
        }
    }

    @Test
    @Order(7)
    @DisplayName(" Patient ID = 0 — doit échouer")
    void testPatientIdZero() {
        Dossier d = dossierValide();
        d.setPatientId(0);
        ValidationResult result = DossierValidator.valider(d);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("patient")));
    }

    @Test
    @Order(8)
    @DisplayName("Psychologue ID négatif — doit échouer")
    void testPsychologueIdNegatif() {
        Dossier d = dossierValide();
        d.setPsychologueId(-5);
        ValidationResult result = DossierValidator.valider(d);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("psychologue")));
    }

    @Test
    @Order(9)
    @DisplayName("Patient = Psychologue — doit échouer")
    void testPatientEgalPsychologue() {
        Dossier d = dossierValide();
        d.setPatientId(2);
        d.setPsychologueId(2); // même personne !
        ValidationResult result = DossierValidator.valider(d);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().stream().anyMatch(e -> e.contains("même personne")));
    }

    @Test
    @Order(10)
    @DisplayName("Dossier null — doit échouer immédiatement")
    void testDossierNull() {
        ValidationResult result = DossierValidator.valider(null);
        assertFalse(result.isValide());
        assertEquals(1, result.getErreurs().size());
    }

    @Test
    @Order(11)
    @DisplayName(" parseId — valeur valide")
    void testParseIdValide() {
        ValidationResult result = new ValidationResult();
        int id = DossierValidator.parseId("3", "Patient ID", result);
        assertEquals(3, id);
        assertTrue(result.isValide());
    }

    @Test
    @Order(12)
    @DisplayName("parseId — texte non numérique")
    void testParseIdInvalide() {
        ValidationResult result = new ValidationResult();
        int id = DossierValidator.parseId("abc", "Patient ID", result);
        assertEquals(-1, id);
        assertFalse(result.isValide());
    }

    @Test
    @Order(13)
    @DisplayName(" parseId — valeur négative")
    void testParseIdNegatif() {
        ValidationResult result = new ValidationResult();
        int id = DossierValidator.parseId("-1", "Patient ID", result);
        assertEquals(-1, id);
        assertFalse(result.isValide());
    }

    @Test
    @Order(14)
    @DisplayName(" Plusieurs erreurs accumulées correctement")
    void testMultiplesErreurs() {
        Dossier d = new Dossier();
        d.setDateCreation(null);       // erreur 1
        d.setNiveauRisque(null);       // erreur 2
        d.setPatientId(0);             // erreur 3
        d.setPsychologueId(0);         // erreur 4
        ValidationResult result = DossierValidator.valider(d);
        assertFalse(result.isValide());
        assertTrue(result.getErreurs().size() >= 4,
                "Doit avoir au moins 4 erreurs, en a : " + result.getErreurs().size());
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
    @DisplayName("🗄 add + getAll — le dossier ajouté est retrouvé")
    void testAddEtGetAll() {
        Dossier d = dossierValide();
        int avantAjout = service.getAll().size();
        service.add(d);
        int apresAjout = service.getAll().size();
        assertTrue(apresAjout > avantAjout,
                "Après add(), la liste doit contenir un élément de plus.");
    }

    @Test
    @Order(22)
    @DisplayName("🗄 find — retrouve un dossier existant")
    void testFind() {
        var list = service.getAll();
        if (list.isEmpty()) {
            System.out.println("[SKIP] Aucun dossier en BDD pour tester find().");
            return;
        }
        int id = list.get(0).getId();
        Dossier found = service.find(id);
        assertNotNull(found, "find() doit retourner le dossier avec l'id " + id);
        assertEquals(id, found.getId());
    }

    @Test
    @Order(23)
    @DisplayName("🗄 find — ID inexistant retourne null")
    void testFindIdInexistant() {
        Dossier found = service.find(Integer.MAX_VALUE);
        assertNull(found, "find() avec un ID inexistant doit retourner null.");
    }

    @Test
    @Order(24)
    @DisplayName("🗄 search — recherche par niveau de risque")
    void testSearch() {
        var results = service.search("faible");
        assertNotNull(results);
        results.forEach(d ->
                assertTrue(
                        (d.getNiveauRisque() != null && d.getNiveauRisque().toLowerCase().contains("faible")) ||
                                (d.getNotesGenerales() != null && d.getNotesGenerales().toLowerCase().contains("faible")),
                        "Chaque résultat doit contenir 'faible'."
                )
        );
    }

    @Test
    @Order(25)
    @DisplayName("🗄 sortByDate ASC — dates croissantes")
    void testSortByDateAsc() {
        var list = service.sortByDate(true);
        assertNotNull(list);
        for (int i = 0; i < list.size() - 1; i++) {
            Date d1 = list.get(i).getDateCreation();
            Date d2 = list.get(i + 1).getDateCreation();
            if (d1 != null && d2 != null) {
                assertTrue(d1.compareTo(d2) <= 0,
                        "Tri ASC : la date " + d1 + " doit être ≤ " + d2);
            }
        }
    }

    @Test
    @Order(26)
    @DisplayName("🗄 sortByDate DESC — dates décroissantes")
    void testSortByDateDesc() {
        var list = service.sortByDate(false);
        assertNotNull(list);
        for (int i = 0; i < list.size() - 1; i++) {
            Date d1 = list.get(i).getDateCreation();
            Date d2 = list.get(i + 1).getDateCreation();
            if (d1 != null && d2 != null) {
                assertTrue(d1.compareTo(d2) >= 0,
                        "Tri DESC : la date " + d1 + " doit être ≥ " + d2);
            }
        }
    }

    @Test
    @Order(27)
    @DisplayName("🗄 update — modification du niveau de risque")
    void testUpdate() {
        // Insérer un dossier
        Dossier d = dossierValide();
        service.add(d);
        var list = service.getAll();
        Dossier insere = list.get(list.size() - 1);

        // Modifier
        insere.setNiveauRisque("élevé");
        service.update(insere);

        // Vérifier
        Dossier modifie = service.find(insere.getId());
        if (modifie != null) {
            assertEquals("élevé", modifie.getNiveauRisque(),
                    "Le niveau de risque doit avoir été mis à jour.");
        }

        // Nettoyage
        service.delete(insere);
    }

    @Test
    @Order(28)
    @DisplayName("🗄 delete — suppression vérifiée")
    void testDelete() {
        Dossier d = dossierValide();
        service.add(d);
        var list = service.getAll();
        Dossier insere = list.get(list.size() - 1);
        int idASupprimer = insere.getId();

        service.delete(insere);

        Dossier apres = service.find(idASupprimer);
        assertNull(apres, "Le dossier supprimé ne doit plus être trouvable.");
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════════════════════════════
    private Dossier dossierValide() {
        Dossier d = new Dossier();
        d.setDateCreation(Date.valueOf(LocalDate.now()));
        d.setNiveauRisque("faible");
        d.setPatientId(1);
        d.setPsychologueId(2);
        d.setNotesGenerales(null); // admin ne touche pas aux notes
        d.setAiSummary(null);
        d.setAiKeyPoints(null);
        return d;
    }
}