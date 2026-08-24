package com.goBhutan.adminPanel.paymentInt.service.gateway;

import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class BfsNvpCodec {

    public String encode(Map<String, String> fields) {
        StringJoiner joiner = new StringJoiner("&");
        fields.forEach((key, value) -> joiner.add(urlEncode(key) + "=" + urlEncode(value == null ? "" : value)));
        return joiner.toString();
    }

    public Map<String, String> decode(String payload) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (payload == null || payload.isBlank()) {
            return fields;
        }

        String normalized = payload.replace("\r", "").replace("\n", "&").trim();
        for (String pair : normalized.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            int idx = pair.indexOf('=');
            if (idx < 0) {
                fields.put(urlDecode(pair.trim()), "");
            } else {
                fields.put(urlDecode(pair.substring(0, idx).trim()), urlDecode(pair.substring(idx + 1).trim()));
            }
        }
        return fields;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
