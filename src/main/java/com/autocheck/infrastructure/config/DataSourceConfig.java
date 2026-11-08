package com.autocheck.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties() {
            @Override
            public String getUrl() {
                String rawUrl = super.getUrl();
                return sanitizeJdbcUrl(rawUrl);
            }

            @Override
            public String getUsername() {
                String username = super.getUsername();
                if (!StringUtils.hasText(username) || "postgres".equalsIgnoreCase(username)) {
                    String rawUrl = super.getUrl();
                    String extracted = extractUserInfo(rawUrl, true);
                    if (extracted != null) return extracted;
                }
                return username;
            }

            @Override
            public String getPassword() {
                String password = super.getPassword();
                if (!StringUtils.hasText(password) || "postgres".equalsIgnoreCase(password)) {
                    String rawUrl = super.getUrl();
                    String extracted = extractUserInfo(rawUrl, false);
                    if (extracted != null) return extracted;
                }
                return password;
            }
        };
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    private static String sanitizeJdbcUrl(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return rawUrl;
        }

        // Se já for JDBC válido, retorna diretamente
        if (rawUrl.startsWith("jdbc:postgresql://")) {
            return rawUrl;
        }

        try {
            // Suporta formatos de URI de nuvem como postgres://user:pass@host:port/db ou postgresql://...
            if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                URI uri = URI.create(rawUrl.replace("postgres://", "http://").replace("postgresql://", "http://"));
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String query = uri.getQuery();

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                        .append(host)
                        .append(":")
                        .append(port)
                        .append(path);

                if (StringUtils.hasText(query)) {
                    jdbcUrl.append("?").append(query);
                }

                log.info("DB_URL convertida automaticamente para formato JDBC PostgreSQL.");
                return jdbcUrl.toString();
            }
        } catch (Exception e) {
            log.warn("Não foi possível converter automaticamente o formato de DB_URL, utilizando valor bruto: {}", e.getMessage());
        }

        return rawUrl;
    }

    private static String extractUserInfo(String rawUrl, boolean returnUsername) {
        if (!StringUtils.hasText(rawUrl)) return null;
        try {
            if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                URI uri = URI.create(rawUrl.replace("postgres://", "http://").replace("postgresql://", "http://"));
                String userInfo = uri.getUserInfo();
                if (StringUtils.hasText(userInfo) && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    return returnUsername ? parts[0] : parts[1];
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
