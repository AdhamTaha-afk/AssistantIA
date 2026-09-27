package com.sodo.ai.odoo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "odoo")
public class OdooConfigProperties {

    private String defaultVersion = "v19";
    private Map<String, InstanceConfig> instances = new HashMap<>();

    @Data
    public static class InstanceConfig {
        private String name;
        private String version;
        private String webUrl;
        private String database;
        private String dbUrl;
        private String username;
        private String password;
    }

    public InstanceConfig getInstance(String version) {
        if (version == null || version.trim().isEmpty()) {
            version = defaultVersion;
        }
        version = version.toLowerCase().trim();
        if (version.startsWith("16") || version.equals("v16")) {
            return instances.getOrDefault("v16", instances.get("v19"));
        }
        if (version.startsWith("19") || version.equals("v19")) {
            return instances.getOrDefault("v19", instances.get("v16"));
        }
        return instances.getOrDefault(version, instances.get(defaultVersion));
    }
}
