package com.goBhutan.adminPanel.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthConverter jwtAuthConverter;

	public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
		this.jwtAuthConverter = jwtAuthConverter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// バ. Enable and configure CORS before security rules
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable()) // No CSRF for APIs
				.authorizeHttpRequests(authz -> authz
						// Allow preflight OPTIONS requests for all routes
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						// Public endpoints
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(HttpMethod.GET,
								"/api/bus-masters/routes",
								"/api/bus-masters/routes/**",
								"/api/config",
								"/api/config/**")
						.permitAll()
						.requestMatchers("/api/bus-app/**").permitAll()
						// Everything else requires a valid Bearer JWT
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	// Define global CORS policy
	/*
	 * @Bean
	 * public CorsConfigurationSource corsConfigurationSource() {
	 * CorsConfiguration config = new CorsConfiguration();
	 * config.setAllowedOrigins(List.of(
	 * "http://localhost:5173",
	 * "http://68.178.160.243",
	 * "https://gobhutan.site"));
	 * config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	 * config.setAllowedHeaders(List.of("*"));
	 * config.setAllowCredentials(true);
	 * config.setMaxAge(3600L); // 1 hour cache for preflight
	 * 
	 * UrlBasedCorsConfigurationSource source = new
	 * UrlBasedCorsConfigurationSource();
	 * source.registerCorsConfiguration("/**", config);
	 * return source;
	 * }
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(List.of(
				"http://localhost:5173",
				"https://gobhutan.site",
				"http://68.178.160.243",
				"https://go-bhutan-admin.vercel.app"));

		config.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
