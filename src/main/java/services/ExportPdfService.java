package services;

import models.SuiviMentale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportPdfService {

    private static final float MARGIN = 30f;
    private static final float ROW_HEIGHT = 16f;
    private static final float CELL_PADDING = 2f;

    public File exporterSuivisUtilisateurEnPdf(List<SuiviMentale> suivis, int userId) throws IOException {
        try (PDDocument document = new PDDocument()) {

            PDPage page = createLandscapePage();
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float pageHeight = page.getMediaBox().getHeight();
            float y = pageHeight - MARGIN;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 16);
            content.newLineAtOffset(MARGIN, y);
            content.showText("Export des suivis mentaux");
            content.endText();

            y -= 20;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 10);
            content.newLineAtOffset(MARGIN, y);
            content.showText("Utilisateur ID : " + userId);
            content.endText();

            y -= 14;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 10);
            content.newLineAtOffset(MARGIN, y);
            content.showText("Date d'export : " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            content.endText();

            y -= 22;

            if (suivis == null || suivis.isEmpty()) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_OBLIQUE, 11);
                content.newLineAtOffset(MARGIN, y);
                content.showText("Aucun suivi disponible.");
                content.endText();
            } else {
                float[] colWidths = {60f, 60f, 80f, 45f, 45f, 45f, 45f, 180f};
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
                            truncate(clean(s.getJournalEmotionnelle()), 42)
                    };

                    y = drawTableRow(content, MARGIN, y, colWidths, row, i);
                }
            }

            content.close();

            String fileName = "suivis_utilisateur_" + userId + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

            File output = new File(
                    System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName
            );

            document.save(output);
            return output;
        }
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
}