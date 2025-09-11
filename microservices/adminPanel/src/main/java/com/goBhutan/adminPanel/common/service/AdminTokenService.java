// AdminTokenService.java
package com.goBhutan.adminPanel.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
public class AdminTokenService {
    private final RestTemplate rest = new RestTemplate();

    @Value("${keycloak.server-url}") // e.g. http://localhost:8084
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

    private String token;
    private Instant expiry;

    private synchronized void refreshIfNeeded(){
        if(token != null && expiry != null && Instant.now().isBefore(expiry.minusSeconds(30))) return;

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", adminClientId);
        body.add("client_secret", adminClientSecret);

        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = rest.postForEntity(tokenUrl, req, Map.class);

        if(resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null){
            Map<String, Object> map = resp.getBody();
            token = (String) map.get("access_token");
            Integer expiresIn = (Integer) map.get("expires_in");
            expiry = Instant.now().plusSeconds(expiresIn);
        } else {
            throw new RuntimeException("Unable to fetch admin token from Keycloak");
        }
    }

    public synchronized String getAdminToken(){
        refreshIfNeeded();
        return token;
    }
}
