package com.avtech.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAO {

    public static void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS Usuario ("
                   + "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "email VARCHAR(100) UNIQUE, "
                   + "password VARCHAR(100), "
                   + "google_id VARCHAR(255) UNIQUE, "
                   + "nombre_fiscal VARCHAR(100) NOT NULL, "
                   + "nif_cif VARCHAR(20) NOT NULL, "
                   + "domicilio_fiscal VARCHAR(255), "
                   + "iban VARCHAR(34), "
                   + "porcentaje_iva DECIMAL(5,2) DEFAULT 21.00, "
                   + "porcentaje_irpf DECIMAL(5,2) DEFAULT 15.00,"
                   + "ruta_logo VARCHAR(255) "
                   + ")";
                   
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ Tabla 'Usuario' verificada/creada.");
        } catch (SQLException e) {
            System.out.println("❌ Error al crear tabla Usuario.");
            e.printStackTrace();
        }
    }

   
    // AUTENTICACIÓN SEGURA
   
    public static Usuario autenticar(String email, String password) {
        //buscamos al usuario por su email
        String sql = "SELECT * FROM Usuario WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String hashGuardado = rs.getString("password");
                
                // BCrypt compara la contraseña tecleada con el hash cifrado de la base de datos
                if (org.mindrot.jbcrypt.BCrypt.checkpw(password, hashGuardado)) {
                    return new Usuario(rs.getInt("id_usuario"), rs.getString("email"), rs.getString("password"),
                        rs.getString("google_id"), rs.getString("nombre_fiscal"), rs.getString("nif_cif"),
                        rs.getString("domicilio_fiscal"), rs.getString("iban"), rs.getDouble("porcentaje_iva"),
                        rs.getDouble("porcentaje_irpf"), rs.getString("ruta_logo"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    
    // REGISTRO SEGURO CON ENCRIPTACIÓN
        public static boolean registrar(String email, String password, String nombreFiscal, String nifCif, String domicilio, String iban, double iva, double irpf) {
        String sql = "INSERT INTO Usuario (email, password, nombre_fiscal, nif_cif, domicilio_fiscal, iban, porcentaje_iva, porcentaje_irpf) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Creamos el Hash seguro antes de guardarlo en SQLite
            String hashFuerte = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
            
            pstmt.setString(1, email);
            pstmt.setString(2, hashFuerte); // Guardamos la huella matemática, NO el texto
            pstmt.setString(3, nombreFiscal);
            pstmt.setString(4, nifCif);
            pstmt.setString(5, domicilio);
            pstmt.setString(6, iban);
            pstmt.setDouble(7, iva);
            pstmt.setDouble(8, irpf);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String obtenerNombreUsuario() {
        String sql = "SELECT nombre_fiscal FROM Usuario LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getString("nombre_fiscal");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Usuario no registrado";
    }

    public static Usuario buscarPorGoogleId(String googleId) {
        String sql = "SELECT * FROM Usuario WHERE google_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, googleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Usuario(rs.getInt("id_usuario"), rs.getString("email"), rs.getString("password"),
                        rs.getString("google_id"), rs.getString("nombre_fiscal"), rs.getString("nif_cif"),
                        rs.getString("domicilio_fiscal"), rs.getString("iban"), rs.getDouble("porcentaje_iva"),
                        rs.getDouble("porcentaje_irpf"), rs.getString("ruta_logo"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM Usuario WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Usuario(rs.getInt("id_usuario"), rs.getString("email"), rs.getString("password"),
                        rs.getString("google_id"), rs.getString("nombre_fiscal"), rs.getString("nif_cif"),
                        rs.getString("domicilio_fiscal"), rs.getString("iban"), rs.getDouble("porcentaje_iva"),
                        rs.getDouble("porcentaje_irpf"), rs.getString("ruta_logo"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static void vincularCuentaGoogle(String email, String googleId) {
        String sql = "UPDATE Usuario SET google_id = ? WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, googleId);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    /**
     * Actualiza los datos del usuario. Si la contraseña viene vacía, no se cambia.
     */
    public static boolean actualizar(int idUsuario, String email, String password, String nombreFiscal, String nifCif, String domicilio, String iban, double iva, double irpf) {
        boolean cambiarPassword = (password != null && !password.trim().isEmpty());
        String sql;
        
        if (cambiarPassword) {
            sql = "UPDATE Usuario SET email = ?, password = ?, nombre_fiscal = ?, nif_cif = ?, domicilio_fiscal = ?, iban = ?, porcentaje_iva = ?, porcentaje_irpf = ? WHERE id_usuario = ?";
        } else {
            sql = "UPDATE Usuario SET email = ?, nombre_fiscal = ?, nif_cif = ?, domicilio_fiscal = ?, iban = ?, porcentaje_iva = ?, porcentaje_irpf = ? WHERE id_usuario = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            pstmt.setString(paramIndex++, email);
            
            if (cambiarPassword) {
                // Si ha escrito una contraseña nueva, la encriptamos antes de guardarla
                String hashFuerte = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
                pstmt.setString(paramIndex++, hashFuerte);
            }
            
            pstmt.setString(paramIndex++, nombreFiscal);
            pstmt.setString(paramIndex++, nifCif);
            pstmt.setString(paramIndex++, domicilio);
            pstmt.setString(paramIndex++, iban);
            pstmt.setDouble(paramIndex++, iva);
            pstmt.setDouble(paramIndex++, irpf);
            pstmt.setInt(paramIndex, idUsuario);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar el perfil de usuario.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina permanentemente la cuenta de usuario.
     */
    public static boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM Usuario WHERE id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}