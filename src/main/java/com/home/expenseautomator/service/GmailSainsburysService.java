package com.home.expenseautomator.service;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GmailSainsburysService extends GmailService {

    public GmailSainsburysService() throws GeneralSecurityException, IOException {
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