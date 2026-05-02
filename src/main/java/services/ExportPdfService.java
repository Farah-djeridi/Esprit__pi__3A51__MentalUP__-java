package services;

import models.SuiviMentale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ExportPdfService {

    private static final float MARGIN = 30f;
    private static final float ROW_HEIGHT = 16f;
    private static final float CELL_PADDING = 2f;

    public File exporterSuivisUtilisateurEnPdf(List<SuiviMentale> suivis, int userId, String nomUtilisateur) throws IOException {
        String fileName = "suivis_" + cleanFileName(nomUtilisateur) + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        File output = new File(
                System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName
        );

        return exporterSuivisUtilisateurEnPdfVersFichier(suivis, userId, nomUtilisateur, output);
    }

    public File exporterSuivisUtilisateurEnPdfVersFichier(List<SuiviMentale> suivis, int userId, String nomUtilisateur, File output) throws IOException {
        try (PDDocument document = new PDDocument()) {

            PDPage page = createLandscapePage();
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float y = pageHeight - MARGIN;

            // =========================
            // 1. LOGO + TITRE
            // =========================
            y = drawHeader(document, content, pageWidth, y, nomUtilisateur, suivis);

            // =========================
            // 2. TABLEAU
            // =========================
            if (suivis == null || suivis.isEmpty()) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_OBLIQUE, 11);
                content.setNonStrokingColor(new Color(90, 100, 115));
                content.newLineAtOffset(MARGIN, y);
                content.showText("Aucun suivi disponible.");
                content.endText();
            } else {
                float[] colWidths = {65f, 65f, 85f, 50f, 50f, 50f, 50f, 170f};
                String[] headers = {"Date", "Humeur", "Sommeil", "Heures", "Stress", "Energie", "Score", "Journal"};

                y = drawTableHeader(content, MARGIN, y, colWidths, headers);

                for (int i = 0; i < suivis.size(); i++) {
                    SuiviMentale s = suivis.get(i);

                    if (y < 50) {
                        content.close();

                        page = createLandscapePage();
                        document.addPage(page);
                        content = new PDPageContentStream(document, page);

                        pageHeight = page.getMediaBox().getHeight();
                        y = pageHeight - MARGIN;

                        y = drawMiniHeaderForNextPage(content, y);
                        y = drawTableHeader(content, MARGIN, y, colWidths, headers);
                    }

                    String[] row = {
                            safe(s.getDateDeSuivi()),
                            safe(s.getHumeur()),
                            safe(s.getQualiteDuSommeil()),
                            String.valueOf(s.getHeureDeSommeil()),
                            String.valueOf(s.getTauxDeStress()),
                            String.valueOf(s.getNiveauDenergie()),
                            String.valueOf(s.getScoreMentale()),
                            truncate(clean(s.getJournalEmotionnelle()), 40)
                    };

                    y = drawTableRow(content, MARGIN, y, colWidths, row, i);
                }
            }

            content.close();
            document.save(output);
            return output;
        }
    }

    private float drawHeader(PDDocument document, PDPageContentStream content, float pageWidth, float y,
                             String nomUtilisateur, List<SuiviMentale> suivis) throws IOException {

        // Bandeau haut
        content.setNonStrokingColor(new Color(44, 95, 138));
        content.addRect(MARGIN, y - 55, pageWidth - 2 * MARGIN, 55);
        content.fill();

        // Logo si disponible
        try {
            String logoPath = "src/main/resources/Images/logo.png";
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                PDImageXObject logo = PDImageXObject.createFromFileByContent(logoFile, document);
                content.drawImage(logo, MARGIN + 10, y - 45, 32, 32);
            }
        } catch (Exception e) {
            // on ignore si logo absent
        }

        // Titre
        content.beginText();
        content.setNonStrokingColor(Color.WHITE);
        content.setFont(PDType1Font.HELVETICA_BOLD, 18);
        content.newLineAtOffset(MARGIN + 50, y - 22);
        content.showText("Rapport de suivi mental");
        content.endText();

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 10);
        content.newLineAtOffset(MARGIN + 50, y - 38);
        content.showText("Analyse personnelle des suivis enregistres");
        content.endText();

        y -= 75;

        // Informations utilisateur
        content.beginText();
        content.setNonStrokingColor(new Color(45, 55, 72));
        content.setFont(PDType1Font.HELVETICA_BOLD, 11);
        content.newLineAtOffset(MARGIN, y);
        content.showText("Utilisateur : " + clean(nomUtilisateur));
        content.endText();

        y -= 14;

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 10);
        content.newLineAtOffset(MARGIN, y);
        content.showText("Date d'export : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        content.endText();

        y -= 14;

        String periode = buildPeriodeTexte(suivis);
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 10);
        content.newLineAtOffset(MARGIN, y);
        content.showText("Periode exportee : " + periode);
        content.endText();

        y -= 24;

        // Statistiques
        y = drawStatsCards(content, y, suivis);

        y -= 14;
        return y;
    }

    private float drawMiniHeaderForNextPage(PDPageContentStream content, float y) throws IOException {
        content.beginText();
        content.setNonStrokingColor(new Color(44, 95, 138));
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.newLineAtOffset(MARGIN, y);
        content.showText("Suite du rapport de suivi mental");
        content.endText();

        return y - 20;
    }

    private float drawStatsCards(PDPageContentStream content, float y, List<SuiviMentale> suivis) throws IOException {
        int total = suivis == null ? 0 : suivis.size();
        double stressMoyen = moyenneStress(suivis);
        double energieMoyenne = moyenneEnergie(suivis);
        double sommeilMoyen = moyenneSommeil(suivis);
        double scoreMoyen = moyenneScore(suivis);

        float cardWidth = 140f;
        float cardHeight = 42f;
        float gap = 10f;
        float startX = MARGIN;

        String[][] stats = {
                {"Total suivis", String.valueOf(total)},
                {"Stress moyen", formatDouble(stressMoyen)},
                {"Energie moyenne", formatDouble(energieMoyenne)},
                {"Sommeil moyen", formatDouble(sommeilMoyen)},
                {"Score moyen", formatDouble(scoreMoyen)}
        };

        for (int i = 0; i < stats.length; i++) {
            float x = startX + i * (cardWidth + gap);

            content.setNonStrokingColor(new Color(245, 248, 252));
            content.addRect(x, y - cardHeight, cardWidth, cardHeight);
            content.fill();

            content.setStrokingColor(new Color(210, 225, 240));
            content.addRect(x, y - cardHeight, cardWidth, cardHeight);
            content.stroke();

            content.beginText();
            content.setNonStrokingColor(new Color(100, 116, 139));
            content.setFont(PDType1Font.HELVETICA, 8);
            content.newLineAtOffset(x + 8, y - 12);
            content.showText(stats[i][0]);
            content.endText();

            content.beginText();
            content.setNonStrokingColor(new Color(44, 95, 138));
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.newLineAtOffset(x + 8, y - 28);
            content.showText(stats[i][1]);
            content.endText();
        }

        return y - cardHeight;
    }

    private String buildPeriodeTexte(List<SuiviMentale> suivis) {
        if (suivis == null || suivis.isEmpty()) {
            return "Aucune periode disponible";
        }

        Date minDate = suivis.stream()
                .map(SuiviMentale::getDateDeSuivi)
                .filter(d -> d != null)
                .min(Comparator.naturalOrder())
                .orElse(null);

        Date maxDate = suivis.stream()
                .map(SuiviMentale::getDateDeSuivi)
                .filter(d -> d != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (minDate == null || maxDate == null) {
            return "Aucune periode disponible";
        }

        return minDate.toString() + " -> " + maxDate.toString();
    }

    private double moyenneStress(List<SuiviMentale> suivis) {
        if (suivis == null || suivis.isEmpty()) return 0;
        double somme = 0;
        for (SuiviMentale s : suivis) somme += s.getTauxDeStress();
        return somme / suivis.size();
    }

    private double moyenneEnergie(List<SuiviMentale> suivis) {
        if (suivis == null || suivis.isEmpty()) return 0;
        double somme = 0;
        for (SuiviMentale s : suivis) somme += s.getNiveauDenergie();
        return somme / suivis.size();
    }

    private double moyenneSommeil(List<SuiviMentale> suivis) {
        if (suivis == null || suivis.isEmpty()) return 0;
        double somme = 0;
        for (SuiviMentale s : suivis) somme += s.getHeureDeSommeil();
        return somme / suivis.size();
    }

    private double moyenneScore(List<SuiviMentale> suivis) {
        if (suivis == null || suivis.isEmpty()) return 0;
        double somme = 0;
        for (SuiviMentale s : suivis) somme += s.getScoreMentale();
        return somme / suivis.size();
    }

    private String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    private PDPage createLandscapePage() {
        return new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
    }

    private float drawTableHeader(PDPageContentStream content, float startX, float y,
                                  float[] colWidths, String[] headers) throws IOException {
        float x = startX;

        for (int i = 0; i < headers.length; i++) {
            content.setNonStrokingColor(new Color(44, 95, 138));
            content.addRect(x, y - ROW_HEIGHT, colWidths[i], ROW_HEIGHT);
            content.fill();

            content.setStrokingColor(Color.WHITE);
            content.addRect(x, y - ROW_HEIGHT, colWidths[i], ROW_HEIGHT);
            content.stroke();

            content.beginText();
            content.setNonStrokingColor(Color.WHITE);
            content.setFont(PDType1Font.HELVETICA_BOLD, 8);
            content.newLineAtOffset(x + CELL_PADDING, y - 11);
            content.showText(headers[i]);
            content.endText();

            x += colWidths[i];
        }

        return y - ROW_HEIGHT;
    }

    private float drawTableRow(PDPageContentStream content, float startX, float y,
                               float[] colWidths, String[] row, int rowIndex) throws IOException {
        float x = startX;

        for (int i = 0; i < row.length; i++) {
            if (rowIndex % 2 == 0) {
                content.setNonStrokingColor(new Color(245, 248, 252));
            } else {
                content.setNonStrokingColor(Color.WHITE);
            }

            content.addRect(x, y - ROW_HEIGHT, colWidths[i], ROW_HEIGHT);
            content.fill();

            content.setStrokingColor(new Color(210, 225, 240));
            content.addRect(x, y - ROW_HEIGHT, colWidths[i], ROW_HEIGHT);
            content.stroke();

            content.beginText();
            content.setNonStrokingColor(new Color(45, 55, 72));
            content.setFont(PDType1Font.HELVETICA, 7);
            content.newLineAtOffset(x + CELL_PADDING, y - 11);
            content.showText(row[i] == null ? "-" : row[i]);
            content.endText();

            x += colWidths[i];
        }

        return y - ROW_HEIGHT;
    }

    private String safe(Object value) {
        return value == null ? "-" : clean(value.toString());
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return "-";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private String clean(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("’", "'")
                .replace("‘", "'")
                .replace("–", "-")
                .replace("—", "-")
                .replace("…", "...")
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("ù", "u")
                .replace("ô", "o")
                .replace("î", "i")
                .replace("ç", "c");
    }

    private String cleanFileName(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "utilisateur";
        }

        return clean(text)
                .replace(" ", "_")
                .replaceAll("[\\\\/:*?\"<>|]", "");
    }
}