package com.example.DiallockAI.Models;

 
import lombok.Data;

@Data
public class CampaignModel {
	private int campaignid;
	private String campaignname;
	
	public CampaignModel(int campaignid,String campaignname) {
		this.campaignid=campaignid;
		this.campaignname=campaignname;
	}

}
