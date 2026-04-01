package com.goBhutan.adminPanel.common.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UpdateClientsRequestDTO {
    @NotEmpty
    private String username;

    @NotEmpty
    private List<String> clients;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getClients() {
        return clients;
    }

    public void setClients(List<String> clients) {
        this.clients = clients;
    }
}
