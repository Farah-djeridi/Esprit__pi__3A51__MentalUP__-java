package services;

public class CitationFilterService {

    public boolean estAppropriee(String citation) {
        if (citation == null) {
            return false;
        }

        String text = citation.trim().toLowerCase();

        if (text.isEmpty()) {
            return false;
        }

        String[] motsInterdits = {
                "hate", "revenge", "kill", "enemy", "violence"
        };

        for (String mot : motsInterdits) {
            if (text.contains(mot)) {
                return false;
            }
        }

        return true;
    }

    public boolean correspondAuTheme(String citation, String theme) {
        if (citation == null || citation.isBlank()) {
            return false;
        }

        String text = citation.toLowerCase();

        return switch (theme) {
            case "calme" -> contientUnMot(text,
                    "peace", "calm", "breathe", "quiet", "still", "serenity", "gentle");

            case "force" -> contientUnMot(text,
                    "strength", "strong", "courage", "rise", "energy", "power", "forward");

            case "repos" -> contientUnMot(text,
                    "rest", "peace", "balance", "sleep", "calm", "healing", "quiet", "breathe");

            case "espoir" -> contientUnMot(text,
                    "hope", "healing", "light", "kindness", "better", "tomorrow", "smile");

            case "equilibre" -> contientUnMot(text,
                    "balance", "peace", "steady", "calm", "rest", "breathe", "center");

            default -> true;
        };
    }

    private boolean contientUnMot(String text, String... mots) {
        for (String mot : mots) {
            if (text.contains(mot)) {
                return true;
            }
        }
        return false;
    }
}