// AdminTokenService.java
package com.goBhutan.adminPanel.common.service;

import com.goBhutan.adminPanel.common.config.ClientProperties;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AdminTokenService {
    private final ClientProperties clientProperties;
    private final RestTemplate rest = new RestTemplate();

    private String token;
    private Instant expiry;

    private synchronized void refreshIfNeeded(String clientKey) {
        var clientConfig = clientProperties.getClient(clientKey).getKeycloak();

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                clientConfig.getServerUrl(),
                clientConfig.getRealm());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientConfig.getAdmin().getClientId());
        body.add("client_secret", clientConfig.getAdmin().getClientSecret());

        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = rest.postForEntity(tokenUrl, req, Map.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            Map<String, Object> map = resp.getBody();
            token = (String) map.get("access_token");
            Integer expiresIn = (Integer) map.get("expires_in");
            expiry = Instant.now().plusSeconds(expiresIn);
        } else {
            throw new RuntimeException("Unable to fetch admin token from Keycloak");
        }
    }

    public synchronized String getAdminToken(String clientKey) {
        if (token == null || expiry == null || Instant.now().isAfter(expiry.minusSeconds(30))) {
            refreshIfNeeded(clientKey);
        }
        return token;
    }
}
