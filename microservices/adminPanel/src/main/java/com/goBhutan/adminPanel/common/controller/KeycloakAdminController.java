package com.goBhutan.adminPanel.common.controller;

import com.goBhutan.adminPanel.common.dto.AuthResponse;
import com.goBhutan.adminPanel.common.dto.SignupRequestDTO;
import com.goBhutan.adminPanel.common.dto.SigninRequestDTO;
import com.goBhutan.adminPanel.common.service.AdminTokenService;
import com.goBhutan.adminPanel.common.service.AppUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
    private final RestTemplate rest = new RestTemplate();

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    public KeycloakAdminController(AdminTokenService tokenService, AppUserService appUserService) {
        this.tokenService = tokenService;
        this.appUserService = appUserService;
    }

    /**
     * 🔹 Signup new user
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequestDTO req) {
        try {
            String adminToken = tokenService.getAdminToken();
            String url = String.format("%s/admin/realms/%s/users", keycloakServerUrl, realm);

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

            Map<String, String> responseBody = new HashMap<>();

            if (resp.getStatusCode().is2xxSuccessful() || resp.getStatusCodeValue() == 201) {
                String searchUrl = String.format("%s/admin/realms/%s/users?username=%s", keycloakServerUrl, realm, req.getUsername());
                HttpEntity<Void> searchEntity = new HttpEntity<>(headers);
                ResponseEntity<List> searchResp = rest.exchange(searchUrl, HttpMethod.GET, searchEntity, List.class);

                if (searchResp.getBody() != null && !searchResp.getBody().isEmpty()) {
                    Map first = (Map) searchResp.getBody().get(0);
                    String kcId = (String) first.get("id");

                    appUserService.getOrCreateFromJwt(kcId, req.getUsername(), req.getEmail());

                    responseBody.put("message", "Signup successful! Please login.");
                } else {
                    responseBody.put("message", "Created but couldn't fetch Keycloak ID. Try login.");
                }
                return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
            } else {
                responseBody.put("message", "Signup failed: " + resp.getBody());
                return ResponseEntity.status(resp.getStatusCode()).body(responseBody);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Signup failed due to server error: " + e.getMessage()));
        }
    }

    /**
     * 🔹 Sign-in user
     */
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody SigninRequestDTO req) {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "password");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("username", req.getUsername());
        body.put("password", req.getPassword());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);
            Map<String, Object> tokenResponse = response.getBody();

            if (tokenResponse != null && tokenResponse.containsKey("access_token")) {
                return ResponseEntity.ok(new AuthResponse(
                        "Sign in successful",
                        (String) tokenResponse.get("access_token"),
                        (String) tokenResponse.get("refresh_token")
                ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid username or password", null, null));
        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid username or password", null, null));
        } catch (HttpClientErrorException.Forbidden e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse("User is forbidden to access", null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Login failed: " + e.getMessage(), null, null));
        }
    }

    @GetMapping("/sync-me")
    public ResponseEntity<?> syncMe(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
