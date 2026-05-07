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

    @Transactional
    public AppUser getOrCreateFromJwt(String keycloakId, String username, String email) {
        return repo.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    AppUser u = new AppUser();
                    u.setKeycloakId(keycloakId);
                    u.setUsername(username);
                    u.setEmail(email);
                    u.setClients(new HashSet<>());
                    u.setRoles(new HashSet<>());   // ✅ init roles
                    return repo.save(u);
                });
    }

    @Transactional
    public boolean createUserIfNotExists(String username, String email, String firstName,
                                         String lastName, String password,
                                         String keycloakId, Set<String> roles, int phoneNumber) {
        if (repo.findByUsername(username).isPresent()) {
            return false;
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setKeycloakId(keycloakId);
        user.setClients(new HashSet<>());
        user.setRoles(new HashSet<>());   // ✅ always empty — roles added LAST
        user.setPhoneNumber(phoneNumber);
        repo.save(user);
        return true;
    }

    @Transactional
    public boolean createStaffIfNotExists(String username, String email, String firstName,
                                          String lastName, String password, String keycloakId,
                                          Set<String> roles, int phoneNumber,
                                          String entityId, String entityType) {
        if (repo.findByUsername(username).isPresent()) {
            return false;
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setKeycloakId(keycloakId);
        user.setClients(new HashSet<>());
        user.setRoles(new HashSet<>());   // ✅ always empty — roles added LAST
        user.setPhoneNumber(phoneNumber);
        user.setEntityId(entityId);
        user.setEntityType(entityType);
        repo.save(user);
        return true;
    }

    // ✅ Add roles AFTER assignClient to prevent overwrite
    @Transactional
    public void addRoles(String username, Set<String> roles) {
        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.getRoles().addAll(roles);
        repo.save(user);
    }

    @Transactional
    public void assignClient(String username, String client) {
        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Set<String> clients = Optional.ofNullable(user.getClients())
                .orElseGet(HashSet::new);
        clients.add(client);
        user.setClients(clients);
        repo.save(user);
    }

    @Transactional
    public AppUser updateClients(String username, Set<String> newClients) {
        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.setClients(new HashSet<>(newClients));
        return repo.save(user);
    }

    @Transactional
    public Optional<AppUser> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    @Transactional
    public Optional<AppUser> findByKeycloakId(String keycloakId) {
        return repo.findByKeycloakId(keycloakId);
    }

    public List<String> getClientsForUser(String username) {
        return repo.findByUsername(username)
                .map(user -> List.copyOf(Optional.ofNullable(user.getClients()).orElse(Set.of())))
                .orElse(List.of());
    }

    @Transactional
    public void updatePassword(String username, String newPassword) {
        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.setPassword(newPassword);
        repo.save(user);
    }

    // Find staff by entity
    public List<AppUser> getStaffByEntity(String entityId, String entityType) {
        return repo.findByEntityIdAndEntityType(entityId, entityType);
    }
}
