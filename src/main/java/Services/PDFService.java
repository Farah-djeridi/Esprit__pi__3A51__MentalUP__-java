package Services;

import Models.Dossier;
import Models.RendezVous;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PDFService {

    private static final Color PRIMARY_COLOR = new Color(44, 95, 138);
    private static final Color SECONDARY_COLOR = new Color(90, 108, 125);
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static {
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("org.apache.fontbox").setLevel(java.util.logging.Level.SEVERE);
    }

    private void drawHeader(PDDocument document, PDPageContentStream contentStream, String title) throws IOException {
        // Logo
        try (InputStream is = getClass().getResourceAsStream("/Images/logo.png")) {
            if (is != null) {
                byte[] imageBytes = is.readAllBytes();
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, imageBytes, "logo");
                contentStream.drawImage(logo, 50, 765, 60, 60);
            }
        } catch (Exception e) {
            System.err.println("Could not load logo for PDF: " + e.getMessage());
        }

        // Header Background bar
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.addRect(120, 770, 430, 50);
        contentStream.fill();

        // Title
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
        contentStream.newLineAtOffset(140, 790);
        contentStream.showText(title.toUpperCase());
        contentStream.endText();

        // Separator Line
        contentStream.setStrokingColor(PRIMARY_COLOR);
        contentStream.setLineWidth(1f);
        contentStream.moveTo(50, 750);
        contentStream.lineTo(550, 750);
        contentStream.stroke();
    }

    private void drawFooter(PDPageContentStream contentStream) throws IOException {
        contentStream.setStrokingColor(new Color(200, 200, 200));
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(50, 50);
        contentStream.lineTo(550, 50);
        contentStream.stroke();

        contentStream.setNonStrokingColor(SECONDARY_COLOR);
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        contentStream.newLineAtOffset(50, 40);
        contentStream.showText("Généré par MentalUp le " + LocalDateTime.now().format(dtFormatter));
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(500, 40);
        contentStream.showText("Page 1/1");
        contentStream.endText();
    }

    public void generatePatientDossierPDF(Dossier dossier, List<RendezVous> history, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawHeader(document, contentStream, "Dossier Médical Patient");

                // Patient Info
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("IDENTITÉ DU PATIENT");
                contentStream.endText();

                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(50, 705);
                contentStream.showText("Nom : " + (dossier.getPatientNom() != null ? dossier.getPatientNom() : "ID #" + dossier.getPatientId()));
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Date de création : " + dossier.getDateCreation().toString());
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Niveau de risque : " + dossier.getNiveauRisque());
                contentStream.endText();

                // Section Notes
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 650);
                contentStream.showText("NOTES CLINIQUES");
                contentStream.endText();

                contentStream.setNonStrokingColor(Color.BLACK);
                String notes = dossier.getNotesGenerales();
                if (notes != null) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.newLineAtOffset(50, 635);
                    // Wrap simple
                    int start = 0;
                    int y = 635;
                    while (start < notes.length() && y > 100) {
                        int end = Math.min(start + 90, notes.length());
                        contentStream.showText(notes.substring(start, end));
                        contentStream.newLineAtOffset(0, -12);
                        start = end;
                        y -= 12;
                    }
                    contentStream.endText();
                }

                // Table Historique
                int yPosition = 480;
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, yPosition);
                contentText(contentStream, "HISTORIQUE DES RENDEZ-VOUS");
                contentStream.endText();
                yPosition -= 20;

                // Table Header
                contentStream.setNonStrokingColor(new Color(240, 244, 248));
                contentStream.addRect(50, yPosition - 5, 500, 20);
                contentStream.fill();

                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                contentStream.newLineAtOffset(60, yPosition);
                contentStream.showText("DATE");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("TYPE");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("STATUT");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("LIEU");
                contentStream.endText();

                yPosition -= 20;
                contentStream.setNonStrokingColor(Color.BLACK);
                for (RendezVous r : history) {
                    if (yPosition < 80) break;
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(r.getDate().toString());
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(r.getTypeRdv());
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(r.getStatut());
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(r.getLieu() != null ? r.getLieu() : "N/A");
                    contentStream.endText();
                    yPosition -= 15;
                }

                drawFooter(contentStream);
            }
            document.save(outputPath);
        }
    }

    private void contentText(PDPageContentStream stream, String text) throws IOException {
        stream.showText(text);
    }

    public void generateGlobalStatsPDF(Map<String, Object> stats, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawHeader(document, contentStream, "Rapport Statistique Global");

                int y = 700;
                addStatLine(contentStream, "Nombre total de patients :", stats.get("totalPatients").toString(), y);
                y -= 40;

                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Volume d'activité par mois");
                contentStream.endText();
                y -= 20;

                Map<String, Integer> rdvPerMonth = (Map<String, Integer>) stats.get("rdvPerMonth");
                if (rdvPerMonth != null) {
                    contentStream.setNonStrokingColor(Color.BLACK);
                    for (Map.Entry<String, Integer> entry : rdvPerMonth.entrySet()) {
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                        contentStream.newLineAtOffset(70, y);
                        contentStream.showText(entry.getKey() + " : " + entry.getValue() + " rendez-vous");
                        contentStream.endText();
                        y -= 15;
                    }
                }

                y -= 20;
                addStatLine(contentStream, "Taux d'annulation global :", stats.get("cancellationRate").toString() + "%", y);
                
                drawFooter(contentStream);
            }
            document.save(outputPath);
        }
    }

    private void addStatLine(PDPageContentStream stream, String label, String value, int y) throws IOException {
        stream.setNonStrokingColor(PRIMARY_COLOR);
        stream.beginText();
        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        stream.newLineAtOffset(50, y);
        stream.showText(label);
        stream.endText();

        stream.setNonStrokingColor(Color.BLACK);
        stream.beginText();
        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        stream.newLineAtOffset(250, y);
        stream.showText(value);
        stream.endText();
    }

    public void generatePlanningPDF(List<RendezVous> rdvs, String period, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawHeader(document, contentStream, "Planning des Rendez-vous");

                contentStream.setNonStrokingColor(SECONDARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 725);
                contentStream.showText("Période sélectionnée : " + period);
                contentStream.endText();

                int yPosition = 690;
                contentStream.setNonStrokingColor(new Color(240, 244, 248));
                contentStream.addRect(50, yPosition - 5, 500, 20);
                contentStream.fill();

                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                contentStream.newLineAtOffset(60, yPosition);
                contentStream.showText("DATE");
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText("HEURE");
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText("PATIENT");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("STATUT");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("MODE");
                contentStream.endText();

                yPosition -= 20;
                contentStream.setNonStrokingColor(Color.BLACK);
                for (RendezVous r : rdvs) {
                    if (yPosition < 80) break;
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(r.getDate() != null ? r.getDate().toString() : "N/A");
                    contentStream.newLineAtOffset(80, 0);
                    contentStream.showText(r.getHeureDebut() != null ? r.getHeureDebut().toString().substring(0,5) : "N/A");
                    contentStream.newLineAtOffset(80, 0);
                    contentStream.showText(r.getEtudiantId() != null ? "ID #" + r.getEtudiantId() : "Libre");
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(r.getStatut());
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(r.getLieu() != null ? r.getLieu() : "N/A");
                    contentStream.endText();
                    yPosition -= 15;
                }

                drawFooter(contentStream);
            }
            document.save(outputPath);
        }
    }
}
