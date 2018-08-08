package frol.excel2gcal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

/**
 * Supported system properties:
 * <ul>
 *     <li>http.proxyHost<br/>default: none</li>
 *     <li>http.proxyPort<br/>default 8081</li>
 * </ul>
 */
public class ExcelToGoogleCalendarEvents {
    private static final Logger LOG = Logger.getLogger("MyLogger");

    private static final String APPLICATION_NAME = "Vika G Events";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final Set<String> SCOPES = CalendarScopes.all();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param httpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        InputStream in = ExcelToGoogleCalendarEvents.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        if (args.length != 2) {
            System.err.println("Usage: java -jar program.jar <input.xls> <googleCalendarID>");
            System.exit(1);
        }
        String calendarId = args[1];
        // Build a new authorized API client service.
        NetHttpTransport httpTransport = netHttpTransport();
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ChildContactToEventConverter converter = new ChildContactToEventConverter();

        int i = 0;
        List<ChildContact> childContacts = new ExcelReader(
                new PersonNameDetector(NamesDictionaryCSVReader.readFromResource("/russian_names.csv"))).parse(args[0]);
        for (ChildContact childContact : childContacts) {
            Event event = converter.convert(childContact);
            event = service.events()
                    .insert(calendarId, event)
                    .execute();
            LOG.info(String.format("%d: %s", ++i, event.toPrettyString()));
        }
    }

    private static NetHttpTransport netHttpTransport() throws GeneralSecurityException, IOException {
        String httpProxy = System.getProperty("http.proxyHost");
        if (httpProxy != null) {
            int httpPort = Integer.parseInt(System.getProperty("http.proxyPort", "8081"));
            return new NetHttpTransport.Builder().setProxy(
                    new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxy, httpPort))).build();
        } else {
            return GoogleNetHttpTransport.newTrustedTransport();
        }
    }
}
