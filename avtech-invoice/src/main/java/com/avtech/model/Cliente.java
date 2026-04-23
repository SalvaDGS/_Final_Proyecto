package com.avtech.model;

public class Cliente {
    private int idCliente;
    private String nombreCalendario;
    private String razonSocial;
    private String cifNif;
    private String direccion;
    private double tarifaJornada;

    // Constructor
    public Cliente(int idCliente, String nombreCalendario, String razonSocial, String cifNif, String direccion, double tarifaJornada) {
        this.idCliente = idCliente;
        this.nombreCalendario = nombreCalendario;
        this.razonSocial = razonSocial;
        this.cifNif = cifNif;
        this.direccion = direccion;
        this.tarifaJornada = tarifaJornada;
    }

    // Getters
    public int getIdCliente() { return idCliente; }
    public String getNombreCalendario() { return nombreCalendario; }
    public String getRazonSocial() { return razonSocial; }
    public String getCifNif() { return cifNif; }
    public String getDireccion() { return direccion; }
    public double getTarifaJornada() { return tarifaJornada; }
}