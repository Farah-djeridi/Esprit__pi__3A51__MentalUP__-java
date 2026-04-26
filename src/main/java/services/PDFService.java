package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Ban;
import models.Commentaire;
import models.Sujet;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PDFService {

    private static final BaseColor COLOR_PRIMARY = new BaseColor(67, 97, 238);
    private static final BaseColor COLOR_ACCENT = new BaseColor(247, 37, 133);
    private static final BaseColor COLOR_SUCCESS = new BaseColor(4, 158, 100); // #049E64
    private static final BaseColor COLOR_DANGER = new BaseColor(220, 53, 69); // #DC3545
    private static final BaseColor COLOR_WARNING = new BaseColor(255, 193, 7); // #FFC107
    private static final BaseColor COLOR_INFO = new BaseColor(23, 162, 184); // #17A2B8
    private static final BaseColor COLOR_BACKGROUND_LIGHT = new BaseColor(248, 249, 250);
    private static final BaseColor COLOR_BACKGROUND_DARK = new BaseColor(233, 236, 239);
    private static final BaseColor COLOR_TEXT_MUTED = new BaseColor(108, 117, 125);

    public static void exportBansToPDF(List<Ban> bans, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport des bannissements");
        fileChooser.setInitialFileName("Rapport_Bannissements_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document(PageSize.A4.rotate());
                document.setMargins(36, 36, 54, 36);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

                document.open();

                addEnhancedHeader(document, "Rapport de Gestion des Bannissements");

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(15f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{20, 20, 20, 10, 25, 15});

                String[] headers = {"👤 Utilisateur", "📅 Date Ban", "⏰ Expiration", "📊 Durée", "⚠️ Raison", "👮 Par"};
                for (String header : headers) {
                    table.addCell(createEnhancedHeaderCell(header));
                }

                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                boolean alternate = false;
                for (Ban ban : bans) {
                    BaseColor bgColor = alternate ? COLOR_BACKGROUND_LIGHT : BaseColor.WHITE;

                    table.addCell(createStyledCell(ban.getUserName(), cellFont, bgColor));
                    table.addCell(createStyledCell(formatSqlDate(ban.getBanDate()), cellFont, bgColor));

                    PdfPCell expiryCell = createStyledCell(formatSqlDate(ban.getBanExpiryDate()), cellFont, bgColor);
                    LocalDateTime expiryDateTime = convertToLocalDateTime(ban.getBanExpiryDate());
                    if (expiryDateTime.isBefore(LocalDateTime.now())) {
                        expiryCell.setBackgroundColor(new BaseColor(255, 235, 238));
                    }
                    table.addCell(expiryCell);

                    long days = ChronoUnit.DAYS.between(
                            convertToLocalDateTime(ban.getBanDate()).toLocalDate(),
                            convertToLocalDateTime(ban.getBanExpiryDate()).toLocalDate()
                    );
                    table.addCell(createStyledCell(formatDuration(days), cellFont, bgColor, COLOR_SUCCESS));
                    table.addCell(createStyledCell(ban.getBanReason(), cellFont, bgColor));
                    table.addCell(createStyledCell(ban.getBannedByName(), cellFont, bgColor));
                    alternate = !alternate;
                }

                document.add(table);
                addSignatureFooter(document);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void exportCommentairesToPDF(List<Commentaire> comments, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport des commentaires");
        fileChooser.setInitialFileName("Rapport_Commentaires_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document(PageSize.A4.rotate());
                document.setMargins(36, 36, 54, 36);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

                document.open();

                addEnhancedHeader(document, "Rapport de Modération des Commentaires");


                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(15f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{30, 15, 15, 15, 10, 15});

                String[] headers = {"💬 Contenu", "✍️ Auteur", "📌 Sujet", "📅 Date", "⚠️ Tox.", "📊 Score"};
                for (String header : headers) {
                    table.addCell(createEnhancedHeaderCell(header));
                }

                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                boolean alternate = false;
                for (Commentaire c : comments) {
                    BaseColor bgColor = alternate ? COLOR_BACKGROUND_LIGHT : BaseColor.WHITE;

                    String content = truncateText(c.getContenu(), 80);
                    table.addCell(createStyledCell(content, cellFont, bgColor));
                    table.addCell(createStyledCell(c.getUserName(), cellFont, bgColor));
                    table.addCell(createStyledCell("Sujet #" + c.getSujetId(), cellFont, bgColor));
                    table.addCell(createStyledCell(formatSqlDate(c.getDateCommentaire()), cellFont, bgColor));

                    String toxicText = c.isEstToxique() ? "⚠️ OUI" : "✅ NON";
                    BaseColor toxicColor = c.isEstToxique() ? COLOR_DANGER : COLOR_SUCCESS;
                    table.addCell(createStyledCell(toxicText, cellFont, bgColor, toxicColor));

                    table.addCell(createToxicityCell(c.getScoreToxicite(), cellFont, bgColor));

                    alternate = !alternate;
                }

                document.add(table);
                addSignatureFooter(document);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void exportSujetsToPDF(List<Sujet> sujets, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport des sujets");
        fileChooser.setInitialFileName("Rapport_Sujets_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document(PageSize.A4.rotate());
                document.setMargins(36, 36, 54, 36);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

                document.open();

                addEnhancedHeader(document, "Rapport de Gestion des Sujets");

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(15f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{20, 15, 30, 15, 10, 10});

                String[] headers = {"📝 Titre", "👤 Auteur", "📄 Contenu", "📅 Date", "⚠️ Toxicité", "👁️ Vues"};
                for (String header : headers) {
                    table.addCell(createEnhancedHeaderCell(header));
                }

                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                boolean alternate = false;
                for (Sujet s : sujets) {
                    BaseColor bgColor = alternate ? COLOR_BACKGROUND_LIGHT : BaseColor.WHITE;

                    table.addCell(createStyledCell(truncateText(s.getTitre(), 40), cellFont, bgColor));
                    table.addCell(createStyledCell(s.getUserName(), cellFont, bgColor));
                    table.addCell(createStyledCell(truncateText(s.getContenu(), 60), cellFont, bgColor));
                    table.addCell(createStyledCell(formatSqlDate(s.getDateCreation()), cellFont, bgColor));

                    BaseColor toxicityColor = getToxicityColor((float) s.getScoreToxicite());
                    table.addCell(createStyledCell(formatToxicity(s.getScoreToxicite()), cellFont, bgColor, toxicityColor));

                    table.addCell(createStyledCell(String.valueOf(s.getNbVues()), cellFont, bgColor));
                    alternate = !alternate;
                }

                document.add(table);
                addSignatureFooter(document);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static LocalDateTime convertToLocalDateTime(Date sqlDate) {
        if (sqlDate == null) return LocalDateTime.now();
        return new java.sql.Timestamp(sqlDate.getTime()).toLocalDateTime();
    }

    private static String formatSqlDate(Date sqlDate) {
        if (sqlDate == null) return "N/A";
        LocalDateTime localDateTime = convertToLocalDateTime(sqlDate);
        return localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }


    private static void addEnhancedHeader(Document document, String titleStr) throws Exception {
        try {
            java.net.URL logoUrl = PDFService.class.getResource("/Images/logo.png");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, COLOR_PRIMARY);
        Paragraph title = new Paragraph(titleStr, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10f);
        title.setSpacingAfter(5f);
        document.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
        Paragraph sub = new Paragraph("Rapport officiel généré par MentalUp Administration", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        document.add(sub);

        Paragraph dateP = new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
        dateP.setAlignment(Element.ALIGN_CENTER);
        dateP.setSpacingAfter(15f);
        document.add(dateP);

        LineSeparator ls = new LineSeparator();
        ls.setLineColor(COLOR_ACCENT);
        ls.setLineWidth(2f);
        document.add(new Chunk(ls));
        document.add(new Paragraph(" "));
    }

    private static PdfPCell createEnhancedHeaderCell(String text) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(12);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private static PdfPCell createStyledCell(String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "N/A", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setBorderColor(COLOR_BACKGROUND_DARK);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static PdfPCell createStyledCell(String text, Font font, BaseColor bgColor, BaseColor textColor) {
        Font coloredFont = FontFactory.getFont(FontFactory.HELVETICA, 10, textColor);
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "N/A", coloredFont));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setBorderColor(COLOR_BACKGROUND_DARK);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private static PdfPCell createToxicityCell(double score, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setBorderColor(COLOR_BACKGROUND_DARK);

        Paragraph paragraph = new Paragraph();
        Chunk percentageChunk = new Chunk(String.format("%.0f%%", score * 100),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, getToxicityColor((float) score)));
        paragraph.add(percentageChunk);

        cell.addElement(paragraph);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        return cell;
    }

    private static BaseColor getToxicityColor(float score) {
        if (score >= 0.7f) return COLOR_DANGER;
        if (score >= 0.3f) return COLOR_WARNING;
        return COLOR_SUCCESS;
    }

    private static String formatDuration(long days) {
        if (days < 1) return "< 1 j";
        if (days < 30) return days + " jours";
        if (days < 365) return (days / 30) + " mois";
        return (days / 365) + " an(s)";
    }

    private static String formatToxicity(double score) {
        if (score >= 0.7f) return "⚠️ Élevée";
        if (score >= 0.3f) return "⚠️ Modérée";
        return "✅ Faible";
    }

    private static String truncateText(String text, int maxLength) {
        if (text == null) return "N/A";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private static void addSignatureFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));

        PdfPTable footer = new PdfPTable(2);
        footer.setWidthPercentage(100);
        footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        footer.setSpacingBefore(20f);

        PdfPCell dateCell = new PdfPCell(new Phrase("Généré le " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, COLOR_TEXT_MUTED)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        footer.addCell(dateCell);

        PdfPCell sigCell = new PdfPCell(new Phrase("MentalUp Administration",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY)));
        sigCell.setBorder(Rectangle.NO_BORDER);
        sigCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footer.addCell(sigCell);

        document.add(footer);

        LineSeparator line = new LineSeparator();
        line.setLineColor(COLOR_BACKGROUND_DARK);
        line.setLineWidth(0.5f);
        document.add(new Chunk(line));

        Paragraph copyright = new Paragraph("© 2024 MentalUp - Tous droits réservés",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, COLOR_TEXT_MUTED));
        copyright.setAlignment(Element.ALIGN_CENTER);
        document.add(copyright);
    }
}