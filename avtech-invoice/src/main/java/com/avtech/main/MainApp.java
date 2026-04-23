package com.avtech.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Importamos nuestros DAOs para inicializar la base de datos
import com.avtech.model.UsuarioDAO;
import com.avtech.model.ClienteDAO;
import com.avtech.model.FacturaDAO;
import com.avtech.model.OpcionesDAO;

public class MainApp extends Application {

    /**
     * El método init() se ejecuta ANTES de que se cargue la interfaz gráfica.
     * Es el lugar perfecto para preparar nuestra base de datos SQLite.
     */
    @Override
    public void init() throws Exception {
        System.out.println("Iniciando AVTech Invoice...");
        System.out.println("Comprobando el estado de la base de datos local...");
        
        // Llamamos a los métodos que crean las tablas si no existen
        UsuarioDAO.crearTabla();
        ClienteDAO.crearTabla();
        FacturaDAO.crearTabla();
        OpcionesDAO.crearTabla();
        
        System.out.println("✅ Base de datos lista para operar.");
    }

    /**
     * El método start() construye y muestra la primera ventana (Login).
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            // Creamos la escena principal
            Scene scene = new Scene(root);
            
            // Configuramos la ventana
            primaryStage.setTitle("AVTech Invoice");
            primaryStage.setScene(scene);
            
            // Centramos la ventana en la pantalla (Opcional, pero queda muy bien)
            primaryStage.centerOnScreen();

            primaryStage.show();

        } catch (Exception e) {
            System.err.println("❌ Error al cargar la interfaz de Login: " + e.getMessage());
            e.printStackTrace(); // Imprime el error completo en consola para facilitar la depuración
        }
    }

    /**
     * El método main es el lanzador tradicional de Java.
     * En aplicaciones JavaFX, simplemente llama a launch(), el cual 
     * automáticamente ejecuta init() y luego start().
     */
    public static void main(String[] args) {
        launch(args);
    }
}