package services;

import models.SuiviMentale;

public class ConseilThemeService {

    public String detecterTheme(SuiviMentale suivi) {
        if (suivi == null) {
            return "general";
        }

        int stress = suivi.getTauxDeStress();
        int energie = suivi.getNiveauDenergie();
        double sommeil = suivi.getHeureDeSommeil();

        String humeur = suivi.getHumeur() == null ? "" : suivi.getHumeur().trim().toLowerCase();
        String qualiteSommeil = suivi.getQualiteDuSommeil() == null ? "" : suivi.getQualiteDuSommeil().trim().toLowerCase();

        if (stress >= 8) {
            return "calme";
        }

        if (energie <= 3) {
            return "force";
        }

        if (sommeil < 5 || qualiteSommeil.equals("terrible") || qualiteSommeil.equals("mauvais")) {
            return "repos";
        }

        if (humeur.equals("très mal") || humeur.equals("tres mal") || humeur.equals("mal")) {
            return "espoir";
        }

        if (stress >= 6 && energie <= 4) {
            return "equilibre";
        }

        return "general";
    }

    public String getMessageLocal(String theme) {
        return switch (theme) {
            case "calme" -> "Votre niveau de stress semble élevé. Prenez quelques minutes pour respirer profondément et ralentir votre rythme.";
            case "force" -> "Votre énergie semble faible aujourd’hui. Essayez de faire une pause, de vous hydrater et de reprendre doucement.";
            case "repos" -> "Votre sommeil semble insuffisant. Essayez ce soir de limiter les écrans et de retrouver une heure de coucher régulière.";
            case "espoir" -> "Votre humeur semble fragile aujourd’hui. Essayez d’écrire ce que vous ressentez ou de parler à une personne de confiance.";
            case "equilibre" -> "Vous semblez fatigué(e) et sous pression. Accordez-vous un moment de récupération aujourd’hui.";
            default -> "Prenez un moment pour respirer, ralentir et écouter vos besoins aujourd’hui.";
        };
    }
}