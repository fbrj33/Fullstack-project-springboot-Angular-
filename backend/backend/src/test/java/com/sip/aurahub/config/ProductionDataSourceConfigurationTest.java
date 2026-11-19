package com.sip.aurahub.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionDataSourceConfigurationTest {
    @Test
    void parsesRenderPostgresUrlIntoJdbcSettings() {
        ProductionDataSourceConfiguration.DatabaseConnection connection =
                ProductionDataSourceConfiguration.parseRenderDatabaseUrl(
                        "postgresql://aurahub_user:p%40ss%3Aword@db.example.internal:5432/aurahub?sslmode=require");

        assertEquals("jdbc:postgresql://db.example.internal:5432/aurahub?sslmode=require", connection.jdbcUrl());
        assertEquals("aurahub_user", connection.username());
        assertEquals("p@ss:word", connection.password());
    }

    @Test
    void rejectsNonPostgresDatabaseUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductionDataSourceConfiguration.parseRenderDatabaseUrl("mysql://db.example/aurahub"));
    }
}
