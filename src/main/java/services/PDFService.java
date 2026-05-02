package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Ban;
import models.Commentaire;
import models.Sujet;
import Models.Dossier;
import Models.RendezVous;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Service unifié pour la génération de rapports PDF
 * Combine les fonctionnalités iTextPDF et Apache PDFBox
 */
public class PDFService {

    // ==================== CONSTANTES PARTAGÉES ====================

    // Couleurs iTextPDF
    private static final BaseColor COLOR_PRIMARY = new BaseColor(67, 97, 238);
    private static final BaseColor COLOR_ACCENT = new BaseColor(247, 37, 133);
    private static final BaseColor COLOR_SUCCESS = new BaseColor(4, 158, 100);
    private static final BaseColor COLOR_DANGER = new BaseColor(220, 53, 69);
    private static final BaseColor COLOR_WARNING = new BaseColor(255, 193, 7);
    private static final BaseColor COLOR_INFO = new BaseColor(23, 162, 184);
    private static final BaseColor COLOR_BACKGROUND_LIGHT = new BaseColor(248, 249, 250);
    private static final BaseColor COLOR_BACKGROUND_DARK = new BaseColor(233, 236, 239);
    private static final BaseColor COLOR_TEXT_MUTED = new BaseColor(108, 117, 125);

    // Couleurs Apache PDFBox
    private static final Color PDFBOX_PRIMARY_COLOR = new Color(26, 115, 232);
    private static final Color PDFBOX_HEADER_BG = new Color(241, 243, 244);
    private static final Color PDFBOX_TEXT_COLOR = new Color(32, 33, 36);
    private static final Color PDFBOX_ACCENT_COLOR = new Color(44, 95, 138);

    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Suppression des logs PDFBox
    static {
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("org.apache.fontbox").setLevel(java.util.logging.Level.SEVERE);
    }

    // ==================== PARTIE 1: RAPPORTS AVEC iTextPDF (Modération) ====================

    /**
     * Exporte la liste des bannissements vers un fichier PDF
     */
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
                PdfWriter.getInstance(document, new FileOutputStream(file));
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

    /**
     * Exporte la liste des commentaires vers un fichier PDF
     */
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
                PdfWriter.getInstance(document, new FileOutputStream(file));
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

    /**
     * Exporte la liste des sujets vers un fichier PDF
     */
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
                PdfWriter.getInstance(document, new FileOutputStream(file));
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

    // ==================== PARTIE 2: RAPPORTS AVEC Apache PDFBox (Médical) ====================
    // Note: Ces méthodes nécessitent les classes Dossier et RendezVous
    // Décommentez et utilisez uniquement si ces classes existent dans votre projet

    public void generatePatientDossierPDF(Dossier dossier, List<RendezVous> history, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawPDFBoxHeader(document, contentStream, "DOSSIER MÉDICAL DU PATIENT");

                // Info Section Box
                contentStream.setNonStrokingColor(PDFBOX_HEADER_BG);
                contentStream.addRect(50, 640, 500, 85);
                contentStream.fill();

                contentStream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(65, 710);
                contentStream.showText("INFORMATIONS GÉNÉRALES");
                contentStream.endText();

                contentStream.setNonStrokingColor(PDFBOX_TEXT_COLOR);
                contentStream.setFont(PDType1Font.HELVETICA, 10);

                contentStream.beginText();
                contentStream.newLineAtOffset(65, 690);
                contentStream.showText("Patient : " + (dossier.getPatientNom() != null ? dossier.getPatientNom() : "ID #" + dossier.getPatientId()));
                contentStream.newLineAtOffset(0, -18);
                contentStream.showText("Ouvert le : " + dossier.getDateCreation().toString());
                contentStream.newLineAtOffset(0, -18);
                contentStream.showText("Niveau de Risque : ");
                contentStream.endText();

                String risque = dossier.getNiveauRisque() != null ? dossier.getNiveauRisque().toUpperCase() : "NORMAL";
                Color risqueColor = new Color(52, 168, 83);
                if (risque.contains("HAUT") || risque.contains("ÉLEVÉ")) risqueColor = new Color(217, 48, 37);
                else if (risque.contains("MOYEN")) risqueColor = new Color(251, 188, 4);

                contentStream.setNonStrokingColor(risqueColor);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(150, 654);
                contentStream.showText(risque);
                contentStream.endText();

                // Notes Section
                contentStream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(50, 615);
                contentStream.showText("NOTES CLINIQUES ET OBSERVATIONS");
                contentStream.endText();

                contentStream.setNonStrokingColor(PDFBOX_TEXT_COLOR);
                String notes = dossier.getNotesGenerales();
                if (notes != null) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.newLineAtOffset(50, 600);

                    String[] words = notes.split(" ");
                    StringBuilder line = new StringBuilder();
                    int y = 600;
                    for (String word : words) {
                        if (line.length() + word.length() > 95) {
                            contentStream.showText(line.toString());
                            contentStream.newLineAtOffset(0, -14);
                            y -= 14;
                            line = new StringBuilder();
                        }
                        line.append(word).append(" ");
                        if (y < 450) break;
                    }
                    contentStream.showText(line.toString());
                    contentStream.endText();
                }

