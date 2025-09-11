package com.goBhutan.adminPanel.common.service;

import com.goBhutan.adminPanel.common.entity.AppUser;
import com.goBhutan.adminPanel.common.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AppUserService {
    private final AppUserRepository repo;

    public AppUserService(AppUserRepository repo){ this.repo = repo; }

    @Transactional
    public AppUser getOrCreateFromJwt(String keycloakId, String username, String email){
        Optional<AppUser> opt = repo.findByKeycloakId(keycloakId);
        if(opt.isPresent()) return opt.get();

        AppUser u = new AppUser();
        u.setKeycloakId(keycloakId);
        u.setUsername(username);
        u.setEmail(email);
        return repo.save(u);
    }
}
