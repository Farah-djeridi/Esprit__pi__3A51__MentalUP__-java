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

    private static final Color PRIMARY_COLOR = new Color(26, 115, 232); // Modern Blue
    private static final Color HEADER_BG = new Color(241, 243, 244);   // Light Gray
    private static final Color TEXT_COLOR = new Color(32, 33, 36);      // Dark Gray
    private static final Color ACCENT_COLOR = new Color(44, 95, 138);
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static {
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("org.apache.fontbox").setLevel(java.util.logging.Level.SEVERE);
    }

    private void drawHeader(PDDocument document, PDPageContentStream contentStream, String title) throws IOException {
        // Draw a light blue top bar
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.addRect(0, 820, 600, 22);
        contentStream.fill();

        // Logo
        try (InputStream is = getClass().getResourceAsStream("/Images/logo.png")) { // Updated path
            if (is != null) {
                byte[] imageBytes = is.readAllBytes();
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, imageBytes, "logo");
                contentStream.drawImage(logo, 50, 755, 50, 50);
            }
        } catch (Exception e) {
            System.err.println("Could not load logo for PDF: " + e.getMessage());
        }

        // Title and App Name
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
        contentStream.newLineAtOffset(110, 780);
        contentStream.showText("MentalUP");
        contentStream.endText();

        contentStream.setNonStrokingColor(TEXT_COLOR);
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        contentStream.newLineAtOffset(110, 765);
        contentStream.showText(title);
        contentStream.endText();

        // Horizontal Line
        contentStream.setStrokingColor(new Color(218, 220, 224));
        contentStream.setLineWidth(1f);
        contentStream.moveTo(50, 740);
        contentStream.lineTo(550, 740);
        contentStream.stroke();
    }

    private void drawFooter(PDPageContentStream contentStream, int pageNum) throws IOException {
        contentStream.setStrokingColor(new Color(218, 220, 224));
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(50, 50);
        contentStream.lineTo(550, 50);
        contentStream.stroke();

        contentStream.setNonStrokingColor(new Color(128, 128, 128));
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        contentStream.newLineAtOffset(50, 40);
        contentStream.showText("Document confidentiel - Généré par MentalUp le " + LocalDateTime.now().format(dtFormatter));
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(520, 40);
        contentStream.showText("Page " + pageNum);
        contentStream.endText();
    }

    public void generatePatientDossierPDF(Dossier dossier, List<RendezVous> history, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawHeader(document, contentStream, "DOSSIER MÉDICAL DU PATIENT");

                // Info Section Box
                contentStream.setNonStrokingColor(HEADER_BG);
                contentStream.addRect(50, 640, 500, 85);
                contentStream.fill();

                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                contentStream.newLineAtOffset(65, 710);
                contentStream.showText("INFORMATIONS GÉNÉRALES");
                contentStream.endText();

                contentStream.setNonStrokingColor(TEXT_COLOR);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                
                contentStream.beginText();
                contentStream.newLineAtOffset(65, 690);
                contentStream.showText("Patient : " + (dossier.getPatientNom() != null ? dossier.getPatientNom() : "ID #" + dossier.getPatientId()));
                contentStream.newLineAtOffset(0, -18);
                contentStream.showText("Ouvert le : " + dossier.getDateCreation().toString());
                contentStream.newLineAtOffset(0, -18);
                contentStream.showText("Niveau de Risque : ");
                contentStream.endText();

                // Risque Badge
                String risque = dossier.getNiveauRisque() != null ? dossier.getNiveauRisque().toUpperCase() : "NORMAL";
                Color risqueColor = new Color(52, 168, 83); // Green
                if (risque.contains("HAUT") || risque.contains("ÉLEVÉ")) risqueColor = new Color(217, 48, 37); // Red
                else if (risque.contains("MOYEN")) risqueColor = new Color(251, 188, 4); // Yellow/Orange
                
                contentStream.setNonStrokingColor(risqueColor);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                contentStream.newLineAtOffset(150, 654);
                contentStream.showText(risque);
                contentStream.endText();

                // Notes Section
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                contentStream.newLineAtOffset(50, 615);
                contentStream.showText("NOTES CLINIQUES ET OBSERVATIONS");
                contentStream.endText();

                contentStream.setNonStrokingColor(TEXT_COLOR);
                String notes = dossier.getNotesGenerales();
                if (notes != null) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
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
                        if (y < 450) break; // Limit for now
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
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                    contentStream.newLineAtOffset(60, 465);
                    contentStream.showText("RÉSUMÉ IA (ANALYSE AUTOMATIQUE)");
                    contentStream.endText();
                    
                    contentStream.setNonStrokingColor(TEXT_COLOR);
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 9);
                    contentStream.newLineAtOffset(60, 445);
                    String summary = dossier.getAiSummary();
                    contentStream.showText(summary.length() > 100 ? summary.substring(0, 97) + "..." : summary);
                    contentStream.endText();
                }

                // Table Header
                int yTable = 380;
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                contentStream.newLineAtOffset(50, yTable);
                contentStream.showText("HISTORIQUE DES SÉANCES");
                contentStream.endText();
                yTable -= 20;

                // Column Headers
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.addRect(50, yTable - 5, 500, 22);
                contentStream.fill();

                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
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
                    
                    contentStream.setNonStrokingColor(TEXT_COLOR);
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
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

                drawFooter(contentStream, 1);
            }
            document.save(outputPath);
        }
    }

    public void generatePlanningPDF(List<RendezVous> rdvs, String period, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawHeader(document, contentStream, "PLANNING DES RENDEZ-VOUS");

                contentStream.setNonStrokingColor(ACCENT_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 715);
                contentStream.showText("Période : " + period);
                contentStream.endText();

                int yPosition = 680;
                
                // Table Header
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.addRect(50, yPosition - 5, 500, 22);
                contentStream.fill();

                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
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
                contentStream.setNonStrokingColor(TEXT_COLOR);
                
                for (RendezVous r : rdvs) {
                    if (yPosition < 80) break;
                    
                    if (zebra) {
                        contentStream.setNonStrokingColor(new Color(248, 249, 250));
                        contentStream.addRect(50, yPosition - 5, 500, 20);
                        contentStream.fill();
                    }
                    
                    contentStream.setNonStrokingColor(TEXT_COLOR);
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
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

                drawFooter(contentStream, 1);
            }
            document.save(outputPath);
        }
    }

    public void generateGlobalStatsPDF(Map<String, Object> stats, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawHeader(document, contentStream, "RAPPORT STATISTIQUE GLOBAL");

                int y = 700;
                
                // Summary Box
                contentStream.setNonStrokingColor(HEADER_BG);
                contentStream.addRect(50, y - 50, 500, 60);
                contentStream.fill();
                
                addStatLine(contentStream, "Nombre total de patients :", stats.get("totalPatients").toString(), y - 15);
                y -= 80;

                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Volume d'activité par mois");
                contentStream.endText();
                y -= 25;

                Map<String, Integer> rdvPerMonth = (Map<String, Integer>) stats.get("rdvPerMonth");
                if (rdvPerMonth != null) {
                    boolean zebra = false;
                    for (Map.Entry<String, Integer> entry : rdvPerMonth.entrySet()) {
                        if (zebra) {
                            contentStream.setNonStrokingColor(new Color(248, 249, 250));
                            contentStream.addRect(70, y - 5, 460, 18);
                            contentStream.fill();
                        }
                        
                        contentStream.setNonStrokingColor(TEXT_COLOR);
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                        contentStream.newLineAtOffset(80, y);
                        contentStream.showText(entry.getKey() + " : " + entry.getValue() + " rendez-vous");
                        contentStream.endText();
                        y -= 18;
                        zebra = !zebra;
                    }
                }

                y -= 30;
                addStatLine(contentStream, "Taux d'annulation global :", stats.get("cancellationRate").toString() + "%", y);
                
                drawFooter(contentStream, 1);
            }
            document.save(outputPath);
        }
    }

    private void addStatLine(PDPageContentStream stream, String label, String value, int y) throws IOException {
        stream.setNonStrokingColor(PRIMARY_COLOR);
        stream.beginText();
        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        stream.newLineAtOffset(65, y);
        stream.showText(label);
        stream.endText();

        stream.setNonStrokingColor(TEXT_COLOR);
        stream.beginText();
        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        stream.newLineAtOffset(300, y);
        stream.showText(value);
        stream.endText();
    }
}
