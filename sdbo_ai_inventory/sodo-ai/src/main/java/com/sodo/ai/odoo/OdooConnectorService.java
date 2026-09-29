package com.sodo.ai.odoo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OdooConnectorService {

    private final OdooConfigProperties configProperties;
    private final Map<String, JdbcTemplate> jdbcTemplateCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> connectivityStatusCache = new ConcurrentHashMap<>();

    public JdbcTemplate getJdbcTemplate(String version) {
        String key = normalizeVersion(version);
        return jdbcTemplateCache.computeIfAbsent(key, k -> {
            OdooConfigProperties.InstanceConfig config = configProperties.getInstance(k);
            if (config == null || config.getDbUrl() == null) {
                return null;
            }
            try {
                DriverManagerDataSource dataSource = new DriverManagerDataSource();
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setUrl(config.getDbUrl());
                dataSource.setUsername(config.getUsername());
                dataSource.setPassword(config.getPassword());
                return new JdbcTemplate(dataSource);
            } catch (Exception e) {
                log.warn("Impossible d'initialiser la source de données pour Odoo {}: {}", key, e.getMessage());
                return null;
            }
        });
    }

    public boolean isDatabaseReachable(String version) {
        String key = normalizeVersion(version);
        JdbcTemplate jdbcTemplate = getJdbcTemplate(key);
        if (jdbcTemplate == null) return false;

        try {
            DataSource ds = jdbcTemplate.getDataSource();
            if (ds == null) return false;
            try (Connection conn = ds.getConnection()) {
                boolean valid = conn.isValid(2);
                connectivityStatusCache.put(key, valid);
                return valid;
            }
        } catch (Exception e) {
            connectivityStatusCache.put(key, false);
            return false;
        }
    }

    public List<Map<String, Object>> query(String version, String sql, Object... params) {
        String key = normalizeVersion(version);
        if (!isDatabaseReachable(key)) {
            return Collections.emptyList();
        }
        try {
            JdbcTemplate jt = getJdbcTemplate(key);
            if (jt != null) {
                return jt.queryForList(sql, params);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution SQL sur Odoo {}: {}", key, e.getMessage());
        }
        return Collections.emptyList();
    }

    public String normalizeVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            return configProperties.getDefaultVersion();
        }
        String v = version.toLowerCase().trim();
        if (v.contains("16")) return "v16";
        if (v.contains("19")) return "v19";
        return v;
    }

    public OdooConfigProperties.InstanceConfig getInstanceConfig(String version) {
        return configProperties.getInstance(normalizeVersion(version));
    }
}
