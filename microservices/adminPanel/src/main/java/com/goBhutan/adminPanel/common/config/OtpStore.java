package com.goBhutan.adminPanel.common.config;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpStore {

    // username -> {otp, expiryTime}
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    public void save(String username, String otp) {
        store.put(username, new OtpEntry(otp, Instant.now().plusSeconds(300))); // 5 min expiry
    }

    public boolean verify(String username, String otp) {
        OtpEntry entry = store.get(username);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiry())) {
            store.remove(username); // expired
            return false;
        }
        return entry.otp().equals(otp);
    }

    public void remove(String username) {
        store.remove(username);
    }

    public record OtpEntry(String otp, Instant expiry) {}
}