package test;

import models.Sujet;
import models.Commentaire;
import services.ServiceSujet;
import services.ServiceCommentaire;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        ServiceSujet ss = new ServiceSujet();
        ServiceCommentaire sc = new ServiceCommentaire();

        // =========================
        // 🔹 1. AJOUT SUJET
        // =========================
        Sujet s = new Sujet();
        s.setTitre("Sujet Test");
        s.setContenu("Contenu du sujet");
        s.setAnonyme(false);
        s.setScoreToxicite(0.1);
        s.setEstToxique(false);

        ss.add(s);

        // =========================
        // 🔹 2. AFFICHAGE SUJETS
        // =========================
        System.out.println("=== LISTE SUJETS ===");
        List<Sujet> sujets = ss.getAll();
        sujets.forEach(System.out::println);

        // =========================
        // 🔹 3. UPDATE SUJET
        // =========================
        if (!sujets.isEmpty()) {
            Sujet sUpdate = sujets.get(0);
            sUpdate.setTitre("Sujet Modifié");
            sUpdate.setContenu("Contenu modifié");

            ss.update(sUpdate);
        }

        // =========================
        // 🔹 4. AJOUT COMMENTAIRE
        // =========================
        if (!sujets.isEmpty()) {

            int sujetId = sujets.get(0).getId();

            Commentaire c = new Commentaire();
            c.setContenu("Commentaire test");
            c.setAnonyme(false);
            c.setSujetId(sujetId);
            c.setScoreToxicite(0.2);
            c.setEstToxique(false);

            sc.add(c);
        }

        // =========================
        // 🔹 5. AFFICHAGE COMMENTAIRES
        // =========================
        System.out.println("=== LISTE COMMENTAIRES ===");
        List<Commentaire> commentaires = sc.getAll();
        commentaires.forEach(System.out::println);

        // =========================
        // 🔹 6. UPDATE COMMENTAIRE
        // =========================
        if (!commentaires.isEmpty()) {
            Commentaire cUpdate = commentaires.get(0);
            cUpdate.setContenu("Commentaire modifié !");

            sc.update(cUpdate);
        }

        // =========================
        // 🔹 7. DELETE COMMENTAIRE
        // =========================
        if (!commentaires.isEmpty()) {
            Commentaire cDelete = commentaires.get(0);
            sc.delete(cDelete);
        }

        // =========================
        // 🔹 8. DELETE SUJET
        // =========================
        if (!sujets.isEmpty()) {
            Sujet sDelete = sujets.get(0);
            ss.delete(sDelete);
        }

        // =========================
        // 🔹 9. VERIFICATION FINALE
        // =========================
        System.out.println("=== SUJETS APRES DELETE ===");
        ss.getAll().forEach(System.out::println);

        System.out.println("=== COMMENTAIRES APRES DELETE ===");
        sc.getAll().forEach(System.out::println);
    }
}