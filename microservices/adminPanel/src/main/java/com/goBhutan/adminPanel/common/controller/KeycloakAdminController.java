package com.goBhutan.adminPanel.common.controller;

import com.goBhutan.adminPanel.common.config.ClientProperties;
import com.goBhutan.adminPanel.common.config.ClientProperties.KeycloakConfig;
import com.goBhutan.adminPanel.common.config.OtpStore;
import com.goBhutan.adminPanel.common.dto.*;
import com.goBhutan.adminPanel.common.entity.AppUser;
import com.goBhutan.adminPanel.common.service.AdminTokenService;
import com.goBhutan.adminPanel.common.service.AppUserService;
import com.goBhutan.adminPanel.common.service.UserTokenService;
import com.goBhutan.adminPanel.hotel.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/auth")
public class KeycloakAdminController {

    private final AdminTokenService tokenService;
    private final AppUserService appUserService;
    private final ClientProperties clientProperties;
    private final RestTemplate rest = new RestTemplate();

    private final UserTokenService userTokenService;
    private final OtpStore otpStore;
    private final EmailService emailService;

    public KeycloakAdminController(AdminTokenService tokenService,
                                   AppUserService appUserService,
                                   ClientProperties clientProperties,
                                   UserTokenService userTokenService,
                                   OtpStore otpStore,
                                   EmailService emailService) {
        this.tokenService = tokenService;
        this.appUserService = appUserService;
        this.clientProperties = clientProperties;
        this.userTokenService = userTokenService;
        this.otpStore = otpStore;
        this.emailService = emailService;
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

            // ✅ Check username uniqueness FIRST before any Keycloak calls
            if (appUserService.findByUsername(req.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Username '" + req.getUsername() + "' is already taken. Please choose another username"));
            }

            String kcId = null;

            Set<String> allRoles = new HashSet<>();

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
                String adminRole = clientProperties.getClient(client).getAdminRole();
                if (adminRole != null && !adminRole.isEmpty() && kcId != null) {
                    assignRealmRole(adminToken, config, kcId, adminRole);
                    allRoles.add(adminRole);
                }

                // 3️⃣ Save user in DB (only once)
                appUserService.createUserIfNotExists(
                        req.getUsername(),
                        req.getEmail(),
                        req.getFirstName(),
                        req.getLastName(),
                        req.getPassword(),
                        kcId,allRoles,req.getPhoneNumber()
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
                    dbUser.getRoles(),dbUser.getPhoneNumber(),
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
                data.put("clients", userClients);
                data.put("roles", dbUser.getRoles());
                data.put("firstName", dbUser.getFirstName());
                data.put("lastName", dbUser.getLastName());

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

    /*@PostMapping("/update-clients")
    public ResponseEntity<ApiResponse<List<String>>> updateClients(
            @RequestBody SignupRequestDTO req) {

        try {
            AppUser updatedUser = appUserService.updateClients(
                    req.getUsername(),
                    new HashSet<>(req.getClients())
            );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Clients updated successfully",
                            List.copyOf(updatedUser.getClients())
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update clients: " + e.getMessage()));
        }
    }*/

    @PostMapping("/update-profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @RequestBody UpdateClientRequestDTO req) {
        try {
            // ✅ Check user exists
            AppUser existingUser = appUserService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + req.getUsername()));

            String kcId = existingUser.getKeycloakId();

            // ✅ Get admin token from first existing client
            List<String> currentClients = appUserService.getClientsForUser(req.getUsername());
            if (currentClients == null || currentClients.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User has no assigned clients"));
            }
            KeycloakConfig config = getKeycloakConfig(currentClients.get(0));
            String adminToken = tokenService.getAdminToken(currentClients.get(0));

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ✅ Update email in Keycloak
            if (req.getEmail() != null && !req.getEmail().isEmpty()) {
                Map<String, Object> kcPayload = new HashMap<>();
                kcPayload.put("email", req.getEmail());
                kcPayload.put("emailVerified", false);

                String updateUrl = String.format("%s/admin/realms/%s/users/%s",
                        config.getServerUrl(), config.getRealm(), kcId);
                rest.exchange(updateUrl, HttpMethod.PUT,
                        new HttpEntity<>(kcPayload, headers), String.class);
            }

            Set<String> newClients = req.getClients() != null
                    ? new HashSet<>(req.getClients()) : new HashSet<>();

            // ✅ Remove roles in Keycloak for deselected clients
            Set<String> removedClients = new HashSet<>(currentClients);
            removedClients.removeAll(newClients);

            System.out.println("Clients to remove: " + removedClients); // debug

            for (String removedClient : removedClients) {
                String roleToRemove = clientProperties.getClient(removedClient).getAdminRole();
                System.out.println("Removing role: " + roleToRemove + " for client: " + removedClient); // debug

                if (roleToRemove != null && !roleToRemove.isEmpty()) {
                    KeycloakConfig removedConfig = getKeycloakConfig(removedClient);
                    String removedAdminToken = tokenService.getAdminToken(removedClient);
                    removeRealmRole(removedAdminToken, removedConfig, kcId, roleToRemove); // ✅ no silent catch
                }
            }

            // ✅ Assign roles in Keycloak for newly added clients
            Set<String> addedClients = new HashSet<>(newClients);
            addedClients.removeAll(currentClients); // clients that were added

            Set<String> finalRoles = new HashSet<>();

            // Keep roles for retained clients
            for (String client : newClients) {
                String adminRole = clientProperties.getClient(client).getAdminRole();
                if (adminRole != null && !adminRole.isEmpty()) {
                    if (addedClients.contains(client)) {
                        // newly added — assign in Keycloak
                        KeycloakConfig clientConfig = getKeycloakConfig(client);
                        String clientAdminToken = tokenService.getAdminToken(client);
                        assignRealmRole(clientAdminToken, clientConfig, kcId, adminRole);
                    }
                    finalRoles.add(adminRole); // collect all final roles
                }
            }

            // ✅ Update clients in DB
            appUserService.updateClients(req.getUsername(), newClients);

            // ✅ Replace roles in DB (not merge)
            appUserService.replaceRoles(req.getUsername(), finalRoles);

            // ✅ Update email and phone in DB
            appUserService.updateProfile(
                    req.getUsername(),
                    req.getEmail(),
                    req.getPhoneNumber(),
                    null  // clients already updated above
            );

            // ✅ Fetch updated user
            AppUser updatedUser = appUserService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after update"));

            Map<String, Object> data = new HashMap<>();
            data.put("username", updatedUser.getUsername());
            data.put("email", updatedUser.getEmail());
            data.put("phoneNumber", updatedUser.getPhoneNumber());
            data.put("clients", new ArrayList<>(updatedUser.getClients()));
            data.put("roles", new ArrayList<>(updatedUser.getRoles()));

            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", data));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Update failed: " + e.getMessage()));
        }
    }

    private void removeRealmRole(String adminToken, KeycloakConfig config, String kcId, String roleName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Fetch role representation
        String roleUrl = String.format("%s/admin/realms/%s/roles/%s",
                config.getServerUrl(), config.getRealm(), roleName);
        ResponseEntity<Map> roleResp = rest.exchange(roleUrl, HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);

        if (roleResp.getBody() == null) {
            System.err.println("Role not found in Keycloak: " + roleName);
            return;
        }

        // ✅ DELETE role mapping
        String removeUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
                config.getServerUrl(), config.getRealm(), kcId);

        // Must send role as body in DELETE request
        HttpEntity<List<Map>> deleteEntity = new HttpEntity<>(
                List.of(roleResp.getBody()), headers);

        rest.exchange(removeUrl, HttpMethod.DELETE, deleteEntity, String.class);

        System.out.println("✅ Removed role " + roleName + " from Keycloak user " + kcId);
    }

    private void assignRealmRole(String adminToken, KeycloakConfig config, String kcId, String roleName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Fetch role representation from Keycloak
        String roleUrl = String.format("%s/admin/realms/%s/roles/%s",
                config.getServerUrl(), config.getRealm(), roleName);
        ResponseEntity<Map> roleResp = rest.exchange(roleUrl, HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);

        // Assign role to user
        String assignUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
                config.getServerUrl(), config.getRealm(), kcId);
        rest.exchange(assignUrl, HttpMethod.POST,
                new HttpEntity<>(List.of(roleResp.getBody()), headers), String.class);
    }
    @PostMapping("/staff/create")
  //  @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'BUS_ADMIN', 'THEATER_ADMIN', 'TAXI_ADMIN')")
    public ResponseEntity<ApiResponse<SignupResponseDTO>> createStaff(
            @RequestBody StaffCreateRequestDTO req) {
        try {
            String client = req.getClient();
            KeycloakConfig config = getKeycloakConfig(client);
            String adminToken = tokenService.getAdminToken(client);

            // ✅ Username uniqueness check
            if (appUserService.findByUsername(req.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Username '" + req.getUsername() + "' is already taken"));
            }

            String counterRole = clientProperties.getClient(client).getCounterRole();
            if (counterRole == null || counterRole.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("No counter role configured for client: " + client));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String searchUrl = String.format("%s/admin/realms/%s/users?username=%s",
                    config.getServerUrl(), config.getRealm(), req.getUsername());
            ResponseEntity<Map[]> searchResp = rest.exchange(searchUrl, HttpMethod.GET,
                    new HttpEntity<>(headers), Map[].class);

            String kcId;
            if (searchResp.getBody() != null && searchResp.getBody().length > 0) {
                kcId = (String) searchResp.getBody()[0].get("id");
            } else {
                Map<String, Object> payload = new HashMap<>();
                payload.put("username", req.getUsername());
                payload.put("email", req.getEmail());
                payload.put("firstName", req.getFirstName());
                payload.put("lastName", req.getLastName());
                payload.put("enabled", true);

                Map<String, Object> cred = new HashMap<>();
                cred.put("type", "password");
                cred.put("value", req.getPassword());
                cred.put("temporary", true);
                payload.put("credentials", List.of(cred));

                String createUrl = String.format("%s/admin/realms/%s/users",
                        config.getServerUrl(), config.getRealm());
                rest.exchange(createUrl, HttpMethod.POST,
                        new HttpEntity<>(payload, headers), String.class);

                searchResp = rest.exchange(searchUrl, HttpMethod.GET,
                        new HttpEntity<>(headers), Map[].class);
                kcId = (String) searchResp.getBody()[0].get("id");
            }

            assignRealmRole(adminToken, config, kcId, counterRole);

            // ✅ 1. Save staff with empty roles
            appUserService.createStaffIfNotExists(
                    req.getUsername(), req.getEmail(),
                    req.getFirstName(), req.getLastName(),
                    req.getPassword(), kcId,
                    new HashSet<>(),        // ✅ empty
                    req.getPhoneNumber(),
                    req.getEntityId(),
                    req.getEntityType()
            );

            // ✅ 2. Assign client second
            appUserService.assignClient(req.getUsername(), client);

            // ✅ 3. Add roles LAST
            appUserService.addRoles(req.getUsername(), Set.of(counterRole));

            AppUser dbUser = appUserService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after creation"));

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                    "Staff created successfully",
                    new SignupResponseDTO(dbUser.getId(), dbUser.getKeycloakId(),
                            dbUser.getUsername(), dbUser.getEmail(),dbUser.getRoles(),dbUser.getPhoneNumber(),
                            List.copyOf(dbUser.getClients()))
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Staff creation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/staff/{entityType}/{entityId}")
   // @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'BUS_ADMIN', 'THEATER_ADMIN', 'TAXI_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppUser>>> getStaffByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        List<AppUser> staff = appUserService.getStaffByEntity(entityId, entityType);
        return ResponseEntity.ok(ApiResponse.success("Staff fetched successfully", staff));
    }


    // ✅ Step 1 — Send OTP to email
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody ForgotPasswordRequestDTO req) {
        try {
            // Check user exists
            AppUser dbUser = appUserService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + req.getUsername()));

            if (dbUser.getEmail() == null || dbUser.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("No email associated with this account"));
            }

            // Generate 6 digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));

            // Save OTP
            otpStore.save(req.getUsername(), otp);

            // Send email
            emailService.sendOtp(dbUser.getEmail(), otp);

            return ResponseEntity.ok(ApiResponse.success(
                    "OTP sent to " + maskEmail(dbUser.getEmail()), null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send OTP: " + e.getMessage()));
        }
    }

    // ✅ Step 2 — Verify OTP only
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestBody VerifyOtpRequestDTO req) {
        try {
            if (!otpStore.verify(req.getUsername(), req.getOtp())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid or expired OTP"));
            }
            return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("OTP verification failed: " + e.getMessage()));
        }
    }

    // ✅ Step 3 — Reset password after OTP verified
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequestDTO req) {
        try {
            // Verify OTP again
            if (!otpStore.verify(req.getUsername(), req.getOtp())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid or expired OTP"));
            }

            AppUser dbUser = appUserService.findByUsername(req.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<String> userClients = appUserService.getClientsForUser(req.getUsername());
            String clientForAuth = userClients.get(0);
            KeycloakConfig config = getKeycloakConfig(clientForAuth);
            String adminToken = tokenService.getAdminToken(clientForAuth);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ✅ Update in Keycloak
            String resetUrl = String.format("%s/admin/realms/%s/users/%s/reset-password",
                    config.getServerUrl(), config.getRealm(), dbUser.getKeycloakId());

            Map<String, Object> credPayload = new HashMap<>();
            credPayload.put("type", "password");
            credPayload.put("value", req.getNewPassword());
            credPayload.put("temporary", false);

            rest.exchange(resetUrl, HttpMethod.PUT,
                    new HttpEntity<>(credPayload, headers), String.class);

            // ✅ Update in DB
            appUserService.updatePassword(req.getUsername(), req.getNewPassword());

            // ✅ Invalidate OTP after successful reset
            otpStore.remove(req.getUsername());

            return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Password reset failed: " + e.getMessage()));
        }
    }

    // ✅ Helper — mask email for privacy: pe***@gmail.com
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
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