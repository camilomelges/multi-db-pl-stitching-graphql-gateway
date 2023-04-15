package com.camilomelges.graphqlsybase.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FlywayConfiguration {

    @Value("${spring.flyway.url}")
    private String url;

    @Value("${spring.flyway.user}")
    private String user;

    @Value("${spring.flyway.password}")
    private String password;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        final var flyway = new Flyway(Flyway.configure().baselineOnMigrate(true).dataSource(url, user, password));

        try {
            flyway.validate();
        } catch (final FlywayException e) {
            flyway.repair();
        }

        return flyway;
    }
}