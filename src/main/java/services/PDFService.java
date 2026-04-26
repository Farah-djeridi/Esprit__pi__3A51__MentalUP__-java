package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Ban;
import models.Commentaire;
import models.Sujet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFService {

    private static final BaseColor COLOR_PRIMARY = new BaseColor(44, 62, 80); // #2C3E50
    private static final BaseColor COLOR_ACCENT = new BaseColor(220, 38, 38); // #DC2626

    public static void exportBansToPDF(List<Ban> bans, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport des bannissements");
        fileChooser.setInitialFileName("Rapport_Bannissements.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                addHeader(document, "Rapport de Gestion des Bannissements");

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setWidths(new float[]{15, 15, 15, 10, 30, 15});

                String[] headers = {"Utilisateur", "Date Ban", "Expiration", "Durée", "Raison", "Par"};
                for (String header : headers) {
                    table.addCell(createHeaderCell(header));
                }

                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                boolean alternate = false;
                for (Ban ban : bans) {
                    BaseColor bgColor = alternate ? new BaseColor(245, 247, 250) : BaseColor.WHITE;
                    table.addCell(createStyledCell(ban.getUserName(), cellFont, bgColor));
                    table.addCell(createStyledCell(ban.getBanDate().toString(), cellFont, bgColor));
                    table.addCell(createStyledCell(ban.getBanExpiryDate().toString(), cellFont, bgColor));
                    long days = java.time.temporal.ChronoUnit.DAYS.between(ban.getBanDate().toLocalDate(), ban.getBanExpiryDate().toLocalDate());
                    table.addCell(createStyledCell(days + " j", cellFont, bgColor));
                    table.addCell(createStyledCell(ban.getBanReason(), cellFont, bgColor));
                    table.addCell(createStyledCell(ban.getBannedByName(), cellFont, bgColor));
                    alternate = !alternate;
                }

                document.add(table);
                addFooter(document);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void exportCommentairesToPDF(List<Commentaire> comments, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport des commentaires");
        fileChooser.setInitialFileName("Rapport_Commentaires.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                addHeader(document, "Rapport de Modération des Commentaires");

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setWidths(new float[]{35, 15, 15, 15, 10, 10});

                String[] headers = {"Contenu", "Auteur", "Sujet", "Date", "Tox.", "Score"};
                for (String header : headers) {
                    table.addCell(createHeaderCell(header));
                }

                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                boolean alternate = false;
                for (Commentaire c : comments) {
                    BaseColor bgColor = alternate ? new BaseColor(245, 247, 250) : BaseColor.WHITE;
                    table.addCell(createStyledCell(c.getContenu(), cellFont, bgColor));
                    table.addCell(createStyledCell(c.getUserName(), cellFont, bgColor));
                    table.addCell(createStyledCell("ID: " + c.getSujetId(), cellFont, bgColor));
                    table.addCell(createStyledCell(c.getDateCommentaire().toString(), cellFont, bgColor));
                    table.addCell(createStyledCell(c.isEstToxique() ? "OUI" : "NON", cellFont, bgColor));
                    table.addCell(createStyledCell(String.format("%.0f%%", c.getScoreToxicite() * 100), cellFont, bgColor));
                    alternate = !alternate;
                }

                document.add(table);
                addFooter(document);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void exportSujetsToPDF(List<Sujet> sujets, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport des sujets");
        fileChooser.setInitialFileName("Rapport_Sujets.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                addHeader(document, "Rapport de Gestion des Sujets du Forum");

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setWidths(new float[]{25, 15, 30, 15, 10, 5});

                String[] headers = {"Titre", "Auteur", "Contenu", "Date", "Toxicité", "Vues"};
                for (String header : headers) {
                    table.addCell(createHeaderCell(header));
                }

                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                boolean alternate = false;
                for (Sujet s : sujets) {
                    BaseColor bgColor = alternate ? new BaseColor(245, 247, 250) : BaseColor.WHITE;
                    table.addCell(createStyledCell(s.getTitre(), cellFont, bgColor));
                    table.addCell(createStyledCell(s.getUserName(), cellFont, bgColor));
                    String content = s.getContenu();
                    if (content.length() > 50) content = content.substring(0, 50) + "...";
                    table.addCell(createStyledCell(content, cellFont, bgColor));
                    table.addCell(createStyledCell(s.getDateCreation().toString(), cellFont, bgColor));
                    table.addCell(createStyledCell(String.format("%.0f%%", s.getScoreToxicite() * 100), cellFont, bgColor));
                    table.addCell(createStyledCell(String.valueOf(s.getNbVues()), cellFont, bgColor));
                    alternate = !alternate;
                }

                document.add(table);
                addFooter(document);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void addHeader(Document document, String titleStr) throws Exception {
        // Logo
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

        Paragraph dateP = new Paragraph("Date: " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(java.time.LocalDateTime.now()), 
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

    private static PdfPCell createHeaderCell(String text) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(10);
        cell.setBorderColor(BaseColor.WHITE);
        return cell;
    }

    private static PdfPCell createStyledCell(String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setBorderColor(new BaseColor(230, 230, 230));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
        Paragraph footer = new Paragraph("MentalUp - Votre compagnon de santé mentale numérique", footerFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
    }
}
