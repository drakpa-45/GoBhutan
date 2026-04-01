// AdminTokenService.java
package com.goBhutan.adminPanel.common.service;

import com.goBhutan.adminPanel.common.config.ClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class AdminTokenService {
    private final ClientProperties clientProperties;
    private final RestTemplate rest = new RestTemplate();

    private final Map<String, TokenEntry> tokenCache = new HashMap<>();

    private static final class TokenEntry {
        private final String token;
        private final Instant expiry;

        private TokenEntry(String token, Instant expiry) {
            this.token = token;
            this.expiry = expiry;
        }
    }

    private synchronized TokenEntry refresh(String clientKey) {
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
            String token = (String) map.get("access_token");
            Integer expiresIn = (Integer) map.get("expires_in");
            Instant expiry = Instant.now().plusSeconds(expiresIn != null ? expiresIn : 60);
            return new TokenEntry(token, expiry);
        } else {
            throw new RuntimeException("Unable to fetch admin token from Keycloak");
        }
    }

    public synchronized String getAdminToken(String clientKey) {
        TokenEntry current = tokenCache.get(clientKey);
        if (current == null || current.expiry == null || Instant.now().isAfter(current.expiry.minusSeconds(30))) {
            TokenEntry refreshed = refresh(clientKey);
            tokenCache.put(clientKey, refreshed);
            return refreshed.token;
        }
        return current.token;
    }
}
