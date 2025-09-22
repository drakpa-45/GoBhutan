package com.goBhutan.adminPanel.common.service;

import com.goBhutan.adminPanel.common.entity.AppUser;
import com.goBhutan.adminPanel.common.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        u.setClients(new HashSet<>());
        return repo.save(u);
    }

    // 🔹 Assign a client to a user
    @Transactional
    public void assignClient(String username, String client) {
        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Set<String> clients = user.getClients();
        if(clients == null) clients = new HashSet<>();
        clients.add(client);
        user.setClients(clients);
        repo.save(user);
    }

    // 🔹 Get all clients assigned to a user
    public List<String> getClientsForUser(String username) {
        return repo.findByUsername(username)
                .map(user -> List.copyOf(user.getClients()))
                .orElse(List.of());
    }
}
