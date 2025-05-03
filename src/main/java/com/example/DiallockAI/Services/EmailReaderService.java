package com.example.DiallockAI.Services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.DiallockAI.Models.Campaignleads;
import com.example.DiallockAI.Models.Conversation;
import com.example.DiallockAI.Models.Direction;
import com.example.DiallockAI.Repository.ConversationRepository;
import com.example.DiallockAI.Repository.campaignleadsrepo;

import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;

@Service
public class EmailReaderService {

    @Autowired
    private campaignleadsrepo leadRepo;

    @Autowired
    private ConversationRepository conversationRepo;

    public void readEmailsForLead(int campaignId, int leadId) {
        String host = "imap.gmail.com";
        String username = "mathesh1623@gmail.com";
        String password = "ywjzfnsohgztpiez";

        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");

        try {
            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore("imaps");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages(Math.max(1, inbox.getMessageCount() - 20), inbox.getMessageCount());

            for (Message message : messages) {
                String[] replyHeaders = message.getHeader("In-Reply-To");
                if (replyHeaders != null && replyHeaders.length > 0) {
                    String replyId = replyHeaders[0];

                    if (replyId.contains("CID-" + campaignId + "-LID-" + leadId)) {
                    	String messageId = message.getHeader("Message-ID") != null ? message.getHeader("Message-ID")[0] : null;

                        // Skip email if Message-ID is null
                        if (messageId == null) {
                            System.out.println("Skipping email with no Message-ID.");
                            continue;
                        }

                        // Check if Message-ID already exists in the database
                        if (conversationRepo.existsByMessageId(messageId)) {
                            System.out.println("Email with Message-ID " + messageId + " already processed.");
                            continue; // Skip this email, it has already been processed
                        }
                        System.out.println("Matched reply to lead: " + replyId);
                        String from = message.getFrom()[0].toString();
                        String body = extractTextFromMessage(message);
                        // Fetch the received date and adjust it to UTC
                        Date receivedDate = message.getReceivedDate(); // Get received date
                        ZonedDateTime receivedZonedDateTime = (receivedDate != null)
                            ? receivedDate.toInstant().atZone(ZoneId.of("Asia/Kolkata")) // Adjusting to UTC
                            : ZonedDateTime.now(ZoneId.of("Asia/Kolkata")); // Fallback to current UTC time if null
                        LocalDateTime receivedLocalDateTime = receivedZonedDateTime.toLocalDateTime();

                        Campaignleads matchedLead = leadRepo.findByCampaign_CampaignidAndLead_Id(campaignId, leadId)
                                .orElse(null);

                        if (matchedLead != null) {
                            Conversation reply = new Conversation();
                            reply.setCampaignLead(matchedLead);
                            reply.setBody(body);
                            reply.setFromEmail(from);
                            reply.setToEmail(username); // your email
                            reply.setDirection(Direction.RECEIVED);
                            reply.setCreatedAt(receivedLocalDateTime);
                            reply.setMessageId(messageId); 
                            conversationRepo.save(reply);

                            System.out.println("Reply saved to DB for " + from);
                        } else {
                            System.out.println("Lead not found for CID-" + campaignId + " LID-" + leadId);
                        }
                    }
                }
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractTextFromMessage(Message message) throws Exception {
        String fullBody = "";

        if (message.isMimeType("text/plain")) {
            fullBody = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            fullBody = getTextFromMimeMultipart(mimeMultipart);
        }

        return extractReplyOnly(fullBody);
    }
    
    private String extractReplyOnly(String fullBody) {
        String[] splitPatterns = {
            "On.*wrote:",
            "From:.*",
            "Sent:.*",
            "Subject:.*",
            "-----Original Message-----"
        };

        for (String pattern : splitPatterns) {
            fullBody = fullBody.split(pattern)[0];
        }

        return fullBody.trim();
    }


    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            }
        }
        return result.toString();
    }
}
