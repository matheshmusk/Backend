package com.example.DiallockAI.Models;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campaignlead_id", nullable = false)
    private Campaignleads campaignLead;

    @Column(columnDefinition = "TEXT")
    private String body;
    
    @Column(name = "message_id", unique = true)
    private String messageId;

    private String fromEmail;
    private String toEmail;

    @Enumerated(EnumType.STRING)
    private Direction direction; // SENT or RECEIVED

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    

}
