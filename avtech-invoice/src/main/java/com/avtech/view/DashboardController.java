package com.avtech.view;

import java.util.Map;

import com.avtech.model.Usuario;
import com.avtech.model.UsuarioDAO;
import com.avtech.service.GoogleAuthService;
import com.google.api.client.auth.oauth2.Credential;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;


public class DashboardController {
    // Vinculamos los botones que creamos en el DashboardView.fxml
    @FXML private Button btnClientes;
    @FXML private Button btnEventos;
    @FXML private Button btnFacturas;
    @FXML private Button btnUsuario;
    // --- CAMPO (Google) ---
    @FXML private Button btnGoogleLogin;   // Botón "Iniciar sesión con Google"
    @FXML private Button btnLogin; 
    @FXML private StackPane contentArea;
    
    //Selectores de mes y año
    @FXML private javafx.scene.control.ComboBox<String> comboMes;
    @FXML private javafx.scene.control.ComboBox<Integer> comboAnio;
    
    /**
     * Método auxiliar para mostrar alertas en pantalla.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Iniciar sesión con Google OAuth 2.0 desde el Dashboard.
     * Vincula el ID de Google al usuario que inició sesión en la pantalla anterior.
     */
    private void iniciarSesionGoogle() {
        System.out.println("Solicitando permisos a Google desde el Dashboard...");
        
        new Thread(() -> {
            try {
                Credential credential = GoogleAuthService.getCredentials();
                
                if (credential != null && credential.getAccessToken() != null) {
                    System.out.println("✅ Permiso de Google concedido. Obteniendo perfil...");
                    
                    // Extraer datos del perfil de Google
                    Map<String, String> perfil = GoogleAuthService.obtenerPerfilUsuario(credential);
                    String googleId = perfil.get("id");
                    
                    Platform.runLater(() -> {
                        javafx.scene.control.Label lblCargando = new javafx.scene.control.Label("Login con Google exitoso");
                        contentArea.getChildren().clear();
                        contentArea.getChildren().add(lblCargando);
                        
                        // Le pedimos al LoginController el usuario que acaba de entrar
                        Usuario usuarioActual = LoginController.usuarioLogueado;
                        
                        if (usuarioActual != null) {
                            System.out.println("✅ Vinculando Google ID a la cuenta actual (" + usuarioActual.getEmail() + ")...");
                            
                            // Lo vinculamos en la base de datos
                            UsuarioDAO.vincularCuentaGoogle(usuarioActual.getEmail(), googleId);
                            
                            // Actualizamos el usuario en memoria
                            LoginController.usuarioLogueado = UsuarioDAO.buscarPorEmail(usuarioActual.getEmail());
                            
                            mostrarAlerta(Alert.AlertType.INFORMATION, "Integración Exitosa", 
                                "Tu cuenta de Google Calendar se ha conectado correctamente.\n\nYa puedes ir a 'Eventos' para sincronizar tus trabajos.");
                        } else {
                            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se ha detectado ningún usuario logueado en la aplicación.");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Autenticación", "No se pudo conectar con Google:\n" + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Este método se ejecuta automáticamente cuando se carga el Dashboard.
     */
    @FXML
    public void initialize() {
        // --- CONFIGURACIÓN DE MES Y AÑO POR DEFECTO ---
        if (comboAnio != null && comboMes != null) {
            int anioActual = java.time.LocalDate.now().getYear();
            comboAnio.getItems().addAll(anioActual, anioActual - 1, anioActual - 2);
            
            int mesActual = java.time.LocalDate.now().getMonthValue(); // 1 a 12
            comboMes.getSelectionModel().select(mesActual - 1); // Las listas empiezan en 0
            comboAnio.getSelectionModel().selectFirst();
        }

         if (btnGoogleLogin != null) {
             btnGoogleLogin.setOnAction(e -> iniciarSesionGoogle());
         }
        
        // ========================================================
        // 1. LÓGICA DEL BOTÓN: EVENTOS
        // ========================================================
        btnEventos.setOnAction(event -> {
            
            // Leemos qué mes y año ha seleccionado el usuario
            int mesSeleccionado = comboMes.getSelectionModel().getSelectedIndex() + 1; // +1 porque enero es índice 0
            int anioSeleccionado = comboAnio.getSelectionModel().getSelectedItem();
            
            System.out.println("Solicitando eventos a Google Calendar (Mes: " + mesSeleccionado + ", Año: " + anioSeleccionado + ")...");
            
            javafx.scene.control.Label lblCargando = new javafx.scene.control.Label("Sincronizando clientes y eventos...");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(lblCargando);
            
            new Thread(() -> {
                try {
                    //Llamamos al servicio con los parámetros del mes y año
                    java.util.Map<String, java.util.List<com.google.api.services.calendar.model.Event>> eventosAgrupados = 
                            com.avtech.service.GoogleCalendarService.obtenerEventosPorMesAno(mesSeleccionado, anioSeleccionado);
                    
                    javafx.application.Platform.runLater(() -> {
                        contentArea.getChildren().clear();
                        
                        if (eventosAgrupados.isEmpty()) {
                            contentArea.getChildren().add(new javafx.scene.control.Label("No hay eventos en el mes seleccionado."));
                        } else {
                            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(15);
                            vbox.setAlignment(javafx.geometry.Pos.CENTER);
                            vbox.setPadding(new javafx.geometry.Insets(20));

                            javafx.scene.control.ListView<String> listaEventos = new javafx.scene.control.ListView<>();
                            
                            // Llenamos la lista agrupando por cliente
                            for (java.util.Map.Entry<String, java.util.List<com.google.api.services.calendar.model.Event>> entry : eventosAgrupados.entrySet()) {
                                String cliente = entry.getKey();
                                
                                // Añadimos una cabecera visual para el cliente
                                listaEventos.getItems().add("🏢 CLIENTE: " + cliente); 
                                
                                for (com.google.api.services.calendar.model.Event evento : entry.getValue()) {
                                    String nombre = evento.getSummary();
                                    com.google.api.client.util.DateTime inicio = evento.getStart().getDateTime();
                                    if (inicio == null) {
                                        inicio = evento.getStart().getDate();
                                    }
                                    listaEventos.getItems().add("    📅 " + nombre + "  |  " + inicio.toString() + "  |  [" + cliente + "]");
                                }
                            }
                            
                            javafx.scene.control.Button btnGenerar = new javafx.scene.control.Button("Generar Factura del Periodo");
                            btnGenerar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
                            
                            // Validación del botón para facturar por CLIENTE y periodo
                            btnGenerar.setOnAction(e -> {
                                String seleccionado = listaEventos.getSelectionModel().getSelectedItem();
                                
                                if (seleccionado != null) {
                                    String clienteVinculado = "";
                                    
                                    if (seleccionado.startsWith("🏢 CLIENTE: ")) {
                                        clienteVinculado = seleccionado.replace("🏢 CLIENTE: ", "");
                                    } else if (seleccionado.startsWith("    📅")) {
                                        String[] partes = seleccionado.split("  \\|  ");
                                        clienteVinculado = partes[2].replace("[", "").replace("]", "");
                                    }
                                    
                                    if (!clienteVinculado.isEmpty()) {
                                        
                                        // 1. Comprobamos si el cliente existe en SQLite
                                        int idCliente = com.avtech.model.ClienteDAO.obtenerIdPorNombre(clienteVinculado);
                                        
                                        if (idCliente == -1) {
                                            mostrarAlerta(Alert.AlertType.ERROR,"Cliente no registrado", 
                                                "El cliente '" + clienteVinculado + "' no existe en tu base de datos.\n\n" +
                                                "Por favor, ve a la pestaña 'Clientes' y regístralo con el mismo 'Nombre en Calendar' antes de generar su factura.");
                                            return; 
                                        }

                                        // 2. Si existe, recuperamos los eventos y la TARIFA
                                        java.util.List<com.google.api.services.calendar.model.Event> eventosDelCliente = eventosAgrupados.get(clienteVinculado);
                                        int numEventos = eventosDelCliente.size();
                                        double tarifaJornada = com.avtech.model.ClienteDAO.obtenerTarifaPorNombre(clienteVinculado);
                                        
                                        // Recuperamos el usuario actual para leer sus impuestos
                                        Usuario usuario = LoginController.usuarioLogueado;
                                        
                                        // 3. Cálculos matemáticos
                                        double baseImponible = tarifaJornada * numEventos;
                                        double importeIva = baseImponible * (usuario.getPorcentajeIva() / 100.0);
                                        double importeIrpf = baseImponible * (usuario.getPorcentajeIrpf() / 100.0);
                                        double totalFinal = baseImponible + importeIva - importeIrpf;
                                        
                                        // 4. Pedimos confirmación mostrando el desglose
                                        javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                                        confirmacion.setTitle("Confirmar Generación de Factura");
                                        confirmacion.setHeaderText("Facturando a: " + clienteVinculado);
                                        confirmacion.setContentText("Resumen del periodo:\n" 
                                            + "- Trabajos realizados: " + numEventos + "\n"
                                            + "- Tarifa por jornada: " + String.format("%.2f", tarifaJornada) + "€\n"
                                            + "--------------------------\n"
                                            + "Base Imponible: " + String.format("%.2f", baseImponible) + "€\n"
                                            + "IVA (" + usuario.getPorcentajeIva() + "%): +" + String.format("%.2f", importeIva) + "€\n"
                                            + "IRPF (" + usuario.getPorcentajeIrpf() + "%): -" + String.format("%.2f", importeIrpf) + "€\n"
                                            + "Total a cobrar: " + String.format("%.2f", totalFinal) + "€\n\n"
                                            + "¿Deseas registrar esta factura y generar el PDF?");

                                        java.util.Optional<javafx.scene.control.ButtonType> resultado = confirmacion.showAndWait();
                                        
                                        if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.OK) {
                                            
                                        	// 5. Preparamos los datos
                                        	java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
                                        	// Creamos un formato que saque el día, mes, año y la hora exacta
                                        	java.time.format.DateTimeFormatter formatoNombre = java.time.format.DateTimeFormatter.ofPattern("dd_MM_yyyy_HHmmss");

                                        	
                                        	String numFactura = "FAC-" + ahora.format(formatoNombre); 

                                        	// Mantenemos la fecha estándar (YYYY-MM-DD) para guardarla limpia en la base de datos
                                        	String fecha = java.time.LocalDate.now().toString(); 

                                        	String rutaDestinoFisica = "facturas/" + numFactura + ".pdf";
                                            
                                            // 6. Guardamos la factura en la base de datos
                                            boolean guardadoOk = com.avtech.model.FacturaDAO.registrarFactura(
                                                    numFactura, idCliente, fecha, numEventos, baseImponible, totalFinal, rutaDestinoFisica);
                                            
                                            if (guardadoOk) {
                                                System.out.println("✅ Factura guardada en BD.");
                                                
                                                String cifCliente = com.avtech.model.ClienteDAO.obtenerCifPorNombre(clienteVinculado);
                                                String direccionCliente = com.avtech.model.ClienteDAO.obtenerDireccionPorNombre(clienteVinculado);
                                                
                                                boolean pdfGenerado = com.avtech.service.FacturaPDFService.generarPDF(
                                                        usuario, numFactura, clienteVinculado, cifCliente, direccionCliente, fecha, numEventos, tarifaJornada, baseImponible, rutaDestinoFisica);
                                                
                                                if (pdfGenerado) {
                                                    mostrarAlerta(Alert.AlertType.INFORMATION,"Factura Registrada y PDF Creado", 
                                                        "¡Éxito!\n\nSe ha guardado en la base de datos y se ha generado el archivo PDF:\n" + rutaDestinoFisica);
                                                } else {
                                                    mostrarAlerta(Alert.AlertType.WARNING,"Aviso", "La factura se guardó en BD, pero hubo un error creando el PDF.");
                                                }
                                                
                                            } else {
                                                mostrarAlerta(Alert.AlertType.ERROR,"Error", "Hubo un problema al guardar la factura en la base de datos. Revisa la consola.");
                                            }
                                        }
                                    }
                                } else {
                                    mostrarAlerta(Alert.AlertType.ERROR,"Atención", "Por favor, selecciona un cliente o uno de sus eventos para facturar el periodo.");
                                }
                            });
                            
                            vbox.getChildren().addAll(listaEventos, btnGenerar);
                            contentArea.getChildren().add(vbox);
                        }
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        contentArea.getChildren().clear();
                        contentArea.getChildren().add(new javafx.scene.control.Label("❌ Error al conectar con Google Calendar."));
                    });
                }
            }).start();
        }); 

        // ========================================================
        // 2. LÓGICA DEL BOTÓN: CLIENTES
        // ========================================================
        btnClientes.setOnAction(e -> {
            try {
                System.out.println("Abriendo pestaña de Gestión de Clientes...");
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ClienteView.fxml"));
                javafx.scene.Parent view = loader.load();
                
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
                
            } catch (Exception ex) {
                System.err.println("❌ Error al cargar la vista de Clientes:");
                ex.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR,"Error", "No se pudo cargar la pantalla de gestión de clientes. Comprueba que ClienteView.fxml existe.");
            }
        }); 
        
        // ========================================================
        // 3. LÓGICA DEL BOTÓN: FACTURAS
        // ========================================================
        btnFacturas.setOnAction(e -> {
            try {
                System.out.println("Abriendo pestaña de Historial de Facturas...");
                javafx.scene.Parent view = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/FacturasView.fxml"));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            } catch (Exception ex) {
                System.err.println("❌ Error al cargar la vista de Facturas:");
                ex.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR,"Error", "No se pudo cargar la vista de Facturas.");
            }
        });
        // ========================================================
        // 4. LÓGICA DEL BOTÓN: MI PERFIL (USUARIO)
        // ========================================================
        if (btnUsuario != null) {
            btnUsuario.setOnAction(e -> {
                try {
                    System.out.println("Abriendo pestaña de Mi Perfil...");
                    javafx.scene.Parent view = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/UsuarioView.fxml"));
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(view);
                } catch (Exception ex) {
                    System.err.println("❌ Error al cargar la vista de Usuario:");
                    ex.printStackTrace();
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista de Perfil.");
                }
            });
        }
    }
    
}