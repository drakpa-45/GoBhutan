package com.goBhutan.adminPanel.common.controller;

import com.goBhutan.adminPanel.common.config.ClientProperties;
import com.goBhutan.adminPanel.common.config.ClientProperties.KeycloakConfig;
import com.goBhutan.adminPanel.common.dto.*;
import com.goBhutan.adminPanel.common.entity.AppUser;
import com.goBhutan.adminPanel.common.service.AdminTokenService;
import com.goBhutan.adminPanel.common.service.AppUserService;
import com.goBhutan.adminPanel.common.service.UserTokenService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/auth")
public class KeycloakAdminController {

    private final AdminTokenService tokenService;
    private final AppUserService appUserService;
    private final ClientProperties clientProperties;
    private final RestTemplate rest = new RestTemplate();

    private final UserTokenService userTokenService;

    public KeycloakAdminController(AdminTokenService tokenService,
                                   AppUserService appUserService,
                                   ClientProperties clientProperties,
                                   UserTokenService userTokenService) {
        this.tokenService = tokenService;
        this.appUserService = appUserService;
        this.clientProperties = clientProperties;
        this.userTokenService = userTokenService;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request) {

        try {
            Map<String, Object> data = userTokenService.refreshToken(
                    request.getUsername(),
                    request.getRefreshToken(),
                    request.getClient()
            );

            AuthResponse response = new AuthResponse(
                    "Token refreshed successfully",
                    (String) data.get("accessToken"),
                    (String) data.get("refreshToken")
            );

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }


    // 🔹 Signup for multiple clients
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDTO>> signup(@RequestBody SignupRequestDTO req) {
        try {
            String kcId = null;

            // 🔹 Loop through each client
            for (String client : req.getClients()) {
                KeycloakConfig config = getKeycloakConfig(client);
                String adminToken = tokenService.getAdminToken(client);
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(adminToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 1️⃣ Search user in Keycloak
                String searchUrl = String.format("%s/admin/realms/%s/users?username=%s",
                        config.getServerUrl(), config.getRealm(), req.getUsername());
                HttpEntity<Void> searchEntity = new HttpEntity<>(headers);
                ResponseEntity<Map[]> searchResp = rest.exchange(searchUrl, HttpMethod.GET, searchEntity, Map[].class);

                if (searchResp.getBody() != null && searchResp.getBody().length > 0) {
                    kcId = (String) searchResp.getBody()[0].get("id");
                } else {
                    // 2️⃣ Build Keycloak user payload
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

                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                    String createUrl = String.format("%s/admin/realms/%s/users", config.getServerUrl(), config.getRealm());

                    ResponseEntity<String> resp = rest.exchange(createUrl, HttpMethod.POST, entity, String.class);
                    if (!resp.getStatusCode().is2xxSuccessful() && resp.getStatusCodeValue() != 201) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Failed to create user in Keycloak for client: " + client));
                    }

                    // Re-fetch user to get kcId
                    searchResp = rest.exchange(searchUrl, HttpMethod.GET, searchEntity, Map[].class);
                    if (searchResp.getBody() != null && searchResp.getBody().length > 0) {
                        kcId = (String) searchResp.getBody()[0].get("id");
                    }
                }

                // 3️⃣ Save user in DB (only once)
                appUserService.createUserIfNotExists(
                        req.getUsername(),
                        req.getEmail(),
                        req.getFirstName(),
                        req.getLastName(),
                        req.getPassword(),
                        kcId
                );

                // 4️⃣ Assign client
                appUserService.assignClient(req.getUsername(), client);
            }

            // 🔹 Fetch DB user for response
            AppUser dbUser = appUserService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after signup"));

            SignupResponseDTO signupResponse = new SignupResponseDTO(
                    dbUser.getId(),
                    dbUser.getKeycloakId(),
                    dbUser.getUsername(),
                    dbUser.getEmail(),
                    List.copyOf(dbUser.getClients())
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Signup successful", signupResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Signup failed due to server error: " + e.getMessage()));
        }
    }

    // 🔹 Signin (single login, dynamic menu)
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signin(@RequestBody SigninRequestDTO req) {
        try {
            // 🔹 Fetch all clients assigned to this user
            List<String> userClients = appUserService.getClientsForUser(req.getUsername());
            if (userClients == null || userClients.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User is not registered with any client"));
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
                // 🔹 Fetch user from DB
                AppUser dbUser = appUserService.findByUsername(req.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found: " + req.getUsername()));

                Map<String, Object> data = new HashMap<>();
                data.put("userId", dbUser.getId());              // ✅ DB UserId
                data.put("keycloakId", dbUser.getKeycloakId());  // ✅ KeycloakId
                data.put("username", dbUser.getUsername());
                data.put("accessToken", tokenResponse.get("access_token"));
                data.put("refreshToken", tokenResponse.get("refresh_token"));
                data.put("clients", userClients);                // ✅ dynamic menu

                return ResponseEntity.ok(ApiResponse.success("Sign in successful", data));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));

        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));
        } catch (HttpClientErrorException.Forbidden e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("User is forbidden to access"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    // 🔹 Signout (invalidate refresh & access tokens)
    @PostMapping("/signout")
    public ResponseEntity<ApiResponse<String>> signout(@RequestBody SignoutRequestDTO req) {
        try {
            // Fetch user clients
            List<String> userClients = appUserService.getClientsForUser(req.getUsername());
            if (userClients == null || userClients.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("User is not registered with any client"));
            }

            // Use the first registered client (or adjust to use a specific one)
            String clientForAuth = userClients.get(0);
            KeycloakConfig config = getKeycloakConfig(clientForAuth);

            String url = String.format("%s/realms/%s/protocol/openid-connect/logout",
                    config.getServerUrl(), config.getRealm());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", config.getAdmin().getClientId());
            body.add("client_secret", config.getAdmin().getClientSecret());
            body.add("refresh_token", req.getRefreshToken());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            rest.postForEntity(url, entity, String.class);

            return ResponseEntity.ok(ApiResponse.success("Sign out successful", "User logged out successfully"));

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error("Sign out failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Sign out failed: " + e.getMessage()));
        }
    }

    @PostMapping("/update-clients")
    public ResponseEntity<ApiResponse<List<String>>> updateClients(@RequestBody SignupRequestDTO req) {
        try {
            // Fetch user from DB
            AppUser user = appUserService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Assign new clients
            for (String client : req.getClients()) {
                if (!user.getClients().contains(client)) {
                    appUserService.assignClient(req.getUsername(), client);
                }
            }

            // Fetch updated user
            AppUser updatedUser = appUserService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after update"));

            return ResponseEntity.ok(ApiResponse.success("Clients updated successfully",
                    List.copyOf(updatedUser.getClients())));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update clients: " + e.getMessage()));
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