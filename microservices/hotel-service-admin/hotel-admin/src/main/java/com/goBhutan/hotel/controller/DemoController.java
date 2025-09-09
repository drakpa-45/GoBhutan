package com.goBhutan.hotel.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.goBhutan.hotel.service.AppUserService;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    private final AppUserService appUserService;

    public DemoController(AppUserService appUserService){
        this.appUserService = appUserService;
    }

    @GetMapping("/hello")
    @PreAuthorize("hasRole('client_user')")
    public String hello() {
        return "Hello from Spring Boot and Keycloak";
    }

    @GetMapping("/hello-2")
    @PreAuthorize("hasRole('client_admin')")
    public String hello2() {
        return "Hello from Spring Boot and Keycloak -- ADMIN";
    }

    @GetMapping("/hello-jwt")
    @PreAuthorize("hasRole('client_admin')")
    public String helloJwt(@AuthenticationPrincipal Jwt jwt) {
        String sub = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        appUserService.getOrCreateFromJwt(sub, username, email);
        return "Hello from Spring Boot and Keycloak (JWT)";
    }
}

