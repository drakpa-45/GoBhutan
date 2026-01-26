package com.goBhutan.adminPanel.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app")
public class ClientProperties {

    private Map<String, ClientConfig> clients = new HashMap<>();

    public Map<String, ClientConfig> getClients() {
        return clients;
    }

    public void setClients(Map<String, ClientConfig> clients) {
        this.clients = clients;
    }

    public ClientConfig getClient(String name) {
        return clients.get(name);
    }

    public static class ClientConfig {
        private KeycloakConfig keycloak;
        private JwtConfig jwt;

        public KeycloakConfig getKeycloak() {
            return keycloak;
        }

        public void setKeycloak(KeycloakConfig keycloak) {
            this.keycloak = keycloak;
        }

        public JwtConfig getJwt() {
            return jwt;
        }

        public void setJwt(JwtConfig jwt) {
            this.jwt = jwt;
        }
    }

    public static class KeycloakConfig {
        private String serverUrl;
        private String realm;
        private Admin admin;

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public Admin getAdmin() {
            return admin;
        }

        public void setAdmin(Admin admin) {
            this.admin = admin;
        }
    }

    public static class Admin {
        private String clientId;
        private String clientSecret;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    public static class JwtConfig {
        private Auth auth;


        public Auth getAuth() {
            return auth;
        }

        public void setAuth(Auth auth) {
            this.auth = auth;
        }
    }

    public static class Auth {
        private Converter converter;

        public Converter getConverter() {
            return converter;
        }

        public void setConverter(Converter converter) {
            this.converter = converter;
        }

    }

    public static class Converter {
        private String resourceId;
        private String principleAttribute;

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getPrincipleAttribute() {
            return principleAttribute;
        }

        public void setPrincipleAttribute(String principleAttribute) {
            this.principleAttribute = principleAttribute;
        }
    }
}