                // AI Summary Section if exists
                if (dossier.getAiSummary() != null && !dossier.getAiSummary().isEmpty()) {
                    contentStream.setNonStrokingColor(new Color(232, 240, 254));
                    contentStream.addRect(50, 420, 500, 60);
                    contentStream.fill();

                    contentStream.setNonStrokingColor(new Color(25, 103, 210));
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    contentStream.newLineAtOffset(60, 465);
                    contentStream.showText("RÉSUMÉ IA (ANALYSE AUTOMATIQUE)");
                    contentStream.endText();

                    contentStream.setNonStrokingColor(PDFBOX_TEXT_COLOR);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                    contentStream.newLineAtOffset(60, 445);
                    String summary = dossier.getAiSummary();
                    contentStream.showText(summary.length() > 100 ? summary.substring(0, 97) + "..." : summary);
                    contentStream.endText();
                }

                // Table Header
                int yTable = 380;
                contentStream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(50, yTable);
                contentStream.showText("HISTORIQUE DES SÉANCES");
                contentStream.endText();
                yTable -= 20;

                // Column Headers
                contentStream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
                contentStream.addRect(50, yTable - 5, 500, 22);
                contentStream.fill();

                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
                contentStream.newLineAtOffset(60, yTable);
                contentStream.showText("DATE");
                contentStream.newLineAtOffset(90, 0);
                contentStream.showText("TYPE");
                contentStream.newLineAtOffset(130, 0);
                contentStream.showText("STATUT");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("LIEU / MODE");
                contentStream.endText();

                yTable -= 22;
                boolean zebra = false;
                for (RendezVous r : history) {
                    if (yTable < 80) break;

                    if (zebra) {
                        contentStream.setNonStrokingColor(new Color(248, 249, 250));
                        contentStream.addRect(50, yTable - 5, 500, 20);
                        contentStream.fill();
                    }

                    contentStream.setNonStrokingColor(PDFBOX_TEXT_COLOR);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.newLineAtOffset(60, yTable);
                    contentStream.showText(r.getDate() != null ? r.getDate().toString() : "N/A");
                    contentStream.newLineAtOffset(90, 0);
                    contentStream.showText(r.getTypeRdv() != null ? r.getTypeRdv() : "N/A");
                    contentStream.newLineAtOffset(130, 0);
                    contentStream.showText(r.getStatut() != null ? r.getStatut().toUpperCase() : "N/A");
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(r.getLieu() != null ? r.getLieu() : "N/A");
                    contentStream.endText();

                    yTable -= 20;
                    zebra = !zebra;
                }

