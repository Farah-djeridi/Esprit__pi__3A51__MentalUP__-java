package validators;

import Models.Dossier;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DossierValidator {

    public static final List<String> NIVEAUX_RISQUE_VALIDES = List.of("faible", "moyen", "élevé");

    // ═══════════════════════════════════════════════════════
    //  RÉSULTAT DE VALIDATION (partagé avec RendezVousValidator)
    // ═══════════════════════════════════════════════════════
    public static class ValidationResult {
        private final List<String> erreurs = new ArrayList<>();

        public void addErreur(String message) { erreurs.add(message); }
        public boolean isValide()             { return erreurs.isEmpty(); }
        public List<String> getErreurs()      { return erreurs; }

        public String getMessageComplet() {
            return String.join("\n", erreurs);
        }

        @Override
        public String toString() {
            return isValide() ? "✅ Valide" : "❌ " + getMessageComplet();
        }
    }

    // ═══════════════════════════════════════════════════════
    //  VALIDATION COMPLÈTE
    // ═══════════════════════════════════════════════════════
    public static ValidationResult valider(Dossier d) {
        ValidationResult result = new ValidationResult();

        if (d == null) {
            result.addErreur("Le dossier ne peut pas être null.");
            return result;
        }

        validerDateCreation(d.getDateCreation(), result);
        validerNiveauRisque(d.getNiveauRisque(), result);
        validerPatientId(d.getPatientId(), result);
        validerPsychologueId(d.getPsychologueId(), result);
        validerIdsDistincts(d.getPatientId(), d.getPsychologueId(), result);

        return result;
    }

    // ═══════════════════════════════════════════════════════
    //  VALIDATIONS INDIVIDUELLES
    // ═══════════════════════════════════════════════════════

    /** La date de création ne peut pas être dans le futur */
    public static void validerDateCreation(Date date, ValidationResult result) {
        if (date == null) {
            result.addErreur("La date de création est obligatoire.");
            return;
        }
        if (date.toLocalDate().isAfter(LocalDate.now())) {
            result.addErreur("La date de création ne peut pas être dans le futur.");
        }
    }

    /** Niveau de risque doit être parmi les valeurs autorisées */
    public static void validerNiveauRisque(String niveauRisque, ValidationResult result) {
        if (niveauRisque == null || niveauRisque.isBlank()) {
            result.addErreur("Le niveau de risque est obligatoire.");
            return;
        }
        if (!NIVEAUX_RISQUE_VALIDES.contains(niveauRisque.toLowerCase())) {
            result.addErreur("Niveau de risque invalide : '" + niveauRisque + "'. Valeurs acceptées : " + NIVEAUX_RISQUE_VALIDES);
        }
    }

    /** L'ID du patient doit être > 0 */
    public static void validerPatientId(int patientId, ValidationResult result) {
        if (patientId <= 0) {
            result.addErreur("L'identifiant du patient doit être supérieur à 0.");
        }
    }

    /** L'ID du psychologue doit être > 0 */
    public static void validerPsychologueId(int psychologueId, ValidationResult result) {
        if (psychologueId <= 0) {
            result.addErreur("L'identifiant du psychologue doit être supérieur à 0.");
        }
    }

    /** Le patient et le psychologue ne peuvent pas être le même utilisateur */
    public static void validerIdsDistincts(int patientId, int psychologueId, ValidationResult result) {
        if (patientId > 0 && psychologueId > 0 && patientId == psychologueId) {
            result.addErreur("Le patient et le psychologue ne peuvent pas être la même personne.");
        }
    }

    // ═══════════════════════════════════════════════════════
    //  HELPER — Parse ID depuis texte
    // ═══════════════════════════════════════════════════════
    public static int parseId(String texte, String label, ValidationResult result) {
        if (texte == null || texte.isBlank()) {
            result.addErreur(label + " est obligatoire.");
            return -1;
        }
        try {
            int val = Integer.parseInt(texte.trim());
            if (val <= 0) {
                result.addErreur(label + " doit être un entier positif.");
                return -1;
            }
            return val;
        } catch (NumberFormatException e) {
            result.addErreur(label + " doit être un nombre entier valide.");
            return -1;
        }
    }
}