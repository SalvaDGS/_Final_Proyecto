package com.avtech.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FacturaDAO {

    /**
     * Crea la tabla Facturas si no existe, enlazándola con Clientes.
     */
    public static void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS Facturas ("
                   + "id_factura INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "numero_factura VARCHAR(50) NOT NULL UNIQUE, "
                   + "id_cliente INTEGER, "
                   + "numero_proyecto VARCHAR(100), " 
                   + "periodo_facturado VARCHAR(50), "
                   + "fecha_emision DATE NOT NULL, "
                   + "total_dias INTEGER NOT NULL, "
                   + "subtotal DECIMAL(10,2) NOT NULL, "
                   + "total_final DECIMAL(10,2) NOT NULL, "
                   + "ruta_pdf VARCHAR(255), "
                   + "FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente) ON DELETE SET NULL"
                   + ")";
                   
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("✅ Tabla 'Facturas' verificada/creada.");
            
        } catch (SQLException e) {
            System.out.println("❌ Error al crear tabla Facturas.");
            e.printStackTrace();
        }
    }

    /**
     * Guarda el registro de una factura recién generada en PDF.
     */
    public static boolean registrarFactura(String numFactura, int idCliente, String fecha, int dias, double subtotal, double totalFinal, String rutaPdf) {
        String sql = "INSERT INTO Facturas (numero_factura, id_cliente, fecha_emision, total_dias, subtotal, total_final, ruta_pdf) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numFactura);
            pstmt.setInt(2, idCliente);
            pstmt.setString(3, fecha); // SQLite maneja las fechas como texto en formato YYYY-MM-DD
            pstmt.setInt(4, dias);
            pstmt.setDouble(5, subtotal);
            pstmt.setDouble(6, totalFinal);
            pstmt.setString(7, rutaPdf);
            
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Obtiene el historial completo de facturas, incluyendo el nombre del cliente.
     */
    public static java.util.List<Factura> obtenerTodas() {
        java.util.List<Factura> lista = new java.util.ArrayList<>();
        // Hacemos JOIN para obtener el nombre del cliente en lugar de solo su ID
        String sql = "SELECT f.*, c.nombre_calendario " +
                     "FROM Facturas f " +
                     "INNER JOIN Clientes c ON f.id_cliente = c.id_cliente " +
                     "ORDER BY f.id_factura DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                lista.add(new Factura(
                    rs.getInt("id_factura"),
                    rs.getString("numero_factura"),
                    rs.getString("nombre_calendario"),
                    rs.getString("fecha_emision"),
                    rs.getInt("total_dias"),
                    rs.getDouble("subtotal"),
                    rs.getDouble("total_final"),
                    rs.getString("ruta_pdf")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener el historial de facturas.");
            e.printStackTrace();
        }
        return lista;
    }
    /**
     * Elimina una factura de la base de datos según su ID.
     */
    public static boolean eliminarFactura(int idFactura) {
        String sql = "DELETE FROM Facturas WHERE id_factura = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idFactura);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar la factura de la base de datos.");
            e.printStackTrace();
            return false;
        }
    }
}