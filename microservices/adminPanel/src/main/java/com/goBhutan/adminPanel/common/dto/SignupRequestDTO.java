package com.goBhutan.adminPanel.common.dto;

import java.util.List;
import java.util.Set;

public class SignupRequestDTO {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    private int phoneNumber;

    private Set<String> roles;
    private List<String> clients; // 🔹 new field to select multiple clients

    public SignupRequestDTO() {}

    public SignupRequestDTO(String username, String email, String password,
                            String firstName, String lastName, List<String> clients) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.clients = clients;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public List<String> getClients() { return clients; } // 🔹 getter
    public void setClients(List<String> clients) { this.clients = clients; } // 🔹 setter

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
