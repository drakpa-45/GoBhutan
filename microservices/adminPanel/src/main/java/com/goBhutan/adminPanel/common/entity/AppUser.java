package com.goBhutan.adminPanel.common.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gb_app_users")
public class AppUser {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id", updatable = false, nullable = false, unique = true)
	private String id;  // 🔹 UUID stored as String

	// Keycloak user id (sub)
	@Column(name = "kc_id", nullable = false, unique = true)
	private String keycloakId;

	@Column(nullable = false)
	private String username; // preferred_username or email

	@Column
	private String email;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKeycloakId() {
		return keycloakId;
	}

	public void setKeycloakId(String keycloakId) {
		this.keycloakId = keycloakId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	// Auto-generate UUID before insert if not set
	@PrePersist
	public void ensureId() {
		if (this.id == null) {
			this.id = UUID.randomUUID().toString();
		}
	}
}
