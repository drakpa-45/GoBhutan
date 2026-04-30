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

    public AppUserService(AppUserRepository repo) {
        this.repo = repo;
    }

    /**
     * Get user by Keycloak ID or create a new one if it doesn't exist.
     */
    @Transactional
    public AppUser getOrCreateFromJwt(String keycloakId, String username, String email) {
        return repo.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    AppUser u = new AppUser();
                    u.setKeycloakId(keycloakId);
                    u.setUsername(username);
                    u.setEmail(email);
                    u.setClients(new HashSet<>());
                    return repo.save(u);
                });
    }

    /**
     * Create a user in DB if not exists by username.
     * Returns true if created, false if already exists.
     */
    @Transactional
    public boolean createUserIfNotExists(String username, String email, String firstName,
                                         String lastName, String password, String keycloakId) {
        Optional<AppUser> existing = repo.findByUsername(username);
        if (existing.isPresent()) {
            return false; // already exists
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password); // optionally hash
        user.setKeycloakId(keycloakId); // ✅ set Keycloak ID
        user.setClients(new HashSet<>());

        repo.save(user);
        return true;
    }


    /**
     * Assign a client to an existing user.
     */
    @Transactional
    public void assignClient(String username, String client) {
        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Set<String> clients = Optional.ofNullable(user.getClients())
                .orElseGet(HashSet::new);
        clients.add(client);
        user.setClients(clients);
        repo.save(user); // persists the change
    }

    @Transactional
    public AppUser updateClients(String username, Set<String> newClients) {

        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Replace existing clients with selected ones
        user.setClients(new HashSet<>(newClients));

        return repo.save(user);
    }

    /**
     * Find AppUser by username.
     */
    @Transactional
    public Optional<AppUser> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    @Transactional
    public Optional<AppUser> findByKeycloakId(String keycloakId) {
        return repo.findByKeycloakId(keycloakId);
    }

    /**
     * Get all clients assigned to a user.
     */
    public List<String> getClientsForUser(String username) {
        return repo.findByUsername(username)
                .map(user -> List.copyOf(Optional.ofNullable(user.getClients()).orElse(Set.of())))
                .orElse(List.of());
    }
}
