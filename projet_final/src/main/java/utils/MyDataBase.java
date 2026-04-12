package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private final String URL      = "jdbc:mysql://127.0.0.1:3306/projet3a51";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private Connection cnx;

    private MyDataBase() {
        try {
            this.cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion à la base de données réussie !");
        } catch (SQLException e) {
            System.err.println("❌ ERREUR de connexion MySQL : " + e.getMessage());
            System.err.println("   → Vérifiez que XAMPP est démarré et que la base 'projet3a51' existe.");
            System.err.println("   → URL : " + URL);
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();
        return instance;
    }

    public Connection getCnx() {
        // Si la connexion est tombée, on tente de se reconnecter
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("⚠️ Reconnexion à la base...");
                cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("✅ Reconnexion réussie !");
            }
        } catch (SQLException e) {
            System.err.println("❌ Reconnexion échouée : " + e.getMessage());
        }
        return cnx;
    }
}