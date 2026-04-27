package com.avtech.model;

public class Factura {
    private int idFactura;
    private String numeroFactura;
    private String nombreCliente; // Lo usaremos para mostrar el nombre, no solo el ID
    private String fechaEmision;
    private int totalDias;
    private double subtotal;
    private double totalFinal;
    private String rutaPdf;
    
    // Constructor
    public Factura(int idFactura, String numeroFactura, String nombreCliente, String fechaEmision, 
                   int totalDias, double subtotal, double totalFinal, String rutaPdf) {
        this.idFactura = idFactura;
        this.numeroFactura = numeroFactura;
        this.nombreCliente = nombreCliente;
        this.fechaEmision = fechaEmision;
        this.totalDias = totalDias;
        this.subtotal = subtotal;
        this.totalFinal = totalFinal;
        this.rutaPdf = rutaPdf;
    }

    // Getters
    public int getIdFactura() { return idFactura; }
    public String getNumeroFactura() { return numeroFactura; }
    public String getNombreCliente() { return nombreCliente; }
    public String getFechaEmision() { return fechaEmision; }
    public int getTotalDias() { return totalDias; }
    public double getSubtotal() { return subtotal; }
    public double getTotalFinal() { return totalFinal; }
    public String getRutaPdf() { return rutaPdf; }
}