package com.example.DiallockAI.Services;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.DiallockAI.Models.CampaignLeadStatus;
import com.example.DiallockAI.Models.CampaignStatus;
import com.example.DiallockAI.Models.Campaignleads;
import com.example.DiallockAI.Models.Campaigntable;
import com.example.DiallockAI.Models.Conversation;
import com.example.DiallockAI.Models.DiallockModel;
import com.example.DiallockAI.Models.Direction;
import com.example.DiallockAI.Models.StartCampaignRequest;
import com.example.DiallockAI.Repository.ConversationRepository;
import com.example.DiallockAI.Repository.DiallockRepo;
import com.example.DiallockAI.Repository.campaignleadsrepo;
import com.example.DiallockAI.Repository.campaigntablerepo;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;



@Service
public class DiallockService {
	
	@Autowired
    private DiallockRepo userRepository;
	@Autowired
	private CsvService csvservice;
	@Autowired
	private campaignleadsrepo leadrepo;
	@Autowired
	private campaigntablerepo tablerepo;
	
	@Autowired
	private  FireCrawl firecrawl;
	
	@Autowired
	private AiService aiservice;
	
	@Autowired
	private JavaMailSender mailsender;	
	
	@Autowired
	private ConversationRepository conversationRepo;


	public void sendMail(@RequestParam int id, @RequestParam int campaignid,@RequestBody String subject,@RequestBody String body) {
	    Campaignleads campaignLead = leadrepo.findByCampaign_CampaignidAndLead_Id(campaignid, id)
	        .orElseThrow(() -> new RuntimeException("Campaign lead not found"));

	    String to = campaignLead.getLead().getEmail();
	   

	    MimeMessage mimeMessage = mailsender.createMimeMessage();

	    try {
	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
	        helper.setTo(to);
	        helper.setSubject(subject);
	        helper.setText(body);
	        

	        // ✅ Add custom headers for tracking
	        String msgId = "<CID-" + campaignid + "-LID-" + id + "@diallock.ai>";
	        mimeMessage.setHeader("Message-ID", msgId);
	        

	        mailsender.send(mimeMessage);
	        
	        Date emailDate = mimeMessage.getSentDate();
	        ZonedDateTime emailZonedDateTime = (emailDate != null)
	        	    ? emailDate.toInstant().atZone(ZoneId.of("Asia/Kolkata")) // Adjusting to UTC
	        	    : ZonedDateTime.now(ZoneId.of("Asia/Kolkata")); // fallback to current UTC time if sentDate is null
	        
	        LocalDateTime emailLocalDateTime = emailZonedDateTime.toLocalDateTime();
	        Conversation sentMessage = new Conversation();
	        sentMessage.setCampaignLead(campaignLead);
	        sentMessage.setBody(body);
	        sentMessage.setFromEmail("mathesh1623@gmail.com"); // or use config
	        sentMessage.setToEmail(to);
	        sentMessage.setCreatedAt(emailLocalDateTime);
	        sentMessage.setDirection(Direction.SENT);
	        conversationRepo.save(sentMessage);
	    } catch (MessagingException e) {
	        throw new RuntimeException("Failed to send email", e);
	    }
	}

	
	
	public String startCampaign(StartCampaignRequest request) {
		
        Campaigntable campaign = new Campaigntable();
        campaign.setCampaignname(request.getCampaignName());
        campaign.setStatus(CampaignStatus.Active); 
        campaign.setPrompt(request.getPrompt());
        String prompt=request.getPrompt();
        tablerepo.save(campaign);

        for (int i = 0; i < request.getLeadIds().size(); i++) {
            Integer leadId = request.getLeadIds().get(i);
            String leadUrl = request.getUrl().get(i); 
            
            
            String scrapedContent = firecrawl.crawlUrl(leadUrl).block();
            String summary = aiservice.generateResponseFromContent(scrapedContent);
            String emailContent = aiservice.generateResponse(summary,prompt);
            String body="";
            String subject="";

            if (emailContent.startsWith("Subject:")) {
                int subjectEnd = emailContent.indexOf("\n\n");
                if (subjectEnd != -1) {
                    subject = emailContent.substring(8, subjectEnd).trim(); // 8 = length of "Subject:"
                    body = emailContent.substring(subjectEnd + 2).trim();
                } else {
                    subject = emailContent.substring(8).trim();
                    body = "";
                }
            }
            
            
            DiallockModel lead = userRepository.findById(leadId).orElse(null);
            if (lead != null) {
                Campaignleads campaignLead = new Campaignleads();
                campaignLead.setLead(lead);
                campaignLead.setCampaign(campaign);
                campaignLead.setBody(body);
                campaignLead.setSubject(subject);
                campaignLead.setResearch(summary);
                campaignLead.setStatus(CampaignLeadStatus.Active);
                leadrepo.save(campaignLead);
            }
        }


	        return "Campaign '" + request.getCampaignName() + "' started successfully!";
	    }


    
    
    public List<DiallockModel> displayAll() {
		return userRepository.findAll();
	}

	public DiallockModel addUser(DiallockModel usermodel) {
		return userRepository.save(usermodel);
	}
	
	public ResponseEntity<DiallockModel> updateUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
	    return userRepository.findById(id).map(existingUser -> {
	        updates.forEach((key, value) -> {
	            switch (key) {
	                case "name": existingUser.setName((String) value); break;
	                case "email": existingUser.setEmail((String) value); break;
	                case "company": existingUser.setCompany((String) value); break;
	                case "phoneno": existingUser.setPhoneno((String) value); break;
	                case "companysize": existingUser.setCompanysize((String) value); break;
	                case "status": existingUser.setStatus((String) value); break;
	                case "country": existingUser.setCountry((String) value); break;
	                case "url": existingUser.setUrl((String) value); break;
	            }
	        });
	        DiallockModel updatedUser = userRepository.save(existingUser);
	        return ResponseEntity.ok(updatedUser);
	    }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	public ResponseEntity<String> deleteUser(int id) {
	    if (!userRepository.existsById(id)) {
	        return ResponseEntity.notFound().build();
	    }
	    userRepository.deleteById(id);
	    return ResponseEntity.ok("Deleted successfully");
	}

	public List<DiallockModel> getByKeyword(String keyword) {
		return userRepository.findByKeyword(keyword);
	}

	public ResponseEntity<DiallockModel> updateUserdetails(int id, DiallockModel usermodel) {
		return userRepository.findById(id).map(existingUser -> {
	        existingUser.setName(usermodel.getName());
	        existingUser.setEmail(usermodel.getEmail());
	        existingUser.setCompany(usermodel.getCompany());
	        existingUser.setPhoneno(usermodel.getPhoneno());
	        existingUser.setCompanysize(usermodel.getCompanysize());
	        existingUser.setStatus(usermodel.getStatus());
	        existingUser.setCountry(usermodel.getCountry());
	        existingUser.setUrl(usermodel.getCountry());
	        DiallockModel updatedUser = userRepository.save(existingUser);
	        return ResponseEntity.ok(updatedUser);
	    }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	public List<DiallockModel> getStatus(String keyword) {
		return userRepository.findStatus(keyword);
	}

	


}
