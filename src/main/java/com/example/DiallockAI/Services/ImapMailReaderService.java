package com.example.DiallockAI.Services;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class ImapMailReaderService {

    public void readMailsAndExtractReplies() {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");

            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", "mathesh1623@gmail.com", "ywjzfnsohgztpiez");

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                String[] campaignHeaders = message.getHeader("X-Campaign-Lead-ID");

                if (campaignHeaders != null && campaignHeaders.length > 0) {
                    String headerValue = campaignHeaders[0]; // like "CID-1-LID-20"
                    String replyContent = message.getContent().toString(); // simple plain-text

                    System.out.println("Reply from: " + message.getFrom()[0]);
                    System.out.println("Campaign Info: " + headerValue);
                    System.out.println("Reply Body: " + replyContent);

                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
