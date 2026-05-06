package com.goBhutan.adminPanel.common.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "gb_app_users")
public class AppUser {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id", updatable = false, nullable = false, unique = true)
	private String id;  // UUID stored as String

	@Column(name = "kc_id", nullable = false, unique = true)
	private String keycloakId;

	@Column(nullable = false)
	private String username; // preferred_username or email

	@Column
	private String email;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "phone_number")
	private Integer phoneNumber;

	@Column
	private String password; // plain text for now; consider hashing

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	// 🔹 Store multiple clients per user
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "gb_user_clients", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "client")
	private Set<String> clients = new HashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "gb_user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role")
	private Set<String> roles = new HashSet<>();

	// getter & setter
	public Set<String> getRoles() { return roles; }
	public void setRoles(Set<String> roles) { this.roles = roles; }

	// Getters and Setters
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getKeycloakId() { return keycloakId; }
	public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }

	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

	public Set<String> getClients() { return clients; }
	public void setClients(Set<String> clients) { this.clients = clients; }

	// Auto-generate UUID before insert if not set
	@PrePersist
	public void ensureId() {
		if (this.id == null) this.id = UUID.randomUUID().toString();
	}

	public Integer getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(Integer phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
