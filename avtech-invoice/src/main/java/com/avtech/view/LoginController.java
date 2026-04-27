package com.avtech.view;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;



import com.avtech.model.Usuario;
import com.avtech.model.UsuarioDAO;


public class LoginController {

    // --- CAMPOS (Inicio de sesión clásico) ---
    @FXML private TextField txtUsuario;    // Para el Email
    @FXML private PasswordField txtPassword; // Para la Contraseña
    @FXML private Button btnLogin;         // Botón "Entrar" (Base de datos)
    @FXML private Button btnRegistro;      // Botón "Crear nueva cuenta"

   
    // Variable global para guardar quién ha iniciado sesión (vital para el PDF luego)
    public static Usuario usuarioLogueado = null;

    @FXML
    public void initialize() {
        System.out.println("Pantalla de Login cargada correctamente.");
        
        // Asignamos las acciones a los botones
        if (btnLogin != null) {
            btnLogin.setOnAction(e -> iniciarSesionLocal());
        }
        if (btnRegistro != null) {
            btnRegistro.setOnAction(e -> abrirPantallaRegistro());
        }
      
        
    }

    /**
     * Iniciar sesión con Email y Contraseña (Base de Datos)
     */
    private void iniciarSesionLocal() {
        String email = txtUsuario.getText().trim();
        String pass = txtPassword.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor, introduce tu email y contraseña.");
            return;
        }

        // Consultamos la base de datos de usuarios
        Usuario user = UsuarioDAO.autenticar(email, pass);

        if (user != null) {
            System.out.println("✅ Acceso local concedido a: " + user.getNombreFiscal());
            usuarioLogueado = user; // Guardamos el usuario en memoria para las facturas
            abrirDashboard(); 
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Autenticación", "Email o contraseña incorrectos.");
        }
    }

   

    /**
     * Abre la pantalla para registrar un usuario nuevo
     */
    private void abrirPantallaRegistro() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RegistroView.fxml"));
            stage.setScene(new Scene(root, 450, 650)); 
            stage.setTitle("AVTech - Crear Nueva Cuenta");
        } catch (Exception ex) {
            System.err.println("❌ Falta crear el archivo RegistroView.fxml");
            ex.printStackTrace();
        }
    }

    /**
     * Carga el Dashboard
     */
    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnLogin.getScene().getWindow(); 
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AVTech Invoice - Dashboard");
            stage.centerOnScreen();
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar el Dashboard.");
        }
    }

    // Adaptado para permitir distintos tipos de icono en la alerta (Error, Info, etc.)
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}