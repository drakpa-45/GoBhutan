package com.goBhutan.adminPanel.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
			new JwtGrantedAuthoritiesConverter();

	@Value("${jwt.auth.converter.principle-attribute:sub}")
	private String principalAttributes;

	@Value("${jwt.auth.converter.resource-id:admin-panel}")
	private String resourceId;

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Collection<GrantedAuthority> authorities = Stream.concat(
				jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
				extractResourceRoles(jwt).stream()
		).collect(Collectors.toSet());

		// ✅ JwtAuthenticationToken is REST-friendly: Spring can serialize its details
		return new JwtAuthenticationToken(
				jwt,
				authorities,
				getPrincipleClaimName(jwt)
		);
	}

	private String getPrincipleClaimName(Jwt jwt) {
		String claimName = JwtClaimNames.SUB;
		if (principalAttributes != null && jwt.hasClaim(principalAttributes)) {
			claimName = principalAttributes;
		}
		return jwt.getClaim(claimName);
	}

	private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
		Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
		if (resourceAccess == null || resourceAccess.get(resourceId) == null) {
			return Set.of();
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);

		@SuppressWarnings("unchecked")
		Collection<String> resourceRoles = resource.get("roles") != null
				? (Collection<String>) resource.get("roles")
				: Set.of();

		return resourceRoles.stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
				.collect(Collectors.toSet());
	}
}
