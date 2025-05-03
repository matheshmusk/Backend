package com.example.DiallockAI.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="setting")
public class Setting {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String fullName;
	private String companyName;
	private String jobTitle;
	private String companyWebsite;
	private String companyDescription;
	@Column(columnDefinition = "TEXT")
	private String usp;
}
