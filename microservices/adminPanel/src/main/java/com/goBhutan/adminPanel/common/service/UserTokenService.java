package com.goBhutan.adminPanel.common.service;

import com.goBhutan.adminPanel.common.config.ClientProperties;
import com.goBhutan.adminPanel.common.config.ClientProperties.KeycloakConfig;
import com.goBhutan.adminPanel.common.entity.AppUser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class UserTokenService {

    private final AppUserService appUserService;
    private final ClientProperties clientProperties;
    private final RestTemplate rest = new RestTemplate();

    public UserTokenService(AppUserService appUserService, ClientProperties clientProperties) {
        this.appUserService = appUserService;
        this.clientProperties = clientProperties;
    }

    public Map<String, Object> refreshToken(String username, String refreshToken, String clientKey) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        // 🔹 Get user's assigned clients
        List<String> userClients = appUserService.getClientsForUser(username);
        if (userClients.isEmpty()) {
            throw new RuntimeException("User is not registered with any client");
        }

        // 🔹 Choose client (either from request or default first)
        String selectedClient = (clientKey != null && userClients.contains(clientKey))
                ? clientKey
                : userClients.get(0);

        KeycloakConfig config = getKeycloakConfig(selectedClient);

        // 🔹 Build Keycloak token refresh request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", config.getAdmin().getClientId());
        params.add("client_secret", config.getAdmin().getClientSecret());
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        Map<String, Object> tokenResponse = rest.postForObject(
                config.getServerUrl() + "/realms/" + config.getRealm() + "/protocol/openid-connect/token",
                entity,
                Map.class
        );

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("Invalid refresh token or expired");
        }

        AppUser dbUser = appUserService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Map<String, Object> data = new HashMap<>();
        data.put("userId", dbUser.getId());
        data.put("keycloakId", dbUser.getKeycloakId());
        data.put("username", dbUser.getUsername());
        data.put("accessToken", tokenResponse.get("access_token"));
        data.put("refreshToken", tokenResponse.get("refresh_token"));
        data.put("clients", userClients);

        return data;
    }

    private KeycloakConfig getKeycloakConfig(String clientKey) {
        var clientConfig = clientProperties.getClient(clientKey);
        if (clientConfig == null || clientConfig.getKeycloak() == null) {
            throw new RuntimeException("No Keycloak configuration found for client: " + clientKey);
        }
        return clientConfig.getKeycloak();
    }
}
