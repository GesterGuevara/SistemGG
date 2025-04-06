package Class;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class VendaDAO {
    
    public static void buscarProdutosPorNome(String nome, DefaultTableModel model) {
        Connection conn = new Connect().getConnection();
        String sql = "SELECT id, nome, descricao, preco, quantidade FROM produtos WHERE nome LIKE ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");
            ResultSet rs = stmt.executeQuery();
            
            model.setRowCount(0); // Limpiar tabla
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getDouble("preco"),
                    rs.getInt("quantidade"),
                    rs.getInt("id") // Guardamos el ID oculto
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar productos: " + e.getMessage());
        }
    }
    
    public static boolean registrarVenda(DefaultTableModel model) {
        Connection conn = new Connect().getConnection();
        
        try {
            conn.setAutoCommit(false); // Iniciar transacción
            
            // 1. Registrar la venta principal
            String sqlVenda = "INSERT INTO vendas (total) VALUES (?)";
            double total = calcularTotal(model);
            
            try (PreparedStatement stmtVenda = conn.prepareStatement(sqlVenda, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmtVenda.setDouble(1, total);
                stmtVenda.executeUpdate();
                
                // Obtener ID de la venta recién creada
                ResultSet rs = stmtVenda.getGeneratedKeys();
                int vendaId = rs.next() ? rs.getInt(1) : 0;
                
                // 2. Registrar items de venta
                String sqlItem = "INSERT INTO itens_venda (venda_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                    for (int i = 0; i < model.getRowCount(); i++) {
                        int produtoId = (int) model.getValueAt(i, 4); // ID oculto
                        int quantidade = (int) model.getValueAt(i, 3);
                        double preco = (double) model.getValueAt(i, 2);
                        
                        stmtItem.setInt(1, vendaId);
                        stmtItem.setInt(2, produtoId);
                        stmtItem.setInt(3, quantidade);
                        stmtItem.setDouble(4, preco);
                        stmtItem.addBatch();
                    }
                    stmtItem.executeBatch();
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(null, "Error al registrar venta: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {}
        }
    }
    
    private static double calcularTotal(DefaultTableModel model) {
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (double) model.getValueAt(i, 2) * (int) model.getValueAt(i, 3);
        }
        return total;
    }
}