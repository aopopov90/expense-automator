package com.home.expenseautomator.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class GmailService {
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Arrays.asList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    private final Gmail service;
    private final String user = "me";

    public GmailService() throws GeneralSecurityException, IOException {
        this.service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GmailService.class.getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public List<String> extractReceipts(int limit) throws IOException, GeneralSecurityException {
        ListMessagesResponse messagesResponse = service.users().messages().list(user)
                .setQ("subject: Receipt for your Sainsbury's").execute();
        log.info("Found {} emails via the following query: \"subject: Receipt for your Sainsbury's\"");

        List<Message> messages = messagesResponse.getMessages();
        List<String> receiptNames = new ArrayList<>();

        log.info("Attempting to extract last {} receipt(s)..", limit);
        for (int i = 0; i < limit; i++) {
            Message message = service.users().messages().get(user, messages.get(i).getId()).setFormat("full").execute();
            receiptNames.add(extractAttachment(message));
        }
        return receiptNames;
    }

    private String extractAttachment(Message message) {
        String filename = null;
        List<MessagePart> parts = message.getPayload().getParts();
        if (parts != null) {
            for (MessagePart part : parts) {
                if ((part.getFilename() != null && part.getFilename().length() > 0)) {
                    filename = part.getFilename();
                    String attId = part.getBody().getAttachmentId();
                    MessagePartBody attachPart;
                    FileOutputStream fileOutFile = null;
                    try {
                        attachPart = service.users().messages().attachments().get(user, part.getPartId(), attId).execute();
                        byte[] fileByteArray = Base64.decodeBase64(attachPart.getData());
                        fileOutFile = new FileOutputStream(filename);
                        fileOutFile.write(fileByteArray);
                        fileOutFile.close();
                    } catch (IOException e) {
                        log.error("IO Exception processing attachment: " + filename);
                    } finally {
                        if (fileOutFile != null) {
                            try {
                                fileOutFile.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                } else if (part.getMimeType().equals("multipart/related")) {
                    log.error("Unable to process multipart emails");
                }
            }
        }
        return filename;
    }
}
