package services;

import interfaces.IService;
import models.Categorie;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategorie implements IService<Categorie> {

    private Connection cnx;

    public ServiceCategorie() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Categorie categorie) {
        String query = "INSERT INTO categorie (nom, description, date_creation) VALUES (?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, categorie.getNom());
            pst.setString(2, categorie.getDescription());
            pst.setTimestamp(3, categorie.getDateCreation());
            pst.executeUpdate();
            System.out.println("Categorie ajoutee avec succes");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la categorie: " + e.getMessage());
        }
    }

    @Override
    public List<Categorie> getAll() {
        List<Categorie> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Categorie c = new Categorie();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescription(rs.getString("description"));
                c.setDateCreation(rs.getTimestamp("date_creation"));
                categories.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recuperation des categories: " + e.getMessage());
        }
        return categories;
    }

    @Override
    public void update(Categorie categorie) {
        String query = "UPDATE categorie SET nom = ?, description = ? WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, categorie.getNom());
            pst.setString(2, categorie.getDescription());
            pst.setInt(3, categorie.getId());
            pst.executeUpdate();
            System.out.println("Categorie modifiee avec succes");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la categorie: " + e.getMessage());
        }
    }

    @Override
    public void delete(Categorie categorie) {
        String query = "DELETE FROM categorie WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, categorie.getId());
            pst.executeUpdate();
            System.out.println("Categorie supprimee avec succes");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la categorie: " + e.getMessage());
        }
    }

    public Categorie getById(int id) {
        String query = "SELECT * FROM categorie WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Categorie c = new Categorie();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescription(rs.getString("description"));
                c.setDateCreation(rs.getTimestamp("date_creation"));
                return c;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recuperation de la categorie: " + e.getMessage());
        }
        return null;
    }
    @Override
    public Categorie find(int id) {
        String query = "SELECT * FROM categorie WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Categorie c = new Categorie();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescription(rs.getString("description"));
                c.setDateCreation(rs.getTimestamp("date_creation"));
                return c;
            }
        } catch (SQLException e) {
            System.out.println("Erreur find categorie: " + e.getMessage());
        }
        return null;
    }
}
