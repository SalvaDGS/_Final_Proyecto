package com.avtech.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class OpcionesDAO {

    public static void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS Opciones ("
                   + "id_opcion INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "generacion_automatica BOOLEAN DEFAULT 0, " 
                   + "plantilla_seleccionada VARCHAR(50) DEFAULT 'Estandar', " 
                   + "directorio_guardado_pdf VARCHAR(255) "
                   + ")";
                   
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("✅ Tabla 'Opciones' verificada/creada.");
            
        } catch (SQLException e) {
            System.out.println("❌ Error al crear tabla Opciones.");
            e.printStackTrace();
        }
    }
}