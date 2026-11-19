package com.sip.aurahub.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("production")
public class ProductionDataSourceConfiguration implements EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() {
        DatabaseConnection connection = databaseConnection();
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName("org.postgresql.Driver")
                .url(connection.jdbcUrl())
                .username(connection.username())
                .password(connection.password())
                .build();
        dataSource.setMaximumPoolSize(environment.getProperty("DB_POOL_SIZE", Integer.class, 5));
        return dataSource;
    }

    static DatabaseConnection parseRenderDatabaseUrl(String value) {
        URI uri = URI.create(value);
        String scheme = uri.getScheme();
        if (!"postgres".equalsIgnoreCase(scheme) && !"postgresql".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("DATABASE_URL must use the postgres or postgresql scheme");
        }

        String userInfo = uri.getUserInfo();
        int separator = userInfo == null ? -1 : userInfo.indexOf(':');
        String authority = uri.getRawAuthority();
        int at = authority == null ? -1 : authority.lastIndexOf('@');
        if (separator < 1 || at < 0 || !StringUtils.hasText(uri.getHost())
                || !StringUtils.hasText(uri.getRawPath()) || "/".equals(uri.getRawPath())) {
            throw new IllegalArgumentException("DATABASE_URL must include a PostgreSQL host, database, username, and password");
        }

        String serverAuthority = authority.substring(at + 1);
        String jdbcUrl = "jdbc:postgresql://" + serverAuthority + uri.getRawPath();
        if (uri.getRawQuery() != null) {
            jdbcUrl += "?" + uri.getRawQuery();
        }
        return new DatabaseConnection(jdbcUrl, userInfo.substring(0, separator), userInfo.substring(separator + 1));
    }

    private DatabaseConnection databaseConnection() {
        String renderUrl = environment.getProperty("DATABASE_URL");
        if (StringUtils.hasText(renderUrl)) {
            if (renderUrl.startsWith("jdbc:postgresql:")) {
                return new DatabaseConnection(renderUrl,
                        environment.getProperty("DB_USER"), environment.getProperty("DB_PASSWORD"));
            }
            return parseRenderDatabaseUrl(renderUrl);
        }

        String jdbcUrl = environment.getProperty("DB_URL");
        if (!StringUtils.hasText(jdbcUrl)) {
            String host = environment.getRequiredProperty("DB_HOST");
            String port = environment.getProperty("DB_PORT", "5432");
            String database = environment.getRequiredProperty("DB_NAME");
            jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
        }
        if (!jdbcUrl.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("DB_URL must be a PostgreSQL JDBC URL");
        }
        return new DatabaseConnection(jdbcUrl,
                environment.getRequiredProperty("DB_USER"), environment.getRequiredProperty("DB_PASSWORD"));
    }

    record DatabaseConnection(String jdbcUrl, String username, String password) {}
}
