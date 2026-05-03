package validators;

import models.RendezVous;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


public class RendezVousValidator {

   
    public static final List<String> STATUTS_VALIDES =
            List.of("libre", "réservé", "confirmé", "annulé", "disponible", "en attente");

   
    public static final List<String> TYPES_VALIDES =
            List.of("consultation", "suivi", "urgence", "bilan");

  
    public static final int HEURE_MIN = 8;
    public static final int HEURE_MAX = 20;

  
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
            return isValide() ? "Valide" :  ""+ getMessageComplet();
        }
    }




    public static ValidationResult valider(RendezVous r) {
        ValidationResult result = new ValidationResult();

        if (r == null) {
            result.addErreur("Le rendez-vous ne peut pas être null.");
            return result;
        }

        validerDate(r.getDate(), result);
        validerHeureDebut(r.getHeureDebut(), result);
        validerHeureFin(r.getHeureFin(), result);
        validerOrdreHeures(r.getHeureDebut(), r.getHeureFin(), result);
        validerStatut(r.getStatut(), result);
        validerType(r.getTypeRdv(), result);
        validerPsychologueId(r.getPsychologueId(), result);

        return result;
    }




    public static void validerDate(Date date, ValidationResult result) {
        if (date == null) {
            result.addErreur("La date est obligatoire.");
            return;
        }
        if (date.toLocalDate().isBefore(LocalDate.now())) {
            result.addErreur("La date ne peut pas être dans le passé.");
        }
    }

  
    public static void validerHeureDebut(Time heureDebut, ValidationResult result) {
        if (heureDebut == null) {
            result.addErreur("L'heure de début est obligatoire.");
            return;
        }
        int h = heureDebut.toLocalTime().getHour();
        if (h < HEURE_MIN || h >= HEURE_MAX) {
            result.addErreur("L'heure de début doit être entre " + HEURE_MIN + "h et " + HEURE_MAX + "h.");
        }
    }

    
    public static void validerHeureFin(Time heureFin, ValidationResult result) {
        if (heureFin == null) {
            result.addErreur("L'heure de fin est obligatoire.");
            return;
        }
        int h = heureFin.toLocalTime().getHour();
        if (h <= HEURE_MIN || h > HEURE_MAX) {
            result.addErreur("L'heure de fin doit être entre " + HEURE_MIN + "h et " + HEURE_MAX + "h.");
        }
    }

    public static void validerOrdreHeures(Time heureDebut, Time heureFin, ValidationResult result) {
        if (heureDebut == null || heureFin == null) return;
        if (!heureFin.toLocalTime().isAfter(heureDebut.toLocalTime())) {
            result.addErreur("L'heure de fin doit être après l'heure de début.");
        }
     
        long minutes = java.time.Duration.between(
                heureDebut.toLocalTime(), heureFin.toLocalTime()
        ).toMinutes();
        if (minutes < 30) {
            result.addErreur("La durée minimale d'un rendez-vous est 30 minutes.");
        }
    }

    
    public static void validerStatut(String statut, ValidationResult result) {
        if (statut == null || statut.isBlank()) {
            result.addErreur("Le statut est obligatoire.");
            return;
        }
        if (!STATUTS_VALIDES.contains(statut.toLowerCase())) {
            result.addErreur("Statut invalide : '" + statut + "'. Valeurs acceptées : " + STATUTS_VALIDES);
        }
    }

    
    public static void validerType(String typeRdv, ValidationResult result) {
        if (typeRdv == null || typeRdv.isBlank()) {
            result.addErreur("Le type de rendez-vous est obligatoire.");
            return;
        }
        if (!TYPES_VALIDES.contains(typeRdv.toLowerCase())) {
            result.addErreur("Type invalide : '" + typeRdv + "'. Valeurs acceptées : " + TYPES_VALIDES);
        }
    }


    public static void validerPsychologueId(int psychologueId, ValidationResult result) {
        if (psychologueId <= 0) {
            result.addErreur("L'identifiant du psychologue doit être supérieur à 0.");
        }
    }

  
    public static Time parseHeure(String texte, String label, ValidationResult result) {
        if (texte == null || texte.isBlank()) {
            result.addErreur(label + " est obligatoire.");
            return null;
        }
        try {
            if (!texte.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
                result.addErreur(label + " doit être au format HH:mm (ex: 09:30).");
                return null;
            }
            return Time.valueOf(texte.trim() + ":00");
        } catch (Exception e) {
            result.addErreur(label + " est invalide.");
            return null;
        }
    }

   
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