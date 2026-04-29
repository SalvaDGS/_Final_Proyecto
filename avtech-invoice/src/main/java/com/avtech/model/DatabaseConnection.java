package com.avtech.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

/**
 * Clase centralizada para gestionar la conexión a la base de datos SQLite.
 * Esta clase es utilizada por los objetos DAO (UsuarioDAO, ClienteDAO, FacturaDAO)
 * para interactuar con los datos.
 */
public class DatabaseConnection {

    // Definimos la ruta en el Home del usuario para evitar problemas de permisos al empaquetar
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String APP_FOLDER = USER_HOME + File.separator + "AVTech Invoice" + File.separator;
    private static final String URL = "jdbc:sqlite:" + APP_FOLDER + "avtech_data.db";

    /**
     * Proporciona una conexión activa a la base de datos.
     * Si el archivo de la base de datos no existe, SQLite lo crea automáticamente.
     * * @return Connection objeto de conexión listo para usar.
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // VITAL: Crear la carpeta si no existe antes de conectar
            File folder = new File(APP_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs(); 
                System.out.println("Carpeta de aplicación creada en: " + APP_FOLDER);
            }

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