package com.goBhutan.adminPanel.common.config;

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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
	private final Set<String> resourceIds;
	private final String principalAttribute;

	public JwtAuthConverter(ClientProperties clientProperties) {
		if (clientProperties == null || clientProperties.getClients() == null) {
			this.resourceIds = Set.of();
			this.principalAttribute = JwtClaimNames.SUB;
			return;
		}

		this.resourceIds = clientProperties.getClients().values().stream()
				.map(ClientProperties.ClientConfig::getJwt)
				.filter(Objects::nonNull)
				.map(ClientProperties.JwtConfig::getAuth)
				.filter(Objects::nonNull)
				.map(ClientProperties.Auth::getConverter)
				.filter(Objects::nonNull)
				.map(ClientProperties.Converter::getResourceId)
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toUnmodifiableSet());

		this.principalAttribute = clientProperties.getClients().values().stream()
				.map(ClientProperties.ClientConfig::getJwt)
				.filter(Objects::nonNull)
				.map(ClientProperties.JwtConfig::getAuth)
				.filter(Objects::nonNull)
				.map(ClientProperties.Auth::getConverter)
				.filter(Objects::nonNull)
				.map(ClientProperties.Converter::getPrincipleAttribute)
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.findFirst()
				.orElse(JwtClaimNames.SUB);
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Collection<GrantedAuthority> authorities = Stream.concat(
				jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
				Stream.concat(extractRealmRoles(jwt).stream(), extractClientRoles(jwt).stream())
		).collect(Collectors.toSet());

		return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimValue(jwt));
	}

	private String getPrincipalClaimValue(Jwt jwt) {
		if (principalAttribute != null && jwt.hasClaim(principalAttribute)) {
			return jwt.getClaim(principalAttribute);
		}
		return jwt.getClaim(JwtClaimNames.SUB);
	}

	private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		if (realmAccess == null) {
			return Set.of();
		}

		@SuppressWarnings("unchecked")
		Collection<String> realmRoles = realmAccess.get("roles") != null
				? (Collection<String>) realmAccess.get("roles")
				: Set.of();

		return realmRoles.stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
				.collect(Collectors.toSet());
	}

	private Collection<? extends GrantedAuthority> extractClientRoles(Jwt jwt) {
		if (resourceIds == null || resourceIds.isEmpty()) {
			return Set.of();
		}

		return resourceIds.stream()
				.flatMap(resourceId -> extractClientRoles(jwt, resourceId).stream())
				.collect(Collectors.toSet());
	}

	private Collection<? extends GrantedAuthority> extractClientRoles(Jwt jwt, String resourceId) {
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
