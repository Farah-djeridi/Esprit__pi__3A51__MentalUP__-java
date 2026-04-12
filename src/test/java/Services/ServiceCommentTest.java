package Services;

import models.Commentaire;
import services.ServiceCommentaire;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceCommentTest {

    private static ServiceCommentaire service;
    private static int commentaireId;
    private static final int USER_ID = 2;
    private static final int SUJET_ID = 4;

    @BeforeAll
    public static void setup() {
        service = new ServiceCommentaire();
    }

    // 🔹 TEST ADD
    @Test
    @Order(1)
    public void testAdd() {
        Commentaire c = new Commentaire();
        c.setContenu("Commentaire JUnit test");
        c.setAnonyme(false);
        c.setScoreToxicite(0.1);
        c.setEstToxique(false);
        c.setUserId(USER_ID);
        c.setSujetId(SUJET_ID);

        service.add(c);

        assertTrue(c.getId() > 0, "ID commentaire doit être généré");
        commentaireId = c.getId();
    }

    // 🔹 TEST GET BY ID
    @Test
    @Order(2)
    public void testGetById() {
        Commentaire c = service.getById(commentaireId);

        assertNotNull(c, "Commentaire ne doit pas être null");
        assertEquals("Commentaire JUnit test", c.getContenu());
        assertEquals(USER_ID, c.getUserId());
        assertEquals(SUJET_ID, c.getSujetId());
    }

    // 🔹 TEST GET ALL
    @Test
    @Order(3)
    public void testGetAll() {
        List<Commentaire> list = service.getAll();

        assertNotNull(list);
        assertTrue(list.size() > 0, "La liste doit contenir au moins un commentaire");
    }

    // 🔹 TEST GET BY SUJET
    @Test
    @Order(4)
    public void testGetBySujetId() {
        List<Commentaire> list = service.getBySujetId(SUJET_ID);

        assertNotNull(list);
        assertTrue(list.size() >= 0);
    }

    // 🔹 TEST GET BY USER
    @Test
    @Order(5)
    public void testGetByUserId() {
        List<Commentaire> list = service.getByUserId(USER_ID);

        assertNotNull(list);
        assertTrue(list.size() >= 0);
    }

    // 🔹 TEST UPDATE
    @Test
    @Order(6)
    public void testUpdate() {
        Commentaire c = service.getById(commentaireId);

        c.setContenu("Commentaire modifié JUnit");
        c.setNbLikes(5);
        c.setNbDislikes(1);

        service.update(c);

        Commentaire updated = service.getById(commentaireId);

        assertEquals("Commentaire modifié JUnit", updated.getContenu());
        assertEquals(5, updated.getNbLikes());
        assertEquals(1, updated.getNbDislikes());
    }

    // 🔹 TEST UPDATE LIKES
    @Test
    @Order(7)
    public void testUpdateLikes() {
        service.updateLikes(commentaireId, 10);

        Commentaire c = service.getById(commentaireId);

        assertEquals(10, c.getNbLikes());
    }

    // 🔹 TEST UPDATE DISLIKES
    @Test
    @Order(8)
    public void testUpdateDislikes() {
        service.updateDislikes(commentaireId, 3);

        Commentaire c = service.getById(commentaireId);

        assertEquals(3, c.getNbDislikes());
    }

    // 🔹 TEST COUNT
    @Test
    @Order(9)
    public void testCountBySujet() {
        int count = service.countBySujetId(SUJET_ID);

        assertTrue(count >= 0);
    }

    // 🔹 TEST DELETE
    @Test
    @Order(10)
    public void testDelete() {
        Commentaire c = service.getById(commentaireId);

        service.delete(c);

        Commentaire deleted = service.getById(commentaireId);

        assertNull(deleted, "Commentaire doit être supprimé");
    }
}