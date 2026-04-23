package com.avtech.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleAuthService {
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    
    // AQUÍ AÑADIMOS LOS PERMISOS PARA LEER EL EMAIL Y EL PERFIL DE GOOGLE
    private static final List<String> SCOPES = Arrays.asList(
            CalendarScopes.CALENDAR_READONLY,
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    public static Credential getCredentials() throws Exception {
        InputStream in = GoogleAuthService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            GsonFactory.getDefaultInstance(), new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Llama a la API de Google para obtener el ID y el Email del usuario conectado.
     */
    public static Map<String, String> obtenerPerfilUsuario(Credential credential) throws Exception {
        com.google.api.client.http.HttpRequestFactory requestFactory = 
                GoogleNetHttpTransport.newTrustedTransport().createRequestFactory(credential);
        com.google.api.client.http.GenericUrl url = 
                new com.google.api.client.http.GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
        com.google.api.client.http.HttpRequest request = requestFactory.buildGetRequest(url);
        
        String jsonResponse = request.execute().parseAsString();

        // Extraemos los datos del JSON de Google
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        Map<String, String> perfil = new HashMap<>();
        perfil.put("id", json.has("id") ? json.get("id").getAsString() : "");
        perfil.put("email", json.has("email") ? json.get("email").getAsString() : "");
        
        return perfil;
    }
}