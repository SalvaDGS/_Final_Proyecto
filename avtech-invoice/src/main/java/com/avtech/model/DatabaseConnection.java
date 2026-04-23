package com.avtech.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase centralizada para gestionar la conexión a la base de datos SQLite.
 * Esta clase es utilizada por los objetos DAO (UsuarioDAO, ClienteDAO, FacturaDAO)
 * para interactuar con los datos.
 */
public class DatabaseConnection {

    /**
     * URL de la base de datos. 
     * "jdbc:sqlite:" indica el protocolo.
     * "avtech_data.db" es el nombre del archivo que se creará en la raíz del proyecto.
     */
    private static final String URL = "jdbc:sqlite:avtech_data.db";

    /**
     * Proporciona una conexión activa a la base de datos.
     * Si el archivo de la base de datos no existe, SQLite lo crea automáticamente.
     * * @return Connection objeto de conexión listo para usar.
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Intentamos establecer la conexión
            conn = DriverManager.getConnection(URL);
            
            // Opcional: Activar las claves foráneas en SQLite (por defecto vienen desactivadas)
            // Esto asegura que la relación entre Facturas y Clientes funcione correctamente.
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");
            
        } catch (SQLException e) {
            System.err.println("❌ Error crítico: No se pudo conectar a la base de datos SQLite.");
            System.err.println("Mensaje: " + e.getMessage());
        }
        return conn;
    }
}