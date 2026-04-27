package com.avtech.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;


public class GoogleCalendarService {
    
    private static final String APPLICATION_NAME = "AVTech Invoice";

    /**
     * Construye y devuelve el servicio cliente de la API de Calendar.
     */
    private static Calendar getCalendarService() throws Exception {
        Credential credential = GoogleAuthService.getCredentials();
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), 
                GsonFactory.getDefaultInstance(), 
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Obtiene los eventos de un MES y AÑO específicos, agrupados por calendario (Cliente).
     * @param mes El mes a facturar (1-12)
     * @param anio El año a facturar 
     */
    public static java.util.Map<String, java.util.List<Event>> obtenerEventosPorMesAno(int mes, int anio) throws Exception {
        Calendar service = getCalendarService();
        java.util.Map<String, java.util.List<Event>> eventosPorCliente = new java.util.HashMap<>();

        // 1. Calcular el primer milisegundo del mes seleccionado
        java.util.Calendar calInicio = java.util.Calendar.getInstance();
        calInicio.set(java.util.Calendar.YEAR, anio);
        calInicio.set(java.util.Calendar.MONTH, mes - 1); // En Java los meses van del 0 al 11
        calInicio.set(java.util.Calendar.DAY_OF_MONTH, 1);
        calInicio.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calInicio.set(java.util.Calendar.MINUTE, 0);
        calInicio.set(java.util.Calendar.SECOND, 0);
        DateTime fechaInicio = new DateTime(calInicio.getTime());

        // 2. Calcular el último milisegundo de ese mismo mes
        java.util.Calendar calFin = (java.util.Calendar) calInicio.clone();
        calFin.add(java.util.Calendar.MONTH, 1);
        calFin.add(java.util.Calendar.MILLISECOND, -1);
        DateTime fechaFin = new DateTime(calFin.getTime());

        System.out.println("Buscando eventos desde " + fechaInicio + " hasta " + fechaFin);

        // 3. Obtener la lista de TODOS los calendarios
        com.google.api.services.calendar.model.CalendarList calendarList = service.calendarList().list().execute();

        // 4. Recorrer cada calendario buscando los eventos en ese rango
        for (com.google.api.services.calendar.model.CalendarListEntry calendarEntry : calendarList.getItems()) {
            String nombreCliente = calendarEntry.getSummary();

            Events events = service.events().list(calendarEntry.getId())
                    .setTimeMin(fechaInicio)
                    .setTimeMax(fechaFin)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            java.util.List<Event> items = events.getItems();
            
            // Si el cliente tuvo eventos este mes, lo añadimos
            if (items != null && !items.isEmpty()) {
                eventosPorCliente.put(nombreCliente, items);
            }
        }
        
        return eventosPorCliente;
    }
}