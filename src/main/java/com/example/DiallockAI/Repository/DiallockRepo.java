package com.example.DiallockAI.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.example.DiallockAI.Models.DiallockModel;

@Repository
public interface DiallockRepo extends JpaRepository<DiallockModel, Integer> {
	
	boolean existsByEmail(String email);
	
	@Query("SELECT p FROM DiallockModel p WHERE "
		       + "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
		       + "LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
		       + "LOWER(p.phoneno) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
		       + "LOWER(p.companysize) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
		       + "LOWER(p.country) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
		       + "LOWER(p.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
		       + "LOWER(p.company) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	 List<DiallockModel> findByKeyword(@Param("keyword") String Keyword);

	 @Query("SELECT p FROM DiallockModel p WHERE "
			 +"LOWER(p.status) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<DiallockModel> findStatus(@Param("keyword") String keyword);
	 
	
	 
	
}
