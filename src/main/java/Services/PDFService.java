package Services;

import Models.Dossier;
import Models.RendezVous;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PDFService {

    static {
        // Silence PDFBox/FontBox logging warnings about fonts
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("org.apache.fontbox").setLevel(java.util.logging.Level.SEVERE);
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void generatePatientDossierPDF(Dossier dossier, List<RendezVous> history, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Header Background
                contentStream.setNonStrokingColor(new Color(44, 95, 138));
                contentStream.addRect(0, 750, 600, 100);
                contentStream.fill();

                // Title
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                contentStream.newLineAtOffset(50, 785);
                contentStream.showText("DOSSIER MÉDICAL PATIENT");
                contentStream.endText();

                // Patient Info Section
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Informations Générales");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Patient : " + (dossier.getPatientNom() != null ? dossier.getPatientNom() : "ID #" + dossier.getPatientId()));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Date de création : " + dossier.getDateCreation().toString());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Niveau de risque : " + dossier.getNiveauRisque());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Nombre total de rendez-vous : " + (history != null ? history.size() : 0));
                contentStream.endText();

                // Notes section
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(50, 620);
                contentStream.showText("Notes Générales");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                contentStream.newLineAtOffset(50, 600);
                String notes = dossier.getNotesGenerales();
                if (notes != null) {
                    // Simple text wrap (very basic)
                    if (notes.length() > 80) {
                        contentStream.showText(notes.substring(0, 80));
                        contentStream.newLineAtOffset(0, -15);
                        contentStream.showText(notes.substring(80, Math.min(notes.length(), 160)));
                    } else {
                        contentStream.showText(notes);
                    }
                }
                contentStream.endText();

                // History Section
                contentStream.setNonStrokingColor(new Color(44, 95, 138));
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 520);
                contentStream.showText("Historique des consultations");
                contentStream.endText();

                // Table Header
                int yPosition = 490;
                contentStream.setNonStrokingColor(new Color(230, 230, 230));
                contentStream.addRect(50, yPosition, 500, 20);
                contentStream.fill();

                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                contentStream.newLineAtOffset(60, yPosition + 5);
                contentStream.showText("Date");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Type");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("Statut");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Lieu");
                contentStream.endText();

                yPosition -= 25;
                for (RendezVous r : history) {
                    if (yPosition < 50) break; // Should add new page here but for simplicity...
                    
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(r.getDate().toString());
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(r.getTypeRdv());
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(r.getStatut());
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(r.getLieu() != null ? r.getLieu() : "N/A");
                    contentStream.endText();
                    yPosition -= 20;
                }
            }
            document.save(outputPath);
        }
    }

    public void generateGlobalStatsPDF(Map<String, Object> stats, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Header Background
                contentStream.setNonStrokingColor(new Color(18, 48, 71));
                contentStream.addRect(0, 750, 600, 100);
                contentStream.fill();

                // Title
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                contentStream.newLineAtOffset(50, 785);
                contentStream.showText("RAPPORT STATISTIQUE GLOBAL");
                contentStream.endText();

                // Stats Section
                contentStream.setNonStrokingColor(Color.BLACK);
                int y = 700;

                addStatLine(contentStream, "Nombre total de patients :", stats.get("totalPatients").toString(), y);
                y -= 40;
                
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Rendez-vous par mois :");
                contentStream.endText();
                y -= 25;

                @SuppressWarnings("unchecked")
                Map<String, Integer> rdvPerMonth = (Map<String, Integer>) stats.get("rdvPerMonth");
                if (rdvPerMonth != null) {
                    for (Map.Entry<String, Integer> entry : rdvPerMonth.entrySet()) {
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                        contentStream.newLineAtOffset(70, y);
                        contentStream.showText(entry.getKey() + " : " + entry.getValue());
                        contentStream.endText();
                        y -= 20;
                    }
                }

                y -= 20;
                addStatLine(contentStream, "Taux d'annulation :", stats.get("cancellationRate").toString() + "%", y);
                
                y -= 40;
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Psychologues les plus sollicités :");
                contentStream.endText();
                y -= 25;

                @SuppressWarnings("unchecked")
                List<String> topPsys = (List<String>) stats.get("topPsys");
                if (topPsys != null) {
                    for (String psy : topPsys) {
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                        contentStream.newLineAtOffset(70, y);
                        contentStream.showText("• " + psy);
                        contentStream.endText();
                        y -= 20;
                    }
                }
            }
            document.save(outputPath);
        }
    }

    private void addStatLine(PDPageContentStream stream, String label, String value, int y) throws IOException {
        stream.beginText();
        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        stream.newLineAtOffset(50, y);
        stream.showText(label);
        stream.endText();

        stream.beginText();
        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        stream.newLineAtOffset(250, y);
        stream.showText(value);
        stream.endText();
    }
    public void generatePlanningPDF(List<RendezVous> rdvs, String period, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Header Background
                contentStream.setNonStrokingColor(new Color(26, 74, 95));
                contentStream.addRect(0, 750, 600, 100);
                contentStream.fill();

                // Title
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                contentStream.newLineAtOffset(50, 785);
                contentStream.showText("RAPPORT DES RENDEZ-VOUS");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
                contentStream.newLineAtOffset(50, 765);
                contentStream.showText("Période : " + period);
                contentStream.endText();

                // Table Header
                int yPosition = 700;
                contentStream.setNonStrokingColor(new Color(230, 230, 230));
                contentStream.addRect(50, yPosition, 500, 20);
                contentStream.fill();

                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                contentStream.newLineAtOffset(60, yPosition + 5);
                contentStream.showText("Date");
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText("Heure");
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText("Patient");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("Statut");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Mode");
                contentStream.endText();

                yPosition -= 25;
                for (RendezVous r : rdvs) {
                    if (yPosition < 50) break;
                    
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(r.getDate() != null ? r.getDate().toString() : "N/A");
                    contentStream.newLineAtOffset(80, 0);
                    contentStream.showText(r.getHeureDebut() != null ? r.getHeureDebut().toString() : "N/A");
                    contentStream.newLineAtOffset(80, 0);
                    contentStream.showText(r.getEtudiantId() != null ? "ID #" + r.getEtudiantId() : "Libre");
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(r.getStatut());
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(r.getLieu() != null ? r.getLieu() : "N/A");
                    contentStream.endText();
                    yPosition -= 20;
                }
            }
            document.save(outputPath);
        }
    }
}
