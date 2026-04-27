package com.avtech.view;

import com.avtech.model.Factura;
import com.avtech.model.FacturaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FacturasController {

    @FXML private ListView<String> listaClientesFiltro;
    
    @FXML private TableView<Factura> tablaFacturas;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, Double> colTotal;
    
    @FXML private Button btnAbrirPdf;
    // NUEVO BOTÓN
    @FXML private Button btnEliminarFactura;
    @FXML private Label lblRuta;

    private ObservableList<Factura> todasLasFacturas;

    @FXML
    public void initialize() {
        // 1. Configurar columnas de la tabla
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaEmision"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalFinal"));

        // 2. Cargar datos
        cargarDatos();

        // 3. Programar el clic en la lista lateral (Filtro)
        listaClientesFiltro.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filtrarTabla(newVal);
        });

        // 4. Programar el clic en la tabla (Mostrar ruta)
        tablaFacturas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblRuta.setText("Archivo: " + newVal.getRutaPdf());
            }
        });

        // 5. Botones de acción
        btnAbrirPdf.setOnAction(e -> abrirDocumentoPdf());
        
        btnEliminarFactura.setOnAction(e -> eliminarFacturaSeleccionada());
    }

    private void cargarDatos() {
        // Obtenemos todo de SQLite
        List<Factura> facturasBD = FacturaDAO.obtenerTodas();
        todasLasFacturas = FXCollections.observableArrayList(facturasBD);
        tablaFacturas.setItems(todasLasFacturas);

        // Extraemos los nombres de clientes únicos para la lista de la izquierda
        ObservableList<String> clientesUnicos = FXCollections.observableArrayList();
        clientesUnicos.add("Todos los Clientes"); // Opción por defecto
        
        List<String> nombres = facturasBD.stream()
                .map(Factura::getNombreCliente)
                .distinct()
                .collect(Collectors.toList());
        clientesUnicos.addAll(nombres);
        
        listaClientesFiltro.setItems(clientesUnicos);
        listaClientesFiltro.getSelectionModel().selectFirst(); // Selecciona "Todos" por defecto
    }

    private void filtrarTabla(String clienteSeleccionado) {
        if (clienteSeleccionado == null || clienteSeleccionado.equals("Todos los Clientes")) {
            tablaFacturas.setItems(todasLasFacturas);
        } else {
            // Filtramos la lista usando streams de Java
            ObservableList<Factura> filtradas = FXCollections.observableArrayList(
                todasLasFacturas.stream()
                    .filter(f -> f.getNombreCliente().equals(clienteSeleccionado))
                    .collect(Collectors.toList())
            );
            tablaFacturas.setItems(filtradas);
        }
    }

    private void abrirDocumentoPdf() {
        Factura seleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Atención", "Selecciona una factura de la tabla para abrirla.");
            return;
        }

        String ruta = seleccionada.getRutaPdf();
        if (ruta == null || ruta.isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Esta factura no tiene un archivo PDF vinculado.");
            return;
        }

        try {
            File archivoPdf = new File(ruta);
            if (archivoPdf.exists()) {
                // Esto abre el archivo con el programa nativo de tu sistema operativo
                Desktop.getDesktop().open(archivoPdf);
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Archivo no encontrado", "No se ha encontrado el PDF físicamente en la carpeta:\n" + ruta);
            }
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir el archivo. ¿Tienes un lector de PDFs instalado?");
            ex.printStackTrace();
        }
    }
    private void eliminarFacturaSeleccionada() {
        Factura seleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
        
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Atención", "Selecciona la factura que deseas eliminar.");
            return;
        }

        // 1. Pedir confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Factura");
        confirmacion.setHeaderText("Vas a eliminar la Factura Nº: " + seleccionada.getNumeroFactura());
        confirmacion.setContentText("Esta acción borrará el registro de la base de datos y eliminará el archivo PDF de tu ordenador.\n\n¿Estás completamente seguro?");
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            
            // 2. Intentar borrar el archivo físico PDF del ordenador
            boolean archivoBorrado = true; // Asumimos true por si la ruta estaba vacía y no había archivo
            String rutaPdf = seleccionada.getRutaPdf();
            
            if (rutaPdf != null && !rutaPdf.isEmpty()) {
                File archivoFisico = new File(rutaPdf);
                if (archivoFisico.exists()) {
                    archivoBorrado = archivoFisico.delete();
                    if (!archivoBorrado) {
                        System.err.println("⚠️ No se pudo borrar el archivo físico: " + rutaPdf);
                        // No cortamos la ejecución aquí. Si el archivo está bloqueado, al menos borramos el registro.
                    }
                }
            }
            
            // 3. Borrar el registro de SQLite
            boolean dbBorrada = FacturaDAO.eliminarFactura(seleccionada.getIdFactura());
            
            // 4. Actualizar la interfaz visual
            if (dbBorrada) {
                // Recargamos los datos para que desaparezca de la tabla
                cargarDatos();
                lblRuta.setText(""); 
                
                String mensaje = archivoBorrado ? 
                    "La factura y su PDF han sido eliminados correctamente." : 
                    "La factura se eliminó de la base de datos, pero el PDF físico no se pudo borrar (puede que lo tengas abierto).";
                    
                mostrarAlerta(Alert.AlertType.INFORMATION, "Factura Eliminada", mensaje);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Hubo un problema al intentar eliminar la factura de la base de datos.");
            }
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