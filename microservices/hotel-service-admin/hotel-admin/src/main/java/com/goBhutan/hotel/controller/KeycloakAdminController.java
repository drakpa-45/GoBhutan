package com.goBhutan.hotel.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.goBhutan.hotel.service.AdminTokenService;
import com.goBhutan.hotel.service.AppUserService;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class KeycloakAdminController {
    private final AdminTokenService tokenService;
    private final RestTemplate rest = new RestTemplate();
    private final AppUserService appUserService;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    public KeycloakAdminController(AdminTokenService tokenService, AppUserService appUserService) {
        this.tokenService = tokenService;
        this.appUserService = appUserService;
    }

    record SignupRequest(String username, String email, String password) {}

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req){
        // 1) create Keycloak user via Admin REST API
        String adminToken = tokenService.getAdminToken();
        String url = String.format("%s/admin/realms/%s/users", keycloakServerUrl, realm);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", req.username());
        payload.put("email", req.email());
        payload.put("enabled", true);
        // Set credentials
        Map<String, Object> cred = new HashMap<>();
        cred.put("type", "password");
        cred.put("value", req.password());
        cred.put("temporary", false);
        payload.put("credentials", List.of(cred));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);

        if(resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode().value() == 201){
            // After creating the user, find its Keycloak id (by username) to link app DB
            String searchUrl = String.format("%s/admin/realms/%s/users?username=%s", keycloakServerUrl, realm, req.username());
            HttpEntity<Void> searchEntity = new HttpEntity<>(headers);
            ResponseEntity<List> searchResp = rest.exchange(searchUrl, HttpMethod.GET, searchEntity, List.class);
            if(searchResp.getBody() != null && !searchResp.getBody().isEmpty()){
                Map first = (Map) searchResp.getBody().get(0);
                String kcId = (String) first.get("id");
                appUserService.getOrCreateFromJwt(kcId, req.username(), req.email());
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body("created but couldn't fetch id - try login");
            }
        } else {
            return ResponseEntity.status(HttpStatus.valueOf(resp.getStatusCodeValue())).body(resp.getBody());
        }
    }

    // Endpoint to be called by your APIs after token validation to ensure the AppUser exists
    @GetMapping("/sync-me")
    public ResponseEntity<?> syncMe(@RequestHeader("Authorization") String authHeader){
        // the request will be authenticated by spring resource server; we can extract principal
        // but simpler: request will be authenticated and you can get Jwt from SecurityContext in controller
        // For brevity, controller method can extract Jwt directly (example below is simplified)
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
