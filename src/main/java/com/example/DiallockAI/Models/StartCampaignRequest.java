package com.example.DiallockAI.Models;

import java.util.List;

public class StartCampaignRequest {
    private String campaignName;
    private List<Integer> leadIds;
    private String prompt;
    private List<String> url;
    
    public StartCampaignRequest(String campaignName,List<Integer> leadIds,String prompt,List<String> Url) {
    	this.campaignName=campaignName;
    	this.leadIds=leadIds;
    	this.prompt=prompt;
    	this.url=Url;
    	
    	
    }
    
    public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	

    public List<String> getUrl() {
		return url;
	}
	public void setUrl(List<String> url) {
		this.url = url;
	}
	
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public List<Integer> getLeadIds() { return leadIds; }
    public void setLeadIds(List<Integer> leadIds) { this.leadIds = leadIds; }
}
