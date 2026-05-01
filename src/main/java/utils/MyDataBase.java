package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private Connection cnx;

    private MyDataBase() {
        String url      = "jdbc:mysql://127.0.0.1:3306/projet3a51";
        String username = "root";
        String password = "";
        // Utiliser AppConfig si disponible
        try {
            url      = AppConfig.get("db.url",      url);
            username = AppConfig.get("db.username",  username);
            password = AppConfig.get("db.password",  password);
        } catch (Exception ignored) {}

        try {
            this.cnx = DriverManager.getConnection(url, username, password);
            System.out.println("Connexion à la base de données réussie!");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion MySQL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();
        return instance;
    }

    /** Utilisé par le module user (main) */
    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed())
                cnx = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/projet3a51", "root", "");
        } catch (SQLException e) {
            System.err.println("Reconnexion échouée : " + e.getMessage());
        }
        return cnx;
    }

    /** Utilisé par le module activiter */
    public Connection getConnection() {
        return getCnx();
    }
}
