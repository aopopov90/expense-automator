package com.home.expenseautomator.service;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GmailAbelAndColeService extends GmailService {
    public GmailAbelAndColeService() throws GeneralSecurityException, IOException {
    }

    public List<String> extractEmailBody(int limit) throws IOException, GeneralSecurityException {
        List<String> emailBodies = new ArrayList<>();
        String emailBody = "";
        ListMessagesResponse messagesResponse = service.users().messages().list(user)
                .setQ("subject: Your receipt from Abel & Cole").execute();
        log.info("Found {} emails via the following query: \"subject: Your receipt from Abel & Cole\"");

        List<Message> messages = messagesResponse.getMessages();
        List<String> receiptNames = new ArrayList<>();

        log.info("Attempting to extract last {} receipt(s)..", limit);
        for (int i = 0; (i < limit && i < messages.size()); i++) {
            Message message = service.users().messages().get(user, messages.get(i).getId()).setFormat("full").execute();
            emailBodies.add(StringUtils.newStringUtf8(
                    Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData())));
        }
        return emailBodies;
    }
}
