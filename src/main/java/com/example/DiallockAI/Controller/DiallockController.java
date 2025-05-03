package com.example.DiallockAI.Controller;


import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.DiallockAI.Models.CampaignModel;
import com.example.DiallockAI.Models.Campaignleads;
import com.example.DiallockAI.Models.Conversation;
import com.example.DiallockAI.Models.DiallockComm;
import com.example.DiallockAI.Models.DiallockModel;
import com.example.DiallockAI.Models.Setting;
import com.example.DiallockAI.Models.StartCampaignRequest;
import com.example.DiallockAI.Repository.ConversationRepository;
import com.example.DiallockAI.Repository.campaignleadsrepo;
import com.example.DiallockAI.Repository.campaigntablerepo;
import com.example.DiallockAI.Services.AiService;
import com.example.DiallockAI.Services.CsvService;
import com.example.DiallockAI.Services.DiallockService;
import com.example.DiallockAI.Services.EmailReaderService;
import com.example.DiallockAI.Services.FireCrawl;
import com.example.DiallockAI.Services.ImapMailReaderService;
import com.example.DiallockAI.Services.SettingService;
import com.opencsv.exceptions.CsvValidationException;

import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;


@RestController
@CrossOrigin
@RequestMapping("/api")
public class DiallockController {
	
	@Autowired
	private DiallockService service;
	@Autowired
	private CsvService csvservice;
	@Autowired
	private AiService aiservice;
	@Autowired
	private campaigntablerepo campaign;
	@Autowired
	private FireCrawl fireCrawlService;
	@Autowired
    private campaignleadsrepo campaignleadsRepository;
	@Autowired
    private SettingService settingService;
	@Autowired
	private EmailReaderService emailReaderService;
	@Autowired
	private ImapMailReaderService imap;
	
	@Autowired
	private ConversationRepository conversationRepo;
	
	//to fetch email from inbox(it will avoid duplicates)
	@GetMapping("/{campaignid}/{id}/read")
	public ResponseEntity<?> readMailForLead(@PathVariable int campaignid, @PathVariable int id) {
	    emailReaderService.readEmailsForLead(campaignid, id);
	    return ResponseEntity.ok("Checked for replies.");
	}
	//To get the full conversation use filter to align sent and recieved
	@GetMapping("/{campaignId}/{leadId}/conversation")
	public List<Map<String, Object>> getConversationDetails(@PathVariable int campaignId, @PathVariable int leadId) {
	    Campaignleads lead = campaignleadsRepository.findByCampaign_CampaignidAndLead_Id(campaignId, leadId)
	        .orElseThrow(() -> new RuntimeException("Campaign lead not found"));

	    List<Conversation> conversations = conversationRepo.findByCampaignLeadOrderByCreatedAtAsc(lead);

	    return conversations.stream().map(convo -> {
	        Map<String, Object> convoMap = new HashMap<>();
	        convoMap.put("body", convo.getBody());
	        convoMap.put("direction", convo.getDirection()); // Example: "sent" or "received"
	        convoMap.put("timestamp", convo.getCreatedAt());  // optional but good for sorting
	        return convoMap;
	    }).toList();
	}

