package com.example.DiallockAI.Repository;


import org.springframework.stereotype.Repository;

import com.example.DiallockAI.Models.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface SettingRepo extends JpaRepository<Setting, Integer>{

}