                drawPDFBoxFooter(contentStream, 1);
            }
            document.save(outputPath);
        }
    }

    public void generatePlanningPDF(List<RendezVous> rdvs, String period, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawPDFBoxHeader(document, contentStream, "PLANNING DES RENDEZ-VOUS");

                contentStream.setNonStrokingColor(PDFBOX_ACCENT_COLOR);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, 715);
                contentStream.showText("Période : " + period);
                contentStream.endText();

                int yPosition = 680;

                // Table Header
                contentStream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
                contentStream.addRect(50, yPosition - 5, 500, 22);
                contentStream.fill();

                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
                contentStream.newLineAtOffset(60, yPosition);
                contentStream.showText("DATE");
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText("HEURE");
                contentStream.newLineAtOffset(70, 0);
                contentStream.showText("PATIENT");
                contentStream.newLineAtOffset(140, 0);
                contentStream.showText("STATUT");
                contentStream.newLineAtOffset(90, 0);
                contentStream.showText("MODE");
                contentStream.endText();

                yPosition -= 22;
                boolean zebra = false;
                contentStream.setNonStrokingColor(PDFBOX_TEXT_COLOR);

                for (RendezVous r : rdvs) {
                    if (yPosition < 80) break;

                    if (zebra) {
                        contentStream.setNonStrokingColor(new Color(248, 249, 250));
                        contentStream.addRect(50, yPosition - 5, 500, 20);
                        contentStream.fill();
                    }

                    contentStream.setNonStrokingColor(PDFBOX_TEXT_COLOR);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(r.getDate() != null ? r.getDate().toString() : "N/A");
                    contentStream.newLineAtOffset(80, 0);
                    contentStream.showText(r.getHeureDebut() != null ? r.getHeureDebut().toString().substring(0,5) : "N/A");
                    contentStream.newLineAtOffset(70, 0);
                    contentStream.showText(r.getEtudiantId() != null ? "Patient #" + r.getEtudiantId() : "Libre");
                    contentStream.newLineAtOffset(140, 0);
                    contentStream.showText(r.getStatut());
                    contentStream.newLineAtOffset(90, 0);
                    contentStream.showText(r.getLieu() != null ? r.getLieu() : "N/A");
                    contentStream.endText();

                    yPosition -= 20;
                    zebra = !zebra;
                }

                drawPDFBoxFooter(contentStream, 1);
            }
            document.save(outputPath);
        }
    }

    public void generateGlobalStatsPDF(Map<String, Object> stats, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawPDFBoxHeader(document, contentStream, "RAPPORT STATISTIQUE GLOBAL");

                int y = 700;

                // Summary Box
                contentStream.setNonStrokingColor(PDFBOX_HEADER_BG);
                contentStream.addRect(50, y - 50, 500, 60);
                contentStream.fill();

                addPDFBoxStatLine(contentStream, "Nombre total de patients :", stats.get("totalPatients").toString(), y - 15);
                y -= 80;

                contentStream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Volume d'activité par mois");
                contentStream.endText();
                y -= 25;

                @SuppressWarnings("unchecked")
                Map<String, Integer> rdvPerMonth = (Map<String, Integer>) stats.get("rdvPerMonth");
                if (rdvPerMonth != null) {
                    boolean zebra = false;
                    for (Map.Entry<String, Integer> entry : rdvPerMonth.entrySet()) {
                        if (zebra) {
                            contentStream.setNonStrokingColor(new Color(248, 249, 250));
                            contentStream.addRect(70, y - 5, 460, 18);
                            contentStream.fill();
                        }

                        contentStream.setNonStrokingColor(PDFBOX_TEXT_COLOR);
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.newLineAtOffset(80, y);
                        contentStream.showText(entry.getKey() + " : " + entry.getValue() + " rendez-vous");
                        contentStream.endText();
                        y -= 18;
                        zebra = !zebra;
                    }
                }

                y -= 30;
                addPDFBoxStatLine(contentStream, "Taux d'annulation global :", stats.get("cancellationRate").toString() + "%", y);

                drawPDFBoxFooter(contentStream, 1);
            }
            document.save(outputPath);
        }
    }

    // ==================== MÉTHODES PRIVÉES iTextPDF ====================

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

    // ==================== MÉTHODES PRIVÉES Apache PDFBox ====================

    private void drawPDFBoxHeader(PDDocument document, PDPageContentStream contentStream, String title) throws IOException {
        contentStream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
        contentStream.addRect(0, 820, 600, 22);
        contentStream.fill();

        try (InputStream is = getClass().getResourceAsStream("/Images/logo.png")) {
            if (is != null) {
                byte[] imageBytes = is.readAllBytes();
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, imageBytes, "logo");
                contentStream.drawImage(logo, 50, 755, 50, 50);
            }
        } catch (Exception e) {
            System.err.println("Could not load logo for PDF: " + e.getMessage());
        }

        contentStream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
        contentStream.newLineAtOffset(110, 780);
        contentStream.showText("MentalUP");
        contentStream.endText();

        contentStream.setNonStrokingColor(PDFBOX_TEXT_COLOR);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(110, 765);
        contentStream.showText(title);
        contentStream.endText();

        contentStream.setStrokingColor(new Color(218, 220, 224));
        contentStream.setLineWidth(1f);
        contentStream.moveTo(50, 740);
        contentStream.lineTo(550, 740);
        contentStream.stroke();
    }

    private void drawPDFBoxFooter(PDPageContentStream contentStream, int pageNum) throws IOException {
        contentStream.setStrokingColor(new Color(218, 220, 224));
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(50, 50);
        contentStream.lineTo(550, 50);
        contentStream.stroke();

        contentStream.setNonStrokingColor(new Color(128, 128, 128));
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 8);
        contentStream.newLineAtOffset(50, 40);
        contentStream.showText("Document confidentiel - Généré par MentalUp le " + LocalDateTime.now().format(dtFormatter));
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(520, 40);
        contentStream.showText("Page " + pageNum);
        contentStream.endText();
    }

    private void addPDFBoxStatLine(PDPageContentStream stream, String label, String value, int y) throws IOException {
        stream.setNonStrokingColor(PDFBOX_PRIMARY_COLOR);
        stream.beginText();
        stream.setFont(PDType1Font.HELVETICA_BOLD, 11);
        stream.newLineAtOffset(65, y);
        stream.showText(label);
        stream.endText();

        stream.setNonStrokingColor(PDFBOX_TEXT_COLOR);
        stream.beginText();
        stream.setFont(PDType1Font.HELVETICA_BOLD, 11);
        stream.newLineAtOffset(300, y);
        stream.showText(value);
        stream.endText();
    }
}