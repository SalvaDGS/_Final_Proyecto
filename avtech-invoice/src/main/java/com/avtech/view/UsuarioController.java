package com.avtech.view;

import com.avtech.model.Usuario;
import com.avtech.model.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class UsuarioController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNombreFiscal;
    @FXML private TextField txtNifCif;
    @FXML private TextField txtDomicilio;
    @FXML private TextField txtIban;
    @FXML private TextField txtIva;
    @FXML private TextField txtIrpf;

    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        // Cargar los datos del usuario que ha iniciado sesión
        usuarioActual = LoginController.usuarioLogueado;
        
        if (usuarioActual != null) {
            txtEmail.setText(usuarioActual.getEmail());
            txtNombreFiscal.setText(usuarioActual.getNombreFiscal());
            txtNifCif.setText(usuarioActual.getNifCif());
            txtDomicilio.setText(usuarioActual.getDomicilioFiscal());
            txtIban.setText(usuarioActual.getIban());
            txtIva.setText(String.valueOf(usuarioActual.getPorcentajeIva()));
            txtIrpf.setText(String.valueOf(usuarioActual.getPorcentajeIrpf()));
        }

        btnActualizar.setOnAction(e -> actualizarDatos());
        btnEliminar.setOnAction(e -> eliminarCuenta());
    }

    private void actualizarDatos() {
        String email = txtEmail.getText().trim();
        String pass = txtPassword.getText().trim();
        String nombre = txtNombreFiscal.getText().trim();
        String nif = txtNifCif.getText().trim();
        String domicilio = txtDomicilio.getText().trim();
        String iban = txtIban.getText().trim();
        String ivaStr = txtIva.getText().trim();
        String irpfStr = txtIrpf.getText().trim();

        if (email.isEmpty() || nombre.isEmpty() || nif.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos obligatorios", "Email, Nombre Fiscal y NIF/CIF no pueden estar vacíos.");
            return;
        }

        double iva = 0.0, irpf = 0.0;
        try {
            if (!ivaStr.isEmpty()) iva = Double.parseDouble(ivaStr.replace(",", "."));
            if (!irpfStr.isEmpty()) irpf = Double.parseDouble(irpfStr.replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de formato", "El IVA y el IRPF deben ser números válidos.");
            return;
        }

        boolean exito = UsuarioDAO.actualizar(usuarioActual.getIdUsuario(), email, pass, nombre, nif, domicilio, iban, iva, irpf);

        if (exito) {
            // Actualizamos la variable global en memoria para que el resto de la app lo sepa
            LoginController.usuarioLogueado = UsuarioDAO.buscarPorEmail(email);
            txtPassword.clear(); // Limpiamos el campo por seguridad
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tus datos fiscales se han actualizado correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el perfil. Comprueba los datos.");
        }
    }

    private void eliminarCuenta() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Cuenta Definitivamente");
        confirmacion.setHeaderText("Vas a eliminar tu cuenta de usuario.");
        confirmacion.setContentText("Esta acción borrará todos tus datos fiscales de la base de datos y no se puede deshacer.\n\n¿Estás completamente seguro de querer continuar?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (UsuarioDAO.eliminar(usuarioActual.getIdUsuario())) {
                LoginController.usuarioLogueado = null; // Borramos la memoria
                cerrarSesion();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la cuenta de la base de datos.");
            }
        }
    }

    private void cerrarSesion() {
        try {
            Stage stage = (Stage) btnEliminar.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            stage.setScene(new Scene(root, 612, 555));
            stage.setTitle("AVTech Invoice - Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}