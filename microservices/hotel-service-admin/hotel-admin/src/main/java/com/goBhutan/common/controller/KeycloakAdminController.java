package com.goBhutan.common.controller;

import com.goBhutan.hotel.dto.AuthResponse;
import com.goBhutan.hotel.service.AdminTokenService;
import com.goBhutan.hotel.service.AppUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class KeycloakAdminController {
    private AdminTokenService tokenService;
    private RestTemplate rest = new RestTemplate();
    private AppUserService appUserService;

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

    record SignupRequest(String username, String email, String password) {}

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String loginPage() {
        return "loginSignUp/login";
    }

    @RequestMapping(value = "/signup",method = RequestMethod.GET)
    public String signupPage() {
        return "loginSignUp/signup";
    }

    @RequestMapping(value = "/dashboard",method = RequestMethod.GET)
    public String dashboard() {
        return "dashboard";
    }

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
        return ResponseEntity.ok(Map.of("ok", true));
    }
    
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody SignupRequest req) {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", req.username());
        body.add("password", req.password());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);
            Map<String, Object> tokenResponse = response.getBody();

            if (tokenResponse != null && tokenResponse.containsKey("access_token")) {
                AuthResponse authResponse = new AuthResponse(
                    "Sign in successful",
                    (String) tokenResponse.get("access_token"),
                    (String) tokenResponse.get("refresh_token")
                );
                return ResponseEntity.ok(authResponse);

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(new AuthResponse("Invalid username or password", null, null));
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new AuthResponse("Invalid username or password", null, null));

        } catch (HttpClientErrorException.Forbidden e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(new AuthResponse("User is forbidden to access", null, null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new AuthResponse("Login failed due to server error: " + e.getMessage(), null, null));
        }
    }

}
