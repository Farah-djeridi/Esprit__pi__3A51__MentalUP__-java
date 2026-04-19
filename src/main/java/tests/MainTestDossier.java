package tests;

import Models.Dossier;
import Services.ServiceDossier;

import java.sql.Date;
import java.util.List;

public class MainTestDossier {

    public static void main(String[] args) {

        // ⚡ Créer le service (assure-toi que le constructeur gère la connexion)
        ServiceDossier service = new ServiceDossier();

        // =======================
        // ✅ TEST ADD
        // =======================
        Dossier d = new Dossier();
        d.setDateCreation(new Date(System.currentTimeMillis()));
        d.setNotesGenerales("Patient stable");
        d.setNiveauRisque("Faible");
        d.setPatientId(1);
        d.setPsychologueId(2);
        d.setAiSummary("Résumé AI test");


        service.add(d);
        System.out.println("✅ Dossier ajouté");

        // =======================
        // ✅ TEST READ
        // =======================
        List<Dossier> list = service.getAll();
        System.out.println("📋 Tous les dossiers :");
        for (Dossier dossier : list) {
            System.out.println(dossier);
        }

        // =======================
        // ✅ TEST UPDATE (modifier le premier dossier)
        // =======================
        if (!list.isEmpty()) {
            Dossier first = list.get(0);
            first.setNotesGenerales("Patient amélioré");
            first.setNiveauRisque("Modéré");
            service.update(first);
            System.out.println("✏️ Dossier modifié");
        }

        // =======================
        // ✅ TEST DELETE (supprimer le dernier dossier)
        // =======================
       /* if (!list.isEmpty()) {
            Dossier last = list.get(list.size() - 1);
            service.delete(last);
            System.out.println("🗑️ Dossier supprimé");
        } */

        // =======================
        // ✅ Vérifier la liste après suppression
        // =======================
        List<Dossier> afterDelete = service.getAll();
        System.out.println("📋 Dossiers restants : " + afterDelete.size());
    }
}