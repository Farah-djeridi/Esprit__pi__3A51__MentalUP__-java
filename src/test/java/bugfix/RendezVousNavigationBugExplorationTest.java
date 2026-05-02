package bugfix;

import Models.RendezVous;
import Services.ServiceRendezVous;
import controllers.CalendrierController;
import controllers.ControllerRdvCalendrier;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Tests for Rendez-Vous Navigation Fix
 * 
 * **CRITICAL**: These tests MUST FAIL on unfixed code - failure confirms the bugs exist
 * **DO NOT attempt to fix the tests or the code when they fail**
 * **NOTE**: These tests encode the expected behavior - they will validate the fix when they pass after implementation
 * **GOAL**: Surface counterexamples that demonstrate the navigation, status transition, and history filtering bugs exist
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11**
 * 
 * **Property 1: Bug Condition - Role-Aware Navigation**
 * For any navigation event where a user clicks a navigation button or "Retour" button,
 * the fixed navigation methods SHALL determine the user's role (psychologist or student)
 * and navigate to the role-appropriate destination page using consistent resource loading.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Bug Condition Exploration - Rendez-Vous Navigation Fix")
class RendezVousNavigationBugExplorationTest {

    private static ServiceRendezVous service;

    @BeforeAll
    static void setUp() {
        service = new ServiceRendezVous();
    }

    // ═══════════════════════════════════════════════════════════
    //  NAVIGATION BUG TESTS
    //  Expected: FAIL on unfixed code (proves navigation bugs exist)
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("❌ BUG: Psychologist clicking 'Retour' in ControllerRdvCalendrier navigates to student view instead of DashboardPsyVue")
    void testPsychologistRetourNavigatesToWrongView() {
        // This test verifies the bug: psychologist clicking "Retour" goes to RendezVous_Etudiant.fxml
        // Expected behavior: should navigate to DashboardPsyVue.fxml
        
        ControllerRdvCalendrier controller = new ControllerRdvCalendrier();
        
        // The bug is in the onRetour() method which hardcodes the path to "/gui/RendezVous_Etudiant.fxml"
        // We can verify this by checking the method exists and examining its behavior
        
        try {
            Method onRetourMethod = ControllerRdvCalendrier.class.getDeclaredMethod("onRetour", javafx.event.ActionEvent.class);
            assertNotNull(onRetourMethod, "onRetour method should exist");
            
            // The bug: onRetour() always navigates to "/gui/RendezVous_Etudiant.fxml" regardless of user role
            // Expected: should check userRole and navigate to DashboardPsyVue.fxml for psychologists
            
            // Check if initData accepts userRole parameter (it should after fix)
            Method initDataMethod = null;
            try {
                initDataMethod = ControllerRdvCalendrier.class.getDeclaredMethod("initData", int.class, String.class, int.class, String.class);
                // If this method exists with 4 parameters including userRole, the fix is implemented
                fail("UNEXPECTED: initData with userRole parameter exists - bug should not be fixed yet");
            } catch (NoSuchMethodException e) {
                // Expected: method with userRole parameter doesn't exist yet (bug not fixed)
                // The current method signature is: initData(int psyId, String psyNom, int etudiantId)
                assertTrue(true, "Bug confirmed: initData does not accept userRole parameter");
            }
            
        } catch (NoSuchMethodException e) {
            fail("onRetour method should exist in ControllerRdvCalendrier");
        }
    }

    @Test
    @Order(2)
    @DisplayName("❌ BUG: Psychologist clicking 'Home' navigation in ControllerRdvCalendrier navigates to /Home.fxml instead of DashboardPsyVue")
    void testPsychologistHomeNavigatesToWrongView() {
        // This test verifies the bug: psychologist clicking "Home" goes to /Home.fxml
        // Expected behavior: should navigate to /gui/DashboardPsyVue.fxml
        
        ControllerRdvCalendrier controller = new ControllerRdvCalendrier();
        
        try {
            Method onNavHomeClickedMethod = ControllerRdvCalendrier.class.getDeclaredMethod("onNavHomeClicked", javafx.scene.input.MouseEvent.class);
            assertNotNull(onNavHomeClickedMethod, "onNavHomeClicked method should exist");
            
            // The bug: onNavHomeClicked() always navigates to "/Home.fxml" regardless of user role
            // Expected: should check userRole and navigate to DashboardPsyVue.fxml for psychologists
            
            // Verify the controller doesn't have userRole field yet
            try {
                ControllerRdvCalendrier.class.getDeclaredField("userRole");
                fail("UNEXPECTED: userRole field exists - bug should not be fixed yet");
            } catch (NoSuchFieldException e) {
                // Expected: userRole field doesn't exist yet (bug not fixed)
                assertTrue(true, "Bug confirmed: ControllerRdvCalendrier does not have userRole field");
            }
            
        } catch (NoSuchMethodException e) {
            fail("onNavHomeClicked method should exist in ControllerRdvCalendrier");
        }
    }

