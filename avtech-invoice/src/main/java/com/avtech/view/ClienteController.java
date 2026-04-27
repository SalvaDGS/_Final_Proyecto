package com.avtech.view;

import com.avtech.model.Cliente;
import com.avtech.model.ClienteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClienteController {
    
    // --- ELEMENTOS DE LA TABLA ---
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colRazon;
    @FXML private TableColumn<Cliente, String> colNif;
    @FXML private TableColumn<Cliente, Double> colTarifa;

    // --- ELEMENTOS DEL FORMULARIO ---
    @FXML private TextField txtNombreCalendario;
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtCifNif;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTarifa;
    // --- ELEMENTOS BOTONES ---
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnEliminar;

    // Variable para saber si estamos editando o creando uno nuevo
    private Cliente clienteSeleccionado = null;

    @FXML
    public void initialize() {
        // 1. Configurar cómo se conectan las columnas de la tabla con la clase Cliente
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCalendario"));
        colRazon.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));
        colNif.setCellValueFactory(new PropertyValueFactory<>("cifNif"));
        colTarifa.setCellValueFactory(new PropertyValueFactory<>("tarifaJornada"));

        // 2. Cargar los datos de la base de datos en la tabla
        cargarDatosTabla();

        // 3. Detectar clics en la tabla para cargar los datos en el formulario
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null) {
                cargarClienteEnFormulario(nuevo);
            }
        });

        // 4. Asignar acciones a los botones
        btnGuardar.setOnAction(e -> guardarOActualizarCliente());
        btnLimpiar.setOnAction(e -> limpiarCampos());
        btnEliminar.setOnAction(e -> eliminarCliente());
    }

    /**
     * Pide todos los clientes al DAO y los pone en la tabla.
     */
    private void cargarDatosTabla() {
        ObservableList<Cliente> lista = FXCollections.observableArrayList(ClienteDAO.obtenerTodos());
        tablaClientes.setItems(lista);
    }

    /**
     * Pasa los datos del cliente seleccionado al formulario.
     */
    private void cargarClienteEnFormulario(Cliente c) {
        this.clienteSeleccionado = c;
        txtNombreCalendario.setText(c.getNombreCalendario());
        txtRazonSocial.setText(c.getRazonSocial());
        txtCifNif.setText(c.getCifNif());
        txtDireccion.setText(c.getDireccion());
        txtTarifa.setText(String.valueOf(c.getTarifaJornada()));
        btnGuardar.setText("Actualizar Cliente"); // Cambiamos el texto del botón
    }

    /**
     * Limpia el formulario y deselecciona la tabla para poder crear uno nuevo.
     */
    private void limpiarCampos() {
        this.clienteSeleccionado = null;
        tablaClientes.getSelectionModel().clearSelection();
        txtNombreCalendario.clear();
        txtRazonSocial.clear();
        txtCifNif.clear();
        txtDireccion.clear();
        txtTarifa.clear();
        btnGuardar.setText("Guardar Nuevo Cliente");
    }

    /**
     * Decide si insertar o actualizar dependiendo de si hay un cliente seleccionado.
     */
    private void guardarOActualizarCliente() {
        String nombre = txtNombreCalendario.getText().trim();
        String razon = txtRazonSocial.getText().trim();
        String nif = txtCifNif.getText().trim();
        String dir = txtDireccion.getText().trim();
        String tarifaStr = txtTarifa.getText().trim();
        
        if (nombre.isEmpty() || razon.isEmpty() || nif.isEmpty() || tarifaStr.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Rellena todos los campos excepto la dirección.");
            return;
        }
        
        double tarifa;
        try {
            tarifa = Double.parseDouble(tarifaStr.replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de formato", "La tarifa debe ser un número (Ej: 250.50).");
            return;
        }
        
        boolean exito;
        if (clienteSeleccionado == null) {
            // MODO CREAR NUEVO
            exito = ClienteDAO.insertar(nombre, razon, nif, dir, tarifa);
            if (exito) mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente creado correctamente.");
        } else {
            // MODO ACTUALIZAR
            exito = ClienteDAO.actualizar(clienteSeleccionado.getIdCliente(), nombre, razon, nif, dir, tarifa);
            if (exito) mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente actualizado correctamente.");
        }
        
        if (exito) {
            cargarDatosTabla(); // Refrescamos la tabla para ver los cambios
            limpiarCampos();    // Dejamos el formulario limpio
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Hubo un problema en la base de datos.");
        }
    }

    /**
     * Elimina el cliente seleccionado tras pedir confirmación.
     */
    private void eliminarCliente() {
        if (clienteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Selecciona un cliente de la tabla para eliminarlo.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Borrado");
        confirm.setHeaderText("Eliminar a: " + clienteSeleccionado.getRazonSocial());
        confirm.setContentText("¿Estás seguro de que quieres eliminar a este cliente?\n(Sus facturas podrían quedar desvinculadas).");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean exito = ClienteDAO.eliminarCliente(clienteSeleccionado.getIdCliente());
            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "El cliente ha sido borrado.");
                cargarDatosTabla();
                limpiarCampos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar al cliente.");
            }
        }
    }
    /**
     * Alertas personalizadas.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}