package com.example.DiallockAI.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.DiallockAI.Models.CampaignModel;
import com.example.DiallockAI.Models.Campaigntable;

@Repository
public interface campaigntablerepo extends JpaRepository<Campaigntable, Integer> {
	
	@Query("SELECT new com.example.DiallockAI.Models.CampaignModel(p.campaignid, p.campaignname) FROM Campaigntable p")
    public List<CampaignModel> displayCampaign();;

}