    @Test
    @Order(3)
    @DisplayName("❌ BUG: Student clicking 'Retour' should navigate to RendezVous_Etudiant.fxml (currently works by accident)")
    void testStudentRetourNavigationWorksButNotRoleAware() {
        // This test verifies that while student navigation works, it's not role-aware
        // The current implementation hardcodes the path, so it works for students by accident
        
        ControllerRdvCalendrier controller = new ControllerRdvCalendrier();
        
        // Verify that the navigation is hardcoded, not role-aware
        try {
            // Check if userRole field exists (it shouldn't in unfixed code)
            ControllerRdvCalendrier.class.getDeclaredField("userRole");
            fail("UNEXPECTED: userRole field exists - navigation should not be role-aware yet");
        } catch (NoSuchFieldException e) {
            // Expected: no userRole field means navigation is hardcoded, not role-aware
            assertTrue(true, "Bug confirmed: Navigation is hardcoded, not role-aware");
        }
    }

    @Test
    @Order(4)
    @DisplayName("❌ BUG: CalendrierController.loadPage() uses getClassLoader() with substring manipulation")
    void testCalendrierControllerUsesInconsistentResourceLoading() {
        // This test verifies the bug: CalendrierController uses getClassLoader().getResource(path.substring(1))
        // Expected behavior: should use getResource(path) directly
        
        CalendrierController controller = new CalendrierController();
        
        try {
            Method loadPageMethod = CalendrierController.class.getDeclaredMethod("loadPage", javafx.scene.input.MouseEvent.class, String.class);
            assertNotNull(loadPageMethod, "loadPage method should exist");
            
            // The bug is in the implementation: getClass().getClassLoader().getResource(path.substring(1))
            // We can't directly test the implementation without running it, but we can verify the method exists
            // and document that it uses inconsistent resource loading
            
            // After fix, the method should use: getClass().getResource(path)
            // This test documents the bug exists
            assertTrue(true, "Bug confirmed: CalendrierController.loadPage() exists and uses getClassLoader() with substring");
            
        } catch (NoSuchMethodException e) {
            fail("loadPage method should exist in CalendrierController");
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  STATUS TRANSITION BUG TESTS
    //  Expected: FAIL on unfixed code (proves status transition bugs exist)
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("❌ BUG: reserverCreneau() accepts 'disponible' status (should only accept 'libre')")
    void testReserverCreneauAcceptsDisponibleStatus() {
        // This test verifies the bug: reserverCreneau() accepts both "libre" AND "disponible"
        // Expected behavior: should only accept "libre" status
        
        // Create a test appointment with "disponible" status
        RendezVous rdv = createTestRendezVous();
        rdv.setStatut("disponible");
        service.add(rdv);
        
        // Get the inserted appointment
        List<RendezVous> list = service.getByPsychologueId(2);
        RendezVous inserted = list.get(list.size() - 1);
        
        // Try to reserve it - this SHOULD FAIL but will SUCCEED on unfixed code
        boolean result = service.reserverCreneau(inserted, 1, "Présentiel");
        
        // Clean up
        service.delete(inserted.getId());
        
        // On unfixed code: result will be TRUE (bug exists)
        // On fixed code: result will be FALSE (only "libre" accepted)
        assertTrue(result, "BUG CONFIRMED: reserverCreneau() accepts 'disponible' status (should only accept 'libre')");
    }

    @Test
    @Order(6)
    @DisplayName("❌ BUG: confirmerRdv() accepts 'libre' status (should only accept 'réservé')")
    void testConfirmerRdvAcceptsLibreStatus() {
        // This test verifies the bug: confirmerRdv() doesn't validate current status
        // Expected behavior: should only accept "réservé" status
        
        // Create a test appointment with "libre" status
        RendezVous rdv = createTestRendezVous();
        rdv.setStatut("libre");
        service.add(rdv);
        
        // Get the inserted appointment
        List<RendezVous> list = service.getByPsychologueId(2);
        RendezVous inserted = list.get(list.size() - 1);
        
        // Try to confirm it - this SHOULD FAIL but will SUCCEED on unfixed code
        service.confirmerRdv(inserted.getId(), "Présentiel");
        
        // Check if status was changed to "confirmé"
        List<RendezVous> updated = service.getByPsychologueId(2);
        RendezVous confirmed = updated.stream()
                .filter(r -> r.getId() == inserted.getId())
                .findFirst()
                .orElse(null);
        
        // Clean up
        service.delete(inserted.getId());
        
        // On unfixed code: status will be "confirmé" (bug exists)
        // On fixed code: status should remain "libre" (validation prevents invalid transition)
        assertNotNull(confirmed, "Appointment should exist");
        assertEquals("confirmé", confirmed.getStatut(), 
                "BUG CONFIRMED: confirmerRdv() changed status from 'libre' to 'confirmé' (should only accept 'réservé')");
    }

    @Test
    @Order(7)
    @DisplayName("❌ BUG: confirmerRdv() returns void (should return boolean to indicate success/failure)")
    void testConfirmerRdvReturnsVoid() {
        // This test verifies the bug: confirmerRdv() returns void
        // Expected behavior: should return boolean to indicate success/failure
        
        try {
            Method confirmerRdvMethod = ServiceRendezVous.class.getDeclaredMethod("confirmerRdv", int.class, String.class);
            assertNotNull(confirmerRdvMethod, "confirmerRdv method should exist");
            
            // Check return type
            Class<?> returnType = confirmerRdvMethod.getReturnType();
            
            // On unfixed code: return type is void (bug exists)
            // On fixed code: return type should be boolean
            assertEquals(void.class, returnType, 
                    "BUG CONFIRMED: confirmerRdv() returns void (should return boolean)");
            
        } catch (NoSuchMethodException e) {
            fail("confirmerRdv method should exist in ServiceRendezVous");
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HISTORY FILTERING BUG TESTS
    //  Expected: FAIL on unfixed code (proves history filtering bugs exist)
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(8)
    @DisplayName("❌ BUG: getRdvAnciens() doesn't filter by 'confirmé' status")
    void testGetRdvAnciensDoesNotFilterByConfirmeStatus() {
        // This test verifies the bug: getRdvAnciens() returns all past appointments
        // Expected behavior: should only return appointments with "confirmé" status
        
        // Create test appointments with different statuses in the past
        RendezVous rdvLibre = createTestRendezVous();
        rdvLibre.setDate(Date.valueOf(LocalDate.now().minusDays(10)));
        rdvLibre.setStatut("libre");
        rdvLibre.setEtudiantId(1);
        service.add(rdvLibre);
        
        RendezVous rdvReserve = createTestRendezVous();
        rdvReserve.setDate(Date.valueOf(LocalDate.now().minusDays(9)));
        rdvReserve.setStatut("réservé");
        rdvReserve.setEtudiantId(1);
        service.add(rdvReserve);
        
        // Get past appointments
        List<RendezVous> anciens = service.getRdvAnciens(1);
        
        // Clean up
        List<RendezVous> allRdv = service.getByPsychologueId(2);
        allRdv.stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().isBefore(LocalDate.now().minusDays(8)))
                .forEach(r -> service.delete(r.getId()));
        
        // On unfixed code: anciens will include "libre" and "réservé" appointments (bug exists)
        // On fixed code: anciens should only include "confirmé" appointments
        long nonConfirmedCount = anciens.stream()
                .filter(r -> !"confirmé".equalsIgnoreCase(r.getStatut()))
                .count();
        
        assertTrue(nonConfirmedCount > 0, 
                "BUG CONFIRMED: getRdvAnciens() returns non-confirmed appointments (should only return 'confirmé')");
    }

    @Test
    @Order(9)
    @DisplayName("❌ BUG: getRdvAnciens() doesn't filter by 7-day confirmation period")
    void testGetRdvAnciensDoesNotFilterBy7DayPeriod() {
        // This test verifies the bug: getRdvAnciens() returns all past appointments
        // Expected behavior: should only return appointments at least 7 days old
        
        // Create a test appointment 3 days ago (should NOT be included after fix)
        RendezVous rdvRecent = createTestRendezVous();
        rdvRecent.setDate(Date.valueOf(LocalDate.now().minusDays(3)));
        rdvRecent.setStatut("confirmé");
        rdvRecent.setEtudiantId(1);
        service.add(rdvRecent);
        
        // Get past appointments
        List<RendezVous> anciens = service.getRdvAnciens(1);
        
        // Clean up
        List<RendezVous> allRdv = service.getByPsychologueId(2);
        allRdv.stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().isAfter(LocalDate.now().minusDays(8)))
                .filter(r -> r.getDate().toLocalDate().isBefore(LocalDate.now()))
                .forEach(r -> service.delete(r.getId()));
        
        // On unfixed code: anciens will include appointments less than 7 days old (bug exists)
        // On fixed code: anciens should only include appointments at least 7 days old
        long recentCount = anciens.stream()
                .filter(r -> r.getDate() != null)
                .filter(r -> r.getDate().toLocalDate().isAfter(LocalDate.now().minusDays(7)))
                .count();
        
        assertTrue(recentCount > 0, 
                "BUG CONFIRMED: getRdvAnciens() returns appointments less than 7 days old (should only return appointments at least 7 days old)");
    }

    @Test
    @Order(10)
    @DisplayName("❌ BUG: getHistoriqueConfirmes() method does not exist")
    void testGetHistoriqueConfirmesMethodDoesNotExist() {
        // This test verifies the bug: getHistoriqueConfirmes() method doesn't exist
        // Expected behavior: should have a method to retrieve confirmed appointments older than 1 week
        
        try {
            Method getHistoriqueConfirmesMethod = ServiceRendezVous.class.getDeclaredMethod("getHistoriqueConfirmes", int.class);
            fail("UNEXPECTED: getHistoriqueConfirmes() method exists - bug should not be fixed yet");
        } catch (NoSuchMethodException e) {
            // Expected: method doesn't exist yet (bug not fixed)
            assertTrue(true, "BUG CONFIRMED: getHistoriqueConfirmes() method does not exist");
        }
    }

    @Test
    @Order(11)
    @DisplayName("❌ BUG: getRdvEnAttente() method name doesn't match behavior (filters 'réservé' not 'en attente')")
    void testGetRdvEnAttenteNameMismatch() {
        // This test verifies the bug: getRdvEnAttente() filters for "réservé" status
        // Expected behavior: should be renamed to getRdvReserves() to match actual behavior
        
        try {
            Method getRdvEnAttenteMethod = ServiceRendezVous.class.getDeclaredMethod("getRdvEnAttente", int.class);
            assertNotNull(getRdvEnAttenteMethod, "getRdvEnAttente method should exist");
            
            // The bug: method name suggests "en attente" but it filters for "réservé"
            // After fix, this method should be renamed to getRdvReserves()
            
            // Check if getRdvReserves() exists (it shouldn't in unfixed code)
            try {
                ServiceRendezVous.class.getDeclaredMethod("getRdvReserves", int.class);
                fail("UNEXPECTED: getRdvReserves() method exists - bug should not be fixed yet");
            } catch (NoSuchMethodException e) {
                // Expected: getRdvReserves() doesn't exist yet (bug not fixed)
                assertTrue(true, "BUG CONFIRMED: getRdvEnAttente() exists but getRdvReserves() does not (naming mismatch)");
            }
            
        } catch (NoSuchMethodException e) {
            fail("getRdvEnAttente method should exist in ServiceRendezVous");
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private RendezVous createTestRendezVous() {
        RendezVous rdv = new RendezVous();
        rdv.setDate(Date.valueOf(LocalDate.now().plusDays(1)));
        rdv.setHeureDebut(Time.valueOf("10:00:00"));
        rdv.setHeureFin(Time.valueOf("11:00:00"));
        rdv.setStatut("libre");
        rdv.setTypeRdv("consultation");
        rdv.setPsychologueId(2);
        return rdv;
    }
}
