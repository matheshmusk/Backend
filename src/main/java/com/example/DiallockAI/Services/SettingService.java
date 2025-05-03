package com.example.DiallockAI.Services;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.DiallockAI.Models.Setting;
import com.example.DiallockAI.Repository.SettingRepo;




@Service
public class SettingService {
	
	@Autowired
    private SettingRepo repo;

    public Setting saveSetting(Setting setting) {
        if (!repo.findAll().isEmpty()) {
            Setting existing = repo.findAll().get(0);
            setting.setId(existing.getId()); // Update the same row
        }
        return repo.save(setting);
    }

    public Optional<Setting> getSetting() {
        List<Setting> settings = repo.findAll();
        return settings.isEmpty() ? Optional.empty() : Optional.of(settings.get(0));
    }
	
	

}
