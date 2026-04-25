package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private final String URL      = AppConfig.get("db.url",      "jdbc:mysql://127.0.0.1:3306/projet3a51");
    private final String USERNAME = AppConfig.get("db.username",  "root");
    private final String PASSWORD = AppConfig.get("db.password",  "");
    private Connection cnx;

    private MyDataBase() {
        try {
            this.cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion a la base de donnees reussie !");
        } catch (SQLException e) {
            System.err.println("ERREUR de connexion MySQL : " + e.getMessage());
            System.err.println("  Verifiez que XAMPP est demarre et que la base existe.");
            System.err.println("  URL : " + URL);
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("Reconnexion a la base...");
                cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Reconnexion reussie !");
            }
        } catch (SQLException e) {
            System.err.println("Reconnexion echouee : " + e.getMessage());
        }
        return cnx;
    }
}
