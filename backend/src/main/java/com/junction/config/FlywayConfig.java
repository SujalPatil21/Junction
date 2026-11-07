package com.junction.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;

/**
 * Explicit Flyway configuration ensuring database migrations execute
 * before the JPA EntityManagerFactory initializes and validates the schema.
 */
@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String locations;

    @Value("${spring.flyway.baseline-on-migrate:false}")
    private boolean baselineOnMigrate;

    @Value("${spring.flyway.validate-on-migrate:true}")
    private boolean validateOnMigrate;

    @Value("${spring.flyway.table:flyway_schema_history}")
    private String table;

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        log.info("[FlywayConfig] Initializing Flyway migration pipeline on datasource: locations={}, table={}", locations, table);
        String[] locArray = Arrays.stream(locations.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);

        return Flyway.configure()
            .dataSource(dataSource)
            .locations(locArray)
            .baselineOnMigrate(baselineOnMigrate)
            .validateOnMigrate(validateOnMigrate)
            .table(table)
            .load();
    }

    @Bean
    public static BeanFactoryPostProcessor entityManagerFactoryDependsOnFlywayPostProcessor() {
        return beanFactory -> {
            for (String beanName : beanFactory.getBeanNamesForType(EntityManagerFactory.class)) {
                var bd = beanFactory.getBeanDefinition(beanName);
                String[] dependsOn = bd.getDependsOn();
                if (dependsOn == null || dependsOn.length == 0) {
                    bd.setDependsOn("flyway");
                } else if (!Arrays.asList(dependsOn).contains("flyway")) {
                    String[] newDependsOn = Arrays.copyOf(dependsOn, dependsOn.length + 1);
                    newDependsOn[dependsOn.length] = "flyway";
                    bd.setDependsOn(newDependsOn);
                }
            }
        };
    }
}

