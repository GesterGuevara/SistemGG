package Class;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class GerenciadorUsuarios {
    
    public static boolean adicionarUsuario(String login, String senha) {
        Connection conn = new Connect().getConnection();
        String sql = "INSERT INTO contas (login, senha) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, senha); // En producción, usa BCrypt para hashear
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            // Si es error de duplicado (login ya existe)
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(null, "Login já está em uso!", "Erro", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao criar conta: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }
    
   public static boolean verificarCredenciais(String login, String senha) throws SQLException {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = new Connect().getConnection();
        String sql = "SELECT senha FROM contas WHERE login = ?";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, login);
        rs = stmt.executeQuery();
        
        return rs.next() && rs.getString("senha").equals(senha);
    } finally {
        if (rs != null) rs.close();
        if (stmt != null) stmt.close();
        if (conn != null) conn.close();
    }
}
}