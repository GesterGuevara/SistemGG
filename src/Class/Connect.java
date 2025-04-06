package Class;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Connect {
    
    private static final String URL = "jdbc:mysql://localhost:3307/SistemGG";
    private static final String USER = "root";
    private static final String PASS = "root";
    
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error de conexión:\n" + e.getMessage(), 
                "Error de BD", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
   
    public static boolean testConnection() {
        try (Connection conn = new Connect().getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}