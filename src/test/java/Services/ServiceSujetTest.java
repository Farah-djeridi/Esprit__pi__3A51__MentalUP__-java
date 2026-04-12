package Services;

import models.Sujet;
import services.ServiceSujet;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceSujetTest {

    private static ServiceSujet service;
    private static int sujetId;

    @BeforeAll
    public static void setup() {
        service = new ServiceSujet();
    }

    // 🔹 TEST ADD
    @Test
    @Order(1)
    public void testAdd() {
        Sujet s = new Sujet("JUnit Sujet", "Contenu test JUnit", false, 2);

        service.add(s);

        assertTrue(s.getId() > 0, "ID doit être généré");
        sujetId = s.getId(); // sauvegarde pour les autres tests
    }

    // 🔹 TEST GET BY ID
    @Test
    @Order(2)
    public void testGetById() {
        Sujet s = service.getById(sujetId);

        assertNotNull(s, "Sujet ne doit pas être null");
        assertEquals("JUnit Sujet", s.getTitre());
        assertEquals("Contenu test JUnit", s.getContenu());
    }

    // 🔹 TEST GET ALL
    @Test
    @Order(3)
    public void testGetAll() {
        List<Sujet> list = service.getAll();

        assertNotNull(list);
        assertTrue(list.size() > 0, "La liste doit contenir au moins un sujet");
    }

    // 🔹 TEST UPDATE
    @Test
    @Order(4)
    public void testUpdate() {
        Sujet s = service.getById(sujetId);

        s.setTitre("Titre modifié");
        s.setContenu("Contenu modifié");

        service.update(s);

        Sujet updated = service.getById(sujetId);

        assertEquals("Titre modifié", updated.getTitre());
        assertEquals("Contenu modifié", updated.getContenu());
    }

    // 🔹 TEST INCREMENT VUES
    @Test
    @Order(5)
    public void testIncrementVues() {
        Sujet s = service.getById(sujetId);
        int oldViews = s.getNbVues();

        service.incrementVues(sujetId);

        Sujet updated = service.getById(sujetId);

        assertEquals(oldViews + 1, updated.getNbVues());
    }

    // 🔹 TEST GET BY USER
    @Test
    @Order(6)
    public void testGetByUser() {
        List<Sujet> list = service.getSujetsByUser(2);

        assertNotNull(list);
        assertTrue(list.size() >= 0);
    }

    // 🔹 TEST DELETE
    @Test
    @Order(7)
    public void testDelete() {
        Sujet s = service.getById(sujetId);

        service.delete(s);

        Sujet deleted = service.getById(sujetId);

        assertNull(deleted, "Sujet doit être supprimé");
    }
}