    @PostMapping("/addsetting")
    public ResponseEntity<Setting> addOrUpdateSetting(@RequestBody Setting setting) {
        Setting saved = settingService.saveSetting(setting);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getsetting")
    public ResponseEntity<Setting> getSetting() {
        Optional<Setting> existing = settingService.getSetting();
        return existing.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

	@GetMapping("/{campaignid}/{id}/body")
	public String displaybody(@PathVariable int campaignid,@PathVariable int id) {
		return campaignleadsRepository.findemailbodybyid(campaignid, id);
	}
	@GetMapping("/{campaignid}/{id}/subject")
	public String displaysubject(@PathVariable int campaignid,@PathVariable int id) {
		return campaignleadsRepository.findemailsubjectbyid(campaignid, id);
	}
	@PutMapping("/{campaignid}/{id}/subject")
	public ResponseEntity<String> updateEmailSubject(@PathVariable int campaignid,
	                                                 @PathVariable int id,
	                                                 @RequestBody String subject) {
	    int updated = campaignleadsRepository.updateEmailSubject(campaignid, id, subject);
	    if (updated > 0) {
	        return ResponseEntity.ok("Subject updated successfully");
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found or update failed");
	    }
	}
	@PutMapping("/{campaignid}/{id}/body")
	public ResponseEntity<String> updateEmailBody(@PathVariable int campaignid,
	                                              @PathVariable int id,
	                                              @RequestBody String body) {
	    int updated = campaignleadsRepository.updateEmailBody(campaignid, id, body);
	    if (updated > 0) {
	        return ResponseEntity.ok("Body updated successfully");
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found or update failed");
	    }
	}
	@PostMapping("/{campaignid}/{id}/send")
	@Transactional
    public String sendMail(@PathVariable int id, @PathVariable int campaignid, @RequestBody Map<String, String> payload) {
        String subject = payload.get("subject");
        String body = payload.get("content");

        service.sendMail(id, campaignid, subject, body);

        // Automatically clear from DB
        campaignleadsRepository.clearSubjectAndBody(campaignid, id);

        return "sent successfully";
    }
	@GetMapping("/campaigns")
	public List<CampaignModel> displayCampaign() {
		return campaign.displayCampaign();
	}

    @GetMapping("/{campaignId}/leads")
    public List<DiallockComm> getLeadsByCampaign(@PathVariable Integer campaignId) {
        return campaignleadsRepository.findLeadsByCampaignId(campaignId);
    }
    
    @GetMapping("/{campaignId}/{id}/research")
    public ResponseEntity<Map<String, String>>  getResearch(@PathVariable Integer campaignId,@PathVariable Integer id) {
    	String research = campaignleadsRepository.findResearchByCampaignId(campaignId,id);
    	Map<String, String> response = new HashMap<>();
        response.put("research", research);
        return ResponseEntity.ok(response);

    }
    
    @GetMapping("/{campaignId}/{id}/emailcontent")
    public ResponseEntity<Map<String, String>> getEmailcontent(@PathVariable Integer campaignId,@PathVariable Integer id) {
    	Campaignleads lead = campaignleadsRepository.findByCampaign_CampaignidAndLead_Id(campaignId, id).orElseThrow(() -> new RuntimeException("Campaign lead not found"));
    	    
    	String emailcontent = lead.getCombinedContent();
    	Map<String, String> response = new HashMap<>();
        response.put("emailcontent", emailcontent);
        return ResponseEntity.ok(response);
    	
    }
    
    @PostMapping("/campaign/start")
    public ResponseEntity<?> startCampaign(
        @RequestParam("campaignName") String campaignName,
        @RequestParam("prompt") String prompt,
        @RequestParam("file") MultipartFile file) {

        try {
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("CSV file is required.");
            }

            List<DiallockModel> savedLeads = csvservice.saveUsersFromCSV(file);
            if (savedLeads == null || savedLeads.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid leads found in CSV.");
            }

            // Prepare campaign request
            List<Integer> leadIds = savedLeads.stream()
                .map(DiallockModel::getId)
                .collect(Collectors.toList());
            List<String> urls = savedLeads.stream()
                .map(DiallockModel::getUrl)
                .collect(Collectors.toList());

            StartCampaignRequest s = new StartCampaignRequest(campaignName, leadIds, prompt, urls);
            service.startCampaign(s);

            return ResponseEntity.ok("Campaign started successfully with " + savedLeads.size() + " leads.");

        } catch (CsvValidationException e) {
            return ResponseEntity.badRequest().body("CSV Validation Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/scrape")
    public Mono<String> scrape(@RequestParam String url) {
        return fireCrawlService.scrapeUrl(url); // For instant results
    }
    @GetMapping("/llm")
    public Mono<String> llm(@RequestParam String url) {
        return fireCrawlService.llmUrl(url);
    }

    
    @GetMapping("/crawl")
    public Mono<String> crawl(@RequestParam String url) {
        return fireCrawlService.crawlUrl(url);
    }
    

	
//    @PostMapping("/process")
//    public String processWebsite(@RequestParam String url) {
//        String scrapedContent = fireCrawlService.scrapeUrl(url).block();
//        String summary=aiservice.generateResponseFromContent(scrapedContent);
//        return aiservice.generateResponse(summary);
//    }

    
	@PostMapping("/upload")
    public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
		    return ResponseEntity.badRequest().body("File is empty!");
		}
		if (!Objects.requireNonNull(file.getContentType()).equals("text/csv")) {
		    return ResponseEntity.badRequest().body("Only CSV files allowed!");
		}
        try {
            csvservice.saveUsersFromCSV(file);
            return ResponseEntity.ok("CSV uploaded and data saved successfully.");
        } catch (Exception e) {
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        	           .body("Error: " + e.getMessage());
        }
    }
	
	
	@GetMapping("/User")
	public ResponseEntity<List<DiallockModel>> displayAll(){
		return new ResponseEntity<>(service.displayAll(),HttpStatus.OK);
	}
	
	@GetMapping("/User/search")
	public ResponseEntity<List<DiallockModel>> getById(@RequestParam String keyword){
		return new ResponseEntity<>(service.getByKeyword(keyword),HttpStatus.OK);
	}
	
	@PostMapping("/add")
	public ResponseEntity<DiallockModel> addUser(@RequestBody DiallockModel usermodel) {
		DiallockModel user1=service.addUser(usermodel);
		return new ResponseEntity<>(user1,HttpStatus.FOUND);
	}
	@PutMapping("/User/{id}")
	public ResponseEntity<DiallockModel> updateUserdetails(@PathVariable int id,@RequestBody DiallockModel	 usermodel){	
		return service.updateUserdetails(id, usermodel);
	} 
	
	@PatchMapping("/users/{id}")
	public ResponseEntity<ResponseEntity<DiallockModel>> updateUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
	    ResponseEntity<DiallockModel> updatedUser = service.updateUser(id, updates);
	    return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	
	@DeleteMapping("/User/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable int id){
	    return service.deleteUser(id);
	}
	
	@GetMapping("/User/keyword")
	public List<DiallockModel> getStatus(@RequestParam("keyword") String keyword){
		return service.getStatus(keyword);
	}

}
