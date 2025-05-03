package com.example.DiallockAI.Repository;

import com.example.DiallockAI.Models.Conversation;
import com.example.DiallockAI.Models.Campaignleads;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByCampaignLead(Campaignleads campaignLead);
    List<Conversation> findByCampaignLeadOrderByCreatedAtAsc(Campaignleads lead);
    boolean existsByMessageId(String messageId);
   

}
