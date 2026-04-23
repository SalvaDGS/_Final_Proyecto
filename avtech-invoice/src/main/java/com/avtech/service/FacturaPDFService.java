package com.avtech.service;

import com.avtech.model.Usuario;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;

public class FacturaPDFService {

    /**
     * Genera un documento PDF con los datos del emisor (Usuario) y del cliente completos.
     */
    public static boolean generarPDF(Usuario emisor, String numFactura, String nombreCliente, 
                                     String cifCliente, String direccionCliente, String fecha, 
                                     int cantidadEventos, double tarifa, double subtotal, String rutaDestino) {
        
        // 1. Asegurarnos de que la carpeta "facturas" existe
        File archivoPdf = new File(rutaDestino);
        archivoPdf.getParentFile().mkdirs(); 

        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, new FileOutputStream(archivoPdf));
            document.open();

            // --- ESTILOS DE LETRA ---
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font fontPequena = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

            // Título principal
            document.add(new Paragraph("FACTURA", fontTitulo));
            document.add(new Paragraph(" ")); 

            // --- CABECERA DIVIDIDA (IZQUIERDA Y DERECHA) ---
            PdfPTable tablaCabecera = new PdfPTable(2);
            tablaCabecera.setWidthPercentage(100);
            
            // Lado Izquierdo: Datos del Emisor (Usuario)
            PdfPCell celdaEmisor = new PdfPCell();
            celdaEmisor.setBorder(PdfPCell.NO_BORDER);
            celdaEmisor.addElement(new Paragraph(emisor.getNombreFiscal(), fontSubtitulo));
            celdaEmisor.addElement(new Paragraph("NIF/CIF: " + emisor.getNifCif(), fontNormal));
            celdaEmisor.addElement(new Paragraph(emisor.getDomicilioFiscal(), fontNormal));
            celdaEmisor.addElement(new Paragraph("Email: " + emisor.getEmail(), fontNormal));

            // Lado Derecho: Datos del Cliente y de Factura
            PdfPCell celdaCliente = new PdfPCell();
            celdaCliente.setBorder(PdfPCell.NO_BORDER);
            
            Paragraph pFacturarA = new Paragraph("FACTURAR A:", fontPequena);
            pFacturarA.setAlignment(Paragraph.ALIGN_RIGHT);
            
            Paragraph pNombreCliente = new Paragraph(nombreCliente, fontSubtitulo);
            pNombreCliente.setAlignment(Paragraph.ALIGN_RIGHT);
            
            Paragraph pCifCliente = new Paragraph("NIF/CIF: " + (cifCliente != null ? cifCliente : "No especificado"), fontNormal);
            pCifCliente.setAlignment(Paragraph.ALIGN_RIGHT);
            
            Paragraph pDireccionCliente = new Paragraph(direccionCliente != null ? direccionCliente : "Dirección no especificada", fontNormal);
            pDireccionCliente.setAlignment(Paragraph.ALIGN_RIGHT);
            
            Paragraph pDatosFactura = new Paragraph(
                "\nFactura Nº: " + numFactura + "\n" +
                "Fecha de Emisión: " + fecha, fontNormal
            );
            pDatosFactura.setAlignment(Paragraph.ALIGN_RIGHT);

            celdaCliente.addElement(pFacturarA);
            celdaCliente.addElement(pNombreCliente);
            celdaCliente.addElement(pCifCliente);
            celdaCliente.addElement(pDireccionCliente);
            celdaCliente.addElement(pDatosFactura);

            // Añadimos las dos celdas a la tabla y la tabla al documento
            tablaCabecera.addCell(celdaEmisor);
            tablaCabecera.addCell(celdaCliente);
            document.add(tablaCabecera);
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // --- TABLA DE CONCEPTOS ---
            PdfPTable tablaConceptos = new PdfPTable(4);
            tablaConceptos.setWidthPercentage(100);
            tablaConceptos.setWidths(new float[]{4f, 1f, 2f, 2f});

            agregarCeldaCabecera(tablaConceptos, "Concepto");
            agregarCeldaCabecera(tablaConceptos, "Días");
            agregarCeldaCabecera(tablaConceptos, "Tarifa/Día");
            agregarCeldaCabecera(tablaConceptos, "Total");

            tablaConceptos.addCell(new Phrase("Servicios Audiovisuales del periodo", fontNormal));
            tablaConceptos.addCell(new Phrase(String.valueOf(cantidadEventos), fontNormal));
            tablaConceptos.addCell(new Phrase(String.format("%.2f €", tarifa), fontNormal));
            tablaConceptos.addCell(new Phrase(String.format("%.2f €", subtotal), fontNormal));

            document.add(tablaConceptos);
            document.add(new Paragraph(" ")); 

            // --- DESGLOSE DE IMPUESTOS Y TOTAL ---
            double importeIva = subtotal * (emisor.getPorcentajeIva() / 100.0);
            double importeIrpf = subtotal * (emisor.getPorcentajeIrpf() / 100.0);
            double totalFinal = subtotal + importeIva - importeIrpf;

            Paragraph pBase = new Paragraph(String.format("Base Imponible: %.2f €", subtotal), fontNormal);
            pBase.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(pBase);

            Paragraph pIva = new Paragraph(String.format("IVA (%.2f%%): %.2f €", emisor.getPorcentajeIva(), importeIva), fontNormal);
            pIva.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(pIva);

            // Solo mostramos el IRPF si es mayor a 0
            if (emisor.getPorcentajeIrpf() > 0) {
                Paragraph pIrpf = new Paragraph(String.format("IRPF Retenido (-%.2f%%): -%.2f €", emisor.getPorcentajeIrpf(), importeIrpf), fontNormal);
                pIrpf.setAlignment(Paragraph.ALIGN_RIGHT);
                document.add(pIrpf);
            }

            Paragraph pTotal = new Paragraph(String.format("TOTAL FACTURA: %.2f €", totalFinal), fontSubtitulo);
            pTotal.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(pTotal);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // --- MÉTODO DE PAGO (IBAN) ---
            document.add(new Paragraph("MÉTODO DE PAGO", fontSubtitulo));
            document.add(new Paragraph("Transferencia bancaria al siguiente número de cuenta:", fontNormal));
            document.add(new Paragraph("IBAN: " + emisor.getIban(), fontSubtitulo));

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void agregarCeldaCabecera(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        celda.setBackgroundColor(Color.GRAY);
        celda.setPadding(5);
        tabla.addCell(celda);
    }
}