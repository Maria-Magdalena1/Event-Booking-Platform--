package main.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Event Booking App";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    //public static Calendar getCalendarService() throws Exception {
    //    InputStream in = GoogleCalendarService.class.getResourceAsStream("/credentials.json");
    //    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
    //    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
    //            GoogleNetHttpTransport.newTrustedTransport(),
    //            JSON_FACTORY,
    //            clientSecrets,
    //            SCOPES)
    //            .setAccessType("offline")
    //            .build();
//
    //    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    //    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//
    //    return new Calendar.Builder(
    //            GoogleNetHttpTransport.newTrustedTransport(),
    //            JSON_FACTORY,
    //            credential)
    //            .setApplicationName(APPLICATION_NAME)
    //            .build();
    //}
}
