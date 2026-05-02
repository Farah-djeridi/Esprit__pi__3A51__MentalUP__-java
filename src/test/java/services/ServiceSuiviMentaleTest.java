package services;

import models.SuiviMentale;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceSuiviMentaleTest {

    static ServiceSuiviMentale service;
    static int idSuiviTest;

    @BeforeAll
    static void setup() {
        service = new ServiceSuiviMentale();
    }

    @Test
    @Order(1)
    void testAjouterSuiviMentale() {
        SuiviMentale s = new SuiviMentale();
        s.setScoreMentale(70);
        s.setTauxDeStress(4);
        s.setTauxDeStressGlobale(5);
        s.setDateDeSuivi(Date.valueOf("2026-04-10"));
        s.setQualiteDuSommeil("Bonne");
        s.setJournalEmotionnelle("Je me sens bien aujourd'hui");
        s.setHeureDeSommeil(7.5);
        s.setHumeur("motivé");
        s.setNiveauDenergie(8);
        s.setUserId(2);
        s.setObjectifId(1);

        service.add(s);

        List<SuiviMentale> suivis = service.getAll();
        assertNotNull(suivis);
        assertFalse(suivis.isEmpty());

        SuiviMentale inserted = suivis.stream()
                .filter(suivi ->
                        "motivé".equalsIgnoreCase(suivi.getHumeur()) &&
                                suivi.getUserId() == 2 &&
                                suivi.getObjectifId() == 1
                )
                .findFirst()
                .orElse(null);

        assertNotNull(inserted);
        idSuiviTest = inserted.getId();

        System.out.println("ID Suivi Test = " + idSuiviTest);
    }

    @Test
    @Order(2)
    void testGetById() {
        SuiviMentale suivi = service.getById(idSuiviTest);

        assertNotNull(suivi);
        assertEquals(idSuiviTest, suivi.getId());
    }

    @Test
    @Order(3)
    void testGetByUser() {
        List<SuiviMentale> suivis = service.getByUser(2);

        assertNotNull(suivis);
        assertFalse(suivis.isEmpty());

        boolean existe = suivis.stream()
                .anyMatch(s -> s.getUserId() == 2);

        assertTrue(existe);
    }

    @Test
    @Order(4)
    void testGetByObjectif() {
        List<SuiviMentale> suivis = service.getByObjectif(1);

        assertNotNull(suivis);
        assertFalse(suivis.isEmpty());

        boolean existe = suivis.stream()
                .anyMatch(s -> s.getObjectifId() == 1);

        assertTrue(existe);
    }

    @Test
    @Order(5)
    void testModifierSuiviMentale() {
        SuiviMentale s = new SuiviMentale();
        s.setId(idSuiviTest);
        s.setScoreMentale(80);
        s.setTauxDeStress(2);
        s.setTauxDeStressGlobale(3);
        s.setDateDeSuivi(Date.valueOf("2026-04-11"));
        s.setQualiteDuSommeil("Excellente");
        s.setJournalEmotionnelle("Je me sens beaucoup mieux");
        s.setHeureDeSommeil(8.0);
        s.setHumeur("heureux");
        s.setNiveauDenergie(9);
        s.setUserId(2);
        s.setObjectifId(1);

        service.update(s);

        SuiviMentale updated = service.getById(idSuiviTest);

        assertNotNull(updated);
        assertEquals("heureux", updated.getHumeur());
        assertEquals(80, updated.getScoreMentale());
        assertEquals(2, updated.getTauxDeStress());
        assertEquals(3, updated.getTauxDeStressGlobale());
    }

    @Test
    @Order(6)
    void testSupprimerSuiviMentale() {
        SuiviMentale s = new SuiviMentale();
        s.setId(idSuiviTest);

        service.delete(s);

        SuiviMentale deleted = service.getById(idSuiviTest);
        assertNull(deleted);
    }
}