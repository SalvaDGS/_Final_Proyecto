package com.avtech.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClienteDAO {

    /**
     * Crea la tabla Clientes si no existe.
     */
    public static void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS Clientes ("
                   + "id_cliente INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "nombre_calendario VARCHAR(100) NOT NULL UNIQUE, "
                   + "razon_social VARCHAR(150) NOT NULL, "
                   + "cif_nif VARCHAR(20) NOT NULL, "
                   + "direccion VARCHAR(255), "
                   + "tarifa_jornada DECIMAL(10,2) NOT NULL"
                   + ")";
                   
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("✅ Tabla 'Clientes' verificada/creada.");
            
        } catch (SQLException e) {
            System.out.println("❌ Error al crear tabla Clientes.");
            e.printStackTrace();
        }
    }

    /**
     * Inserta un nuevo cliente desde la interfaz gráfica.
     * He cambiado el nombre a "insertar" para que coincida con el controlador.
     */
    public static boolean insertar(String nombreCalendario, String razonSocial, String cifNif, String direccion, double tarifa) {
        String sql = "INSERT INTO Clientes (nombre_calendario, razon_social, cif_nif, direccion, tarifa_jornada) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreCalendario);
            pstmt.setString(2, razonSocial);
            pstmt.setString(3, cifNif);
            pstmt.setString(4, direccion);
            pstmt.setDouble(5, tarifa);
            
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.out.println("❌ Error al guardar el cliente. Posible nombre de calendario duplicado.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un cliente basándose en su ID.
     */
    public static boolean eliminarCliente(int idCliente) {
        String sql = "DELETE FROM Clientes WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idCliente);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca el ID del cliente utilizando el nombre del calendario de Google.
     * (Corregido: Antes ponía nombre_fiscal, ahora busca en nombre_calendario).
     */
    public static int obtenerIdPorNombre(String nombreCalendario) {
        String sql = "SELECT id_cliente FROM Clientes WHERE nombre_calendario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreCalendario);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_cliente");
            }
            
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        
        return -1; // No encontrado
    }

    /**
     * Obtiene la tarifa por jornada configurada para un cliente.
     */
    public static double obtenerTarifaPorNombre(String nombreCalendario) {
        String sql = "SELECT tarifa_jornada FROM Clientes WHERE nombre_calendario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreCalendario);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("tarifa_jornada");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Si no lo encuentra o hay error
    }

    /**
     * Obtiene la lista completa de clientes de la base de datos.
     */
    public static java.util.List<Cliente> obtenerTodos() {
        java.util.List<Cliente> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM Clientes ORDER BY razon_social ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre_calendario"),
                    rs.getString("razon_social"),
                    rs.getString("cif_nif"),
                    rs.getString("direccion"),
                    rs.getDouble("tarifa_jornada")
                );
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener la lista de clientes.");
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Actualiza los datos de un cliente existente.
     */
    public static boolean actualizar(int idCliente, String nombreCalendario, String razonSocial, String cifNif, String direccion, double tarifa) {
        String sql = "UPDATE Clientes SET nombre_calendario = ?, razon_social = ?, cif_nif = ?, direccion = ?, tarifa_jornada = ? WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreCalendario);
            pstmt.setString(2, razonSocial);
            pstmt.setString(3, cifNif);
            pstmt.setString(4, direccion);
            pstmt.setDouble(5, tarifa);
            pstmt.setInt(6, idCliente); // El ID es fundamental para saber a quién actualizamos
            
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar el cliente en SQLite.");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Obtiene el CIF/NIF del cliente utilizando su nombre de calendario.
     */
    public static String obtenerCifPorNombre(String nombreCalendario) {
        String sql = "SELECT cif_nif FROM Clientes WHERE nombre_calendario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreCalendario);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("cif_nif");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "CIF No especificado";
    }

    /**
     * Obtiene la dirección del cliente utilizando su nombre de calendario.
     */
    public static String obtenerDireccionPorNombre(String nombreCalendario) {
        String sql = "SELECT direccion FROM Clientes WHERE nombre_calendario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreCalendario);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("direccion");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Dirección no especificada";
    }
}