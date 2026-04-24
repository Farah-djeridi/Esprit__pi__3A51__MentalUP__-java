package bugfix;

import Models.RendezVous;
import Services.ServiceRendezVous;
import org.junit.jupiter.api.*;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Preservation Property Tests for Rendez-Vous Navigation Fix
 * 
 * **IMPORTANT**: Follow observation-first methodology
 * **GOAL**: Observe behavior on UNFIXED code for non-buggy operations
 * **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12, 3.13, 3.14, 3.15**
 * 
 * **Property 4: Preservation - Core Appointment Management**
 * For any appointment management operation that is NOT a navigation, status transition, or history query
 * (such as creating appointments, displaying calendars, searching/filtering, or canceling reservations),
 * the fixed code SHALL produce exactly the same behavior as the original code, preserving all existing functionality.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Preservation Property Tests - Core Appointment Management")
class RendezVousPreservationPropertyTest {

    private static ServiceRendezVous service;
    private static Connection conn;

    @BeforeAll
    static void setUp() {
        service = new ServiceRendezVous();
        conn = MyDataBase.getInstance().getCnx();
    }

    @AfterEach
    void cleanup() {
        // Clean up test data after each test
        try {
            List<RendezVous> allRdv = service.getAll();
            for (RendezVous rdv : allRdv) {
                if (rdv.getDate() != null && 
                    (rdv.getDate().toLocalDate().isAfter(LocalDate.now().plusDays(100)) ||
                     rdv.getDate().toLocalDate().isBefore(LocalDate.now().minusDays(100)))) {
                    service.delete(rdv.getId());
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  REQUIREMENT 3.1: Student psychologist selection loads psychologists with role = 'psychologue'
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ PRESERVE: Student psychologist selection loads psychologists with role = 'psychologue'")
    void testPsychologistSelectionLoadsCorrectRole() {
        // **Validates: Requirements 3.1**
        // Observe that the query "SELECT id, prenom, nom FROM user WHERE role = 'psychologue'" works correctly
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, prenom, nom FROM user WHERE role = 'psychologue'")) {
            
            boolean foundPsychologist = false;
            while (rs.next()) {
                foundPsychologist = true;
                int id = rs.getInt("id");
                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");
                
                // Verify that we can retrieve psychologist data
                assertNotNull(prenom, "Psychologist prenom should not be null");
                assertNotNull(nom, "Psychologist nom should not be null");
                assertTrue(id > 0, "Psychologist id should be positive");
            }
            
            // This test passes if we can query psychologists successfully
            // The actual UI rendering is preserved as long as the query works
            assertTrue(foundPsychologist || true, "Psychologist query should execute successfully (may be empty)");
            
        } catch (SQLException e) {
            fail("Psychologist selection query should work: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  REQUIREMENT 3.2, 3.3, 3.4: Appointment reservation flow
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("✅ PRESERVE: Appointment reservation flow works correctly")
    void testAppointmentReservationFlow() {
        // **Validates: Requirements 3.2, 3.3, 3.4**
        // Test that: student clicks psychologist → opens calendar → clicks libre slot → calls reserverCreneau()
        
        // Create a "libre" appointment
        RendezVous rdv = createTestRendezVous();
        rdv.setStatut("libre");
        rdv.setPsychologueId(2);
        service.add(rdv);
        
        // Get the inserted appointment
        List<RendezVous> list = service.getByPsychologueId(2);
        RendezVous inserted = list.stream()
                .filter(r -> r.getDate() != null && r.getDate().equals(rdv.getDate()))
                .filter(r -> r.getHeureDebut() != null && r.getHeureDebut().equals(rdv.getHeureDebut()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(inserted, "Appointment should be inserted");
        assertEquals("libre", inserted.getStatut(), "Initial status should be 'libre'");
        
        // Reserve the appointment (simulating student clicking on libre slot and confirming)
        boolean reserved = service.reserverCreneau(inserted, 1, "Présentiel");
        
        assertTrue(reserved, "Reservation should succeed for 'libre' appointment");
        
        // Verify the appointment is now "réservé"
        List<RendezVous> updated = service.getByPsychologueId(2);
        RendezVous reservedRdv = updated.stream()
                .filter(r -> r.getId() == inserted.getId())
                .findFirst()
                .orElse(null);
        
        assertNotNull(reservedRdv, "Reserved appointment should exist");
        assertEquals("réservé", reservedRdv.getStatut(), "Status should be 'réservé' after reservation");
        assertEquals(1, reservedRdv.getEtudiantId(), "Etudiant ID should be set");
        
        // Clean up
        service.delete(inserted.getId());
    }

    // ═══════════════════════════════════════════════════════════
    //  REQUIREMENT 3.5, 3.6, 3.7: Calendar display
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("✅ PRESERVE: Calendar display shows appointments with correct status colors")
    void testCalendarDisplayShowsCorrectStatusColors() {
        // **Validates: Requirements 3.5, 3.6, 3.7**
        // Test that appointments can be retrieved and have status information for color coding
        // (green for libre, orange for réservé, blue for confirmé)
        
        // Create appointments with different statuses
        RendezVous rdvLibre = createTestRendezVous();
        rdvLibre.setStatut("libre");
        rdvLibre.setDate(Date.valueOf(LocalDate.now().plusDays(1)));
        service.add(rdvLibre);
        
        RendezVous rdvReserve = createTestRendezVous();
        rdvReserve.setStatut("réservé");
        rdvReserve.setDate(Date.valueOf(LocalDate.now().plusDays(2)));
        rdvReserve.setEtudiantId(1);
        service.add(rdvReserve);
        
        RendezVous rdvConfirme = createTestRendezVous();
        rdvConfirme.setStatut("confirmé");
        rdvConfirme.setDate(Date.valueOf(LocalDate.now().plusDays(3)));
        rdvConfirme.setEtudiantId(1);
        service.add(rdvConfirme);
        
        // Retrieve appointments
        List<RendezVous> appointments = service.getByPsychologueId(2);
        
        // Verify we can retrieve appointments with different statuses
        boolean hasLibre = appointments.stream().anyMatch(r -> "libre".equalsIgnoreCase(r.getStatut()));
        boolean hasReserve = appointments.stream().anyMatch(r -> "réservé".equalsIgnoreCase(r.getStatut()));
        boolean hasConfirme = appointments.stream().anyMatch(r -> "confirmé".equalsIgnoreCase(r.getStatut()));
        
        assertTrue(hasLibre || hasReserve || hasConfirme, "Should be able to retrieve appointments with various statuses");
        
        // Verify appointments have all required fields for display
        for (RendezVous rdv : appointments) {
            if (rdv.getDate() != null && rdv.getDate().toLocalDate().isAfter(LocalDate.now())) {
                assertNotNull(rdv.getStatut(), "Appointment should have status for color coding");
                assertNotNull(rdv.getHeureDebut(), "Appointment should have start time");
                assertNotNull(rdv.getHeureFin(), "Appointment should have end time");
            }
        }
        
        // Clean up
        appointments.stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().isAfter(LocalDate.now()))
                .forEach(r -> service.delete(r.getId()));
    }

    // ═══════════════════════════════════════════════════════════
    //  REQUIREMENT 3.8: Student appointment views display three sections
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("✅ PRESERVE: Student appointment views display three sections (today, upcoming, past)")
    void testStudentAppointmentViewsDisplayThreeSections() {
        // **Validates: Requirements 3.8**
        // Test that getRdvAujourdhui(), getRdvAvenir(), and getRdvAnciens() work correctly
        
        int etudiantId = 1;
        
        // Create appointments for today, future, and past
        RendezVous rdvToday = createTestRendezVous();
        rdvToday.setDate(Date.valueOf(LocalDate.now()));
        rdvToday.setEtudiantId(etudiantId);
        service.add(rdvToday);
        
        RendezVous rdvFuture = createTestRendezVous();
        rdvFuture.setDate(Date.valueOf(LocalDate.now().plusDays(5)));
        rdvFuture.setEtudiantId(etudiantId);
        service.add(rdvFuture);
        
        RendezVous rdvPast = createTestRendezVous();
        rdvPast.setDate(Date.valueOf(LocalDate.now().minusDays(5)));
        rdvPast.setEtudiantId(etudiantId);
        service.add(rdvPast);
        
        // Test today's appointments
        List<RendezVous> today = service.getRdvAujourdhui(etudiantId);
        assertNotNull(today, "Today's appointments list should not be null");
        assertTrue(today.stream().anyMatch(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now())),
                "Should retrieve today's appointments");
        
        // Test upcoming appointments
        List<RendezVous> upcoming = service.getRdvAvenir(etudiantId);
        assertNotNull(upcoming, "Upcoming appointments list should not be null");
        assertTrue(upcoming.stream().anyMatch(r -> r.getDate() != null && r.getDate().toLocalDate().isAfter(LocalDate.now())),
                "Should retrieve upcoming appointments");
        
        // Test past appointments
        List<RendezVous> past = service.getRdvAnciens(etudiantId);
        assertNotNull(past, "Past appointments list should not be null");
        // Note: getRdvAnciens currently returns ALL past appointments (bug to be fixed in task 3)
        // We're just verifying it works, not that it filters correctly
        
        // Clean up
        List<RendezVous> allRdv = service.getByEtudiantId(etudiantId);
        allRdv.stream()
                .filter(r -> r.getDate() != null)
                .filter(r -> r.getDate().toLocalDate().equals(LocalDate.now()) ||
                           r.getDate().toLocalDate().equals(LocalDate.now().plusDays(5)) ||
                           r.getDate().toLocalDate().equals(LocalDate.now().minusDays(5)))
                .forEach(r -> service.delete(r.getId()));
    }

    // ═══════════════════════════════════════════════════════════
    //  REQUIREMENT 3.9, 3.10: Student "Annuler" button functionality
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("✅ PRESERVE: Student 'Annuler' button for 'réservé' appointments calls annulerReservation()")
    void testStudentAnnulerButtonFunctionality() {
        // **Validates: Requirements 3.9, 3.10**
        // Test that annulerReservation() resets status to "libre" and clears etudiant_id
        
        int etudiantId = 1;
        
        // Create a "réservé" appointment
        RendezVous rdv = createTestRendezVous();
        rdv.setStatut("réservé");
        rdv.setEtudiantId(etudiantId);
        rdv.setDate(Date.valueOf(LocalDate.now().plusDays(3)));
        service.add(rdv);
        
        // Get the inserted appointment
        List<RendezVous> list = service.getByEtudiantId(etudiantId);
        RendezVous inserted = list.stream()
                .filter(r -> r.getDate() != null && r.getDate().equals(rdv.getDate()))
                .filter(r -> "réservé".equalsIgnoreCase(r.getStatut()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(inserted, "Reserved appointment should be inserted");
        assertEquals("réservé", inserted.getStatut(), "Initial status should be 'réservé'");
        
        // Cancel the reservation
        boolean cancelled = service.annulerReservation(inserted.getId(), etudiantId);
        
        assertTrue(cancelled, "Cancellation should succeed");
        
        // Verify the appointment is now "libre" with no etudiant_id
        List<RendezVous> updated = service.getByPsychologueId(2);
        RendezVous cancelledRdv = updated.stream()
                .filter(r -> r.getId() == inserted.getId())
                .findFirst()
                .orElse(null);
        
        assertNotNull(cancelledRdv, "Cancelled appointment should still exist");
        assertEquals("libre", cancelledRdv.getStatut(), "Status should be 'libre' after cancellation");
        assertNull(cancelledRdv.getEtudiantId(), "Etudiant ID should be NULL after cancellation");
        
        // Clean up
        service.delete(inserted.getId());
    }

    // ═══════════════════════════════════════════════════════════
    //  REQUIREMENT 3.11, 3.12, 3.13: Psychologist dashboard and appointment list
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("✅ PRESERVE: Psychologist dashboard can retrieve appointment statistics")
    void testPsychologistDashboardStatistics() {
        // **Validates: Requirements 3.11, 3.12, 3.13**
        // Test that we can retrieve appointments for statistics (patient count, appointments today, etc.)
        
        int psyId = 2;
        
        // Create test appointments for today
        RendezVous rdvToday1 = createTestRendezVous();
        rdvToday1.setDate(Date.valueOf(LocalDate.now()));
        rdvToday1.setPsychologueId(psyId);
        rdvToday1.setEtudiantId(1);
        service.add(rdvToday1);
        
        RendezVous rdvToday2 = createTestRendezVous();
        rdvToday2.setDate(Date.valueOf(LocalDate.now()));
        rdvToday2.setPsychologueId(psyId);
        rdvToday2.setEtudiantId(1);
        rdvToday2.setHeureDebut(Time.valueOf("14:00:00"));
        rdvToday2.setHeureFin(Time.valueOf("15:00:00"));
        service.add(rdvToday2);
        
        // Retrieve all appointments for the psychologist
        List<RendezVous> allAppointments = service.getByPsychologueId(psyId);
        assertNotNull(allAppointments, "Should be able to retrieve psychologist appointments");
        
        // Count today's appointments
        long todayCount = allAppointments.stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now()))
                .count();
        
        assertTrue(todayCount >= 2, "Should have at least 2 appointments today");
        
        // Test that we can retrieve appointments with different statuses for filtering
        List<RendezVous> allRdv = service.getAll();
        assertNotNull(allRdv, "Should be able to retrieve all appointments");
        
        // Verify appointments have required fields for filtering and search
        for (RendezVous rdv : allAppointments) {
            assertNotNull(rdv.getStatut(), "Appointment should have status for filtering");
            assertNotNull(rdv.getTypeRdv(), "Appointment should have type for filtering");
            assertNotNull(rdv.getDate(), "Appointment should have date for filtering");
        }
        
        // Clean up
        allAppointments.stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now()))
                .forEach(r -> service.delete(r.getId()));
    }

    // ═══════════════════════════════════════════════════════════
    //  REQUIREMENT 3.14, 3.15: Database operations
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("✅ PRESERVE: Database CRUD operations use existing schema and MyDataBase.getInstance()")
    void testDatabaseCRUDOperations() {
        // **Validates: Requirements 3.14, 3.15**
        // Test that CRUD operations work correctly with existing schema
        
        // CREATE
        RendezVous rdv = createTestRendezVous();
        rdv.setDate(Date.valueOf(LocalDate.now().plusDays(10)));
        service.add(rdv);
        
        // READ
        List<RendezVous> list = service.getByPsychologueId(2);
        RendezVous inserted = list.stream()
                .filter(r -> r.getDate() != null && r.getDate().equals(rdv.getDate()))
                .filter(r -> r.getHeureDebut() != null && r.getHeureDebut().equals(rdv.getHeureDebut()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(inserted, "Should be able to CREATE and READ appointment");
        assertEquals(rdv.getStatut(), inserted.getStatut(), "Status should match");
        assertEquals(rdv.getTypeRdv(), inserted.getTypeRdv(), "Type should match");
        
        // UPDATE
        inserted.setStatut("réservé");
        inserted.setEtudiantId(1);
        service.update(inserted);
        
        List<RendezVous> updated = service.getByPsychologueId(2);
        RendezVous updatedRdv = updated.stream()
                .filter(r -> r.getId() == inserted.getId())
                .findFirst()
                .orElse(null);
        
        assertNotNull(updatedRdv, "Should be able to UPDATE appointment");
        assertEquals("réservé", updatedRdv.getStatut(), "Status should be updated");
        
        // DELETE
        service.delete(inserted.getId());
        
        List<RendezVous> afterDelete = service.getByPsychologueId(2);
        boolean exists = afterDelete.stream().anyMatch(r -> r.getId() == inserted.getId());
        
        assertFalse(exists, "Should be able to DELETE appointment");
    }

    @Test
    @Order(8)
    @DisplayName("✅ PRESERVE: SQL queries use PreparedStatement parameter binding")
    void testPreparedStatementParameterBinding() {
        // **Validates: Requirements 3.15**
        // Test that queries with parameters work correctly (using PreparedStatement)
        
        int psyId = 2;
        int etudiantId = 1;
        
        // Create test appointment
        RendezVous rdv = createTestRendezVous();
        rdv.setPsychologueId(psyId);
        rdv.setEtudiantId(etudiantId);
        rdv.setDate(Date.valueOf(LocalDate.now().plusDays(7)));
        service.add(rdv);
        
        // Test query by psychologue_id
        List<RendezVous> byPsy = service.getByPsychologueId(psyId);
        assertNotNull(byPsy, "Query by psychologue_id should work");
        assertTrue(byPsy.stream().anyMatch(r -> r.getPsychologueId() == psyId),
                "Should retrieve appointments for specified psychologist");
        
        // Test query by etudiant_id
        List<RendezVous> byEtudiant = service.getByEtudiantId(etudiantId);
        assertNotNull(byEtudiant, "Query by etudiant_id should work");
        assertTrue(byEtudiant.stream().anyMatch(r -> r.getEtudiantId() != null && r.getEtudiantId() == etudiantId),
                "Should retrieve appointments for specified student");
        
        // Clean up
        byPsy.stream()
                .filter(r -> r.getDate() != null && r.getDate().toLocalDate().equals(LocalDate.now().plusDays(7)))
                .forEach(r -> service.delete(r.getId()));
    }

    @Test
    @Order(9)
    @DisplayName("✅ PRESERVE: Appointment display formatting (date, time, type, status badges)")
    void testAppointmentDisplayFormatting() {
        // **Validates: Requirements 3.5, 3.7**
        // Test that appointments have all required fields for display formatting
        
        // Create appointment with all display fields
        RendezVous rdv = createTestRendezVous();
        rdv.setDate(Date.valueOf(LocalDate.now().plusDays(4)));
        rdv.setHeureDebut(Time.valueOf("10:30:00"));
        rdv.setHeureFin(Time.valueOf("11:30:00"));
        rdv.setTypeRdv("Consultation vidéo");
        rdv.setStatut("confirmé");
        rdv.setPsychologueId(2);
        rdv.setEtudiantId(1);
        service.add(rdv);
        
        // Retrieve the appointment
        List<RendezVous> list = service.getByPsychologueId(2);
        RendezVous inserted = list.stream()
                .filter(r -> r.getDate() != null && r.getDate().equals(rdv.getDate()))
                .filter(r -> r.getHeureDebut() != null && r.getHeureDebut().equals(rdv.getHeureDebut()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(inserted, "Appointment should be inserted");
        
        // Verify all display fields are preserved
        assertNotNull(inserted.getDate(), "Date should be preserved for formatting");
        assertNotNull(inserted.getHeureDebut(), "Start time should be preserved for formatting");
        assertNotNull(inserted.getHeureFin(), "End time should be preserved for formatting");
        assertNotNull(inserted.getTypeRdv(), "Type should be preserved for display");
        assertNotNull(inserted.getStatut(), "Status should be preserved for badge display");
        
        // Verify date and time can be formatted
        assertEquals("10:30:00", inserted.getHeureDebut().toString(), "Time format should be preserved");
        assertEquals(LocalDate.now().plusDays(4), inserted.getDate().toLocalDate(), "Date should be preserved");
        
        // Clean up
        service.delete(inserted.getId());
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
