package com.example.DiallockAI.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.DiallockAI.Models.Campaignleads;
import com.example.DiallockAI.Models.DiallockComm;


@Repository
public interface campaignleadsrepo extends JpaRepository<Campaignleads, Integer>{
	@Query("SELECT new com.example.DiallockAI.Models.DiallockComm(c.lead.id, c.lead.name, c.lead.email, c.status, c.lead.company) " +
	           "FROM Campaignleads c WHERE c.campaign.campaignid = :campaignId")
	    List<DiallockComm> findLeadsByCampaignId(Integer campaignId);
	
	@Query("SELECT cl.research FROM Campaignleads cl WHERE cl.lead.id = :id AND cl.campaign.campaignid= :campaignId ")
	String findResearchByCampaignId(@Param("campaignId") Integer campaignId,@Param("id")Integer id);
	
	@Query("SELECT cl.body FROM Campaignleads cl WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignId ")
	String findemailbodybyid(@Param("campaignId")Integer campaignId,@Param("id") Integer id);
	
	@Query("SELECT cl.subject FROM Campaignleads cl WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignId ")
	String findemailsubjectbyid(@Param("campaignId")Integer campaignId,@Param("id") Integer id);
	
	@Modifying
	@Query("UPDATE Campaignleads cl SET cl.subject = :subject WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignId")
	int updateEmailSubject(@Param("campaignid") int campaignid,
	                       @Param("id") int id,
	                       @Param("subject") String subject);
	@Modifying
	@Query("UPDATE Campaignleads cl SET cl.body = :body WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignId")
	int updateEmailBody(@Param("campaignid") int campaignid,
	                    @Param("id") int id,
	                    @Param("body") String body);
	
	Optional<Campaignleads> findByCampaign_CampaignidAndLead_Id(int campaignId, int leadId);
	
	@Modifying
	@Query("UPDATE Campaignleads cl SET cl.subject = '', cl.body = '' WHERE cl.lead.id= :id AND cl.campaign.campaignid= :campaignId")
	void clearSubjectAndBody(@Param("campaignId") int campaignid, @Param("id") int id);
	



}
