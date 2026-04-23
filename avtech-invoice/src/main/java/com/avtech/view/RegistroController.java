package com.avtech.view;

import com.avtech.model.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistroController {

    // Vinculación con los campos del FXML
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNombreFiscal;
    @FXML private TextField txtNifCif;
    @FXML private TextField txtDomicilio;
    @FXML private TextField txtIban;
    @FXML private TextField txtIva;
    @FXML private TextField txtIrpf;

    @FXML private Button btnRegistrar;
    @FXML private Button btnVolver;

    @FXML
    public void initialize() {
        // Asignamos las acciones a los botones
        btnRegistrar.setOnAction(e -> registrarUsuario());
        btnVolver.setOnAction(e -> volverAlLogin());
    }

    /**
     * Recoge los datos, los valida y los envía a la base de datos.
     */
    private void registrarUsuario() {
        String email = txtEmail.getText().trim();
        String pass = txtPassword.getText().trim();
        String nombre = txtNombreFiscal.getText().trim();
        String nif = txtNifCif.getText().trim();
        String domicilio = txtDomicilio.getText().trim();
        String iban = txtIban.getText().trim();
        String ivaStr = txtIva.getText().trim();
        String irpfStr = txtIrpf.getText().trim();

        // 1. Validación básica
        if (email.isEmpty() || pass.isEmpty() || nombre.isEmpty() || nif.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos obligatorios", "Por favor, rellena al menos el Email, Contraseña, Nombre Fiscal y NIF/CIF.");
            return;
        }

        // 2. Convertir impuestos a números
        double iva = 0.0;
        double irpf = 0.0;
        try {
            if (!ivaStr.isEmpty()) iva = Double.parseDouble(ivaStr.replace(",", "."));
            if (!irpfStr.isEmpty()) irpf = Double.parseDouble(irpfStr.replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de formato", "El IVA y el IRPF deben ser valores numéricos válidos (Ej: 21.00).");
            return;
        }

        // 3. Guardar en SQLite
        System.out.println("Intentando registrar nuevo usuario: " + email);
        boolean exito = UsuarioDAO.registrar(email, pass, nombre, nif, domicilio, iban, iva, irpf);

        // 4. Respuesta visual
        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Exitoso", "¡Cuenta creada correctamente!\n\nYa puedes iniciar sesión con tu email y contraseña.");
            volverAlLogin(); // Le devolvemos a la pantalla de login para que entre
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Registro", "No se pudo crear la cuenta. Es posible que el correo ya esté registrado.");
        }
    }

    /**
     * Cierra la ventana de registro y vuelve a cargar la de Login.
     */
    private void volverAlLogin() {
        try {
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            
            // Ponemos las medidas originales de pantalla de login
            stage.setScene(new Scene(root, 612, 555)); 
            stage.setTitle("AVTech Invoice - Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error al intentar cargar LoginView.fxml");
        }
    }

    /**
     * Método auxiliar para mostrar alertas emergentes.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}