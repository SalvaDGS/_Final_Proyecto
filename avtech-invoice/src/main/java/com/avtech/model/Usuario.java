package com.avtech.model;

public class Usuario {
    private int idUsuario;
    private String email;
    private String password;
    private String googleId;
    private String nombreFiscal;
    private String nifCif;
    private String domicilioFiscal;
    private String iban;
    private double porcentajeIva;
    private double porcentajeIrpf;
    private String rutaLogo;

    // Constructor
    public Usuario(int idUsuario, String email, String password, String googleId, String nombreFiscal, 
                   String nifCif, String domicilioFiscal, String iban, double porcentajeIva, double porcentajeIrpf, String rutaLogo) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.password = password;
        this.googleId = googleId;
        this.nombreFiscal = nombreFiscal;
        this.nifCif = nifCif;
        this.domicilioFiscal = domicilioFiscal;
        this.iban = iban;
        this.porcentajeIva = porcentajeIva;
        this.porcentajeIrpf = porcentajeIrpf;
        this.rutaLogo = rutaLogo;
    }

    // Getters
    public int getIdUsuario() { return idUsuario; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getGoogleId() { return googleId; }
    public String getNombreFiscal() { return nombreFiscal; }
    public String getNifCif() { return nifCif; }
    public String getDomicilioFiscal() { return domicilioFiscal; }
    public String getIban() { return iban; }
    public double getPorcentajeIva() { return porcentajeIva; }
    public double getPorcentajeIrpf() { return porcentajeIrpf; }
    public String getRutaLogo() { return rutaLogo; }
}