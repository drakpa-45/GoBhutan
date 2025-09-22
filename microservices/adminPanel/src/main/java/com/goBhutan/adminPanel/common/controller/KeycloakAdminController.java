package com.goBhutan.adminPanel.common.controller;

import com.goBhutan.adminPanel.common.config.ClientProperties;
import com.goBhutan.adminPanel.common.config.ClientProperties.KeycloakConfig;
import com.goBhutan.adminPanel.common.dto.SignupRequestDTO;
import com.goBhutan.adminPanel.common.dto.SigninRequestDTO;
import com.goBhutan.adminPanel.common.service.AdminTokenService;
import com.goBhutan.adminPanel.common.service.AppUserService;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class KeycloakAdminController {

    private final AdminTokenService tokenService;
    private final AppUserService appUserService;
    private final ClientProperties clientProperties;
    private final RestTemplate rest = new RestTemplate();

    public KeycloakAdminController(AdminTokenService tokenService,
                                   AppUserService appUserService,
                                   ClientProperties clientProperties) {
        this.tokenService = tokenService;
        this.appUserService = appUserService;
        this.clientProperties = clientProperties;
    }

    // 🔹 Signup for multiple clients
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequestDTO req) {
        Map<String, String> responseBody = new HashMap<>();
        try {
            for (String client : req.getClients()) {
                KeycloakConfig config = getKeycloakConfig(client);
                String adminToken = tokenService.getAdminToken(client);
                String url = String.format("%s/admin/realms/%s/users", config.getServerUrl(), config.getRealm());

                Map<String, Object> payload = new HashMap<>();
                payload.put("username", req.getUsername());
                payload.put("email", req.getEmail());
                payload.put("firstName", req.getFirstName());
                payload.put("lastName", req.getLastName());
                payload.put("enabled", true);

                Map<String, Object> cred = new HashMap<>();
                cred.put("type", "password");
                cred.put("value", req.getPassword());
                cred.put("temporary", false);
                payload.put("credentials", List.of(cred));

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(adminToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);

                if (resp.getStatusCode().is2xxSuccessful() || resp.getStatusCodeValue() == 201) {
                    // Store mapping in DB
                    appUserService.assignClient(req.getUsername(), client);
                    responseBody.put(client, "Signup successful");
                } else {
                    responseBody.put(client, "Signup failed: " + resp.getBody());
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Signup failed due to server error: " + e.getMessage()));
        }
    }

    // 🔹 Signin (single login, dynamic menu)
    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@RequestBody SigninRequestDTO req) {
        try {
            // 🔹 Fetch all clients assigned to this user
            List<String> userClients = appUserService.getClientsForUser(req.getUsername());
            if (userClients == null || userClients.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User is not registered with any client"));
            }

            // 🔹 Pick the first registered client for authentication
            String clientForAuth = userClients.get(0);
            KeycloakConfig config = getKeycloakConfig(clientForAuth);

            String url = String.format("%s/realms/%s/protocol/openid-connect/token",
                    config.getServerUrl(), config.getRealm());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", config.getAdmin().getClientId());
            body.add("client_secret", config.getAdmin().getClientSecret());
            body.add("username", req.getUsername());
            body.add("password", req.getPassword());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);
            Map<String, Object> tokenResponse = response.getBody();

            if (tokenResponse != null && tokenResponse.containsKey("access_token")) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("message", "Sign in successful");
                resp.put("accessToken", tokenResponse.get("access_token"));
                resp.put("refreshToken", tokenResponse.get("refresh_token"));
                resp.put("clients", userClients); // dynamic menu

                return ResponseEntity.ok(resp);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));

        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        } catch (HttpClientErrorException.Forbidden e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "User is forbidden to access"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Login failed: " + e.getMessage()));
        }
    }


    // 🔹 Helper method to fetch Keycloak config
    private KeycloakConfig getKeycloakConfig(String clientKey) {
        var clientConfig = clientProperties.getClient(clientKey);
        if (clientConfig == null || clientConfig.getKeycloak() == null) {
            throw new RuntimeException("No Keycloak configuration found for client: " + clientKey);
        }
        return clientConfig.getKeycloak();
    }
}

