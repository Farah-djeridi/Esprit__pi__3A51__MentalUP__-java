package services;

import models.Objectif;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ObjectifServiceTest {

    static ServiceObjectif service;
    static int idObjectifTest;

    @BeforeAll
    static void setup() {
        service = new ServiceObjectif();
    }

    @Test
    @Order(1)
    void testAjouterObjectif() {
        Objectif o = new Objectif();
        o.setDateDebut(Date.valueOf("2026-04-01"));
        o.setDateFin(Date.valueOf("2026-05-01"));
        o.setTitre("Objectif Test");
        o.setDescription("Description test objectif");
        o.setDateCreation(Date.valueOf("2026-04-01"));
        o.setStatutObjectif("En cours");
        o.setProgression(25);
        o.setTypeObjectif("Mental");
        o.setValeurCible(100.0);
        o.setIdActivite(null);
        o.setUserId(2);

        service.add(o);

        List<Objectif> objectifs = service.getAll();
        assertNotNull(objectifs);
        assertFalse(objectifs.isEmpty());

        Objectif inserted = objectifs.stream()
                .filter(obj -> "Objectif Test".equals(obj.getTitre()))
                .findFirst()
                .orElse(null);

        assertNotNull(inserted);
        idObjectifTest = inserted.getId();

        System.out.println("ID Objectif Test = " + idObjectifTest);
    }

    @Test
    @Order(2)
    void testModifierObjectif() {
        Objectif o = new Objectif();
        o.setId(idObjectifTest);
        o.setDateDebut(Date.valueOf("2026-04-02"));
        o.setDateFin(Date.valueOf("2026-06-01"));
        o.setTitre("Objectif Modifie");
        o.setDescription("Description modifiée");
        o.setDateCreation(Date.valueOf("2026-04-02"));
        o.setStatutObjectif("Atteint");
        o.setProgression(100);
        o.setTypeObjectif("Bien-être");
        o.setValeurCible(150.0);
        o.setIdActivite(null);
        o.setUserId(2);

        service.update(o);

        List<Objectif> objectifs = service.getAll();

        boolean trouve = objectifs.stream()
                .anyMatch(obj ->
                        obj.getId() == idObjectifTest &&
                                "Objectif Modifie".equals(obj.getTitre()) &&
                                "Atteint".equals(obj.getStatutObjectif())
                );

        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerObjectif() {
        Objectif o = new Objectif();
        o.setId(idObjectifTest);

        service.delete(o);

        List<Objectif> objectifs = service.getAll();

        boolean existe = objectifs.stream()
                .anyMatch(obj -> obj.getId() == idObjectifTest);

        assertFalse(existe);
    }
}