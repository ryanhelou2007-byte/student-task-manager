package com.studenttaskmanager.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DataSourceConfig {

    private static final String LOCAL_DATABASE_URL =
            "jdbc:h2:file:./data/student-task-manager;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";

    @Bean
    public DataSource dataSource(Environment environment) {
        DatabaseSettings databaseSettings = resolveDatabaseSettings(environment);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseSettings.url());
        config.setUsername(databaseSettings.username());
        config.setPassword(databaseSettings.password());
        config.setMaximumPoolSize(Integer.parseInt(environment.getProperty("DB_POOL_SIZE", "5")));
        config.setPoolName("student-task-manager-pool");

        return new HikariDataSource(config);
    }

    private DatabaseSettings resolveDatabaseSettings(Environment environment) {
        String configuredUrl = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("JDBC_DATABASE_URL"),
                environment.getProperty("DATABASE_URL")
        );

        if (configuredUrl == null) {
            return new DatabaseSettings(
                    LOCAL_DATABASE_URL,
                    environment.getProperty("SPRING_DATASOURCE_USERNAME", "sa"),
                    environment.getProperty("SPRING_DATASOURCE_PASSWORD", "")
            );
        }

        if (configuredUrl.startsWith("postgres://") || configuredUrl.startsWith("postgresql://")) {
            return convertPostgresUrl(configuredUrl);
        }

        return new DatabaseSettings(
                configuredUrl,
                firstNonBlank(
                        environment.getProperty("SPRING_DATASOURCE_USERNAME"),
                        environment.getProperty("DATABASE_USERNAME"),
                        "sa"
                ),
                firstNonBlank(
                        environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
                        environment.getProperty("DATABASE_PASSWORD"),
                        ""
                )
        );
    }

    private DatabaseSettings convertPostgresUrl(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String userInfo = uri.getRawUserInfo();
        String username = "";
        String password = "";

        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            username = decode(parts[0]);
            if (parts.length > 1) {
                password = decode(parts[1]);
            }
        }

        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + query;

        return new DatabaseSettings(jdbcUrl, username, password);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record DatabaseSettings(String url, String username, String password) {
    }
}
