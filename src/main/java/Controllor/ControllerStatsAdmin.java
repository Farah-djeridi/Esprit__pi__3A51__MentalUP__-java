package Controllor;

import Controllor.AdminSidebarHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import models.User;
import services.ServiceUser;
import utils.SceneManager;
import utils.SessionManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerStatsAdmin {

    @FXML private WebView  webView;
    @FXML private Label    lblStatsInfo;
    @FXML private ImageView logoImage;
    @FXML private Label avatarInitials;
    @FXML private Label labelUserName;

    private final ServiceUser service = new ServiceUser();

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/Images/logo.png")));
        } catch (Exception ignored) {}

        loadCharts();
    }

    private void loadCharts() {
        List<User> all = service.getAll();

        // Compter par role
        long nbEtudiants    = all.stream().filter(u -> "etudiant".equalsIgnoreCase(u.getRole())).count();
        long nbPsychologues = all.stream().filter(u -> "psychologue".equalsIgnoreCase(u.getRole())).count();
        long nbAdmins       = all.stream().filter(u -> "admin".equalsIgnoreCase(u.getRole())).count();
        long total          = all.size();

        // Compter inscrits via Google (mot de passe genere aleatoirement = pas de compte local)
        // On detecte les comptes Google par le champ github_username ou par un pattern
        // Ici on utilise une heuristique : email contenant gmail.com
        long nbGoogle  = all.stream().filter(u -> u.getEmail() != null && u.getEmail().contains("gmail.com")).count();
        long nbNormal  = total - nbGoogle;

        lblStatsInfo.setText(total + " utilisateurs au total");

        String html = buildChartsHtml(nbEtudiants, nbPsychologues, nbAdmins, nbGoogle, nbNormal, all);
        webView.getEngine().loadContent(html, "text/html");
    }

    private String buildChartsHtml(long etudiants, long psychologues, long admins,
                                    long google, long normal, List<User> all) {

        // Calcul inscriptions par mois (6 derniers mois simulés depuis created_at)
        // On groupe par mois si created_at disponible
        Map<String, Long> byMonth = all.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(u -> {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(u.getCreatedAt());
                    return String.format("%02d/%d", cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.YEAR));
                }, Collectors.counting()));

        StringBuilder monthRows = new StringBuilder();
        byMonth.forEach((mois, count) ->
                monthRows.append("['").append(mois).append("', ").append(count).append("],\n"));

        // Si pas de données par mois, mettre des données exemple
        if (monthRows.length() == 0) {
            monthRows.append("['Jan', 2],\n['Fev', 3],\n['Mar', 1],\n['Avr', ").append(all.size()).append("],\n");
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <script type='text/javascript' src='https://www.gstatic.com/charts/loader.js'></script>\n" +
                "  <style>\n" +
                "    body { font-family: 'Segoe UI', sans-serif; background: #F4F8FF; margin: 0; padding: 20px; }\n" +
                "    h2 { color: #2C3E50; font-size: 16px; margin: 20px 0 8px 0; }\n" +
                "    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }\n" +
                "    .card { background: white; border-radius: 14px; padding: 20px;\n" +
                "            box-shadow: 0 2px 12px rgba(0,0,0,0.07); }\n" +
                "    .card-full { background: white; border-radius: 14px; padding: 20px;\n" +
                "                 box-shadow: 0 2px 12px rgba(0,0,0,0.07); margin-bottom: 24px; }\n" +
                "    .stat-row { display: flex; gap: 16px; margin-bottom: 24px; }\n" +
                "    .stat-box { flex: 1; background: white; border-radius: 14px; padding: 18px;\n" +
                "                box-shadow: 0 2px 8px rgba(0,0,0,0.06); text-align: center; }\n" +
                "    .stat-num { font-size: 32px; font-weight: bold; color: #2C3E50; }\n" +
                "    .stat-lbl { font-size: 13px; color: #7A8C9A; margin-top: 4px; }\n" +
                "  </style>\n" +
                "  <script type='text/javascript'>\n" +
                "    google.charts.load('current', {'packages':['corechart','bar','geochart']});\n" +
                "    google.charts.setOnLoadCallback(drawCharts);\n" +
                "\n" +
                "    function drawCharts() {\n" +
                "      drawPie3D();\n" +
                "      drawMethodBar();\n" +
                "      drawColumnMonth();\n" +
                "      drawDonut();\n" +
                "    }\n" +
                "\n" +
                "    // Graphique 1 : Camembert 3D par role\n" +
                "    function drawPie3D() {\n" +
                "      var data = google.visualization.arrayToDataTable([\n" +
                "        ['Role', 'Nombre'],\n" +
                "        ['Etudiants', " + etudiants + "],\n" +
                "        ['Psychologues', " + psychologues + "],\n" +
                "        ['Admins', " + admins + "]\n" +
                "      ]);\n" +
                "      var options = {\n" +
                "        title: 'Repartition par role',\n" +
                "        titleTextStyle: { color: '#2C3E50', fontSize: 15, bold: true },\n" +
                "        is3D: true,\n" +
                "        colors: ['#3498DB', '#27AE60', '#E74C3C'],\n" +
                "        pieSliceTextStyle: { color: 'white', fontSize: 13 },\n" +
                "        legend: { position: 'bottom', textStyle: { color: '#5A6C7D', fontSize: 12 } },\n" +
                "        backgroundColor: 'transparent',\n" +
                "        chartArea: { width: '90%', height: '75%' }\n" +
                "      };\n" +
                "      var chart = new google.visualization.PieChart(document.getElementById('chart_pie3d'));\n" +
                "      chart.draw(data, options);\n" +
                "    }\n" +
                "\n" +
                "    // Graphique 2 : Barres 3D methode inscription (Normal vs Google)\n" +
                "    function drawMethodBar() {\n" +
                "      var data = google.visualization.arrayToDataTable([\n" +
                "        ['Methode', 'Nombre', { role: 'style' }],\n" +
                "        ['Inscription normale', " + normal + ", 'color: #2C5F8A'],\n" +
                "        ['Connexion Google', " + google + ", 'color: #DB4437']\n" +
                "      ]);\n" +
                "      var options = {\n" +
                "        title: 'Methode d inscription',\n" +
                "        titleTextStyle: { color: '#2C3E50', fontSize: 15, bold: true },\n" +
                "        legend: { position: 'none' },\n" +
                "        backgroundColor: 'transparent',\n" +
                "        chartArea: { width: '80%', height: '70%' },\n" +
                "        hAxis: { textStyle: { color: '#5A6C7D' } },\n" +
                "        vAxis: { textStyle: { color: '#5A6C7D' }, minValue: 0 },\n" +
                "        bar: { groupWidth: '50%' },\n" +
                "        enableInteractivity: true\n" +
                "      };\n" +
                "      var chart = new google.visualization.ColumnChart(document.getElementById('chart_method'));\n" +
                "      chart.draw(data, options);\n" +
                "    }\n" +
                "\n" +
                "    // Graphique 3 : Courbe inscriptions par mois\n" +
                "    function drawColumnMonth() {\n" +
                "      var data = google.visualization.arrayToDataTable([\n" +
                "        ['Mois', 'Inscriptions'],\n" +
                "        " + monthRows + "\n" +
                "      ]);\n" +
                "      var options = {\n" +
                "        title: 'Inscriptions par mois',\n" +
                "        titleTextStyle: { color: '#2C3E50', fontSize: 15, bold: true },\n" +
                "        legend: { position: 'none' },\n" +
                "        backgroundColor: 'transparent',\n" +
                "        chartArea: { width: '85%', height: '70%' },\n" +
                "        colors: ['#8E44AD'],\n" +
                "        lineWidth: 3,\n" +
                "        pointSize: 6,\n" +
                "        hAxis: { textStyle: { color: '#5A6C7D' } },\n" +
                "        vAxis: { textStyle: { color: '#5A6C7D' }, minValue: 0 }\n" +
                "      };\n" +
                "      var chart = new google.visualization.LineChart(document.getElementById('chart_month'));\n" +
                "      chart.draw(data, options);\n" +
                "    }\n" +
                "\n" +
                "    // Graphique 4 : Donut total\n" +
                "    function drawDonut() {\n" +
                "      var data = google.visualization.arrayToDataTable([\n" +
                "        ['Type', 'Nombre'],\n" +
                "        ['Normale', " + normal + "],\n" +
                "        ['Google OAuth', " + google + "]\n" +
                "      ]);\n" +
                "      var options = {\n" +
                "        title: 'Normal vs Google OAuth',\n" +
                "        titleTextStyle: { color: '#2C3E50', fontSize: 15, bold: true },\n" +
                "        pieHole: 0.45,\n" +
                "        is3D: false,\n" +
                "        colors: ['#2C5F8A', '#DB4437'],\n" +
                "        legend: { position: 'bottom', textStyle: { color: '#5A6C7D', fontSize: 12 } },\n" +
                "        backgroundColor: 'transparent',\n" +
                "        chartArea: { width: '90%', height: '75%' }\n" +
                "      };\n" +
                "      var chart = new google.visualization.PieChart(document.getElementById('chart_donut'));\n" +
                "      chart.draw(data, options);\n" +
                "    }\n" +
                "  </script>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "  <!-- Compteurs en haut -->\n" +
                "  <div class='stat-row'>\n" +
                "    <div class='stat-box'>\n" +
                "      <div class='stat-num' style='color:#2C3E50'>" + (etudiants + psychologues + admins) + "</div>\n" +
                "      <div class='stat-lbl'>Total utilisateurs</div>\n" +
                "    </div>\n" +
                "    <div class='stat-box'>\n" +
                "      <div class='stat-num' style='color:#3498DB'>" + etudiants + "</div>\n" +
                "      <div class='stat-lbl'>Etudiants</div>\n" +
                "    </div>\n" +
                "    <div class='stat-box'>\n" +
                "      <div class='stat-num' style='color:#27AE60'>" + psychologues + "</div>\n" +
                "      <div class='stat-lbl'>Psychologues</div>\n" +
                "    </div>\n" +
                "    <div class='stat-box'>\n" +
                "      <div class='stat-num' style='color:#DB4437'>" + google + "</div>\n" +
                "      <div class='stat-lbl'>Via Google</div>\n" +
                "    </div>\n" +
                "    <div class='stat-box'>\n" +
                "      <div class='stat-num' style='color:#8E44AD'>" + normal + "</div>\n" +
                "      <div class='stat-lbl'>Inscription normale</div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- Graphiques en grille 2x2 -->\n" +
                "  <div class='grid'>\n" +
                "    <div class='card'>\n" +
                "      <div id='chart_pie3d' style='width:100%; height:280px;'></div>\n" +
                "    </div>\n" +
                "    <div class='card'>\n" +
                "      <div id='chart_method' style='width:100%; height:280px;'></div>\n" +
                "    </div>\n" +
                "    <div class='card'>\n" +
                "      <div id='chart_month' style='width:100%; height:280px;'></div>\n" +
                "    </div>\n" +
                "    <div class='card'>\n" +
                "      <div id='chart_donut' style='width:100%; height:280px;'></div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    // Navigation
    @FXML public void goToDashboard() { AdminSidebarHelper.goToAccueil(); }
    @FXML public void goToUsers()     { AdminSidebarHelper.goToUtilisateurs(); }
    @FXML public void handleLogout()  { AdminSidebarHelper.logout(); }

    @FXML public void onNavHomeClicked(javafx.scene.input.MouseEvent e)         { AdminSidebarHelper.goToAccueil(); }
    @FXML public void onNavSuiviClicked(javafx.scene.input.MouseEvent e)        { AdminSidebarHelper.goToSuiviMental(); }
    @FXML public void onNavForumClicked(javafx.scene.input.MouseEvent e)        { AdminSidebarHelper.goToForum(); }
    @FXML public void onNavRdvClicked(javafx.scene.input.MouseEvent e)          { AdminSidebarHelper.goToRendezVous(); }
    @FXML public void onNavUtilisateursClicked(javafx.scene.input.MouseEvent e) { AdminSidebarHelper.goToUtilisateurs(); }
    @FXML public void onNavDossiersClicked(javafx.scene.input.MouseEvent e)     { AdminSidebarHelper.goToDossiers(); }
    @FXML public void onNavContenusClicked(javafx.scene.input.MouseEvent e)     { AdminSidebarHelper.goToContenus(); }
    @FXML public void onNavActivitesClicked(javafx.scene.input.MouseEvent e)    { AdminSidebarHelper.goToActivites(); }
    @FXML public void onNavReservationsClicked(javafx.scene.input.MouseEvent e) { AdminSidebarHelper.goToReservations(); }
    @FXML public void onNavHoverEnter(javafx.scene.input.MouseEvent e)          { }
    @FXML public void onNavHoverExit(javafx.scene.input.MouseEvent e)           { }
    @FXML public void onLogout(javafx.event.ActionEvent e)                      { AdminSidebarHelper.logout(); }

    @FXML public void onHoverEnter(javafx.scene.input.MouseEvent e) {
        HBox h = (HBox) e.getSource();
        h.setStyle("-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
    }
    @FXML public void onHoverExit(javafx.scene.input.MouseEvent e) {
        HBox h = (HBox) e.getSource();
        h.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 10 12; -fx-cursor: hand;");
    }
    @FXML public void onNavSuiviStatsClicked(MouseEvent e)    { AdminSidebarHelper.goToSuiviMental(); }
    @FXML public void onNavObjectifsClicked(MouseEvent e)     { AdminSidebarHelper.goToObjectifs(); }
    @FXML public void onNavSujetsClicked(MouseEvent e)        { AdminSidebarHelper.goToForum(); }
    @FXML public void onNavCommentairesClicked(MouseEvent e)  { AdminSidebarHelper.goToCommentaires(); }
    @FXML public void onSubmenuHoverEnter(MouseEvent e)       { }
    @FXML public void onSubmenuHoverExit(MouseEvent e)        { }
}
